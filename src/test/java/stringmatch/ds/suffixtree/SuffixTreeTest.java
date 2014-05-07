package stringmatch.ds.suffixtree;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;
import stringmatch.ds.text.TextSubstring;

public class SuffixTreeTest {

  @Test
  public void testBanana() {
    Text.Builder inputTextBuilder = new Text.Builder();
    String s = "BANANA";
    for (char c : s.toCharArray()) {
      inputTextBuilder.addAlphabetCharacter(new AlphabetCharacter(new Character(c)));
    }
    inputTextBuilder.addAlphabetCharacter(AlphabetCharacter.END_CHAR);
    Text inputText = inputTextBuilder.build();
    
    SuffixTree.Builder suffixTreeBuilder = new SuffixTree.Builder(inputText);
    SuffixTree st = suffixTreeBuilder.build();
    assertEquals("[BANANA$, A, $, NA, $, NA$, NA, $, NA$, $]", 
        st.getRoot().getEdgeStringsInDFS().toString());
    assertEquals("[BANANA$, A, NA, $, $, NA, $, NA$, $, NA$]",
        st.getRoot().getEdgeStringsInBFS().toString());
    
    List<String> allSuffixesAsStrings = new ArrayList<String>();
    for (TextSubstring x : st.getRoot().getAllSuffixes()) {
      allSuffixesAsStrings.add(x.getSubstringAsText().toString());
    }
    String[] trueSuffixes = new String[] { "$", "A$", "NA$", "ANA$",
        "NANA$", "ANANA$", "BANANA$" };
    assertEquals(trueSuffixes.length, allSuffixesAsStrings.size());
    for (String x : trueSuffixes) {
      assertTrue(allSuffixesAsStrings.contains(x));
    }
  }

}
