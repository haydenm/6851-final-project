package stringmatch.ds.suffixtree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;
import stringmatch.ds.text.TextSubstring;

public class SuffixTreeNaiveBigSpace extends SuffixTree {
  
  public SuffixTreeNaiveBigSpace(Builder builder) {
    root = builder.root;
  }

  public static class Builder extends SuffixTreeWithWildcards.Builder {
    
    public Builder(Text inputText) {
      super(inputText);
    }
    
    protected static void addWildcardSubtreesAt(Node node) {   
      if (node.isLeaf)
        return;
      
      // Make the wildcard subtree to be attached to node.
      Node nodeClone = node.clone();
      nodeClone = turnIntoWildcardSubtreeAt(nodeClone);
      
      // Attach nodeClone onto node. nodeClone should have just one outgoing
      // edge: the wildcard edge.
      Edge wildcardEdge = nodeClone.outgoingEdges.get(0);
      wildcardEdge.fromNode = node;
      node.addOutgoingEdge(wildcardEdge);
      
      // Recursively add wildcard subtrees.
      for (Edge edge : node.outgoingEdges) {
        // Recursively add them to the new wildcard subtree as well.
        addWildcardSubtreesAt(edge.getToNode());
      }
    }
    
    public SuffixTreeNaiveBigSpace build() {
      addWildcardSubtreesAt(root);
      
      return new SuffixTreeNaiveBigSpace(this);
    }
  }
  
}
