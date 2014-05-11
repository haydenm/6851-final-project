package stringmatch.ds.yfasttrie;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

public class XFastTrieTest {

  private static Random r;
  private static TreeSet<Integer> reference;
  private static XFastTrie xft;
  
  @BeforeClass
  public static void setUp() {
    r = new Random(12345L);
    reference = new TreeSet<Integer>();
    for (int i = 0; i < 100000; i++) {
      int j = r.nextInt(10000000);
      if (!reference.contains(j))
        reference.add(j);
    }
    
    XFastTrie.Builder xftBuilder = new XFastTrie.Builder();
    xft = xftBuilder.buildFromKeys(new ArrayList<Integer>(reference));
    xft.verifyPointers();
  }
  
  @Test
  public void testHasKey() {
    for (int n : reference) {
      assertTrue(xft.hasKey(n));
    }
    
    for (int i = 0; i < 10000; i++) {
      int j = r.nextInt(100000000);
      if (!reference.contains(j))
        assertFalse(xft.hasKey(j));
    }
  }
  
  @Test
  public void testPredecessor() {
    for (int i = 0; i < 10000; i++) {
      int j = r.nextInt(100000000);
      assertEquals(reference.lower(j), xft.predecessor(j));
    }
  }
  
  @Test
  public void testSucccessor() {
    for (int i = 0; i < 10000; i++) {
      int j = r.nextInt(10000000);
      assertEquals(reference.higher(j), xft.successor(j));
    }
  }
  
}
