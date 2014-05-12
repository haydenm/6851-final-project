package stringmatch.ds.suffixtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;
import stringmatch.ds.text.TextSubstring;
import stringmatch.ds.util.Pair;
import stringmatch.ds.yfasttrie.YFastTrie;
import stringmatch.ds.yfasttrie.YFastTrie.Builder;

public abstract class SuffixTreeWithWildcards extends SuffixTree {
  
  protected int k; // The number of wildcards.
  
  // Used for LCA queries
  private List<Pair<Integer, Node>> LCAOrder;
  Map<Integer, Map<Integer, Integer>> LCATable;
  
  // Used or MA queries
  Map<Node, Map<Integer, Pair<Node, Integer>>> MATable;
    
  public SuffixTreeWithWildcards(Node root) {
    super(root);
  }
  
  public SuffixTreeWithWildcards(Builder builder) {
    super(builder.root);
  }
  
  public void constructLCAAndMA() {
    LCAOrder = eulerTour();
    LCATable = buildLCATable();
    MATable = buildMATable();
    computeHeights();
    buildLongPaths();
    extendLadders();
  }
  
  /*
   * Get the number of characters for which an edge and a TextSubstring (starting at start) match
   */
  private Integer lengthOfMatch(TextSubstring p, int start, Edge e) {
    if (e != null) {
      for (int i = 0; i < Math.min(e.getTextSubstring().length, p.getLength() - start); i++) {
        AlphabetCharacter nextOnEdge = e.getTextSubstring().getIthChar(i);
        AlphabetCharacter nextInPattern = p.getIthChar(i + start);
        if (!nextOnEdge.equals(nextInPattern)) {
          return i;
        }
      }
      return Math.min(e.getTextSubstring().length, p.getLength() - start);
    }
    return 0;
  }
  
  /*
   * Find the point with the most overlap with p. This is represented as a pair, where
   * the first element is the closest node and the second is the number of letters below the
   * node which match.
   */
  protected Pair<Node, Integer> highestOverlapPoint(TextSubstring p) {
    return highestOverlapPoint(p, 0, root);
  }
  
  private Pair<Node, Integer> highestOverlapPoint(TextSubstring p, int start, Node current) {
    if (start >= p.getLength()) {
      return new Pair<Node, Integer>(current, start - p.getLength());
    }
    Edge e = current.follow(p.getIthChar(start));
    int length = lengthOfMatch(p, start, e);
    if (e != null) {
      if (length == e.getLength()) {
        return highestOverlapPoint(p, start + e.getTextSubstring().length, e.getToNode());
      } else {
        return new Pair<Node, Integer>(e.getToNode(), length - e.getLength());
      }
    } else {
      return new Pair<Node, Integer>(current, 0);
    }
  }
  
  /*
   * Perform an euler tour on the tree, recording the depth of each node. This is
   * used for LCA.
   */
  protected List<Pair<Integer, Node>> eulerTour() {
    return eulerTour(0, root);
  }

  protected List<Pair<Integer, Node>> eulerTour(int depth, Node start) {
    Pair<Integer, Node> pair = new Pair<Integer, Node>(depth, start);
    List<Pair<Integer, Node>> order = new ArrayList<Pair<Integer, Node>>();
    order.add(pair);
    if (!start.isLeaf()) {
      for (int i = 0; i < start.numChildren(); i++) {
        if (!start.outgoingEdges.get(i).isWildcardEdge()) {
          List<Pair<Integer, Node>> l = eulerTour(depth + 1, start.getChild(i));
          order.addAll(l);
          order.add(pair);
        }
      }
    }
    return order;
  }
  
  /*
   * Find the minimum of LCAOrder between indices start and end. Takes time
   * O(len(LCAOrder)), should only be used for pre-processing.
   */
  private int findMin(int start, int end) {
    int min = LCAOrder.get(start).getLeft();
    int index = start;
    for (int i = start + 1; i <= end; i++) {
      int val = LCAOrder.get(i).getLeft();
      if (val < min) {
        min = val;
        index = i;
      }
    }
    return index;
  }
  
  /*
   * Build the look-up table used for LCA. For each index in LCAOrder, the table stores
   * the index of the minimum element between that index and the index 2^i to the right,
   * for all values of i.
   */
  private Map<Integer, Map<Integer, Integer>> buildLCATable() {
    Map<Integer, Map<Integer, Integer>> table = new HashMap<Integer, Map<Integer, Integer>>();
    for (int index = 0; index < LCAOrder.size(); index++) {
      Map<Integer, Integer> inner = new HashMap<Integer, Integer>();
      for (int i = 1; i < log2(LCAOrder.size()); i++) {
        int end = Math.min(LCAOrder.size() - 1, (int) Math.pow(2, i) + index);
        inner.put((int) Math.pow(2, i), findMin(index, end));
      }
      table.put(index, inner);
    }
    updateIndices();
    return table;
  }
  
  /*
   * Make sure each node in the tree has a pointer to its first reference in LCAOrder
   */
  private void updateIndices() {
    for (int i = 0; i < LCAOrder.size(); i++) {
      if (LCAOrder.get(i).getRight().LCAIndex == -1) {
        LCAOrder.get(i).getRight().LCAIndex = i;
      }
    }
  }
  
  /* 
   * Get the index of the minimum element in LCAOrder between i and j (inclusive).
   * Used for LCA queries.
   */
  public int RMQ(int i, int j) {
    if (i > j) {
      int tmp = i;
      i = j;
      j = tmp;
    }
    int width = (int) Math.pow(2, Math.floor(log2(j - i)));
    int min1;
    int min2;
    if (width <= 1) {
      min1 = i;
      min2 = j;
    } else {
      min1 = LCATable.get(i).get(width);
      min2 = LCATable.get(j - width).get(width);
    }
    if (LCAOrder.get(min1).getLeft() <= LCAOrder.get(min2).getLeft()) {
      return min1;
    } else {
      return min2;
    }
  }
  
  /*
   * Get the LCA of two nodes in the suffix tree
   */
  public Node LCA(Node n1, Node n2) {
    int min_index = RMQ(n1.LCAIndex, n2.LCAIndex);
    return LCAOrder.get(min_index).getRight();
  }
  
  public static double log2(int x) {
    return Math.log(x) / Math.log(2);
  }
  
  /*
   * Build the look-up table used for MA (implementing jump pointers). For each node in the graph,
   * the table stores the point 2^i steps above. It stores the point as a pair, where the first element
   * is either this point or the node above this point if the point appears on the edge. The second
   * element represents the distance that that point occurs below the node.
   */
  private Map<Node, Map<Integer, Pair<Node, Integer>>> buildMATable() {
    Map<Node, Map<Integer, Pair<Node, Integer>>> table = new HashMap<Node, Map<Integer, Pair<Node, Integer>>>();
    buildMATable(root, table);
    return table;
  }
  
  private void buildMATable(Node node, Map<Node, Map<Integer, Pair<Node, Integer>>> table) {
    for (Edge e: node.outgoingEdges) {
      Node n = e.getToNode();
      Map<Integer, Pair<Node, Integer>> inner = buildInnerMATable(n);
      table.put(n, inner);
      buildMATable(n, table);
    }
  }
  
  private Map<Integer, Pair<Node, Integer>> buildInnerMATable(Node node) {
    Map<Integer, Pair<Node, Integer>> inner = new HashMap<Integer, Pair<Node, Integer>>();
    for (int i = 0; i < log2(node.incomingEdge.getTextSubstring().getText().getSize()); i++) {
      int jump = (int) Math.pow(2, i);
      int k = 0;
      Node current = node;
        while (k < jump && node.incomingEdge != null) {
          Edge edge = current.incomingEdge;
          if (edge != null) {
            k += current.incomingEdge.getTextSubstring().getLength();
            current = current.incomingEdge.getFromNode();
          } else {
            break;
          }
        }
      int diff = Math.max(0, k - jump);
      inner.put(jump, new Pair<Node, Integer>(current, diff));
    }
    return inner;
  }
  
  /*
   * For each element, compute the maximum height above any of its descendants
   */
  public void computeHeights() {
    computeHeights(root);
  }
  
  public int computeHeights(Node node) {
    if (node.isLeaf()) {
      node.maxHeight = 0;
    } else {
      for (Edge e: node.outgoingEdges) {
        Node child = e.getToNode();
        int childHeight = computeHeights(child);
        if (childHeight + e.getLength() > node.maxHeight) {
          node.maxHeight = childHeight + e.getLength();
          node.longPathEdge = e;
        }
      }
    }
    return node.maxHeight;
  }
  
  /*
   * Decompose the suffix tree into long paths. Give each node a pointer to the long
   * path on which it belongs
   */
  public void buildLongPaths() {
    buildLongPaths(root);
  }
  
  public void buildLongPaths(Node node) {
    Path path = new Path();
    Node current = node;
    while (current != null) {
      for (Edge e: current.outgoingEdges) {
        if (e!= current.longPathEdge && !e.isWildcardEdge()) {
          buildLongPaths(e.getToNode());
        }
      }
      int height = path.addNode(current);
      current.ladder = new Pair<Integer, Path>(height, path);
      Edge e = current.longPathEdge;
      if (e != null) {
        current = e.getToNode();
      } else {
        current = null;
      }
    }
  }
  
  /*
   * Extend each long path upwards to be twice as tall
   */
  public void extendLadders() {
    extendLadders(root);
  }
  
  public void extendLadders(Node node) {
    Path path = node.ladder.getRight();
    int nodeHeight = node.ladder.getLeft();
    int height = node.ladder.getRight().getLength();
    if (nodeHeight == height) {
      Node current = node;
      Edge e = node.incomingEdge;
      while (e != null && path.getLength() < 2 * height && !e.isWildcardEdge()) {
        current = e.getFromNode();
        path.prependNode(current);
        e = current.incomingEdge;
      }
      path.buildYFastTrie();
    }
    for (Edge e: node.outgoingEdges) {
      if (!e.isWildcardEdge()) {
        extendLadders(e.getToNode());
      }
    }
  }
  
  /*
   * Compute the measured ancestor of a node. Returns a pair containing either the resulting node
   * (if the measured ancestor is a node) or the next node above the edge where the measured ancestor
   * was. Also contains an interger corresponding to the distance below the node where the measured
   * ancestor can be found. 
   */
  public Pair<Node, Integer> MA(Node node, int k) {
    if (k == 0) {
      return new Pair<Node, Integer>(node, 0);
    }
    int jump = (int) Math.pow(2, Math.floor(log2(k)));
    Pair<Node, Integer> res = MATable.get(node).get(jump);
    int diff = res.getLeft().maxHeight - node.maxHeight;
    // We've already made it up at least k
    if (diff > k) {
      return new Pair<Node, Integer>(res.getLeft(), res.getRight() - (k - jump));
    // Otherwise we need to look up in a ladder
    } else {
      Path ladder = res.getLeft().ladder.getRight();
      int rem = k - diff;
      return ladder.jump(res.getLeft().maxHeight, rem);
    }
    
  }

  public abstract static class Builder extends SuffixTree.Builder {

    protected int k; // The number of wildcards.
    
    public Builder(Text inputText, int k) {
      super(inputText);
      root = super.build().root;
      this.k = k;
    } 
    
    /*
     * Turns the given tree into a wildcard subtree by replacing
     * the first characters with * and condensing the tree rooted at node.
     */
    protected static Node turnIntoWildcardSubtree(
        SuffixTreeWithWildcards wildcardSubtree) {
      // Replace first char of each edge with a wildcard.
      // Specifically, represent this by:
      // (1) adding a new WildcardEdge out of node,
      // (2) "deleting" the first character from the textSubstring in each of
      //     node's outgoing edges, and
      // (3) taking the outgoing edges whose textSubstrings became empty
      //     (i.e., they initially only had one character), taking the outgoing
      //     edges of each of these edges' toNodes, and moving these to be
      //     outgoing edges of node (effectively, delete the outgoing edges that
      //     became empty).
      
      // Step (1)
      // Turn node into the split node and make the root a new node.
     
      Node node = wildcardSubtree.root;
      Node newRoot = new Node(null);
      Edge wildcardEdge = new WildcardEdge(newRoot, wildcardSubtree);
      newRoot.addOutgoingEdge(wildcardEdge);
      node.setIncomingEdge(wildcardEdge);
      
      // There's a special case in which node will become a leaf. This happens
      // if it has just one child, and the edge along that child is simply '$'.
      // In a regular suffix tree there are no nodes with just one child (at least
      // not at the root). However, this case can come about when recursively
      // building wildcard subtrees.
      if (node.outgoingEdges.size() == 1 &&
          node.outgoingEdges.get(0).getTextSubstring().getLength() == 1 &&
          node.outgoingEdges.get(0).getTextSubstring().getFirstChar().equals(
              AlphabetCharacter.END_CHAR)) {
        node.isLeaf = true;
        node.leafOffsetIndexInS = node.outgoingEdges.get(0).getToNode().leafOffsetIndexInS;
        node.leafLexicographicIndexInS = node.outgoingEdges.get(0).getToNode().leafLexicographicIndexInS;
        node.outgoingEdges.clear();
        return newRoot;
      }
            
      // Steps (2) and (3)
      List<Edge> edgesToDelete = new ArrayList<Edge>();
      Iterator<Edge> outgoingEdgeIterator = node.getOutgoingEdges().iterator();
      while (outgoingEdgeIterator.hasNext()) {
        Edge outgoingEdge = outgoingEdgeIterator.next();
        outgoingEdge.getTextSubstring().deleteFirstChar();
        if (outgoingEdge.getTextSubstring().getLength() == 0) {
          edgesToDelete.add(outgoingEdge);
          outgoingEdgeIterator.remove();
        }
      }
      for (Edge edgeToDelete : edgesToDelete) {
        // centroidEdge and numLeaves will become incorrect, but those should be
        // corrected later on if needed (in SuffixTreeWithCPD).
        for (Edge edgeToAdd : edgeToDelete.getToNode().getOutgoingEdges()) {
          node.addOutgoingEdge(edgeToAdd);
          edgeToAdd.fromNode = node;
        }
      }
      Collections.sort(node.outgoingEdges);
      
      // Now condense the tree rooted at node.
      node.condense();
            
      return newRoot;
    }
    
  }
  
  public static void main(String[] args) {
    Text t = new Text("BANANABANANA", true);
    SuffixTreeWithCPD.Builder suffixTreeBuilder = new SuffixTreeWithCPD.Builder(t, 1);
    SuffixTreeWithCPD st = suffixTreeBuilder.build();
    st.printTree();
    System.out.println(st.eulerTour(0, st.root));
    AlphabetCharacter A = new AlphabetCharacter(new Character('A'));
    AlphabetCharacter B = new AlphabetCharacter(new Character('B'));
    AlphabetCharacter N = new AlphabetCharacter(new Character('N'));
    AlphabetCharacter D = new AlphabetCharacter(new Character('$'));
    AlphabetCharacter S = new AlphabetCharacter(new Character('*'));
    Node NA = st.root.follow(S).getToNode().follow(A).getToNode().follow(N).getToNode().follow(N).getToNode();
    Node BANANA = st.root.follow(S).getToNode().follow(A).getToNode().follow(N).getToNode().follow(N).getToNode().follow(B).getToNode();
    //Node n2 = st.root.follow(A).getToNode().follow(N).getToNode().follow(N).getToNode().follow(B).getToNode().follow(B).getToNode().follow(B).getToNode();
    //Text t = new Text("ANAF", false);
    //Pair<Node, Integer> p = st.highestOverlapPoint(new TextSubstring(t, 0, t.getSize()));
    //System.out.println(st.LCA(n1, n2));
    //System.out.println(st.root.maxHeight);
    //System.out.println(st.MA(n2, 0));
    //System.out.println(st.eulerTour());
    System.out.println(NA.follow(B).getFromNode() == NA);
    System.out.println(BANANA);
    System.out.println(BANANA.incomingEdge.getFromNode() == NA);
  }
  
}
