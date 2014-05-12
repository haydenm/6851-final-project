package stringmatch.ds.suffixtree;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ext.LexicalHandler;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;
import stringmatch.ds.util.Pair;
import stringmatch.ds.yfasttrie.YFastTrie;
import stringmatch.ds.yfasttrie.cuckoohash.CuckooHashMap;

/*
 * Suffix tree as described by Cole et al. (i.e., with Centroid Path Decomposition).
 */
public class SuffixTreeWithCPD extends SuffixTreeWithWildcards {

  // The depth of this wildcard subtree starting from the root of the original
  // suffix tree S.
  protected int subtreeDepth;
  
  // Mapping in the root suffix tree (S) from offset indices to lexicographic
  // order.
  protected CuckooHashMap<Integer, Integer> offsetToLexicographicIndexInS;
  
  // Y-Fast tries for leaf lexicographic indices (in T).
  YFastTrie<Node> leafLexicographicIndices;
  
  public SuffixTreeWithCPD(Node root) {
    super(root);
  }
  
  public SuffixTreeWithCPD(Builder builder) {
    super(builder.root);
  }
  
  // Determines and sets leafOffsetIndexInT and leafLexicographicIndexInT.
  public void determineLeafValuesInSubtree() {
    determineLeafValuesInSubtree(root);
  }
  
  protected void determineLeafValuesInSubtree(Node node) {
    if (node.isLeaf()) {
      node.leafOffsetIndexInT = node.leafOffsetIndexInS + subtreeDepth;
      node.leafLexicographicIndexInT = offsetToLexicographicIndexInS.
          get(node.leafOffsetIndexInT);
      return;
    }
    
    for (Edge outgoingEdge : node.getOutgoingEdges()) {
      determineLeafValuesInSubtree(outgoingEdge.getToNode());
    }
  }
  
  public void constructLeafLexicographicIndexYFT() {
    List<Pair<Integer, Node>> leaves = constructLeafIndexArray(root);
    YFastTrie.Builder<Node> yftBuilder = new YFastTrie.Builder<Node>();
    leafLexicographicIndices = yftBuilder.buildFromPairs(leaves);
  }
  
  protected List<Pair<Integer, Node>> constructLeafIndexArray(
      Node node) {
    List<Pair<Integer, Node>> leaves =
        new ArrayList<Pair<Integer, Node>>();
    if (node.isLeaf) {
      leaves.add(new Pair<Integer, Node>(node.leafLexicographicIndexInT, node));
      return leaves;
    }
    
    for (Edge edge : node.getOutgoingEdges()) {
      leaves.addAll(constructLeafIndexArray(edge.getToNode()));
    }
    
    return leaves;
  }
  
  public static class Builder extends SuffixTreeWithWildcards.Builder {
    
    public Builder(Text inputText, int k) {
      super(inputText, k);
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
    
    protected static void addWildcardSubtreesAt(Node node, int k, int depthToNode,
        CuckooHashMap<Integer, Integer> offsetToLexicographicIndexInS) {   
      if (node.isLeaf || k <= 0)
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
          
          SuffixTreeWithCPD wildcardSubtree = new SuffixTreeWithCPD(nodeClone);
          wildcardSubtree.offsetToLexicographicIndexInS = offsetToLexicographicIndexInS;
          wildcardSubtree.subtreeDepth = depthToNode + 1; // +1 for the wildcard edge.
          nodeClone = turnIntoWildcardSubtree(wildcardSubtree);
          wildcardSubtree.constructLCAAndMA();
          wildcardSubtree.determineLeafValuesInSubtree();
          wildcardSubtree.constructLeafLexicographicIndexYFT();
      
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
        int newK = edge.isWildcardEdge() ? k - 1 : k;
        int edgeDepth = edge.getTextSubstring().getLength();
        addWildcardSubtreesAt(edge.getToNode(), newK, depthToNode + edgeDepth,
            offsetToLexicographicIndexInS);
      }
    }
    
    protected CuckooHashMap<Integer, Integer>
        constructOffsetToLexicographicIndexMap() {
      CuckooHashMap<Integer, Integer> map = new CuckooHashMap<Integer, Integer>();
      constructOffsetToLexicographicIndexMap(root, map);
      return map;
    }
    
    protected void constructOffsetToLexicographicIndexMap(
        Node node, CuckooHashMap<Integer, Integer> map) {
      if (node.isLeaf()) {
        map.put(node.leafOffsetIndexInS, node.leafLexicographicIndexInS);
        return;
      }
      
      for (Edge outgoingEdge : node.getOutgoingEdges()) {
        constructOffsetToLexicographicIndexMap(outgoingEdge.getToNode(), map);
      }
    }
    
    public SuffixTreeWithCPD build() {
      SuffixTreeWithCPD stcpd = new SuffixTreeWithCPD(this);
      stcpd.offsetToLexicographicIndexInS = constructOffsetToLexicographicIndexMap();
      stcpd.subtreeDepth = 0;
      stcpd.constructLCAAndMA();
      stcpd.determineLeafValuesInSubtree();
      stcpd.constructLeafLexicographicIndexYFT();
      
      // Find the centroid paths in the original suffix tree (i.e., the one
      // rooted at root). Then when we make copies we find the centroid
      // paths *once* in each of those.
      findCentroidPaths(root);
      
      // Add wildcards (without including centroid edges).
      addWildcardSubtreesAt(root, k, 0, stcpd.offsetToLexicographicIndexInS);
      
      return stcpd;
    }
  }

}
