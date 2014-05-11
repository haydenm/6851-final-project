package stringmatch.ds.suffixtree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import stringmatch.ds.text.Text;

public class SuffixTreeNaiveBigSpaceTest {

  @Test
  public void testBanana() {
    SuffixTreeNaiveBigSpace.Builder suffixTreeBuilder = new SuffixTreeNaiveBigSpace.Builder(new Text("BANANAB", true), 2);
    SuffixTreeNaiveBigSpace st = suffixTreeBuilder.build();
    st.printTree();
    System.out.println(st.getAllSuffixesAsStrings());
  }
  
  @Test
  public void testBuild() {
    String s = "";
    Random r = new Random();
    for (int i = 0; i < 60; i++) {
      s += String.valueOf((char)(r.nextInt(26)+'A'));
      //s += "BANANA";
    }
    System.out.println("building");
    SuffixTree.Builder suffixTreeBuilder = new SuffixTree.Builder(new Text(s, true));
    SuffixTree st = suffixTreeBuilder.build();
    //st.printTree();
    //SuffixTreeNaiveBigSpace.Builder suffixTreeBuilder1 = new SuffixTreeNaiveBigSpace.Builder(new Text(s, true));
    //SuffixTreeNaiveBigSpace st1 = suffixTreeBuilder1.build();
    //st1.printTree();
  }
  
  @Test
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
  
}
