package stringmatch.ds.suffixtree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;
import stringmatch.ds.text.TextSubstring;
import stringmatch.ds.util.Pair;
import stringmatch.ds.suffixtree.Node;

/*
 * Stores a suffix tree.
*/

public class SuffixTree {

  protected Node root;

  protected SuffixTree() { }
  
  public SuffixTree(Node root) {
    this.root = root;
  }
  
  private SuffixTree(Builder builder) {
    root = builder.root;
  }

  public Node getRoot() {
    return root;
  }

  public List<List<AlphabetCharacter>> getAllSuffixes() {
    return getAllSuffixes(false);
  }
  
  public List<List<AlphabetCharacter>> getAllSuffixes(boolean ignoreCentroid) {
    return root.getAllSuffixes(ignoreCentroid);
  }
  
  public List<String> getAllSuffixesAsStrings() {
    return getAllSuffixesAsStrings(false);
  }
  
  public List<String> getAllSuffixesAsStrings(boolean ignoreCentroid) {
    List<String> allSuffixesAsStrings = new ArrayList<String>();
    for (List<AlphabetCharacter> x : getAllSuffixes(ignoreCentroid)) {
      String xStr = "";
      for (AlphabetCharacter y : x) {
        xStr += y.toString();
      }
      allSuffixesAsStrings.add(xStr);
    }
    return allSuffixesAsStrings;
  }
  
  public void printNode(Node node) {
    String s = "";
    Edge e = node.incomingEdge;
    while (e != null) {
      s = e.toString() + s;
      Node n = e.getFromNode();
      e = n.incomingEdge;
    }
    System.out.println(s);
  }
    
  /* 
   * Checks that following an edge matches all the characters along the edge. If allowWildcards
   * is set, then all characters except AlphabetCharacter.END_CHAR are matched to
   * AlphabetCharacter.WILDCARD.
   */
  protected boolean checkMatch(Text p, int start, Edge e, boolean allowWildcards) {
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
  public Pair<Node, Integer> query(Text p) {
    return query(p, 0, root);
  }

  protected Pair<Node, Integer> query(Text p, int start, Node current) {
    if (start >= p.getSize()) {
      return new Pair<Node, Integer>(current, p.getSize() - start);
    }
    Edge e = current.follow(p.getCharAtIndex(start));
    if (checkMatch(p, start, e, false)) {
      return query(p, start + e.getTextSubstring().length, e.getToNode());
    }
    return null;
  }

  public boolean wildcardMatch(AlphabetCharacter nextOnEdge,
      AlphabetCharacter nextInPattern) {
    return (nextInPattern.isWild() && !nextOnEdge.isEnd());
  }
  
  public static void main(String[] args) {
    Text t = new Text("BANANABANANA", true);
    SuffixTree.Builder suffixTreeBuilder = new SuffixTree.Builder(t);
    SuffixTree st = suffixTreeBuilder.build();
    st.printTree();
    System.out.println(st.query(new Text("ANABA", false), 1, st.root));
  }

  public List<Integer> naiveWildcardQueryIndices(Text p) {
    List<Node> nodes = naiveWildcardQuery(p);
    Set<Integer> nodeIndices = new HashSet<Integer>();
    for (Node node : nodes) {
      nodeIndices.addAll(node.getOffsetIndicesOfLeaves());
    }
    return new ArrayList<Integer>(nodeIndices);
  }
  
  /*
   * Returns the node corresponding to the longest common prefix of p with the
   * suffix tree, or null if there is no such prefix. Allows wildcards.
   */
  protected List<Node> naiveWildcardQuery(Text p) {
    return naiveWildcardQuery(p, 0, root);
  }

  protected List<Node> naiveWildcardQuery(Text p, int start, Node current) {
    List<Node> results = new ArrayList<Node>();
    if (start >= p.getSize()) {
      results.add(current);
      return results;
    }
    Edge e = current.follow(p.getCharAtIndex(start));
    if (checkMatch(p, start, e, true)) {
      results.addAll(naiveWildcardQuery(p, start
          + e.getTextSubstring().length, e.getToNode()));
    }
    for (Edge next : current.getOutgoingEdges()) {
      if (p.getCharAtIndex(start).isWild()) {
        if (checkMatch(p, start, next, true)) {
          results.addAll(naiveWildcardQuery(p,
              start + next.getTextSubstring().length, next.getToNode()));
        }
      }
    }
    return results;
  }

  /*
   * Print out the tree in a readable form.
   */
  protected void printTree() {
    printTree("", root, "ROOT", true);
  }

  protected void printTree(String prefix, Node n, String label, boolean leaf) {
    System.out.println(prefix + (leaf ? "|-- " : "|-- ") + label);
    if (n.outgoingEdges != null && n.outgoingEdges.size() > 0) {
      for (int i = 0; i < n.outgoingEdges.size() - 1; i++) {
        Edge e = n.outgoingEdges.get(i);
        String l = e.toString();
        printTree(prefix + (leaf ? "    " : "|   "), e.getToNode(), l,
            false);
      }
      if (n.outgoingEdges.size() >= 1) {
        Edge e = n.outgoingEdges.get(n.outgoingEdges.size() - 1);
        String l = e.toString();
        printTree(prefix + (leaf ? "    " : "|   "), e.getToNode(), l,
            true);
      }
    }
  }

 /*
  * Builds the suffix tree using Ukkonen's algorithm, as described in:
  * http://stackoverflow.com/questions/9452701/ukkonens-suffix-tree-algorithm-in-plain-english/
  *
  * Based largely on:
  * https://github.com/maxgarfinkel/suffixTree/
  */
  public static class Builder {
    protected Node root;
    private ActivePoint activePoint;
    private int insertsAtStep;
    private Node lastInsertedNode;
    private int prefixStart;
    int prefixEnd;
    private int endPosition;

    private Text inputText;

    public Builder(Text inputText) {
      root = new Node(null);
      activePoint = new ActivePoint(root);
      insertsAtStep = 0;
      lastInsertedNode = null;
      prefixStart = 0;
      prefixEnd = 0;
      endPosition = 0;

      this.inputText = inputText;
    }

    protected int getEndPosition() {
      return endPosition;
    }

    protected Text getInputText() {
      return inputText;
    }

    private void processPrefixes() {
      for (int i = 0; i < inputText.getLength(); i++) {
        prefixEnd++;
        int length = prefixEnd - prefixStart;
        TextSubstring prefix = new TextSubstring(inputText, prefixStart, length);
        insertsAtStep = 0;
        addSubstring(prefix);
        endPosition++;
      }
    }

    private void addSubstring(TextSubstring substr) {
      if (activePoint.isOnNode())
        addOntoNode(substr);
      else if (activePoint.isInsideEdge())
        addIntoEdge(substr);
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

        if (prefixStart == prefixEnd)
          prefixEnd++;
        prefixStart++;
        TextSubstring newSubstr = new TextSubstring(inputText, prefixStart,
            prefixEnd - prefixStart);
        // activeNode as used below should stay as is, even if activePoint.getActiveNode()
        // changes with the call immediately below.

        resetWithSuffixLinks(newSubstr);
        if (insertsAtStep > 0 && activeNode != root) {
          lastInsertedNode.setSuffixLink(activeNode);
          lastInsertedNode = activeNode;
        }
        if (newSubstr.getStartIndex() < newSubstr.getEndIndex() &&
            newSubstr.getEndIndex() <= newSubstr.getText().getLength())
          addSubstring(newSubstr);
      }
    }

    protected void resetWithSuffixLinks(TextSubstring currentSubstr) {
      if (activePoint.getActiveNode() == root
          && (currentSubstr.getStartIndex() >= currentSubstr.getEndIndex() ||
          currentSubstr.getEndIndex() > currentSubstr.getText().getLength())) {
        // Start back at root.
        activePoint.setActiveEdge(null);
        activePoint.setActiveLength(0);
        return;
      }

      if (activePoint.getActiveNode() == root) {
        // Move into edge.
        AlphabetCharacter substrFirstChar = currentSubstr.getFirstChar();
        activePoint.setActiveEdge(null);
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
        Edge oldEdge = new Edge(newNode, oldEdgeTextStartIndex, this);
        if (!activePoint.getActiveEdge().textEndsAtTreeEnd()) {
          oldEdge.setTextSubstring(new TextSubstring(inputText, oldEdgeTextStartIndex,
              activePoint.getActiveEdge().getTextSubstring().getEndIndex() -
              oldEdgeTextStartIndex));
        }
        oldEdge.setToNode(activePoint.getActiveEdge().getToNode());
        if (activePoint.getActiveEdge() != null && activePoint.getActiveEdge().getToNode() != null)
          activePoint.getActiveEdge().getToNode().setIncomingEdge(oldEdge);
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

        if (prefixStart == prefixEnd)
          prefixEnd++;
        prefixStart++;
        TextSubstring newSubstr = new TextSubstring(inputText, prefixStart,
            prefixEnd - prefixStart);
        resetWithSuffixLinks(newSubstr);

        if (newSubstr.getStartIndex() < newSubstr.getEndIndex())
          addSubstring(newSubstr);
      }
    }

    public SuffixTree build() {
      processPrefixes();
      //root.removeEndCharEdge();
      root.sortEdgesAndPutNodesAtLeaves(0, new int[] { 0 });
      return new SuffixTree(this);
    }

  }

}
