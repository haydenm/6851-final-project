package stringmatch.ds.suffixtree;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;

public class SuffixTreeNaiveBigSpace extends SuffixTree {
  
  public SuffixTreeNaiveBigSpace(Builder builder) {
    root = builder.root;
  }

  public static class Builder extends SuffixTreeWithWildcards.Builder {
    
    public Builder(Text inputText, int k) {
      super(inputText, k);
    }
    
    protected static void addWildcardSubtreesAt(Node node, int k) {   
      if (node.isLeaf || k <= 0)
        return;
      
      // Make the wildcard subtree to be attached to node.
      Node nodeClone = node.clone();
      if (!(nodeClone.outgoingEdges.size() == 1 &&
          nodeClone.outgoingEdges.get(0).getTextSubstring().getFirstChar().
          equals(AlphabetCharacter.END_CHAR))) {
        // There's a special case in which nodeClone has just one outgoing edge,
        // which is '$'. We don't want to turn this edge into a wildcard because
        // otherwise wildcards could match for '$', which isn't really in the
        // input text.
        nodeClone = turnIntoWildcardSubtreeAt(nodeClone);
      
        // Attach nodeClone onto node. nodeClone should have just one outgoing
        // edge: the wildcard edge.
        Edge wildcardEdge = nodeClone.outgoingEdges.get(0);
        wildcardEdge.fromNode = node;
        node.addOutgoingEdge(wildcardEdge);
      
        // Recursively add wildcard subtrees.
        for (Edge edge : node.outgoingEdges) {
          // Recursively add them to the new wildcard subtree as well.
          int newK = edge.isWildcardEdge() ? k - 1 : k;
          addWildcardSubtreesAt(edge.getToNode(), newK);
        }
      }
    }
    
    public SuffixTreeNaiveBigSpace build() {
      addWildcardSubtreesAt(root, k);
      
      return new SuffixTreeNaiveBigSpace(this);
    }
  }
  
}
