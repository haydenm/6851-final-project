package stringmatch.ds.suffixtree;

import java.util.Random;

import org.junit.Test;

import stringmatch.ds.text.Text;

public class SuffixTreeNaiveBigSpaceTest {

  @Test
  public void testBuild() {
    String s = "";
    Random r = new Random();
    for (int i = 0; i < 60000; i++) {
      s += String.valueOf((char)(r.nextInt(26)+'A'));
    }
    System.out.println("building");
    SuffixTree.Builder suffixTreeBuilder = new SuffixTree.Builder(new Text(s, true));
    SuffixTree st = suffixTreeBuilder.build();
    //st.printTree();
    //SuffixTreeNaiveBigSpace.Builder suffixTreeBuilder1 = new SuffixTreeNaiveBigSpace.Builder(new Text(s, true));
    //SuffixTreeNaiveBigSpace st1 = suffixTreeBuilder1.build();
    //st1.printTree();
  }
  
}
