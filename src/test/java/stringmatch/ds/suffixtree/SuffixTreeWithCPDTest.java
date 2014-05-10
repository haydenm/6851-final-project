package stringmatch.ds.suffixtree;

import org.junit.Test;

import stringmatch.ds.text.Text;

public class SuffixTreeWithCPDTest {

  @Test
  public void testBanana() {
    SuffixTreeWithCPD.Builder suffixTreeBuilder = new SuffixTreeWithCPD.Builder(new Text("BANANABANANABANANA", true));
    SuffixTreeWithCPD st = suffixTreeBuilder.build();
    st.printTree();
    System.out.println(st.getAllSuffixesAsStrings());
    System.out.println(st.getAllSuffixesAsStrings(true));
  }
  
}
