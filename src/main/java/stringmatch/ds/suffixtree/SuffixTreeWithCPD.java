package stringmatch.ds.suffixtree;

import stringmatch.ds.text.AlphabetCharacter;
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
    
    protected static void addWildcardSubtreesAt(Node node) {   
      if (node.isLeaf)
        return;
      
      // Make the wildcard subtree to be attached to node, without the
      // centroid edge.
      Node nodeClone = node.clone(true, null);
      if (nodeClone.outgoingEdges.size() > 0) {
        // There's a special case where nodeClone may have no outgoing edges.
        // The reason is that node could be the root of a wildcard subtree and
        // node has only one outgoing edge. This edge is labeled the centroid
        // edge and then its children are not included in nodeClone.
        // We should only turn nodeClone into a wildcard subtree and attach it to
        // node if it has children. If nodeClone doesn't have children, then
        // node won't have a wildcard subtree -- but this is ok because we
        // will simply follow the centroid edge.
        
        if (!(nodeClone.outgoingEdges.size() == 1 &&
            nodeClone.outgoingEdges.get(0).getTextSubstring().getFirstChar().
            equals(AlphabetCharacter.END_CHAR))) {
          // There's another special case in which nodeClone has just one outgoing edge,
          // which is '$'. We don't want to turn this edge into a wildcard because
          // otherwise wildcards could match for '$', which isn't really in the
          // input text.
          
          nodeClone = turnIntoWildcardSubtreeAt(nodeClone);
      
          // Attach nodeClone onto node. nodeClone should have just one outgoing
          // edge: the wildcard edge.
          Edge wildcardEdge = nodeClone.outgoingEdges.get(0);
          wildcardEdge.fromNode = node;
          node.addOutgoingEdge(wildcardEdge);
        
          // Find the centroid path decomposition of the copied tree (i.e., the
          // wildcard subtree), which is rooted at wildcardEdge.getToNode().
          findCentroidPaths(wildcardEdge.getToNode());
        }
      }
      
      // Recursively add wildcard subtrees.
      for (Edge edge : node.outgoingEdges) {
        // Recursively add them to the new wildcard subtree as well.
        addWildcardSubtreesAt(edge.getToNode());
      }
    }
    
    public SuffixTreeWithCPD build() {
      // Find the centroid paths in the original suffix tree (i.e., the one
      // rooted at root). Then when we make copies we find the centroid
      // paths *once* in each of those.
      findCentroidPaths(root);
      
      // Add wildcards (without including centroid edges).
      addWildcardSubtreesAt(root);
      
      return new SuffixTreeWithCPD(this);
    }
  }

}
