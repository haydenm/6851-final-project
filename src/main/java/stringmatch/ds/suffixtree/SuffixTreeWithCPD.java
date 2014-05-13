package stringmatch.ds.suffixtree;

import java.util.ArrayList;
import java.util.List;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;
import stringmatch.ds.util.Pair;
import stringmatch.ds.yfasttrie.YFastTrie;
import stringmatch.ds.yfasttrie.cuckoohash.CuckooHashMap;

/*
 * Suffix tree as described by Cole et al. (i.e., with Centroid Path Decomposition).
 */
public class SuffixTreeWithCPD extends SuffixTreeWithWildcards {
  
  private List<Pair<Integer, Node>> LCAOrder;
  CuckooHashMap<Integer, CuckooHashMap<Integer, Integer>> LCATable;
  
  // Used or MA queries
  CuckooHashMap<Node, CuckooHashMap<Integer, Pair<Node, Integer>>> MATable;
  

  // The depth of this wildcard subtree starting from the root of the original
  // suffix tree S.
  protected int subtreeDepth;
  
  // Mapping in the root suffix tree (S) from offset indices to lexicographic
  // order.
  protected CuckooHashMap<Integer, Integer> offsetToLexicographicIndexInS;
  
  // Y-Fast tries for leaf lexicographic indices (in T).
  protected YFastTrie<Node> leafLexicographicIndices;
  
  // Mapping from lexicographic index of leaves to the leaf node.
  // (This is only computed for the original suffix tree S.)
  protected CuckooHashMap<Integer, Node> lexicographicIndexToLeafInS;
  
  public SuffixTreeWithCPD(Node root) {
    super(root);
  }
  
  public SuffixTreeWithCPD(Builder builder) {
    super(builder.root);
  }
  
  // Determines and sets leafOffsetIndexInT and leafLexicographicIndexInT.
  public void determineLeafValuesInSubtree() {
    determineLeafValuesInSubtree(root);
  }
  
  protected void determineLeafValuesInSubtree(Node node) {
    if (node.isLeaf()) {
      node.leafOffsetIndexInT = node.leafOffsetIndexInS + subtreeDepth;
      node.leafLexicographicIndexInT = offsetToLexicographicIndexInS.
          get(node.leafOffsetIndexInT);
      return;
    }
    
    for (Edge outgoingEdge : node.getOutgoingEdges()) {
      determineLeafValuesInSubtree(outgoingEdge.getToNode());
    }
  }
  
  protected void constructLeafLexicographicIndexYFT() {
    List<Pair<Integer, Node>> leaves = constructLeafIndexArray(root);
    YFastTrie.Builder<Node> yftBuilder = new YFastTrie.Builder<Node>();
    leafLexicographicIndices = yftBuilder.buildFromPairs(leaves);
  }
  
  protected List<Pair<Integer, Node>> constructLeafIndexArray(
      Node node) {
    List<Pair<Integer, Node>> leaves =
        new ArrayList<Pair<Integer, Node>>();
    if (node.isLeaf) {
      leaves.add(new Pair<Integer, Node>(node.leafLexicographicIndexInT, node));
      return leaves;
    }
    
    for (Edge edge : node.getOutgoingEdges()) {
      leaves.addAll(constructLeafIndexArray(edge.getToNode()));
    }
    
    return leaves;
  }
  
  // Returns the node in S with the same string as leaf, where leaf's
  // string is considered to be from the root of the wildcard subtree
  // containing leaf down to leaf.
  protected Node getCorrespondingNodeInS(Node leaf) {
    return lexicographicIndexToLeafInS.get(leaf.leafLexicographicIndexInT);
  }
  
  protected void constructNodeDepths(Node node, int height) {
    node.depthInSubtree = height;
    for (Edge outgoingEdge : node.getOutgoingEdges()) {
      int outgoingEdgeHeight = outgoingEdge.getTextSubstring().getLength();
      constructNodeDepths(outgoingEdge.getToNode(), outgoingEdgeHeight + height);
    }
  }
  
  public void constructLCAAndMA() {
    LCAOrder = eulerTour();
    LCATable = buildLCATable();
    MATable = buildMATable();
    computeHeights();
    buildLongPaths();
    extendLadders();
    setLeftAndRightLeaves();
  }
  
  private void setLeftAndRightLeaves() {
    setLeftAndRightLeaves(root);
  }
  
  private void setLeftAndRightLeaves(Node n) {
    if (!n.isLeaf()) {
      for (Edge e: n.outgoingEdges) {
        setLeftAndRightLeaves(e.getToNode());
      }
      Node leftChild = n.followLeft().getToNode();
      n.leftMost = leftChild.leftMost;
      Node rightChild = n.followRight().getToNode();
      n.rightMost = rightChild.rightMost;
    } else {
      n.leftMost = n;
      n.rightMost = n;
    }
  }
  
  /*
   * Get the number of characters for which an edge and a TextSubstring (starting at start) match
   */
  private Integer lengthOfMatch(Text p, int start, Edge e) {
    if (e != null) {
      for (int i = 0; i < Math.min(e.getTextSubstring().length, p.getLength() - start); i++) {
        AlphabetCharacter nextOnEdge = e.getTextSubstring().getIthChar(i);
        AlphabetCharacter nextInPattern = p.getCharAtIndex(i + start);
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
  protected Pair<Node, Integer> highestOverlapPoint(Text p) {
    return highestOverlapPoint(p, 0, root);
  }
  
  private Pair<Node, Integer> highestOverlapPoint(Text p, int start, Node current) {
    if (start >= p.getLength()) {
      return new Pair<Node, Integer>(current, start - p.getLength());
    }
    Edge e = current.follow(p.getCharAtIndex(start));
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
   * Get the leaf corresponding to the suffix with the most overlap with p.
   * Return that leaf node and also the length of the overlap.
   */
  protected Pair<Node, Pair<Integer, Boolean>> highestOverlapLeaf(Text p) {
    Pair<Node, Integer> hop = highestOverlapPoint(p);
    Text overlap = constructHighestOverlap(hop);
    int h = overlap.getLength();
    boolean pred;
    Node node;
    if (h == p.getLength()) {
      // Reached end of pattern along path
      node = hop.getLeft().leftMost;
      pred = false;
    } else if (hop.getRight() == 0 && p.getLength() > h) {
      // Pattern diverged from a node
      node = hop.getLeft().followPrevious(p.getCharAtIndex(h)).getToNode().rightMost;
      pred = true;
    } else if (hop.getRight() != 0 && p.getLength() > h) {
      // Pattern diverged along an edge
      Edge e = hop.getLeft().incomingEdge;
      AlphabetCharacter nextOnEdge = e.getCharAt(e.getLength() + hop.getRight());
      AlphabetCharacter nextInPattern = p.getCharAtIndex(h);
      if (nextInPattern.compareTo(nextOnEdge) > 0) {
        node = hop.getLeft().rightMost;
        pred = true;
      } else if (nextInPattern.compareTo(nextOnEdge) < 0) {
        node = hop.getLeft().leftMost;
        pred = false;
      } else {
        throw new RuntimeException("Something wrong with highest overlap leaf");
      }
    } else {
      throw new RuntimeException("Something wrong with highest overlap leaf");
    }
    Pair<Integer, Boolean> info = new Pair<Integer, Boolean>(h, pred);
    return new Pair<Node, Pair<Integer, Boolean>>(node, info);
  }
  
  protected Text constructHighestOverlap(Pair<Node, Integer> highestOverlapPoint) {
    List<String> strs = new ArrayList<String>();
    Node current = highestOverlapPoint.getLeft();
    Edge e = current.incomingEdge;
    strs.add(e.toString().substring(0, e.getLength() + highestOverlapPoint.getRight()));
    while (e != null) {
      current = e.getFromNode();
      e = current.incomingEdge;
      if (e != null) {
        strs.add(e.toString()); 
      }
    }
    StringBuilder sb = new StringBuilder();
    for (int i = strs.size() - 1; i >= 0; i--) {
      sb.append(strs.get(i));
    }
    return new Text(sb.toString(), false);
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
  private CuckooHashMap<Integer, CuckooHashMap<Integer, Integer>> buildLCATable() {
    CuckooHashMap<Integer, CuckooHashMap<Integer, Integer>> table =
        new CuckooHashMap<Integer, CuckooHashMap<Integer, Integer>>();
    for (int index = 0; index < LCAOrder.size(); index++) {
      CuckooHashMap<Integer, Integer> inner = new CuckooHashMap<Integer, Integer>();
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
  private CuckooHashMap<Node, CuckooHashMap<Integer, Pair<Node, Integer>>> buildMATable() {
    CuckooHashMap<Node, CuckooHashMap<Integer, Pair<Node, Integer>>> table =
        new CuckooHashMap<Node, CuckooHashMap<Integer, Pair<Node, Integer>>>();
    buildMATable(root, table);
    return table;
  }
  
  private void buildMATable(Node node, CuckooHashMap<Node, CuckooHashMap<Integer, Pair<Node, Integer>>> table) {
    for (Edge e: node.outgoingEdges) {
      Node n = e.getToNode();
      CuckooHashMap<Integer, Pair<Node, Integer>> inner = buildInnerMATable(n);
      table.put(n, inner);
      buildMATable(n, table);
    }
  }
  
  private CuckooHashMap<Integer, Pair<Node, Integer>> buildInnerMATable(Node node) {
    CuckooHashMap<Integer, Pair<Node, Integer>> inner = new CuckooHashMap<Integer, Pair<Node, Integer>>();
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

  protected boolean checkMatch(Text p, int start, Edge e, int offset) {
    if (e != null) {
      for (int i = 0; i < Math.min(e.getTextSubstring().length - offset, p.getLength()
          - start); i++) {
        AlphabetCharacter nextOnEdge = e.getTextSubstring().getIthChar(i + offset);
        AlphabetCharacter nextInPattern = p.getCharAtIndex(start + i);
        if (!nextOnEdge.equals(nextInPattern)) {
            return false;
        }
      }
      return true;
    }
    return false;
  }
  
  public List<Pair<Node, Integer>> smartQuery(Text p) {
    List<Text> subQueries = breakQuery(p);
    Text query = subQueries.get(0);
    Pair<Node, Integer> prev = slowRootedLCP(query);
    if (subQueries.size() > 1) {
      return smartQuery(subQueries, 1, prev);
    } else {
      List<Pair<Node, Integer>> matches = new ArrayList<Pair<Node, Integer>>();
      matches.add(prev);
      return matches;
    }
  }
  
  public List<Pair<Node, Integer>> smartQuery(List<Text> subQueries, int i, Pair<Node, Integer> prev) {
    List<Pair<Node, Integer>> matches = new ArrayList<Pair<Node, Integer>>();
    if (prev.getRight() == 0) {
      // Check wildcard subtree
      WildcardEdge wce =  (WildcardEdge) prev.getLeft().follow(AlphabetCharacter.WILDCARD);
      if (wce != null) {
        SuffixTreeWithCPD w = (SuffixTreeWithCPD) wce.wildcardTree;
        Pair<Node, Integer> res1 = w.slowRootedLCP(subQueries.get(i));
        if (res1 != null) {
          if (subQueries.size() > i + 1) {
            matches.addAll(w.smartQuery(subQueries, i + 1, res1));
          } else {
            matches.add(res1);
          }
        }
      }
      // Check along centroid path
      Edge e = prev.getLeft().centroidEdge;
      Pair<Node, Integer> next = new Pair<Node, Integer>(e.getToNode(), 1 - e.getLength());
      Pair<Node, Integer> res2 = slowUnrootedLCP(subQueries.get(i), next);
      if (res2 != null) {
        if (subQueries.size() > i + 1) {
          matches.addAll(smartQuery(subQueries, i + 1, res2));
        } else {
          matches.add(res2);
        }
      }
    } else {
      // Just check along current path
      Pair<Node, Integer> next = new Pair<Node, Integer>(prev.getLeft(), prev.getRight() + 1);
      Pair<Node, Integer> res = slowUnrootedLCP(subQueries.get(i), next);
      if (res != null) {
        if (subQueries.size() > i + 1) {
          matches.addAll(smartQuery(subQueries, i + 1, res));
        } else {
          matches.add(res);
        }
      }
    }
    return matches;
  }
  
  public Pair<Node, Integer> slowRootedLCP(Text p) {
    return query(p);
  }
  
  public Pair<Node, Integer> rootedLCP(Text p, int queryIndex, int overlapHeight, Node ssp, SuffixTreeWithCPS S) {
    if (leafLexicographicIndices.hasKey(queryIndex)) {
      throw new RuntimeException("Query index in predecessor");
    }
    Pair<Integer, Node> pred = leafLexicographicIndices.predecessor(queryIndex);
    Pair<Integer, Node> succ = leafLexicographicIndices.successor(queryIndex);
    
    if (pred == null && succ == null) {
      throw new RuntimeException("Predecessor and Successor both null");
    }
    
    //Node lca = LCA(pred.getRight(), succ.getRight());
    
    // Compute h_p
    int hp;
    if (pred != null) {
      Node predInS = pred.getRight(); // TODO
      Node hpNode = S.LCA(predInS, ssp);
      hp = hpNode.depth;
    } else {
      hp = 0;
    }
    
    // Compute h_u
    int hu;
    if (succ != null) {
      Node succInS = succ.getRight(); //TODO
      Node huNode = S.LCA(succInS, ssp);
      hu = huNode.depth;
    } else {
      hu = 0;
    }
    
    if (overlapHeight != Math.max(hp, hu) && succ != null && pred != null) {
      throw new RuntimeException("NOT EQUAL TO SUCCESSOR OR PREDECESSOR");
    }
    
    if (overlapHeight == hp) {
      int lenPred = pred.getRight().depth;
      return MA(pred.getRight(), lenPred - hp);
    } else if (overlapHeight == hu) {
      int lenSucc = succ.getRight().depth;
      return MA(succ.getRight(), lenSucc - hu);
    }
    return null;
  }
  
  public Pair<Node, Integer> slowUnrootedLCP(Text p, Pair<Node, Integer> start) {
    Node node = start.getLeft();
    int offset = start.getRight();
    Edge e = node.incomingEdge;
    if (e == null || e.getLength() + offset < 0) {
      throw new RuntimeException("Malformed unrooted LCP query");
    }
    if (!checkMatch(p, 0, e, e.getLength() + offset)) {
      return null;
    } else {
      return query(p, -offset, node);
    }
    
  }
  
  public int getDepth(Node node) {
    return root.maxHeight - node.maxHeight;
  }
  
  public static List<Text> breakQuery(Text p) {
    List<Text> subQueries = new ArrayList<Text>();
    int start = 0;
    int len = 0;
    for (int i = 0; i < p.getSize(); i++) {
      if (p.getCharAtIndex(i).equals(AlphabetCharacter.WILDCARD)) {
        subQueries.add(new Text(p.toString().substring(start, start + len), false));
        start = i + 1;
        len = 0;
      } else {
        len += 1;
      }
    }
    subQueries.add(new Text(p.toString().substring(start, start + len), false));
    return subQueries;
  }
  
  public static void main(String[] args) {
    Text t = new Text("BANANABANANA", true);
    SuffixTreeWithCPD.Builder suffixTreeBuilder = new SuffixTreeWithCPD.Builder(t, 2);
    SuffixTreeWithCPD st = suffixTreeBuilder.build();
    st.printTree();
    AlphabetCharacter B = new AlphabetCharacter(new Character('B'));
    AlphabetCharacter A = new AlphabetCharacter(new Character('A'));
    AlphabetCharacter N = new AlphabetCharacter(new Character('N'));
    AlphabetCharacter D = new AlphabetCharacter(new Character('A'));
    Node n1 = st.root.follow(B).getToNode();
    Pair<Node, Integer> start = new Pair<Node, Integer>(n1, -4);
    for (Pair<Node, Integer> p: st.smartQuery(new Text("A", false))) {
      st.printNode(p.getLeft());
    }
    System.out.println("TESTA".compareTo("TEST$"));
    System.out.println(n1.followLeft());
    Text p = new Text("ANQ", false);
    Pair<Node, Integer> hop = st.highestOverlapPoint(p);
    System.out.println(hop);
    st.printNode(st.highestOverlapLeaf(p).getLeft());
    //st.rootedLCP(new Text("TEST", false));
    //System.out.println(SuffixTreeWithCPD.breakQuery(new Text("***TEST*AGAIN**TEST*", false)));
  }
  
  public static class Builder extends SuffixTreeWithWildcards.Builder {
    
    public Builder(Text inputText, int k) {
      super(inputText, k);
    }  
    
    protected static void findCentroidPaths(Node node) {
      node.numLeaves = 0;
      int maxNumberOfChildrenLeaves = 0;
      for (Edge outgoingEdge : node.outgoingEdges) {
        // In each child, count the number of their children.
        findCentroidPaths(outgoingEdge.getToNode());
        
        if (outgoingEdge.getToNode().isLeaf)
          node.numLeaves += 1;
        else
          node.numLeaves += outgoingEdge.getToNode().numLeaves;
        
        // Find the max number of children leaves. And keep track of the heaviest
        // children, breaking ties arbitrarily.
        if (outgoingEdge.getToNode().numLeaves >= maxNumberOfChildrenLeaves) {
          maxNumberOfChildrenLeaves = outgoingEdge.getToNode().numLeaves;
          node.centroidEdge = outgoingEdge;
        }
      }
    }
    
    protected static void addWildcardSubtreesAt(Node node, int k, int depthToNode,
        CuckooHashMap<Integer, Integer> offsetToLexicographicIndexInS,
        CuckooHashMap<Integer, Node> lexicographicIndexToNodeInS) {   
      if (node.isLeaf || k <= 0)
        return;
      
      // Make the wildcard subtree to be attached to node, without the
      // centroid edge.
      Node nodeClone = node.clone(true, null);
      if (nodeClone.outgoingEdges.size() > 0) {
        // There's a special case where nodeClone may have no outgoing edges.
        // The reason is that node could be the root of a wildcard subtree and
        // node has only one outgoing edge. This edge is labeled the centroid
        // edge and then its children are not included in nodeClone.
        // We should only turn nodeClone into a wildcard subtree and attach it to
        // node if it has children. If nodeClone doesn't have children, then
        // node won't have a wildcard subtree -- but this is ok because we
        // will simply follow the centroid edge.
        
        if (!(nodeClone.outgoingEdges.size() == 1 &&
            nodeClone.outgoingEdges.get(0).getTextSubstring().getFirstChar().
            equals(AlphabetCharacter.END_CHAR))) {
          // There's another special case in which nodeClone has just one outgoing edge,
          // which is '$'. We don't want to turn this edge into a wildcard because
          // otherwise wildcards could match for '$', which isn't really in the
          // input text.
          
          SuffixTreeWithCPD wildcardSubtree = new SuffixTreeWithCPD(nodeClone);
          wildcardSubtree.offsetToLexicographicIndexInS = offsetToLexicographicIndexInS;
          wildcardSubtree.lexicographicIndexToLeafInS = lexicographicIndexToNodeInS;
          wildcardSubtree.subtreeDepth = depthToNode + 1; // +1 for the wildcard edge.
          nodeClone = turnIntoWildcardSubtree(wildcardSubtree);
          wildcardSubtree.constructLCAAndMA();
          wildcardSubtree.determineLeafValuesInSubtree();
          wildcardSubtree.constructLeafLexicographicIndexYFT();
          wildcardSubtree.constructNodeDepths(wildcardSubtree.root, 0);
      
          // Attach nodeClone onto node. nodeClone should have just one outgoing
          // edge: the wildcard edge.
          Edge wildcardEdge = nodeClone.outgoingEdges.get(0);
          wildcardEdge.fromNode = node;
          node.addOutgoingEdge(wildcardEdge);
        
          // Find the centroid path decomposition of the copied tree (i.e., the
          // wildcard subtree), which is rooted at wildcardEdge.getToNode().
          findCentroidPaths(wildcardEdge.getToNode());
        }
      }
      
      // Recursively add wildcard subtrees.
      for (Edge edge : node.outgoingEdges) {
        // Recursively add them to the new wildcard subtree as well.
        int newK = edge.isWildcardEdge() ? k - 1 : k;
        int edgeDepth = edge.getTextSubstring().getLength();
        addWildcardSubtreesAt(edge.getToNode(), newK, depthToNode + edgeDepth,
            offsetToLexicographicIndexInS, lexicographicIndexToNodeInS);
      }
    }
    
    protected CuckooHashMap<Integer, Integer>
        constructOffsetToLexicographicIndexMap() {
      CuckooHashMap<Integer, Integer> map = new CuckooHashMap<Integer, Integer>();
      constructOffsetToLexicographicIndexMap(root, map);
      return map;
    }
    
    protected void constructOffsetToLexicographicIndexMap(
        Node node, CuckooHashMap<Integer, Integer> map) {
      if (node.isLeaf()) {
        map.put(node.leafOffsetIndexInS, node.leafLexicographicIndexInS);
        return;
      }
      
      for (Edge outgoingEdge : node.getOutgoingEdges()) {
        constructOffsetToLexicographicIndexMap(outgoingEdge.getToNode(), map);
      }
    }
    
    protected CuckooHashMap<Integer, Node> constructLexicographicIndexToLeafMap() {
      CuckooHashMap<Integer, Node> map = new CuckooHashMap<Integer, Node>();
      constructLexicographicIndexToLeafMap(root, map);
      return map;
    }
    
    protected void constructLexicographicIndexToLeafMap(Node node,
        CuckooHashMap<Integer, Node> map) {
      if (node.isLeaf()) {
        map.put(node.leafLexicographicIndexInS, node);
        return;
      }
      
      for (Edge outgoingEdge : node.getOutgoingEdges()) {
        constructLexicographicIndexToLeafMap(outgoingEdge.getToNode(), map);
      }
    }
    
    public SuffixTreeWithCPD build() {
      SuffixTreeWithCPD stcpd = new SuffixTreeWithCPD(this);
      stcpd.offsetToLexicographicIndexInS = constructOffsetToLexicographicIndexMap();
      stcpd.subtreeDepth = 0;
      stcpd.constructLCAAndMA();
      stcpd.determineLeafValuesInSubtree();
      stcpd.constructLeafLexicographicIndexYFT();
      stcpd.constructNodeDepths(root, 0);
      
      // Only do this for the original suffix tree S, and not for every wildcard
      // subtree.
      stcpd.lexicographicIndexToLeafInS = constructLexicographicIndexToLeafMap();
      
      // Find the centroid paths in the original suffix tree (i.e., the one
      // rooted at root). Then when we make copies we find the centroid
      // paths *once* in each of those.
      findCentroidPaths(root);
      
      // Add wildcards (without including centroid edges).
      addWildcardSubtreesAt(root, k, 0, stcpd.offsetToLexicographicIndexInS,
          stcpd.lexicographicIndexToLeafInS);
      
      return stcpd;
    }
  }

}
