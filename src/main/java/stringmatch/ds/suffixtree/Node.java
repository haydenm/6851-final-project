package stringmatch.ds.suffixtree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import stringmatch.ds.text.Text;
import stringmatch.ds.text.TextSubstring;

public class Node {

  private List<Edge> outgoingEdges;
  private Node suffixLink;
  
  private int numChildren;
  private Edge centroidEdge;
  private boolean isLeaf;
  
  protected Node() {
    outgoingEdges = new ArrayList<Edge>();
    suffixLink = null;
    numChildren = 0;
    centroidEdge = null;
    isLeaf = false;
  }
  
  protected Node(boolean isLeaf) {
    this();
    this.isLeaf = isLeaf;
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
  
  protected void putNodesAtLeaves() {
    for (Edge outgoingEdge : outgoingEdges) {
      if (outgoingEdge.getToNode() != null) {
        outgoingEdge.getToNode().putNodesAtLeaves();
      } else {
        Node leaf = new Node(true);
        outgoingEdge.setToNode(leaf);
      }
    }
  }
  
  protected void findCentroidPaths() {
    numChildren = 0;
    int maxNumberOfChildren = 0;
    for (Edge outgoingEdge : outgoingEdges) {
      // In each child, count the number of their children.
      outgoingEdge.getToNode().findCentroidPaths();
      
      // Add up this node's number of children.
      numChildren += 1 + outgoingEdge.getToNode().numChildren;
      
      // Find the max number of children. And keep track of the heaviest
      // children, breaking ties arbitrarily.
      if (outgoingEdge.getToNode().numChildren >= maxNumberOfChildren) {
        maxNumberOfChildren = outgoingEdge.getToNode().numChildren;
        centroidEdge = outgoingEdge;
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