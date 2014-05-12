package stringmatch.ds.suffixtree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;

public class SuffixTreeWithCPDTest {

  @Test
  public void testBanana() {
    SuffixTreeWithCPD.Builder suffixTreeBuilder = new SuffixTreeWithCPD.Builder(new Text("BANANAB", true), 1);
    SuffixTreeWithCPD st = suffixTreeBuilder.build();
    st.printTree();
    System.out.println(st.getAllSuffixesAsStrings());
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
    for (int i = 0; i < "BANANA".length(); i++) {
      assertTrue(st.leafLexicographicIndices.hasKey(i));
    }
    for (int i = "BANANA".length(); i < 20; i++) {
      assertFalse(st.leafLexicographicIndices.hasKey(i));
    }
    
    assertEquals(null, st.leafLexicographicIndices.predecessor(0));
    for (int i = 1; i <= "BANANA".length(); i++) {
      assertEquals(i-1, st.leafLexicographicIndices.predecessor(i).getLeft().intValue());
    }
    assertEquals(5, st.leafLexicographicIndices.predecessor(10).getLeft().intValue());
    
    assertEquals("$", st.leafLexicographicIndices.predecessor(1).getRight().toString());
    assertEquals("$", st.leafLexicographicIndices.predecessor(2).getRight().toString());
    assertEquals("NA$", st.leafLexicographicIndices.predecessor(3).getRight().toString());
    assertEquals("BANANA$", st.leafLexicographicIndices.predecessor(4).getRight().toString());
    assertEquals("$", st.leafLexicographicIndices.predecessor(5).getRight().toString());
    
    assertEquals(null, st.leafLexicographicIndices.successor(5));
    for (int i = 0; i < "BANANA".length() - 1; i++) {
      assertEquals(i+1, st.leafLexicographicIndices.successor(i).getLeft().intValue());
    }
    
    assertEquals("$", st.leafLexicographicIndices.successor(0).getRight().toString());
    assertEquals("NA$", st.leafLexicographicIndices.successor(1).getRight().toString());
    assertEquals("BANANA$", st.leafLexicographicIndices.successor(2).getRight().toString());
    assertEquals("$", st.leafLexicographicIndices.successor(3).getRight().toString());
    assertEquals("NA$", st.leafLexicographicIndices.successor(4).getRight().toString());
    
    SuffixTreeWithWildcards stWild = ((WildcardEdge)st.root.follow(new AlphabetCharacter('*'))).wildcardTree;
    assertTrue(stWild.leafLexicographicIndices.hasKey(3));
    assertTrue(stWild.leafLexicographicIndices.hasKey(4));
    assertTrue(stWild.leafLexicographicIndices.hasKey(5));
    assertFalse(stWild.leafLexicographicIndices.hasKey(0));
    assertFalse(stWild.leafLexicographicIndices.hasKey(1));
    assertFalse(stWild.leafLexicographicIndices.hasKey(2));
    
    assertEquals(null, stWild.leafLexicographicIndices.predecessor(1));
    assertEquals(3, stWild.leafLexicographicIndices.successor(0).getLeft().intValue());
    
    assertEquals("NA$", stWild.leafLexicographicIndices.predecessor(4).getRight().toString());
  }
  
}
