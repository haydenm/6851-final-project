package stringmatch.ds.yfasttrie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

public class YFastTrieTest {

  private static Random r;
  private static TreeSet<Integer> reference;
  private static YFastTrie yft;
  
  @BeforeClass
  public static void setUp() {
    r = new Random(12345L);
    reference = new TreeSet<Integer>();
    for (int i = 0; i < 100000; i++) {
      int j = r.nextInt(100000000);
      if (!reference.contains(j))
        reference.add(j);
    }
    
    YFastTrie.Builder yftBuilder = new YFastTrie.Builder();
    yft = yftBuilder.buildFromKeys(new ArrayList<Integer>(reference));
  }
  
  @Test
  public void testHasKey() {
    for (int n : reference) {
      assertTrue(yft.hasKey(n));
    }
    
    for (int i = 0; i < 10000; i++) {
      int j = r.nextInt(1000000000);
      if (!reference.contains(j))
        assertFalse(yft.hasKey(j));
    }
  }
  
  @Test
  public void testPredecessor() {
    for (int i = 0; i < 10000; i++) {
      int j = r.nextInt(1000000000);
      assertEquals(reference.lower(j), yft.predecessor(j));
    }
  }
  
  @Test
  public void testSucccessor() {
    for (int i = 0; i < 10000; i++) {
      int j = r.nextInt(100000000);
      assertEquals(reference.higher(j), yft.successor(j));
    }
  }
  
}
