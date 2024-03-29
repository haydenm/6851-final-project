package stringmatch.ds.suffixtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;
import stringmatch.ds.text.TextSubstring;
import stringmatch.ds.util.Pair;

public class Node {

  List<Edge> outgoingEdges;
  protected Edge incomingEdge;
  protected Node leftMost;
  protected Node rightMost;

  protected int numLeaves;
  
  protected Edge centroidEdge;
  protected boolean isLeaf;
  
  // Stores where the leaf falls in the input string. For *all* leaves
  // (including those in wildcard subtrees), this accounts for the entire
  // path from the root of the original suffix tree S down to the leaf.
  // (That is, it is not simply the offset of the string from the root
  // of the wildcard subtree down to the leaf. It is the offset of the
  // string from the root of S down to the leaf.)
  protected int leafOffsetIndexInS;
  
  // Stores where the leaf falls in the input string based on the
  // string from the root of the wildcard subtree containing this
  // leaf down to the leaf. Note that this value is simply the
  // depth of the wildcard subtree containing this leaf plus
  // leafOffsetIndexInS.
  protected int leafOffsetIndexInT;
  
  // Stores where the leaf falls in lexicographic order, where the
  // leaf's string is considered from the root of the original suffix
  // tree S down to the leaf.
  // The value is DOUBLED from what it would normally be.
  protected int leafLexicographicIndexInS;
  
  // Stores where the leaf falls in lexicographic order in the input string
  // based on the string from the root of the wildcard subtree containing this leaf
  // down to the leaf.
  // The value is DOUBLED from what it would normally be.
  protected int leafLexicographicIndexInT;
  
  // Stores the depth of the node from the root of the wildcard subtree containing
  // this leaf.
  protected int depthInSubtree;
  
  protected int LCAIndex;
  protected int maxHeight;
  protected Edge longPathEdge;
  protected Pair<Integer, Path> ladder;
  protected Node rootPointer;
  
  /* Used for building! */
  protected Node suffixLink;
  /* Used for building! */
  
  protected Node(Edge incomingEdge) {
    this.incomingEdge = incomingEdge;
    outgoingEdges = new ArrayList<Edge>();
    suffixLink = null;
    numLeaves = -1;
    centroidEdge = null;
    isLeaf = false;
    leafOffsetIndexInS = -1;
    leafOffsetIndexInT = -1;
    leafLexicographicIndexInS = -1;
    leafLexicographicIndexInT = -1;
    depthInSubtree = -1;
    LCAIndex = -1;
    maxHeight = -1;
  }
  
  protected Node(Edge incomingEdge, boolean isLeaf, int leafOffsetIndexInS,
      int leafLexicographicIndexInS) {
    this(incomingEdge);
    this.isLeaf = isLeaf;
    this.leafOffsetIndexInS = leafOffsetIndexInS;
    this.leafLexicographicIndexInS = leafLexicographicIndexInS;
    leafOffsetIndexInT = -1;
    leafLexicographicIndexInT = -1;
    depthInSubtree = -1;
    LCAIndex = -1;
    maxHeight = -1;
  }
  
  protected void removeEndCharEdge() {
    for (int i = 0; i < outgoingEdges.size(); i++) {
      Edge outgoingEdge = outgoingEdges.get(i);
      if (outgoingEdge.getTextSubstring().getLength() == 1 &&
          outgoingEdge.getTextSubstring().getFirstChar().equals(AlphabetCharacter.END_CHAR)) {
        outgoingEdges.remove(i);
        break;
      }
    }
  }
  
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
  
  protected Edge followPrevious(AlphabetCharacter c) {
    if (outgoingEdges.size() > 0) {
      Edge prev = outgoingEdges.get(0);
      AlphabetCharacter first = prev.getCharAt(0);
      for (Edge e: outgoingEdges) {
        AlphabetCharacter next = e.getCharAt(0);
        if (first.compareTo(next) < 0 && c.compareTo(next) > 0 && !e.isWildcardEdge()) {
          prev = e;
          first = next;
        }
      }
      return prev;
    }
    return null;
  }
  
  protected Edge followLeft() {
    if (outgoingEdges.size() > 0) {
      Edge left = outgoingEdges.get(0);
      AlphabetCharacter first = left.getCharAt(0);
        for (Edge e: outgoingEdges) {
          AlphabetCharacter next = e.getCharAt(0);
          if (first.compareTo(next) > 0 && !e.isWildcardEdge()) {
            left = e;
            first = next;
          }
        }
        return left;
    }
    return null;
  }
  
  protected Edge followRight() {
    if (outgoingEdges.size() > 0) {
      Edge right = outgoingEdges.get(0);
      AlphabetCharacter first = right.getCharAt(0);
        for (Edge e: outgoingEdges) {
          AlphabetCharacter next = e.getCharAt(0);
          if (first.compareTo(next) < 0 && !e.isWildcardEdge()) {
            right = e;
            first = next;
          }
        }
        return right;
    }
    return null;
  }
  
  protected boolean isLeaf() {
    return outgoingEdges == null || outgoingEdges.size() == 0;
  }
  
  protected int numChildren() {
    return outgoingEdges.size();
  }
  
  protected Node getChild(int i) {
    return outgoingEdges.get(i).getToNode();
  }
  
  public String toString() {
    if (incomingEdge != null) {
      return incomingEdge.toString();
    } else {
      return "ROOT";
    }
  }
  
  protected void setIncomingEdge(Edge edge) {
    incomingEdge = edge;
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
  
  protected void sortEdgesAndPutNodesAtLeaves(int height, int[] leafCount) {
    Collections.sort(outgoingEdges);
    for (Edge outgoingEdge : outgoingEdges) {
      outgoingEdge.fixTextSubstringAfterBuild();
      int outgoingEdgeHeight = outgoingEdge.getTextSubstring().getLength();
      
      if (outgoingEdge.getToNode() != null && outgoingEdge.getToNode().incomingEdge != outgoingEdge)
        throw new RuntimeException("Mismatched incomingEdge");
      if (outgoingEdge.getFromNode() != this)
        throw new RuntimeException("Mismatched fromNode.");
      
      if (outgoingEdge.getToNode() != null) {
        outgoingEdge.getToNode().sortEdgesAndPutNodesAtLeaves(
            height + outgoingEdgeHeight, leafCount);
      } else {
        int offset = outgoingEdge.getTextSubstring().getText().getLength() - 
            (height + outgoingEdgeHeight);
        Node leaf = new Node(outgoingEdge, true, offset, 2*leafCount[0]);
        outgoingEdge.setToNode(leaf);
        leafCount[0]++;
      }
    }
  }
  
  public void doubleCheckOutgoingEdges() {
    for (Edge outgoingEdge : outgoingEdges) {
      if (outgoingEdge.getToNode() != null && outgoingEdge.getToNode().incomingEdge != outgoingEdge)
        throw new RuntimeException("Mismatched incomingEdge");
      if (outgoingEdge.getFromNode() != this)
        throw new RuntimeException("Mismatched fromNode.");
      
      if (outgoingEdge.getToNode() != null) {
        outgoingEdge.getToNode().doubleCheckOutgoingEdges();
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
  
  protected List<List<AlphabetCharacter>> getAllSuffixes() {
    return getAllSuffixes(false);
  }
  
  protected List<List<AlphabetCharacter>> getAllSuffixes(boolean ignoreCentroidEdge) {
    if (ignoreCentroidEdge && centroidEdge == null)
      throw new IllegalArgumentException();
    
    List<List<AlphabetCharacter>> suffixes = new ArrayList<List<AlphabetCharacter>>();
    for (Edge outgoingEdge : outgoingEdges) {
      if (ignoreCentroidEdge && outgoingEdge == centroidEdge)
        continue;
      
      if (outgoingEdge.getToNode().isLeaf) {
        suffixes.add(outgoingEdge.getTextSubstring().getSubstringAsText().getList());
      } else {
        List<List<AlphabetCharacter>> suffixesAtChild = outgoingEdge.getToNode()
            .getAllSuffixes(false);
        for (List<AlphabetCharacter> childSuffix : suffixesAtChild) {
          List<AlphabetCharacter> outgoingEdgeSubstring = new ArrayList<AlphabetCharacter>(
              outgoingEdge.getTextSubstring().getSubstringAsText().getList());
          outgoingEdgeSubstring.addAll(childSuffix);
          suffixes.add(outgoingEdgeSubstring);
        }
      }
    }
    return suffixes;
  }
  
  /*
   * Returns a list of the leafOffsetIndexInS (i.e., the positions of the leaves in the
   * input text) for all the leaves in the subtree rooted at this, based on the
   * strings considered from the root of the suffix tree S down to the leaf.
   */
  public List<Integer> getOffsetIndicesOfLeaves() {
    List<Integer> indices = new ArrayList<Integer>();
    
    if (isLeaf()) {
      indices.add(leafOffsetIndexInS);
      return indices;
    }
    
    for (Edge e : outgoingEdges) {
      if (!e.isWildcardEdge()) {
        // There's no need to follow wildcard edges because wildcard subtrees
        // are just copies of the tree we're in.
        indices.addAll(e.getToNode().getOffsetIndicesOfLeaves());
      }
    }
    
    return indices;
  }
  
  /*
   * Condenses the outgoing edges by merging ones that share characters.
   */
  protected void condense() {
    Map<AlphabetCharacter, Edge> outgoingEdgesByFirstChar
      = new HashMap<AlphabetCharacter, Edge>();
    for (Edge edge : outgoingEdges) {
      if (outgoingEdgesByFirstChar.containsKey(edge.getTextSubstring().getFirstChar())) {
        // We already found an edge that starts with the same char, so let's merge them.
        Edge matchingEdge = outgoingEdgesByFirstChar.get(edge.getTextSubstring().getFirstChar());
        int commonPrefixLength = edge.getTextSubstring().
            commonPrefixLength(matchingEdge.getTextSubstring());
        TextSubstring mergedTextSubstring = new TextSubstring(
            edge.getTextSubstring().getText(),
            edge.getTextSubstring().getStartIndex(), commonPrefixLength);
        Edge mergedEdge = new Edge(this, mergedTextSubstring);
        Node mergedNode = new Node(mergedEdge);
        mergedEdge.setToNode(mergedNode);
        // Note: mergedNode cannot be a leaf because it cannot be the case that both of
        // the two edges being merged are attached directly to leaf nodes (otherwise
        // those two leaf nodes would represent exactly the same suffix).
        
        // Process the children attached to edge.
        if (commonPrefixLength == edge.getTextSubstring().getLength()) {
          // The common prefix is the entire edge, so add all of its children to the new node.
          // Note: edge cannot be pointing directly to a leaf node (i.e., it must have children)
          // because otherwise there would be another edge that shares a '$' in common with
          // this one.
          
          for (Edge childEdge : edge.getToNode().getOutgoingEdges()) {
            childEdge.fromNode = mergedNode;
            mergedNode.outgoingEdges.add(childEdge);
          }
        } else {
          // mergedNode must have as an outgoing edge a new edge that represents what is
          // left of edge (i.e., what is not in mergedEdge).
          TextSubstring newChildEdgeTextSubstring = new TextSubstring(
              edge.getTextSubstring().getText(),
              edge.getTextSubstring().getStartIndex() + commonPrefixLength,
              edge.getTextSubstring().getLength() - commonPrefixLength);
          Edge newChildEdge = new Edge(mergedNode, newChildEdgeTextSubstring);
          Node newChildNode = new Node(newChildEdge);
          newChildNode.outgoingEdges.addAll(edge.getToNode().outgoingEdges);
          for (Edge e : edge.getToNode().outgoingEdges) {
            e.fromNode = newChildNode;
          }
          if (newChildNode.outgoingEdges.size() == 0) {
            newChildNode.isLeaf = true;
            newChildNode.leafOffsetIndexInS = edge.getToNode().leafOffsetIndexInS;
            newChildNode.leafLexicographicIndexInS = edge.getToNode().leafLexicographicIndexInS;
          }
          mergedNode.addOutgoingEdge(newChildEdge);
          newChildEdge.setToNode(newChildNode);
        }
        
        // Process the children attached to matchingEdge.
        if (commonPrefixLength == matchingEdge.getTextSubstring().getLength()) {
          // The common prefix is the entire edge, so add all of its children to the new node.
          // Note: matchingEdge cannot be pointing directly to a leaf node (i.e., it must have children)
          // because otherwise there would be another edge that shares a '$' in common with
          // this one.
          for (Edge childEdge : matchingEdge.getToNode().getOutgoingEdges()) {
            childEdge.fromNode = mergedNode;
            mergedNode.outgoingEdges.add(childEdge);
          }
        } else {
          // mergedNode must have as an outgoing edge a new edge that represents what is
          // left of matchingEdge (i.e., what is not in mergedEdge).
          TextSubstring newChildEdgeTextSubstring = new TextSubstring(
              matchingEdge.getTextSubstring().getText(),
              matchingEdge.getTextSubstring().getStartIndex() + commonPrefixLength,
              matchingEdge.getTextSubstring().getLength() - commonPrefixLength);
          Edge newChildEdge = new Edge(mergedNode, newChildEdgeTextSubstring);
          Node newChildNode = new Node(newChildEdge);
          newChildNode.outgoingEdges.addAll(matchingEdge.getToNode().outgoingEdges);
          for (Edge e : matchingEdge.getToNode().outgoingEdges) {
            e.fromNode = newChildNode;
          }
          if (newChildNode.outgoingEdges.size() == 0) {
            newChildNode.isLeaf = true;
            newChildNode.leafOffsetIndexInS = matchingEdge.getToNode().leafOffsetIndexInS;
            newChildNode.leafLexicographicIndexInS = matchingEdge.getToNode().leafLexicographicIndexInS;
          }
          mergedNode.addOutgoingEdge(newChildEdge);
          newChildEdge.setToNode(newChildNode);
        }

        // Update the map of first characters to store the mergedEdge.
        outgoingEdgesByFirstChar.put(mergedEdge.getTextSubstring().getFirstChar(),
            mergedEdge);
      } else {
        // If this edge does not share a first char with an edge seen so far, put it
        // in the map.
        outgoingEdgesByFirstChar.put(edge.getTextSubstring().getFirstChar(), edge);
      }
    }
    
    // Update this node's outgoing edges.
    outgoingEdges.clear();
    outgoingEdges.addAll(outgoingEdgesByFirstChar.values());
    Collections.sort(outgoingEdges);
    
    // Recursively condense the children.
    for (Edge edge : outgoingEdges) {
      edge.getToNode().condense();
    }
  }
  
  protected Node clone() {
    return clone(false, null);
  }
  
  /*
   * Makes a deep clone of the tree rooted at this.
   */
  protected Node clone(boolean removeCentroidEdge, Edge incomingEdge) {
    Node copy = new Node(incomingEdge, isLeaf, leafOffsetIndexInS,
        leafLexicographicIndexInS);
    
    copy.numLeaves = numLeaves;
    if (removeCentroidEdge && centroidEdge != null)
      copy.numLeaves -= centroidEdge.getToNode().numLeaves;

    
    for (Edge outgoingEdge : outgoingEdges) {
      Edge edgeCopy = outgoingEdge.clone(copy);
      if (centroidEdge == outgoingEdge) {
        if (!removeCentroidEdge) {
          copy.outgoingEdges.add(edgeCopy);
          copy.centroidEdge = edgeCopy;
        }
      } else {
        copy.outgoingEdges.add(edgeCopy);
      }
    }
    
    return copy;
  }
  
}
