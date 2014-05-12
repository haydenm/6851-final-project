package stringmatch.ds.suffixtree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;
import stringmatch.ds.util.Pair;

public class SuffixTreeNaiveBigSpaceTest {

  @Test
  //@Ignore
  public void testBanana() {
    SuffixTreeNaiveBigSpace.Builder suffixTreeBuilder = new SuffixTreeNaiveBigSpace.Builder(new Text("BANANABANANA", true), 1);
    SuffixTreeNaiveBigSpace st = suffixTreeBuilder.build();
    st.printTree();
    AlphabetCharacter S = new AlphabetCharacter('*');
    AlphabetCharacter A = new AlphabetCharacter('A');
    AlphabetCharacter N = new AlphabetCharacter('N');
    AlphabetCharacter B = new AlphabetCharacter('B');
    Node NA = st.root.follow(S).getToNode().follow(A).getToNode().follow(N).getToNode().follow(N).getToNode();
    Node BANANA = st.root.follow(S).getToNode().follow(A).getToNode().follow(N).getToNode().follow(N).getToNode().follow(B).getToNode();
    System.out.println(NA);
    System.out.println(BANANA);
    System.out.println(BANANA.incomingEdge.getFromNode());
    System.out.println(NA);
    //System.out.println(st.getAllSuffixesAsStrings());
  }
  
  @Test
  public void testBuild() {
    String s = "";
    Random r = new Random(123L);
    for (int i = 0; i < 60; i++) {
      s += String.valueOf((char)(r.nextInt(26)+'A'));
      //s += "BANANA";
    }
    //System.out.println("building");
    SuffixTree.Builder suffixTreeBuilder = new SuffixTree.Builder(new Text(s, true));
    SuffixTree st = suffixTreeBuilder.build();
    //st.printTree();
    //SuffixTreeNaiveBigSpace.Builder suffixTreeBuilder1 = new SuffixTreeNaiveBigSpace.Builder(new Text(s, true));
    //SuffixTreeNaiveBigSpace st1 = suffixTreeBuilder1.build();
    //st1.printTree();
  }
  
  @Test
  //@Ignore
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
        SuffixTreeNaiveBigSpace.Builder suffixTreeBuilder = new SuffixTreeNaiveBigSpace.Builder(t, k);
        SuffixTreeNaiveBigSpace st = suffixTreeBuilder.build();
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
  public void testQueriesBanana() {
    String text = "BANANAB";
    
    List<Pair<Integer[], String>> checks = new ArrayList<Pair<Integer[], String>>();
    checks.add(new Pair<Integer[], String>(new Integer[] { 0, 6 }, "B"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0 }, "BA"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0 }, "B*"));
    checks.add(new Pair<Integer[], String>(new Integer[] { }, "X"));
    checks.add(new Pair<Integer[], String>(new Integer[] { }, "B*D"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 1, 3 }, "AN"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 1, 3, 5 }, "A*"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0, 1, 2, 3, 4, 5, 6 }, "*"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 1, 3 }, "A*A"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 3 }, "A*AB"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 2 }, "NANAB"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 2 }, "N*NAB"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0 }, "B*N"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0 }, "B**"));
    checks.add(new Pair<Integer[], String>(new Integer[] { }, "A**A"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 1, 3 }, "A*A*"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0 }, "B*NAN*"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0 }, "**NANAB"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 4 }, "**B"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 2 }, "NA**"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 1 }, "AN*N*B"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 2 }, "N*NA*"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 1, 3 }, "*N*"));
    
    testQueries(text, 2, checks);
  }
  
  @Test
  public void testQueriesBandanaBanana() {
    String text = "BANDANABANANA";
    
    List<Pair<Integer[], String>> checks = new ArrayList<Pair<Integer[], String>>();
    checks.add(new Pair<Integer[], String>(new Integer[] { 0, 7 }, "B"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0, 7 }, "BA"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0, 7 }, "B*"));
    checks.add(new Pair<Integer[], String>(new Integer[] { }, "X"));
    checks.add(new Pair<Integer[], String>(new Integer[] { }, "B*D"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0 }, "B*ND"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 1, 4, 8, 10 }, "AN"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 1, 4, 6, 8, 10 }, "A*"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 }, "*"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 }, "**"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, "***"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 4, 6, 8, 10 }, "A*A"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 4 }, "A*AB"));
    checks.add(new Pair<Integer[], String>(new Integer[] { }, "NANAB"));
    checks.add(new Pair<Integer[], String>(new Integer[] { }, "N*NAB"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0, 7 }, "B*N"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0, 7 }, "B**"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 1 }, "A**A"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 4, 6, 8 }, "A*A*"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 7 }, "B*NAN*"));
    checks.add(new Pair<Integer[], String>(new Integer[] { }, "**NANAB"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 5 }, "**B"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 5, 9 }, "NA**"));
    checks.add(new Pair<Integer[], String>(new Integer[] { }, "AN*N*B"));
    checks.add(new Pair<Integer[], String>(new Integer[] { }, "N*NA*"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 1, 4, 8, 10 }, "*N*"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0, 7 }, "B***"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0 }, "BA**A*A"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0, 7 }, "BAN***"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 0, 3, 7, 9 }, "**N*"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 3, 7, 9 }, "*ANA"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 3, 7 }, "*ANA**"));
    checks.add(new Pair<Integer[], String>(new Integer[] { 2, 6 }, "**ANA*"));
    checks.add(new Pair<Integer[], String>(new Integer[] { }, "**X*"));
    checks.add(new Pair<Integer[], String>(new Integer[] { }, "A*N"));
    checks.add(new Pair<Integer[], String>(new Integer[] { }, "*A*N*"));
    
    testQueries(text, 3, checks);
  }
  
  protected void testQueries(String text, int k, List<Pair<Integer[], String>> checks) {
    SuffixTreeNaiveBigSpace.Builder suffixTreeBuilder = new SuffixTreeNaiveBigSpace.Builder(new Text(text, true), k);
    SuffixTreeNaiveBigSpace st = suffixTreeBuilder.build();
    
    for (Pair<Integer[], String> check : checks) {
      Integer[] expected = check.getLeft();
      List<Integer> expectedList = new ArrayList<Integer>(Arrays.asList(expected));
      Collections.sort(expectedList);
      
      Text p = new Text(check.getRight(), false);
      List<Integer> matches = st.queryForIndices(p);
      Collections.sort(matches);
      
      assertEquals(expectedList, matches);
    }
  }
  
  @Test
  public void testMatchCount() {
    StringBuilder sb = new StringBuilder();
    Random r = new Random();
    for (int i = 0; i < 10000; i++) {
      sb.append((char)(r.nextInt(26) + (int)'A'));
    }
    SuffixTreeNaiveBigSpace.Builder suffixTreeBuilder = new SuffixTreeNaiveBigSpace.Builder(new Text(sb.toString(), true), 3);
    SuffixTreeNaiveBigSpace st = suffixTreeBuilder.build();
    //st.printTree();
    System.out.println("Number of matches: " + st.queryForIndices(new Text("H**A*", false)).size());
  }
  
}
