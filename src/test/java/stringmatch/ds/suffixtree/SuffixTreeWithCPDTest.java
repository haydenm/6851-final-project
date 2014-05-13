package stringmatch.ds.suffixtree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;
import stringmatch.ds.util.Pair;

public class SuffixTreeWithCPDTest {

  @Test
  @Ignore
  public void testBanana() {
    SuffixTreeWithCPD.Builder suffixTreeBuilder = new SuffixTreeWithCPD.Builder(new Text("BANANABANANA", true), 2);
    SuffixTreeWithCPD st = suffixTreeBuilder.build();
    st.printTree();
    System.out.println(st.getAllSuffixesAsStrings());
  }
  
  @Test
  @Ignore
  public void testSuffixes() {
    String[] strs = new String[] {
        "BANANA",
        "BANANABANANA",
        "B",
        "ABC",
        "BANDANA",
        "BANANAC",
        "BANANXYZANA"
        };
    
    for (String s : strs) {
      Text t = new Text(s, true);
      for (int k = 0; k <= s.length(); k++) {
        SuffixTreeWithCPD.Builder suffixTreeBuilder = new SuffixTreeWithCPD.Builder(t, k);
        SuffixTreeWithCPD st = suffixTreeBuilder.build();
        List<String> stSuffixes = st.getAllSuffixesAsStrings();
        Collections.sort(stSuffixes);
        List<String> expected = SuffixTreeWithWildcardsTest.generateAllSuffixesWithKWildcards(t, k);
        Collections.sort(expected);
        
        // Note: Because we don't expand along edges (i.e., wildcard subtrees don't come out of edges)
        // expected may have more suffixes than stSuffixes. But stSuffixes should be a subset of
        // expected.
        for (String suffix : stSuffixes) {
          //System.out.println(suffix);
          assertTrue(expected.contains(suffix));
        }
      }
    }
  }
  
  @Test
  public void testYFTBanana() {
    SuffixTreeWithCPD.Builder suffixTreeBuilder = new SuffixTreeWithCPD.Builder(new Text("BANANA", true), 1);
    SuffixTreeWithCPD st = suffixTreeBuilder.build();
    for (int i = 0; i < "BANANA$".length(); i++) {
      assertTrue(st.leafLexicographicIndices.hasKey(2*i));
    }
    for (int i = "BANANA$".length(); i < 20; i++) {
      assertFalse(st.leafLexicographicIndices.hasKey(2*i));
    }
    
    assertEquals(null, st.leafLexicographicIndices.predecessor(2*0));
    for (int i = 1; i <= "BANANA$".length(); i++) {
      assertEquals(2*(i-1), st.leafLexicographicIndices.predecessor(2*i).getLeft().intValue());
    }
    assertEquals(2*6, st.leafLexicographicIndices.predecessor(2*10).getLeft().intValue());
    
    assertEquals("$", st.leafLexicographicIndices.predecessor(2*1).getRight().toString());
    assertEquals("$", st.leafLexicographicIndices.predecessor(2*2).getRight().toString());
    assertEquals("$", st.leafLexicographicIndices.predecessor(2*3).getRight().toString());
    assertEquals("NA$", st.leafLexicographicIndices.predecessor(2*4).getRight().toString());
    assertEquals("BANANA$", st.leafLexicographicIndices.predecessor(2*5).getRight().toString());
    assertEquals("$", st.leafLexicographicIndices.predecessor(2*6).getRight().toString());
    
    assertEquals(null, st.leafLexicographicIndices.successor(2*6));
    for (int i = 0; i < "BANANA$".length() - 1; i++) {
      assertEquals(2*(i+1), st.leafLexicographicIndices.successor(2*i).getLeft().intValue());
    }
    
    assertEquals("$", st.leafLexicographicIndices.successor(2*0).getRight().toString());
    assertEquals("$", st.leafLexicographicIndices.successor(2*1).getRight().toString());
    assertEquals("NA$", st.leafLexicographicIndices.successor(2*2).getRight().toString());
    assertEquals("BANANA$", st.leafLexicographicIndices.successor(2*3).getRight().toString());
    assertEquals("$", st.leafLexicographicIndices.successor(2*4).getRight().toString());
    assertEquals("NA$", st.leafLexicographicIndices.successor(2*5).getRight().toString());
    
    SuffixTreeWithCPD stWild = (SuffixTreeWithCPD) ((WildcardEdge)st.root.follow(AlphabetCharacter.WILDCARD)).wildcardTree;
    assertTrue(stWild.leafLexicographicIndices.hasKey(2*1));
    assertTrue(stWild.leafLexicographicIndices.hasKey(2*2));
    assertTrue(stWild.leafLexicographicIndices.hasKey(2*3));
    assertFalse(stWild.leafLexicographicIndices.hasKey(2*0));
    assertFalse(stWild.leafLexicographicIndices.hasKey(2*4));
    assertFalse(stWild.leafLexicographicIndices.hasKey(2*5));
    assertFalse(stWild.leafLexicographicIndices.hasKey(2*6));
    
    assertEquals(null, stWild.leafLexicographicIndices.predecessor(2*1));
    assertEquals(2*1, stWild.leafLexicographicIndices.successor(2*0).getLeft().intValue());
    assertEquals(2*3, stWild.leafLexicographicIndices.successor(2*2).getLeft().intValue());    assertEquals(2*1, stWild.leafLexicographicIndices.successor(2*0).getLeft().intValue());
    assertEquals(null, stWild.leafLexicographicIndices.successor(2*3));

    assertEquals("NA$", stWild.leafLexicographicIndices.predecessor(2*4).getRight().toString());
  }
  
  @Test
  public void testYFTBananaBanana() {
    SuffixTreeWithCPD.Builder suffixTreeBuilder = new SuffixTreeWithCPD.Builder(new Text("BANANABANANA", true), 2);
    SuffixTreeWithCPD st = suffixTreeBuilder.build();
    for (int i = 0; i < "BANANABANANA$".length(); i++) {
      assertTrue(st.leafLexicographicIndices.hasKey(2*i));
    }
    for (int i = "BANANABANANA$".length(); i < 20; i++) {
      assertFalse(st.leafLexicographicIndices.hasKey(2*i));
    }
    
    // Check A* subtree
    SuffixTreeWithCPD stWild = (SuffixTreeWithCPD) ((WildcardEdge)st.root.follow(new AlphabetCharacter('A')).getToNode().follow(AlphabetCharacter.WILDCARD)).wildcardTree;
    assertTrue(stWild.leafLexicographicIndices.hasKey(2*5));
    for (int i = 0; i < 20; i++) {
      if (2*i != 2*5)
        assertFalse(stWild.leafLexicographicIndices.hasKey(2*i));
    }
    assertEquals("ANANA$", stWild.leafLexicographicIndices.predecessor(2*6).getRight().toString());
    
    // Check * subtree
    stWild = (SuffixTreeWithCPD) ((WildcardEdge)st.root.follow(AlphabetCharacter.WILDCARD)).wildcardTree;
    assertFalse(stWild.leafLexicographicIndices.hasKey(2*0));
    assertTrue(stWild.leafLexicographicIndices.hasKey(2*1));
    assertTrue(stWild.leafLexicographicIndices.hasKey(2*2));
    assertTrue(stWild.leafLexicographicIndices.hasKey(2*3));
    assertTrue(stWild.leafLexicographicIndices.hasKey(2*4));
    assertTrue(stWild.leafLexicographicIndices.hasKey(2*5));
    assertTrue(stWild.leafLexicographicIndices.hasKey(2*6));
    for (int i = 2*6 + 1; i < 40; i++) {
      assertFalse(stWild.leafLexicographicIndices.hasKey(i));
    }
    
    // Check *ANA* subtree
    stWild = (SuffixTreeWithCPD) ((WildcardEdge)st.root.follow(AlphabetCharacter.WILDCARD).getToNode().follow(new AlphabetCharacter('A')).getToNode().follow(new AlphabetCharacter('N')).getToNode().follow(AlphabetCharacter.WILDCARD)).wildcardTree;
    assertTrue(stWild.leafLexicographicIndices.hasKey(2*5));
    for (int i = 0; i < 20; i++) {
      if (2*i != 2*5)
        assertFalse(stWild.leafLexicographicIndices.hasKey(2*i));
    }
  }
  
  @Test
  public void testSmartQuery() {
    SuffixTreeWithCPD.Builder suffixTreeBuilder =
        new SuffixTreeWithCPD.Builder(new Text("BANANABANANA", true), 2);
    SuffixTreeWithCPD st = suffixTreeBuilder.build();
    
    AlphabetCharacter A = new AlphabetCharacter(new Character('A'));
    AlphabetCharacter B = new AlphabetCharacter(new Character('B'));
    AlphabetCharacter N = new AlphabetCharacter(new Character('N'));
    AlphabetCharacter S = new AlphabetCharacter(new Character('*'));
    
    Text t = new Text("BAN", false);
    List<Pair<Node, Integer>> result = st.smartQuery(t);
    List<Pair<Node, Integer>> expected = new ArrayList<Pair<Node, Integer>>();
    Node n = st.root.follow(B).getToNode();
    expected.add(new Pair<Node, Integer>(n, -3));
    assertEquals(expected, result);
    
    t = new Text("A*AN*", false);
    result = st.smartQuery(t);
    expected = new ArrayList<Pair<Node, Integer>>();
    Node n1 = st.root.follow(A).getToNode().follow(S).getToNode().follow(A).getToNode();
    Node n2 = st.root.follow(A).getToNode().follow(N).getToNode().follow(N).getToNode();
    expected.add(new Pair<Node, Integer>(n1, -3));
    expected.add(new Pair<Node, Integer>(n2, 0));
    assertEquals(expected, result);
    
    t = new Text("**T", false);
    result = st.smartQuery(t);
    expected = new ArrayList<Pair<Node, Integer>>();
    assertEquals(expected, result);
    
    t = new Text("**A", false);
    result = st.smartQuery(t);
    expected = new ArrayList<Pair<Node, Integer>>();
    n1 = st.root.follow(A).getToNode().follow(S).getToNode().follow(A).getToNode();
    n2 = st.root.follow(A).getToNode().follow(N).getToNode();
    expected.add(new Pair<Node, Integer>(n1, -5));
    expected.add(new Pair<Node, Integer>(n2, 0));
    assertEquals(expected, result);
    
    t = new Text("**", false);
    result = st.smartQuery(t);
    expected = new ArrayList<Pair<Node, Integer>>();
    n1 = st.root.follow(S).getToNode().follow(A).getToNode();
    n2 = st.root.follow(A).getToNode().follow(S).getToNode();
    Node n3 = st.root.follow(A).getToNode().follow(N).getToNode();
    expected.add(new Pair<Node, Integer>(n1, 0));
    expected.add(new Pair<Node, Integer>(n2, 0));
    expected.add(new Pair<Node, Integer>(n3, -1));
    assertEquals(expected, result);
  }
  
  @Test
  public void testHighestOverlapLeaf() {
    SuffixTreeWithCPD.Builder suffixTreeBuilder =
        new SuffixTreeWithCPD.Builder(new Text("BANANABANANA", true), 2);
    SuffixTreeWithCPD st = suffixTreeBuilder.build();
    
    AlphabetCharacter A = new AlphabetCharacter(new Character('A'));
    AlphabetCharacter B = new AlphabetCharacter(new Character('B'));
    AlphabetCharacter N = new AlphabetCharacter(new Character('N'));
    
    AlphabetCharacter D = new AlphabetCharacter(new Character('$'));
    AlphabetCharacter S = new AlphabetCharacter(new Character('*'));
    
    Text t = new Text("BAN", false);
    Node n = st.root.follow(B).getToNode().follow(D).getToNode();
    Pair<Node, Pair<Integer, Boolean>> expected =
        new Pair<Node, Pair<Integer, Boolean>>(n,
            new Pair<Integer, Boolean>(3, false));
    Pair<Node, Pair<Integer, Boolean>> result = st.highestOverlapLeaf(t);
    assertEquals(expected, result);
    
    t = new Text("NA", false);
    n = st.root.follow(N).getToNode().follow(D).getToNode();
    expected = new Pair<Node, Pair<Integer, Boolean>>(n,
            new Pair<Integer, Boolean>(2, false));
    result = st.highestOverlapLeaf(t);
    assertEquals(expected, result);
    
    t = new Text("ANAP", false);
    n = st.root.follow(A).getToNode().follow(N).getToNode().follow(N).getToNode().follow(B).getToNode();
    expected = new Pair<Node, Pair<Integer, Boolean>>(n,
        new Pair<Integer, Boolean>(3, true));
    result = st.highestOverlapLeaf(t);
    assertEquals(expected, result);
    
    t = new Text("BANANABAA", false);
    n = st.root.follow(B).getToNode().follow(B).getToNode();
    expected = new Pair<Node, Pair<Integer, Boolean>>(n,
        new Pair<Integer, Boolean>(8, false));
    result = st.highestOverlapLeaf(t);
    assertEquals(expected, result);
    
    t = new Text("ANP", false);
    n = st.root.follow(A).getToNode().follow(N).getToNode().follow(N).getToNode().follow(B).getToNode();
    expected = new Pair<Node, Pair<Integer, Boolean>>(n,
        new Pair<Integer, Boolean>(2, true));
    result = st.highestOverlapLeaf(t);
    assertEquals(expected, result);
  }

  @Test
  public void testCorrespondingNodeInSBanana() {
    SuffixTreeWithCPD.Builder suffixTreeBuilder =
        new SuffixTreeWithCPD.Builder(new Text("BANANA", true), 1);
    SuffixTreeWithCPD st = suffixTreeBuilder.build();
    
    AlphabetCharacter A = new AlphabetCharacter(new Character('A'));
    AlphabetCharacter B = new AlphabetCharacter(new Character('B'));
    AlphabetCharacter N = new AlphabetCharacter(new Character('N'));
    AlphabetCharacter S = new AlphabetCharacter(new Character('*'));
    
    SuffixTreeWithCPD stWild = (SuffixTreeWithCPD)((WildcardEdge)st.root.follow(AlphabetCharacter.WILDCARD)).wildcardTree;
    Node n1 = stWild.root.follow(A).getToNode().follow(AlphabetCharacter.END_CHAR).getToNode();
    Node n2 = st.root.follow(A).getToNode().follow(AlphabetCharacter.END_CHAR).getToNode();
    assertEquals(n2, stWild.getCorrespondingNodeInS(n1));

  }
  
  @Test
  public void easyTestRootedLCP() {
    SuffixTreeWithCPD.Builder suffixTreeBuilder =
        new SuffixTreeWithCPD.Builder(new Text("BANANA", true), 1);
    SuffixTreeWithCPD st = suffixTreeBuilder.build();
    
    AlphabetCharacter A = new AlphabetCharacter(new Character('A'));
    AlphabetCharacter B = new AlphabetCharacter(new Character('B'));
    AlphabetCharacter N = new AlphabetCharacter(new Character('N'));
    AlphabetCharacter D = new AlphabetCharacter(new Character('$'));
    AlphabetCharacter S = new AlphabetCharacter(new Character('*'));
    
    Text t = new Text("BAN", false);
    int queryIndex = 7;
    int overlapHeight = 3;
    Node ssp = st.root.follow(B).getToNode();
    Node n = st.root.follow(B).getToNode();
    Pair<Node, Integer> expected = new Pair<Node, Integer>(n, -4);
    Pair<Node, Integer> result = st.rootedLCP(queryIndex, overlapHeight, ssp, st, st.root);
    assertEquals(expected, result);
    
    t = new Text("ANA", false);
    queryIndex = 3;
    overlapHeight = 3;
    ssp = st.root.follow(A).getToNode().follow(N).getToNode().follow(D).getToNode();
    n = st.root.follow(A).getToNode().follow(N).getToNode();
    expected = new Pair<Node, Integer>(n, 0);
    result = st.rootedLCP(queryIndex, overlapHeight, ssp, st, st.root);
    assertEquals(expected, result);
  }
  
  @Test
  public void hardTestRootedLCP() {
    SuffixTreeWithCPD.Builder suffixTreeBuilder =
        new SuffixTreeWithCPD.Builder(new Text("BANANABANANA", true), 2);
    SuffixTreeWithCPD st = suffixTreeBuilder.build();
    st.printTree();
    
    AlphabetCharacter A = new AlphabetCharacter(new Character('A'));
    AlphabetCharacter B = new AlphabetCharacter(new Character('B'));
    AlphabetCharacter N = new AlphabetCharacter(new Character('N'));
    AlphabetCharacter D = new AlphabetCharacter(new Character('$'));
    AlphabetCharacter S = new AlphabetCharacter(new Character('*'));
    
    Text t = new Text("BAN", false);
    int queryIndex = 13;
    int overlapHeight = 3;
    Node ssp = st.root.follow(B).getToNode();
    Node n = st.root.follow(B).getToNode();
    Pair<Node, Integer> expected = new Pair<Node, Integer>(n, -3);
    Pair<Node, Integer> result = st.rootedLCP(queryIndex, overlapHeight, ssp, st, st.root);
    assertEquals(expected, result);
    
    SuffixTreeWithCPD s = (SuffixTreeWithCPD) ((WildcardEdge) st.root.follow(S)).wildcardTree;
    t = new Text("ANAB", false);
    queryIndex = 7;
    overlapHeight = 4;
    ssp = st.root.follow(A).getToNode().follow(N).getToNode().follow(B).getToNode();
    n = s.root.follow(A).getToNode().follow(N).getToNode().follow(B).getToNode();
    expected = new Pair<Node, Integer>(n, -6);
    result = s.rootedLCP(queryIndex, overlapHeight, ssp, st, st.root);
    assertEquals(expected, result);
    
    t = new Text("T", false);
    queryIndex = 25;
    overlapHeight = 0;
    ssp = st.root;
    expected = new Pair<Node, Integer>(s.root, 0);
    result = s.rootedLCP(queryIndex, overlapHeight, ssp, st, st.root);
    assertEquals(expected, result);
    
    t = new Text("NANA", false);
    queryIndex = 19;
    overlapHeight = 4;
    ssp = st.root.follow(N).getToNode().follow(N).getToNode().follow(D).getToNode();
    expected = new Pair<Node, Integer>(s.root, 0);
    result = s.rootedLCP(queryIndex, overlapHeight, ssp, st, st.root);
    assertEquals(expected, result);
    
    s = (SuffixTreeWithCPD) ((WildcardEdge) s.root.follow(A).getToNode().follow(N).getToNode().follow(S)).wildcardTree;
    t = new Text("ANA", false);
    n = s.root.follow(A).getToNode();
    expected = new Pair<Node, Integer>(n, -3);
    result = s.slowRootedLCP(t);
    assertEquals(expected, result);
  }
  
  @Test
  public void testRootedLCP() {
    SuffixTreeWithCPD.Builder suffixTreeBuilder =
        new SuffixTreeWithCPD.Builder(new Text("BANANABANANA", true), 2);
    SuffixTreeWithCPD st = suffixTreeBuilder.build();
    
    AlphabetCharacter A = new AlphabetCharacter(new Character('A'));
    AlphabetCharacter B = new AlphabetCharacter(new Character('B'));
    AlphabetCharacter N = new AlphabetCharacter(new Character('N'));
    AlphabetCharacter D = new AlphabetCharacter(new Character('$'));
    AlphabetCharacter S = new AlphabetCharacter(new Character('*'));
    
    Text t = new Text("BAN", false);
    Node n = st.root.follow(B).getToNode();
    Pair<Node, Integer> expected = new Pair<Node, Integer>(n, -3);
    Pair<Node, Integer> result = st.slowRootedLCP(t);
    assertEquals(expected, result);
    
    SuffixTreeWithCPD s = (SuffixTreeWithCPD) ((WildcardEdge) st.root.follow(S)).wildcardTree;
    t = new Text("ANAB", false);
    n = s.root.follow(A).getToNode().follow(N).getToNode().follow(B).getToNode();
    expected = new Pair<Node, Integer>(n, -6);
    result = s.slowRootedLCP(t);
    assertEquals(expected, result);
    
    t = new Text("T", false);
    expected = null;
    result = s.slowRootedLCP(t);
    assertEquals(expected, result);
    
    s = (SuffixTreeWithCPD) ((WildcardEdge) s.root.follow(A).getToNode().follow(N).getToNode().follow(S)).wildcardTree;
    t = new Text("ANA", false);
    n = s.root.follow(A).getToNode();
    expected = new Pair<Node, Integer>(n, -3);
    result = s.slowRootedLCP(t);
    assertEquals(expected, result);
  }
  
  @Test
  public void testNodeDepthBanana() {
    SuffixTreeWithCPD.Builder suffixTreeBuilder =
        new SuffixTreeWithCPD.Builder(new Text("BANANA", true), 1);
    SuffixTreeWithCPD st = suffixTreeBuilder.build();
    
    AlphabetCharacter A = new AlphabetCharacter(new Character('A'));
    AlphabetCharacter B = new AlphabetCharacter(new Character('B'));
    AlphabetCharacter N = new AlphabetCharacter(new Character('N'));
    AlphabetCharacter S = new AlphabetCharacter(new Character('*'));
    
    SuffixTreeWithCPD stWild = (SuffixTreeWithCPD)((WildcardEdge)st.root.follow(AlphabetCharacter.WILDCARD)).wildcardTree;
    Node n1 = stWild.root.follow(A).getToNode().follow(AlphabetCharacter.END_CHAR).getToNode();
    assertEquals(2, n1.depthInSubtree);
  }
  
  @Test
  public void testCorrespondingNodeInSBananaBanana() {
    SuffixTreeWithCPD.Builder suffixTreeBuilder =
        new SuffixTreeWithCPD.Builder(new Text("BANANABANANA", true), 2);
    SuffixTreeWithCPD st = suffixTreeBuilder.build();
    
    AlphabetCharacter A = new AlphabetCharacter(new Character('A'));
    AlphabetCharacter B = new AlphabetCharacter(new Character('B'));
    AlphabetCharacter N = new AlphabetCharacter(new Character('N'));
    AlphabetCharacter S = new AlphabetCharacter(new Character('*'));
    
    SuffixTreeWithCPD stWild = (SuffixTreeWithCPD)((WildcardEdge)st.root.follow(AlphabetCharacter.WILDCARD)).wildcardTree;
    Node n1 = stWild.root.follow(A).getToNode().follow(N).getToNode().follow(AlphabetCharacter.WILDCARD).getToNode().follow(A).getToNode();
    Node n2 = st.root.follow(A).getToNode().follow(N).getToNode().follow(N).getToNode().follow(AlphabetCharacter.END_CHAR).getToNode();
    assertEquals(n2, stWild.getCorrespondingNodeInS(n1));
  }
  
  @Test
  public void testNodeDepthBananaBanana() {
    SuffixTreeWithCPD.Builder suffixTreeBuilder =
        new SuffixTreeWithCPD.Builder(new Text("BANANABANANA", true), 2);
    SuffixTreeWithCPD st = suffixTreeBuilder.build();
    
    AlphabetCharacter A = new AlphabetCharacter(new Character('A'));
    AlphabetCharacter B = new AlphabetCharacter(new Character('B'));
    AlphabetCharacter N = new AlphabetCharacter(new Character('N'));
    AlphabetCharacter S = new AlphabetCharacter(new Character('*'));
    
    SuffixTreeWithCPD stWild = (SuffixTreeWithCPD)((WildcardEdge)st.root.follow(AlphabetCharacter.WILDCARD)).wildcardTree;
    Node n1 = stWild.root.follow(A).getToNode().follow(N).getToNode().follow(AlphabetCharacter.WILDCARD).getToNode().follow(A).getToNode();
    assertEquals(6, n1.depthInSubtree);
  }
  
  @Test
  public void testProblem() {
    Text t = new Text("ALLRIGHTTHISISELISSAADAMSANDELISSAYOUVELIVEDINCHARLOTTEFORFIVEYEARSFIVEYEARSOKUMFIRSTTHINGIWANTTOAS", true);
    SuffixTreeWithCPD.Builder suffixTreeBuilder =
      new SuffixTreeWithCPD.Builder(t, 1);
    SuffixTreeWithCPD st = suffixTreeBuilder.build();

    AlphabetCharacter V = new AlphabetCharacter(new Character('V'));
    AlphabetCharacter L = new AlphabetCharacter(new Character('L'));
    AlphabetCharacter N = new AlphabetCharacter(new Character('N'));
    AlphabetCharacter S = new AlphabetCharacter(new Character('S'));
    AlphabetCharacter I = new AlphabetCharacter(new Character('I'));
    AlphabetCharacter Y = new AlphabetCharacter(new Character('Y'));
    
  
    Text p = new Text("V*LIVEDINCHARLOTTEFORFIVEYEARSFIVEYEARSOKUMFIRSTTH", false);
    List<Pair<Node, Integer>> result = st.smartQuery(p);
    Node n = st.root.follow(V).getToNode().follow(L).getToNode();
    List<Pair<Node, Integer>> expected = new ArrayList<Pair<Node, Integer>>();
    expected.add(new Pair<Node, Integer>(n, -13));
    assertEquals(expected, result);

    p = new Text("*SSAYOUVELIVEDINCHARLOTTEFORFIVEYEARSFIVEYEARSOKUM", false);
    result = st.smartQuery(p);
    n = st.root.follow(I).getToNode().follow(S).getToNode().follow(S).getToNode().follow(Y).getToNode();
    expected = new ArrayList<Pair<Node, Integer>>();
    expected.add(new Pair<Node, Integer>(n, -20));
    assertEquals(expected, result);
  }
}
