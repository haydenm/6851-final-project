package stringmatch.ds.suffixtree;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.TextSubstring;

/*
 * The active point is a triple (activeNode, activeEdge, activeLength),
 * following the definition at:
 * http://stackoverflow.com/questions/9452701/ukkonens-suffix-tree-algorithm-in-plain-english/
 *
 * Based largely on:
 * https://github.com/maxgarfinkel/suffixTree/
 */

public class ActivePoint {

  private Node activeNode;
  private Edge activeEdge;
  private int activeLength;
  
  protected ActivePoint(Node root) {
    activeNode = root;
    activeEdge = null;
    activeLength = 0;
  }
  
  protected Node getActiveNode() {
    return activeNode;
  }
  
  protected Edge getActiveEdge() {
    return activeEdge;
  }
  
  protected int getActiveLength() {
    return activeLength;
  }
  
  protected void setActiveNode(Node node) {
    activeNode = node;
  }
  
  protected void setActiveEdge(Edge edge) {
    activeEdge = edge;
  }
  
  protected void setActiveLength(int length) {
    activeLength = length;
  }
  
  protected boolean isOnNode() {
    return activeEdge == null && activeLength == 0;
  }
  
  protected boolean isInsideEdge() {
    return activeEdge != null;
  }
  
  protected void updateToMiddleOfOutgoingEdge(Edge edge) {
    activeEdge = edge;
    activeLength++;
    moveToActiveEdgeEndpointIfNeeded();
  }
  
  protected void moveToActiveEdgeEndpointIfNeeded() {
    if (isInsideEdge() &&
        activeEdge.getTextSubstring().getLength() == activeLength &&
        activeEdge.getToNode() != null) {
      activeNode = activeEdge.getToNode();
      activeEdge = null;
      activeLength = 0;
    }
  }
  
  protected void moveForwardActiveEdge(TextSubstring currentSubstr) {
    while (isInsideEdge() &&
        activeLength > activeEdge.getTextSubstring().getLength()) {
      // Move forward to active edge endpoint. Update activeNode to endpoint
      // and update active edge.
      activeNode = activeEdge.getToNode();
      activeLength -= activeEdge.getTextSubstring().getLength();
      
      int substrFindIndex = currentSubstr.getLength() - (activeLength + 1);
      AlphabetCharacter substrFindChar = currentSubstr.getIthChar(substrFindIndex);
      for (Edge outgoingEdge : activeNode.getOutgoingEdges()) {
        if (outgoingEdge.getTextSubstring().getFirstChar().equals(substrFindChar)) {
          activeEdge = outgoingEdge;
          break;
        }
      }
    }
    
    if (activeLength == 0)
      activeEdge = null;
    
    moveToActiveEdgeEndpointIfNeeded();
  }
  
  protected void updateActiveEdgeAfterNodeChange() {
    if (!isInsideEdge())
      return;
    
    AlphabetCharacter activeEdgeFirstChar = activeEdge.getTextSubstring().
        getFirstChar();
    for (Edge outgoingEdge : activeNode.getOutgoingEdges()) {
      if (outgoingEdge.getTextSubstring().getFirstChar().
          equals(activeEdgeFirstChar)) {
        activeEdge = outgoingEdge;
        break;
      }
    }
  }
	
}
