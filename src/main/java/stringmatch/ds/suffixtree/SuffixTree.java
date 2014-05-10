package stringmatch.ds.suffixtree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;
import stringmatch.ds.text.TextSubstring;
import stringmatch.ds.util.Pair;
import stringmatch.ds.suffixtree.Node;

/*
 * Stores a suffix tree.
 * Builds it using Ukkonen's algorithm, as described in:
 * http://stackoverflow.com/questions/9452701/ukkonens-suffix-tree-algorithm-in-plain-english/
 *
 * Based largely on:
 * https://github.com/maxgarfinkel/suffixTree/
 */

public class SuffixTree {

  protected Node root;
  private List<Pair<Integer, Node>> LCAOrder;
  Map<Integer, Map<Integer, Integer>> LCATable;
  Map<Node, Map<Integer, Pair<Node, Integer>>> MATable;

  protected SuffixTree() {
  }

  private SuffixTree(Builder builder) {
    root = builder.root;
    fixIncomingEdges();
    LCAOrder = eulerTour();
    LCATable = buildLCATable();
    MATable = buildMATable();
  }

  public Node getRoot() {
    return root;
  }

  /*
   * Checks that following an edge matches all the characters along the edge. If
   * allowWildcards is set, then all characters except
   * AlphabetCharacter.END_CHAR are matched to AlphabetCharacter.WILDCARD.
   */
  private boolean checkMatch(Text p, int start, Edge e, boolean allowWildcards) {
    if (e != null) {
      for (int i = 0; i < Math.min(e.getTextSubstring().length, p.getLength()
          - start); i++) {
        AlphabetCharacter nextOnEdge = e.getTextSubstring().getIthChar(i);
        AlphabetCharacter nextInPattern = p.getCharAtIndex(start + i);
        if (!nextOnEdge.equals(nextInPattern)) {
          if (!(allowWildcards && wildcardMatch(nextOnEdge, nextInPattern))) {
            return false;
          }
        }
      }
      return true;
    }
    return false;
  }

  /*
   * Returns the node corresponding to the longest common prefix of p with the
   * suffix tree, or null if there is no such prefix. No wildcards allowed.
   */
  public Node query(Text p) {
    return queryHelper(p, 0, root);
  }

  private Node queryHelper(Text p, int start, Node current) {
    if (start >= p.getSize()) {
      return current;
    }
    Edge e = current.follow(p.getCharAtIndex(start));
    if (checkMatch(p, start, e, false)) {
      return queryHelper(p, start + e.getTextSubstring().length, e.getToNode());
    }
    return null;
  }

  public boolean wildcardMatch(AlphabetCharacter nextOnEdge,
      AlphabetCharacter nextInPattern) {
    return (nextInPattern.isWild() && !nextOnEdge.isEnd());
  }

  /*
   * Returns the node corresponding to the longest common prefix of p with the
   * suffix tree, or null if there is no such prefix. Allows wildcards.
   */
  public List<Node> naiveWildcardQuery(Text p) {
    return naiveWildcardQueryHelper(p, 0, root);
  }

  private List<Node> naiveWildcardQueryHelper(Text p, int start, Node current) {
    List<Node> results = new ArrayList<Node>();
    if (start >= p.getSize()) {
      results.add(current);
      return results;
    }
    Edge e = current.follow(p.getCharAtIndex(start));
    if (checkMatch(p, start, e, true)) {
      results.addAll(naiveWildcardQueryHelper(p, start
          + e.getTextSubstring().length, e.getToNode()));
    }
    for (Edge next : current.getOutgoingEdges()) {
      if (p.getCharAtIndex(start).isWild()) {
        if (checkMatch(p, start, next, true)) {
          results.addAll(naiveWildcardQueryHelper(p,
              start + next.getTextSubstring().length, next.getToNode()));
        }
      }
    }
    return results;
  }

  public void printTree() {
    printTreeHelper("", root, "ROOT", true);
  }

  public void printTreeHelper(String prefix, Node n, String label, boolean leaf) {
    System.out.println(prefix + (leaf ? "|-- " : "|-- ") + label);
    // System.out.println(n);
    if (n.outgoingEdges != null && n.outgoingEdges.size() > 0) {
      for (int i = 0; i < n.outgoingEdges.size() - 1; i++) {
        Edge e = n.outgoingEdges.get(i);
        String l = e.toString();
        printTreeHelper(prefix + (leaf ? "    " : "|   "), e.getToNode(), l,
            false);
      }
      if (n.outgoingEdges.size() >= 1) {
        Edge e = n.outgoingEdges.get(n.outgoingEdges.size() - 1);
        String l = e.toString();
        printTreeHelper(prefix + (leaf ? "    " : "|   "), e.getToNode(), l,
            true);
      }
    }
  }
  
  public List<Pair<Integer, Node>> eulerTour() {
    return eulerTour(0, root);
  }

  private List<Pair<Integer, Node>> eulerTour(int depth, Node start) {
    Pair<Integer, Node> pair = new Pair<Integer, Node>(depth, start);
    List<Pair<Integer, Node>> order = new ArrayList<Pair<Integer, Node>>();
    order.add(pair);
    if (!start.isLeaf()) {
      for (int i = 0; i < start.numChildren(); i++) {
        List<Pair<Integer, Node>> l = eulerTour(depth + 1, start.getChild(i));
        order.addAll(l);
        order.add(pair);
      }
    }
    return order;
  }
  
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
  
  private Map<Node, Map<Integer, Pair<Node, Integer>>> buildMATable() {
    Map<Node, Map<Integer, Pair<Node, Integer>>> table = new HashMap<Node, Map<Integer, Pair<Node, Integer>>>();
    buildMATableHelper(root, table);
    return table;
  }
  
  private void buildMATableHelper(Node node, Map<Node, Map<Integer, Pair<Node, Integer>>> table) {
    for (Edge e: node.outgoingEdges) {
      Node n = e.getToNode();
      Map<Integer, Pair<Node, Integer>> inner = buildInnerMATable(n);
      table.put(n, inner);
      buildMATableHelper(n, table);
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
  
  private void updateIndices() {
    for (int i = 0; i < LCAOrder.size(); i++) {
      if (LCAOrder.get(i).getRight().LCAIndex == -1) {
        LCAOrder.get(i).getRight().LCAIndex = i;
      }
    }
  }
  
  public Node LCA(Node n1, Node n2) {
    int min_index = RMQ(n1.LCAIndex, n2.LCAIndex);
    return LCAOrder.get(min_index).getRight();
  }
  
  public static double log2(int x) {
    return Math.log(x) / Math.log(2);
  }
  
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
  
  public void computeHeights() {
    computeHeightsHelper(root);
  }
  
  public int computeHeightsHelper(Node node) {
    if (node.isLeaf()) {
      node.maxHeight = 0;
    } else {
      for (Edge e: node.outgoingEdges) {
        Node child = e.getToNode();
        int childHeight = computeHeightsHelper(child);
        if (childHeight + e.getLength() > node.maxHeight) {
          node.maxHeight = childHeight + e.getLength();
          node.longPathEdge = e;
        }
      }
    }
    return node.maxHeight;
  }
  
  public void buildLongPaths() {
    buildLongPathsHelper(root);
  }
  
  public void buildLongPathsHelper(Node node) {
    Path path = new Path();
    Node current = node;
    while (current != null) {
      int height = path.addNode(current);
      current.ladder = new Pair<Integer, Path>(height, path);
      Edge e = current.longPathEdge;
      if (e != null) {
        current = e.getToNode();
      } else {
        current = null;
      }
    }
    for (Edge e: node.outgoingEdges) {
      if (e!= node.longPathEdge) {
        buildLongPathsHelper(e.getToNode());
      }
    }
    System.out.println(path);
  }
  
  public void extendLadders() {
    System.out.println("--------");
    extendLaddersHelper(root);
  }
  
  public void extendLaddersHelper(Node node) {
    Path path = node.ladder.getRight();
    int nodeHeight = node.ladder.getLeft();
    int height = node.ladder.getRight().getLength();
    if (nodeHeight == height) {
      Node current = node;
      Edge e = node.incomingEdge;
      while (e != null && path.getLength() < 2 * height) {
        current = e.getFromNode();
        path.prependNode(current);
        e = current.incomingEdge;
      }
    }
    for (Edge e: node.outgoingEdges) {
      if (e!= node.longPathEdge) {
        extendLaddersHelper(e.getToNode());
      }
    }
    System.out.println(path);
  }

  public void fixIncomingEdges() {
    recFixIncomingEdges(root);
  }

  public void recFixIncomingEdges(Node node) {
    for (Edge e : node.outgoingEdges) {
      Node n = e.getToNode();
      n.incomingEdge = e;
      recFixIncomingEdges(n);
    }
  }

  public static void main(String[] args) {
    SuffixTree.Builder suffixTreeBuilder = new SuffixTree.Builder(new Text(
        "BANANA", true));
    SuffixTree st = suffixTreeBuilder.build();
    st.printTree();
    //System.out.println(st.eulerTour(0, st.root));
    AlphabetCharacter A = new AlphabetCharacter(new Character('A'));
    AlphabetCharacter B = new AlphabetCharacter(new Character('B'));
    AlphabetCharacter N = new AlphabetCharacter(new Character('N'));
    AlphabetCharacter D = new AlphabetCharacter(new Character('$'));
    Node n1 = st.root.follow(N).getToNode().follow(D).getToNode();
    Node n2 = st.root.follow(A).getToNode().follow(N).getToNode();
    //System.out.println(st.LCA(n1, n2));
    st.computeHeights();
    //System.out.println(st.root.maxHeight);
    st.buildLongPaths();
    st.extendLadders();
  }

  public static class Builder {
    private Node root;
    private ActivePoint activePoint;
    private int insertsAtStep;
    private Node lastInsertedNode;
    private int prefixStart;
    private int endPosition;

    private Text inputText;

    public Builder(Text inputText) {
      root = new Node(null);
      activePoint = new ActivePoint(root);
      insertsAtStep = 0;
      lastInsertedNode = null;
      prefixStart = 0;
      endPosition = 1;

      this.inputText = inputText;
    }

    protected int getEndPosition() {
      return endPosition;
    }

    protected Text getInputText() {
      return inputText;
    }

    private void processPrefixes() {
      for (int endIndex = 1; endIndex <= inputText.getLength(); endIndex++) {
        int length = endIndex - prefixStart;
        TextSubstring prefix = new TextSubstring(inputText, prefixStart, length);
        insertsAtStep = 0;
        addSubstring(prefix);
        if (endIndex < inputText.getLength())
          endPosition++;
      }
    }

    private void addSubstring(TextSubstring substr) {
      if (activePoint.isInsideEdge())
        addIntoEdge(substr);
      else if (activePoint.isOnNode())
        addOntoNode(substr);
    }

    private void addOntoNode(TextSubstring substr) {
      AlphabetCharacter lastCharInSubstr = substr.getLastChar();

      Node activeNode = activePoint.getActiveNode();
      Edge edgeStartingWithLastChar = null;
      for (Edge outgoingEdge : activeNode.getOutgoingEdges()) {
        if (outgoingEdge.getTextSubstring().getFirstChar()
            .equals(lastCharInSubstr)) {
          edgeStartingWithLastChar = outgoingEdge;
          break;
        }
      }

      if (edgeStartingWithLastChar != null) {
        // Update suffix links.
        if (insertsAtStep > 0 && activePoint.getActiveNode() != root) {
          lastInsertedNode.setSuffixLink(activePoint.getActiveNode());
          lastInsertedNode = activePoint.getActiveNode();
        }
        // Move into middle of this edge.
        activePoint.updateToMiddleOfOutgoingEdge(edgeStartingWithLastChar);
      } else {
        // Add a new edge.
        Edge edge = new Edge(activeNode, substr.getEndIndex() - 1, this);
        activeNode.addOutgoingEdge(edge);
        prefixStart++;
        TextSubstring newSubstr = new TextSubstring(inputText, prefixStart,
            substr.getEndIndex() - prefixStart);
        resetWithSuffixLinks(newSubstr);
        if (insertsAtStep > 0 && activePoint.getActiveNode() != root) {
          lastInsertedNode.setSuffixLink(activePoint.getActiveNode());
          lastInsertedNode = activePoint.getActiveNode();
        }
        if (newSubstr.getStartIndex() < newSubstr.getEndIndex())
          addSubstring(newSubstr);
      }
    }

    protected void resetWithSuffixLinks(TextSubstring currentSubstr) {
      if (activePoint.getActiveNode() == root
          && currentSubstr.getStartIndex() >= currentSubstr.getEndIndex()) {
        // Start back at root.
        activePoint.setActiveEdge(null);
        activePoint.setActiveLength(0);
        return;
      }

      if (activePoint.getActiveNode() == root) {
        // Move into edge.
        AlphabetCharacter substrFirstChar = currentSubstr.getFirstChar();
        for (Edge outgoingEdge : activePoint.getActiveNode().getOutgoingEdges()) {
          if (outgoingEdge.getTextSubstring().getFirstChar()
              .equals(substrFirstChar)) {
            activePoint.setActiveEdge(outgoingEdge);
            break;
          }
        }

        if (activePoint.getActiveLength() > 0)
          activePoint.setActiveLength(activePoint.getActiveLength() - 1);

        activePoint.moveToActiveEdgeEndpointIfNeeded();
        activePoint.moveForwardActiveEdge(currentSubstr);
        return;
      }

      if (activePoint.getActiveNode().hasSuffixLink()) {
        // Move active node to suffix link.
        activePoint.setActiveNode(activePoint.getActiveNode().getSuffixLink());
        activePoint.updateActiveEdgeAfterNodeChange();
        activePoint.moveForwardActiveEdge(currentSubstr);
        return;
      }

      activePoint.setActiveNode(root);
      activePoint.updateActiveEdgeAfterNodeChange();
      activePoint.moveForwardActiveEdge(currentSubstr);
    }

    protected void addIntoEdge(TextSubstring substr) {
      AlphabetCharacter lastCharInSubstr = substr.getLastChar();
      AlphabetCharacter charForInsertion = activePoint.getActiveEdge()
          .getTextSubstring().getIthChar(activePoint.getActiveLength());

      if (lastCharInSubstr.equals(charForInsertion)) {
        activePoint.setActiveLength(activePoint.getActiveLength() + 1);
        activePoint.moveToActiveEdgeEndpointIfNeeded();
      } else {
        // Split the edge: activeEdge -> newNode -> newEdge, oldEdge
        Node newNode = new Node(activePoint.getActiveEdge());
        Edge newEdge = new Edge(newNode, substr.getEndIndex() - 1, this);
        newNode.addOutgoingEdge(newEdge);
        int oldEdgeTextStartIndex = activePoint.getActiveEdge()
            .getTextSubstring().getStartIndex()
            + activePoint.getActiveLength();
        Edge oldEdge = new Edge(newNode, new TextSubstring(inputText,
            oldEdgeTextStartIndex, activePoint.getActiveEdge()
                .getTextSubstring().getEndIndex()
                - oldEdgeTextStartIndex));
        oldEdge.setToNode(activePoint.getActiveEdge().getToNode());
        newNode.addOutgoingEdge(oldEdge);
        activePoint.getActiveEdge().setToNode(newNode);
        activePoint.getActiveEdge().setTextSubstring(
            new TextSubstring(inputText, activePoint.getActiveEdge()
                .getTextSubstring().getStartIndex(), activePoint
                .getActiveLength()));
        if (insertsAtStep > 0)
          lastInsertedNode.setSuffixLink(newNode);
        lastInsertedNode = newNode;
        insertsAtStep++;

        prefixStart++;
        TextSubstring newSubstr = new TextSubstring(inputText, prefixStart,
            substr.getEndIndex() - prefixStart);
        resetWithSuffixLinks(newSubstr);
        if (newSubstr.getStartIndex() < newSubstr.getEndIndex())
          addSubstring(newSubstr);
      }
    }

    protected SuffixTree build() {
      processPrefixes();
      root.sortEdgesAndPutNodesAtLeaves(0);
      return new SuffixTree(this);
    }

  }

}
