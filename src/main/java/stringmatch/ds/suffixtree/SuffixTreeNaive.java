package stringmatch.ds.suffixtree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;
import stringmatch.ds.text.TextSubstring;

public class SuffixTreeNaive extends SuffixTree {
  
  public SuffixTreeNaive(Builder builder) {
    root = builder.root;
  }
  
  /*
   * Takes a node which might have multiple edges sharing characters, and updates the node
   * to condense the edges
   */
  public void condense(Node n) {
	  Map<AlphabetCharacter, Edge> edges = new HashMap<AlphabetCharacter, Edge>();
	  for (Edge e: root.getOutgoingEdges()) {
	    
	    // We already found an edge with this starting letter
		  if (edges.containsKey(e.getCharAt(0))) {
			  int lengthOfSimilarity = 0;
			  Edge e1 = edges.get(e.getCharAt(0));
			  for (int i = 0; i < Math.min(e1.getTextSubstring().getLength(), e.getTextSubstring().getLength()); i++) {
				  if (e1.getCharAt(i).equals(e.getCharAt(i))) {
					  lengthOfSimilarity++;
				  } else {
					  break;
				  }
			  }
			  
			  // Create a new node which will follow this new merged edge
			  TextSubstring sub = new TextSubstring(e1.getTextSubstring().getText(),
					  								e1.getTextSubstring().getStartIndex(),
					  								lengthOfSimilarity);
			  Edge newEdge = new Edge(n, e1.getTextStart(), sub, e1.wildcard);
			  Node newNode = new Node();
			  
			  // If the similarity was the entire edge, add all the children to the new node
			  if (lengthOfSimilarity == e1.getTextSubstring().getLength()) {
				  newNode.outgoingEdges.addAll(e1.getToNode().outgoingEdges); 
				// Otherwise add a new single edge with the rest of the characters
			  } else {
				  TextSubstring newSub = new TextSubstring(e1.getTextSubstring().getText(),
				  										   e1.getTextSubstring().getStartIndex() + lengthOfSimilarity,
				  										   e1.getTextSubstring().getLength() - lengthOfSimilarity);
				  Edge newChildEdge = new Edge(newNode, e1.getTextStart(), newSub, false);
				  Node newChild = new Node();
				  newChild.outgoingEdges.addAll(e1.getToNode().outgoingEdges);
				  newNode.outgoingEdges.add(newChildEdge);
				  newChildEdge.setToNode(newChild);
			  }
			  // Do the same for the other edge being merged
			  if (lengthOfSimilarity == e.getTextSubstring().getLength()) {
				  newNode.outgoingEdges.addAll(e.getToNode().outgoingEdges);  
			  } else {
				  TextSubstring newSub = new TextSubstring(e.getTextSubstring().getText(),
				  										   e.getTextSubstring().getStartIndex() + lengthOfSimilarity,
				  										   e.getTextSubstring().getLength() - lengthOfSimilarity);
				  Edge newChildEdge = new Edge(newNode, e.getTextStart(), newSub, false);
				  Node newChild = new Node();
				  newChild.outgoingEdges.addAll(e.getToNode().outgoingEdges);
				  newNode.outgoingEdges.add(newChildEdge);
				  newChildEdge.setToNode(newChild);
			  }
			  newNode.outgoingEdges.addAll(e.getToNode().outgoingEdges);
			  newEdge.setToNode(newNode);
			  edges.put(e1.getCharAt(0), newEdge);
		  } else {
		    // No match, just save the edge
			  edges.put(e.getCharAt(0), e);
		  }
	  }
	  // Update the outgoingEdges
	  n.outgoingEdges = new ArrayList<Edge>();
	  for (Edge e: edges.values()) {
		  n.outgoingEdges.add(e);
	  }
	  
	  // TODO: After this, we should recursively condense all the children
  }
  
  
  // Add a wildcard subtree hanging from node
  public void addWildcardTree(Node node) {
	  Node n = node.clone();
	  for (Edge e: root.getOutgoingEdges()) {
		  e.wildcard = true;
	  }
	  node.outgoingEdges.addAll(n.outgoingEdges);
	  condense(node);
  }

public static class Builder {
    private Text inputText;
    private Node root;
    
    public Builder(Text inputText) {
      this.inputText = inputText;
    }
    
    public SuffixTreeNaive build() {
      // Build regular suffix tree.
      root = (new SuffixTree.Builder(inputText)).build().getRoot();
      SuffixTreeNaive s = new SuffixTreeNaive(this);
      
      s.addWildcardTree(s.root);
      
      return s;
    }
  }
  
}
