package stringmatch.ds.suffixtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import stringmatch.ds.suffixtree.SuffixTree.Builder;
import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;
import stringmatch.ds.text.TextSubstring;

public class SuffixTreeWithWildcards extends SuffixTree {
  
  public SuffixTreeWithWildcards() { }
  
  public SuffixTreeWithWildcards(Builder builder) {
    root = builder.root;
  }

  public static class Builder extends SuffixTree.Builder {

    public Builder(Text inputText) {
      super(inputText);
      root = super.build().root;
    } 
    
    /*
     * Turns the tree rooted at node into a wildcard subtree by replacing
     * the first characters with * and condensing the tree rooted at node.
     */
    protected static Node turnIntoWildcardSubtreeAt(Node node) {
      // Replace first char of each edge with a wildcard.
      // Specifically, represent this by:
      // (1) adding a new WildcardEdge out of node,
      // (2) "deleting" the first character from the textSubstring in each of
      //     node's outgoing edges, and
      // (3) taking the outgoing edges whose textSubstrings became empty
      //     (i.e., they initially only had one character), taking the outgoing
      //     edges of each of these edges' toNodes, and moving these to be
      //     outgoing edges of node (effectively, delete the outgoing edges that
      //     became empty).
      
      // Step (1)
      // Turn node into the split node and make the root a new node.
     
      Node newRoot = new Node(null);
      Edge wildcardEdge = new WildcardEdge(newRoot);
      newRoot.addOutgoingEdge(wildcardEdge);
      wildcardEdge.setToNode(node);
      node.setIncomingEdge(wildcardEdge);
      
      // There's a special case in which node will become a leaf. This happens
      // if it has just one child, and the edge along that child is simply '$'.
      // In a regular suffix tree there are no nodes with just one child (at least
      // not at the root). However, this case can come about when recursively
      // building wildcard subtrees.
      if (node.outgoingEdges.size() == 1 &&
          node.outgoingEdges.get(0).getTextSubstring().getLength() == 1 &&
          node.outgoingEdges.get(0).getTextSubstring().getFirstChar().equals(
              AlphabetCharacter.END_CHAR)) {
        node.isLeaf = true;
        node.leafIndex = node.outgoingEdges.get(0).getToNode().leafIndex;
        node.outgoingEdges.clear();
        return newRoot;
      }
      
      // Steps (2) and (3)
      List<Edge> edgesToDelete = new ArrayList<Edge>();
      Iterator<Edge> outgoingEdgeIterator = node.getOutgoingEdges().iterator();
      while (outgoingEdgeIterator.hasNext()) {
        Edge outgoingEdge = outgoingEdgeIterator.next();
        outgoingEdge.getTextSubstring().deleteFirstChar();
        if (outgoingEdge.getTextSubstring().getLength() == 0) {
          edgesToDelete.add(outgoingEdge);
          outgoingEdgeIterator.remove();
        }
      }
      for (Edge edgeToDelete : edgesToDelete) {
        // centroidEdge and numLeaves will become incorrect, but those should be
        // corrected later on if needed (in SuffixTreeWithCPD).
        for (Edge edgeToAdd : edgeToDelete.getToNode().getOutgoingEdges()) {
          node.addOutgoingEdge(edgeToAdd);
          edgeToAdd.fromNode = node;
        }
      }
      Collections.sort(node.outgoingEdges);
      
      // Now condense the tree rooted at node.
      node.condense();
      
      return newRoot;
    }
    
  }
  
}
