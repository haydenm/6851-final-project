package stringmatch.ds.suffixtree;

import java.util.ArrayList;
import java.util.List;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;
import stringmatch.ds.text.TextSubstring;
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
  
  protected SuffixTree() { }
  
  private SuffixTree(Builder builder) {
    root = builder.root;
  }
  
  public Node getRoot() {
    return root;
  }
  
  /* 
   * Checks that following an edge matches all the characters along the edge. If allowWildcards
   * is set, then all characters except AlphabetCharacter.END_CHAR are matched to
   * AlphabetCharacter.WILDCARD.
   */
  private boolean checkMatch(Text p, int start, Edge e, boolean allowWildcards) {
	  if (e != null) {
		  for (int i = 0; i < Math.min(e.getTextSubstring().length, p.getLength() - start); i++) {
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
   * Returns the node corresponding to the longest common prefix of p with the suffix tree,
   * or null if there is no such prefix. No wildcards allowed.
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
  
  public boolean wildcardMatch(AlphabetCharacter nextOnEdge, AlphabetCharacter nextInPattern) {
	  return (nextInPattern.isWild() && !nextOnEdge.isEnd());
  }
  
  /*
   * Returns the node corresponding to the longest common prefix of p with the suffix tree,
   * or null if there is no such prefix. Allows wildcards.
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
		  results.addAll(naiveWildcardQueryHelper(p, start + e.getTextSubstring().length, e.getToNode()));
	  }
	  for (Edge next: current.getOutgoingEdges()) {
		  if (p.getCharAtIndex(start).isWild()) {
			  if (checkMatch(p, start, next, true)) {
				  results.addAll(naiveWildcardQueryHelper(p, start + next.getTextSubstring().length, next.getToNode()));
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
            printTreeHelper(prefix + (leaf ? "    " : "|   "), e.getToNode(), l, false);
        }
        if (n.outgoingEdges.size() >= 1) {
        	Edge e = n.outgoingEdges.get(n.outgoingEdges.size() - 1);
        	String l = e.toString();
            printTreeHelper(prefix + (leaf ?"    " : "|   "), e.getToNode(), l, true);
        }
        }
    }
  
  public static void main(String[] args) {
	    /*SuffixTree.Builder suffixTreeBuilder = new SuffixTree.Builder(new Text("BANANA", true));
	    SuffixTree st = suffixTreeBuilder.build();
	    Text queryText = new Text("AN*", false);
	    System.out.println(st.naiveWildcardQuery(queryText));
	    st.printTree();*/
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
        TextSubstring prefix = new TextSubstring(inputText, prefixStart,
            length);
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
        if (outgoingEdge.getTextSubstring().getFirstChar().equals(
            lastCharInSubstr)) {
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
        activePoint.updateToMiddleOfOutgoingEdge(
            edgeStartingWithLastChar);
      } else {
        // Add a new edge.
        Edge edge = new Edge(activeNode, substr.getEndIndex() - 1,
            this);
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
      if (activePoint.getActiveNode() == root &&
          currentSubstr.getStartIndex() >= currentSubstr.getEndIndex()) {
        // Start back at root.
        activePoint.setActiveEdge(null);
        activePoint.setActiveLength(0);
        return;
      }
      
      if (activePoint.getActiveNode() == root) {
        // Move into edge.
        AlphabetCharacter substrFirstChar = currentSubstr.getFirstChar();
        for (Edge outgoingEdge : activePoint.getActiveNode().getOutgoingEdges()) {
          if (outgoingEdge.getTextSubstring().getFirstChar().equals(
              substrFirstChar)) {
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
        int oldEdgeTextStartIndex = activePoint.getActiveEdge().
            getTextSubstring().getStartIndex() + activePoint.getActiveLength();
        Edge oldEdge = new Edge(newNode,
            new TextSubstring(inputText, oldEdgeTextStartIndex,
                activePoint.getActiveEdge().getTextSubstring().getEndIndex() -
                oldEdgeTextStartIndex));
        oldEdge.setToNode(activePoint.getActiveEdge().getToNode());
        newNode.addOutgoingEdge(oldEdge);
        activePoint.getActiveEdge().setToNode(newNode);
        activePoint.getActiveEdge().setTextSubstring(
            new TextSubstring(inputText, activePoint.getActiveEdge().
                getTextSubstring().getStartIndex(), activePoint.getActiveLength()));
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
