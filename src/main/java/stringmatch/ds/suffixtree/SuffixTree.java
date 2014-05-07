package stringmatch.ds.suffixtree;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;
import stringmatch.ds.text.TextSubstring;

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
      root.putNodesAtLeaves(0);
      return new SuffixTree(this);
    }
    
  }
  
}
