package stringmatch.ds.suffixtree;

import stringmatch.ds.text.Text;

public class SuffixTreeWithWildcards extends SuffixTree {
  
  public SuffixTreeWithWildcards(Builder builder) {
    root = builder.root;
  }

  public static class Builder {
    private Text inputText;
    private Node root;
    
    public Builder(Text inputText) {
      this.inputText = inputText;
    }
    
    protected void findCentroidPaths(Node node) {
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
      // Build regular suffix tree.
      root = (new SuffixTree.Builder(inputText)).build().getRoot();
      
      findCentroidPaths(root);
      
      return new SuffixTreeWithWildcards(this);
    }
  }
  
}
