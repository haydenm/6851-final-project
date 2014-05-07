package stringmatch.ds.suffixtree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;
import stringmatch.ds.text.TextSubstring;

public class Node {

  protected Edge incomingEdge;
  protected List<Edge> outgoingEdges;
  protected Node suffixLink;
  
  protected int numLeaves;
  
  protected Edge centroidEdge;
  protected boolean isLeaf;
  protected int leafIndex;
  
  protected Node(Edge incomingEdge) {
    this.incomingEdge = incomingEdge;
    outgoingEdges = new ArrayList<Edge>();
    suffixLink = null;
    numLeaves = -1;
    centroidEdge = null;
    isLeaf = false;
    leafIndex = -1;
  }
  
  protected Node(Edge incomingEdge, boolean isLeaf, int leafIndex) {
    this(incomingEdge);
    this.isLeaf = isLeaf;
    this.leafIndex = leafIndex;
  }
  
  /*protected Edge follow(Text p, int start) {
	  for (Edge e: outgoingEdges) {
		  boolean match = true;
		  TextSubstring edgeSubstring = e.getTextSubstring();
		  TextSubstring next = new TextSubstring(p, start, Math.min(edgeSubstring.getLength(), p.getLength() - start));
		  String edgeString = edgeSubstring.toString();
		  String nextString = next.toString();
		  System.out.println(edgeString);
		  System.out.println(nextString);
		  for (int i = 0; i < Math.min(edgeSubstring.length, p.getLength() - start); i++) {
			  System.out.println(edgeString.charAt(i));
			  System.out.println(nextString.charAt(i));
			  if (edgeString.charAt(i) != nextString.charAt(i)) {
				  match = false;
			  }
		  }
		  if (match) {
			  return e;
		  }
	  }
	return null;
  }*/
  
  protected Edge follow(AlphabetCharacter c) {
	  for (Edge e: outgoingEdges) {
		  TextSubstring edgeSubstring = e.getTextSubstring();
		  AlphabetCharacter first = edgeSubstring.getIthChar(0);
		  if (c.equals(first)) {
			  return e;
		  }
	  }
	  return null;
  }
  
  public String toString() {
	  if (isLeaf) {
		  return "LEAF";
	  } else {
		  return outgoingEdges.toString();
	  }
  }
  
  protected List<Edge> getOutgoingEdges() {
    return outgoingEdges;
  }
  
  protected void addOutgoingEdge(Edge edge) {
    outgoingEdges.add(edge);
  }
  
  protected void removeOutgoingEdge(Edge edge) {
    boolean removed = outgoingEdges.remove(edge);
    if (!removed)
      throw new IllegalArgumentException("No such outgoing edge to remove.");
  }
  
  protected boolean hasSuffixLink() {
    return suffixLink != null;
  }
  
  protected Node getSuffixLink() {
    return suffixLink;
  }
  
  protected void setSuffixLink(Node node) {
    suffixLink = node;
  }
  
  protected void putNodesAtLeaves(int height) {
    for (Edge outgoingEdge : outgoingEdges) {
      int outgoingEdgeHeight = outgoingEdge.getTextSubstring().getLength();
      if (outgoingEdge.getToNode() != null) {
        outgoingEdge.getToNode().putNodesAtLeaves(height + outgoingEdgeHeight);
      } else {
        int offset = outgoingEdge.getTextSubstring().getText().getLength() - 
            (height + outgoingEdgeHeight);
        Node leaf = new Node(outgoingEdge, true, offset);
        outgoingEdge.setToNode(leaf);
      }
    }
  }
  
  protected List<Text> getEdgeStringsInDFS() {
    List<Text> edgeTexts = new ArrayList<Text>();
    for (Edge outgoingEdge : outgoingEdges) {
      Text edgeText = outgoingEdge.getTextSubstring().getSubstringAsText();
      edgeTexts.add(edgeText);
      edgeTexts.addAll(outgoingEdge.getToNode().getEdgeStringsInDFS());
    }
    return edgeTexts;
  }
  
  protected List<Text> getEdgeStringsInBFS() {
    List<Text> edgeTexts = new ArrayList<Text>();
    Queue<Node> toCall = new LinkedList<Node>();
    toCall.add(this);
    while (toCall.size() > 0) {
      Node node = toCall.remove();
      for (Edge outgoingEdge : node.outgoingEdges) {
        Text edgeText = outgoingEdge.getTextSubstring().getSubstringAsText();
        edgeTexts.add(edgeText);
        toCall.add(outgoingEdge.getToNode());
      }
    }
    return edgeTexts;
  }
  
  protected List<TextSubstring> getAllSuffixes() {
    List<TextSubstring> suffixes = new ArrayList<TextSubstring>();
    for (Edge outgoingEdge : outgoingEdges) {
      if (outgoingEdge.getToNode().isLeaf) {
        suffixes.add(outgoingEdge.getTextSubstring());
      } else {
        List<TextSubstring> suffixesAtChild = outgoingEdge.getToNode().getAllSuffixes();
        for (TextSubstring childSuffix : suffixesAtChild) {
          suffixes.add(outgoingEdge.getTextSubstring().mergeWith(childSuffix));
        }
      }
    }
    return suffixes;
  }
  
}
