package stringmatch.ds.suffixtree;

import java.util.ArrayList;
import java.util.List;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;

public class SuffixTreeNaiveBigSpace extends SuffixTree {
  
  public SuffixTreeNaiveBigSpace(Builder builder) {
    root = builder.root;
  }

  /*
   * Returns the indices of all the matches for the query pattern p.
   */
  public List<Integer> queryForIndices(Text p) {
    Node node = query(p);
    if (node == null)
      return new ArrayList<Integer>();
    else
      return node.getIndicesOfLeaves();
  }
  
  /*
   * Returns node rooting the matches.
   */
  public Node query(Text p) {
    return query(p, 0, root);
  }
  
  private Node query(Text p, int pStart, Node current) {
    if (pStart >= p.getLength()) {
      // We're done reading through the pattern, so return the node where
      // we are. Everything under it is a match.
      return current;
    }
    
    if (current.isLeaf()) {
      // There's still more of the pattern, but we reached a leaf.
      return null;
    }
    
    AlphabetCharacter firstChar = p.getCharAtIndex(pStart);
    if (firstChar.isWild()) {
      // Follow the wildcard edge.
      for (Edge e : current.getOutgoingEdges()) {
        if (e.isWildcardEdge())
          return query(p, pStart + 1, e.getToNode());
      }
      
      // There's no wildcard edge, which means this query contains more
      // wildcard characters than the trie was built to handle.
      throw new RuntimeException("This query has too many wildcards.");
    }
    
    // Follow edgeToFollow. We allow wildcards when comparing against the
    // text on this edge. Note that this means we may use more wildcards
    // than our tree was built for (i.e., k). But k just determines how
    // many times we can branch off to a wildcard subtree; here, we're
    // just comparing against an edge text and allowing wildcards in the
    // comparison.
    Edge edgeToFollow = current.follow(firstChar);
    if (edgeToFollow != null)
      System.out.println("Checking " + edgeToFollow.getTextSubstring().toString());
    if (checkMatch(p, pStart, edgeToFollow, true)) {
      return query(p, pStart + edgeToFollow.getTextSubstring().getLength(),
          edgeToFollow.getToNode());
    } else {
      // There's no edge that matches the remaining pattern.
      return null;
    }
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
