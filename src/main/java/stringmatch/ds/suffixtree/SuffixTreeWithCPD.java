package stringmatch.ds.suffixtree;

import java.util.ArrayList;
import java.util.List;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;
import stringmatch.ds.text.TextSubstring;
import stringmatch.ds.util.Pair;

/*
 * Suffix tree as described by Cole et al. (i.e., with Centroid Path Decomposition).
 */
public class SuffixTreeWithCPD extends SuffixTreeWithWildcards {

  public SuffixTreeWithCPD(Node root) {
    super(root);
  }
  
  public SuffixTreeWithCPD(Builder builder) {
    super(builder.root);
  }
  
  protected boolean checkMatch(Text p, int start, Edge e, int offset) {
    if (e != null) {
      for (int i = 0; i < Math.min(e.getTextSubstring().length - offset, p.getLength()
          - start); i++) {
        AlphabetCharacter nextOnEdge = e.getTextSubstring().getIthChar(i + offset);
        AlphabetCharacter nextInPattern = p.getCharAtIndex(start + i);
        if (!nextOnEdge.equals(nextInPattern)) {
            return false;
        }
      }
      return true;
    }
    return false;
  }
  
  public List<Pair<Node, Integer>> smartQuery(Text p) {
    List<Text> subQueries = breakQuery(p);
    Text query = subQueries.get(0);
    Pair<Node, Integer> prev = slowRootedLCP(query);
    //System.out.println("PREV");
    //System.out.println(prev);
    if (subQueries.size() > 1) {
      return smartQuery(subQueries, 1, prev);
    } else {
      List<Pair<Node, Integer>> matches = new ArrayList<Pair<Node, Integer>>();
      matches.add(prev);
      return matches;
    }
  }
  
  public List<Pair<Node, Integer>> smartQuery(List<Text> subQueries, int i, Pair<Node, Integer> prev) {
    List<Pair<Node, Integer>> matches = new ArrayList<Pair<Node, Integer>>();
    if (prev.getRight() == 0) {
      // Check wildcard subtree
      WildcardEdge wce =  (WildcardEdge) prev.getLeft().follow(AlphabetCharacter.WILDCARD);
      if (wce != null) {
        SuffixTreeWithCPD w = (SuffixTreeWithCPD) wce.wildcardTree;
        Pair<Node, Integer> res1 = w.slowRootedLCP(subQueries.get(i));
        //System.out.println("WILDCARD RES");
        //System.out.println(res1);
        if (res1 != null) {
          if (subQueries.size() > i + 1) {
            matches.addAll(w.smartQuery(subQueries, i + 1, res1));
          } else {
            //System.out.println("USE WILDCARD");
            matches.add(res1);
          }
        }
      }
      // Check along centroid path
      Edge e = prev.getLeft().centroidEdge;
      Pair<Node, Integer> next = new Pair<Node, Integer>(e.getToNode(), 1 - e.getLength());
      //System.out.println("CENTROID NEXT");
      //System.out.println(next);
      Pair<Node, Integer> res2 = slowUnrootedLCP(subQueries.get(i), next);
      //System.out.println(res2);
      if (res2 != null) {
        if (subQueries.size() > i + 1) {
          matches.addAll(smartQuery(subQueries, i + 1, res2));
        } else {
          //System.out.println("HERE");
          matches.add(res2);
        }
      }
    } else {
      // Just check along current path
      Pair<Node, Integer> next = new Pair<Node, Integer>(prev.getLeft(), prev.getRight() + 1);
      System.out.println("NEXT");
      System.out.println(next);
      Pair<Node, Integer> res = slowUnrootedLCP(subQueries.get(i), next);
      if (res != null) {
        if (subQueries.size() > i + 1) {
          matches.addAll(smartQuery(subQueries, i + 1, res));
        } else {
          matches.add(res);
        }
      }
    }
    return matches;
  }
  
  public Pair<Node, Integer> slowRootedLCP(Text p) {
    return query(p);
  }
  
  public Pair<Node, Integer> rootedLCP(Text p, Pair<Node, Integer> highestOverlapPoint, Text hos) {
    //Pair<Node, Integer> hop = highestOverlapPoint(p);
    //Text hos = constructHighestOverlap(hop);
    int i = highestOverlapPoint.getLeft().leafLexicographicIndex;
    Text t = highestOverlapPoint.getLeft().incomingEdge.getTextSubstring().getText();
    int pIndex;
    if (hos.compareTo(p) > 0) {
      pIndex = i - 1;
    } else {
      pIndex = i + 1;
    }
    return null;
  }
  
  public Pair<Node, Integer> slowUnrootedLCP(Text p, Pair<Node, Integer> start) {
    Node node = start.getLeft();
    int offset = start.getRight();
    Edge e = node.incomingEdge;
    if (e == null || e.getLength() + offset < 0) {
      throw new RuntimeException("Malformed unrooted LCP query");
    }
    if (!checkMatch(p, 0, e, e.getLength() + offset)) {
      return null;
    } else {
      return query(p, -offset, node);
    }
    
  }
  
  public int getDepth(Node node) {
    return root.maxHeight - node.maxHeight;
  }
  
  public static List<Text> breakQuery(Text p) {
    List<Text> subQueries = new ArrayList<Text>();
    int start = 0;
    int len = 0;
    for (int i = 0; i < p.getSize(); i++) {
      if (p.getCharAtIndex(i).equals(AlphabetCharacter.WILDCARD)) {
        subQueries.add(new Text(p.toString().substring(start, start + len), false));
        start = i + 1;
        len = 0;
      } else {
        len += 1;
      }
    }
    subQueries.add(new Text(p.toString().substring(start, start + len), false));
    return subQueries;
  }
  
  public static void main(String[] args) {
    Text t = new Text("BANANABANANA", true);
    SuffixTreeWithCPD.Builder suffixTreeBuilder = new SuffixTreeWithCPD.Builder(t, 2);
    SuffixTreeWithCPD st = suffixTreeBuilder.build();
    st.printTree();
    AlphabetCharacter B = new AlphabetCharacter(new Character('B'));
    AlphabetCharacter A = new AlphabetCharacter(new Character('A'));
    AlphabetCharacter N = new AlphabetCharacter(new Character('N'));
    Node n1 = st.root.follow(B).getToNode();
    Pair<Node, Integer> start = new Pair<Node, Integer>(n1, -4);
    for (Pair<Node, Integer> p: st.smartQuery(new Text("A", false))) {
      st.printNode(p.getLeft());
      System.out.println(p.getRight());
    }
    System.out.println("TESTA".compareTo("TEST$"));
    System.out.println(n1.followLeft());
    //st.rootedLCP(new Text("TEST", false));
    //System.out.println(SuffixTreeWithCPD.breakQuery(new Text("***TEST*AGAIN**TEST*", false)));
    
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
    
    protected static void addWildcardSubtreesAt(Node node, int k) {   
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
          
          SuffixTreeWithWildcards wildcardSubtree = new SuffixTreeWithCPD(nodeClone);
          nodeClone = turnIntoWildcardSubtree(wildcardSubtree);
          wildcardSubtree.constructLCAAndMA();
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
        addWildcardSubtreesAt(edge.getToNode(), newK);
      }
    }
    
    public SuffixTreeWithCPD build() {
      SuffixTreeWithCPD stcpd = new SuffixTreeWithCPD(this);
      stcpd.constructLCAAndMA();
      stcpd.constructLeafLexicographicIndexYFT();
      
      // Find the centroid paths in the original suffix tree (i.e., the one
      // rooted at root). Then when we make copies we find the centroid
      // paths *once* in each of those.
      findCentroidPaths(root);
      
      // Add wildcards (without including centroid edges).
      addWildcardSubtreesAt(root, k);
      
      return stcpd;
    }
  }

}
