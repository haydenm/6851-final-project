package stringmatch.ds.suffixtree;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    assertEquals("[A, $, NA, $, NA$, BANANA$, NA, $, NA$]", 
        st.getRoot().getEdgeStringsInDFS().toString());
    assertEquals("[A, BANANA$, NA, $, NA, $, NA$, $, NA$]",
        st.getRoot().getEdgeStringsInBFS().toString());
    
    List<String> allSuffixesAsStrings = st.getAllSuffixesAsStrings();
    String[] trueSuffixes = new String[] { "A$", "NA$", "ANA$",
        "NANA$", "ANANA$", "BANANA$" };
    assertEquals(trueSuffixes.length, allSuffixesAsStrings.size());
    for (String x : trueSuffixes) {
      assertTrue(allSuffixesAsStrings.contains(x));
    }
  }
  
  @Test
  public void simpleQueryTest() {
	  SuffixTree.Builder suffixTreeBuilder = new SuffixTree.Builder(new Text("BANANA", true));
	  SuffixTree st = suffixTreeBuilder.build();
	  Node expected =
			  st.root
			  .follow(new AlphabetCharacter(new Character('B'))).getToNode();
	  assertEquals(expected, st.query(new Text("BANA", false)));
  }

  @Test
  public void branchingQueryTest() {
	  SuffixTree.Builder suffixTreeBuilder = new SuffixTree.Builder(new Text("BANANA", true));
	  SuffixTree st = suffixTreeBuilder.build();
	  Node expected =
			  st.root
			  .follow(new AlphabetCharacter(new Character('A'))).getToNode()
			  .follow(new AlphabetCharacter(new Character('N'))).getToNode();
	  assertEquals(expected, st.query(new Text("ANA", false)));
  }
  
  @Test
  public void wildcardOnBranchQueryTest() {
	  SuffixTree.Builder suffixTreeBuilder = new SuffixTree.Builder(new Text("BANANA", true));
	  SuffixTree st = suffixTreeBuilder.build();
	  Node node =
			  st.root
			  .follow(new AlphabetCharacter(new Character('A'))).getToNode()
			  .follow(new AlphabetCharacter(new Character('N'))).getToNode();
	  List<Node> expected = new ArrayList<Node>();
	  expected.add(node);
	  assertEquals(expected, st.naiveWildcardQuery(new Text("A*A", false)));
  }
  
  @Test
  public void wildcardAlongEdgeQueryTest() {
	  SuffixTree.Builder suffixTreeBuilder = new SuffixTree.Builder(new Text("BANANA", true));
	  SuffixTree st = suffixTreeBuilder.build();
	  Node node =
			  st.root
			  .follow(new AlphabetCharacter(new Character('N'))).getToNode()
			  .follow(new AlphabetCharacter(new Character('N'))).getToNode();
	  List<Node> expected = new ArrayList<Node>();
	  expected.add(node);
	  assertEquals(expected, st.naiveWildcardQuery(new Text("N*N*", false)));
  }
  
  @Test
  public void wildcardMultipleMatchesQueryTest() {
	  SuffixTree.Builder suffixTreeBuilder = new SuffixTree.Builder(new Text("BANANA", true));
	  SuffixTree st = suffixTreeBuilder.build();
	  Node node1 =
			  st.root
			  .follow(new AlphabetCharacter(new Character('B'))).getToNode();
	  Node node2 = 
			  st.root
			  .follow(new AlphabetCharacter(new Character('A'))).getToNode()
			  .follow(new AlphabetCharacter(new Character('N'))).getToNode();
	  Node node3 = 
			  st.root
			  .follow(new AlphabetCharacter(new Character('N'))).getToNode();
	  List<Node> expected = new ArrayList<Node>();
	  expected.add(node1);
	  expected.add(node2);
	  expected.add(node3);
	  assertEquals(expected, st.naiveWildcardQuery(new Text("**", false)));
  }
  
}
