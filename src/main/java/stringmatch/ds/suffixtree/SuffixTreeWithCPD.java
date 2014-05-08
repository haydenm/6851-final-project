package stringmatch.ds.suffixtree;

import stringmatch.ds.text.Text;

/*
 * Suffix tree as described by Cole et al. (i.e., with Centroid Path Decomposition).
 */
public class SuffixTreeWithCPD extends SuffixTreeWithWildcards {

  public SuffixTreeWithCPD(Builder builder) {
    root = builder.root;
  }
  
  public static class Builder extends SuffixTreeWithWildcards.Builder {
    
    public Builder(Text inputText) {
      super(inputText);
    }  
    
    protected static void findCentroidPaths(Node node) {
      node.numLeaves = 0;
      int maxNumberOfChildrenLeaves = 0;
      for (Edge outgoingEdge : node.outgoingEdges) {
        // In each child, count the number of their children.
        findCentroidPaths(outgoingEdge.getToNode());
        
        if (outgoingEdge.getToNode().isLeaf)
          node.numLeaves += 1;
        else
          node.numLeaves += outgoingEdge.getToNode().numLeaves;
        
        // Find the max number of children leaves. And keep track of the heaviest
        // children, breaking ties arbitrarily.
        if (outgoingEdge.getToNode().numLeaves >= maxNumberOfChildrenLeaves) {
          maxNumberOfChildrenLeaves = outgoingEdge.getToNode().numLeaves;
          node.centroidEdge = outgoingEdge;
        }
      }
    }
    
    public SuffixTreeWithWildcards build() {      
      findCentroidPaths(root);
      
      // Add wildcards (without including centroid edges).
      // ....
      
      // remember to find centroid paths again on copy after condensing!
      
      return new SuffixTreeWithCPD(this);
    }
  }

}
