package stringmatch.ds.yfasttrie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

import stringmatch.ds.util.Pair;

public class YFastTrieTest {

  private static Random r;
  private static TreeMap<Integer, Object> reference;
  private static YFastTrie<Object> yft;
  
  @BeforeClass
  public static void setUp() {
    r = new Random(12345L);
    reference = new TreeMap<Integer, Object>();
    for (int i = 0; i < 100000; i++) {
      int j = r.nextInt(100000000);
      if (!reference.containsKey(j))
        reference.put(j, new Object());
    }
    
    YFastTrie.Builder<Object> yftBuilder = new YFastTrie.Builder<Object>();
    List<Pair<Integer, Object>> pairs = new ArrayList<Pair<Integer, Object>>();
    for (Map.Entry<Integer, Object> kv : reference.entrySet()) {
      Pair<Integer, Object> p = new Pair<Integer, Object>(kv.getKey(), kv.getValue());
      pairs.add(p);
    }
    yft = yftBuilder.buildFromPairs(pairs);
  }
  
  @Test
  public void testHasKey() {
    for (int n : reference.keySet()) {
      assertTrue(yft.hasKey(n));
    }
    
    for (int i = 0; i < 10000; i++) {
      int j = r.nextInt(1000000000);
      if (!reference.containsKey(j))
        assertFalse(yft.hasKey(j));
    }
  }
  
  @Test
  public void testPredecessor() {
    for (int i = 0; i < 10000; i++) {
      int j = r.nextInt(1000000000);
      assertEquals(reference.lowerKey(j), yft.predecessor(j).getLeft());
      assertEquals(reference.lowerEntry(j).getValue(), yft.predecessor(j).getRight());
    }
  }
  
  @Test
  public void testSucccessor() {
    for (int i = 0; i < 10000; i++) {
      int j = r.nextInt(100000000);
      assertEquals(reference.higherKey(j), yft.successor(j).getLeft());
      assertEquals(reference.higherEntry(j).getValue(), yft.successor(j).getRight());
    }
  }
  
  @Test
  public void testOneElement() {
    YFastTrie.Builder<Object> yftBuilder = new YFastTrie.Builder<Object>();
    List<Pair<Integer, Object>> pairs = new ArrayList<Pair<Integer, Object>>();
    pairs.add(new Pair<Integer, Object>(2, new Object()));
    YFastTrie<Object> yft1 = yftBuilder.buildFromPairs(pairs);
    assertTrue(yft1.hasKey(2));
    assertFalse(yft1.hasKey(0));
    assertEquals(null, yft1.predecessor(2));
    assertEquals(null, yft1.successor(2));
    assertEquals(null, yft1.predecessor(1));
    assertEquals(2, yft1.predecessor(3).getLeft().intValue());
    assertEquals(2, yft1.successor(0).getLeft().intValue());
  }
  
  @Test
  public void testTwoElements() {
    YFastTrie.Builder<Object> yftBuilder = new YFastTrie.Builder<Object>();
    List<Pair<Integer, Object>> pairs = new ArrayList<Pair<Integer, Object>>();
    pairs.add(new Pair<Integer, Object>(2, new Object()));
    pairs.add(new Pair<Integer, Object>(4, new Object()));
    YFastTrie<Object> yft1 = yftBuilder.buildFromPairs(pairs);
    assertTrue(yft1.hasKey(2));
    assertFalse(yft1.hasKey(0));
    assertTrue(yft1.hasKey(4));
    assertEquals(null, yft1.predecessor(2));
    assertEquals(4, yft1.successor(2).getLeft().intValue());
    assertEquals(null, yft1.predecessor(1));
    assertEquals(2, yft1.predecessor(3).getLeft().intValue());
    assertEquals(2, yft1.successor(0).getLeft().intValue());
    assertEquals(4, yft1.predecessor(6).getLeft().intValue());
    assertEquals(4, yft1.successor(3).getLeft().intValue());
    assertEquals(null, yft1.successor(4));
  }
  
  @Test
  public void testThreeElements() {
    YFastTrie.Builder<Object> yftBuilder = new YFastTrie.Builder<Object>();
    List<Pair<Integer, Object>> pairs = new ArrayList<Pair<Integer, Object>>();
    pairs.add(new Pair<Integer, Object>(2, new Object()));
    pairs.add(new Pair<Integer, Object>(4, new Object()));
    pairs.add(new Pair<Integer, Object>(100, new Object()));
    YFastTrie<Object> yft1 = yftBuilder.buildFromPairs(pairs);
    assertTrue(yft1.hasKey(2));
    assertFalse(yft1.hasKey(0));
    assertTrue(yft1.hasKey(4));
    assertTrue(yft1.hasKey(100));
    assertFalse(yft1.hasKey(50));
    assertEquals(null, yft1.predecessor(2));
    assertEquals(4, yft1.successor(2).getLeft().intValue());
    assertEquals(null, yft1.predecessor(1));
    assertEquals(2, yft1.predecessor(3).getLeft().intValue());
    assertEquals(2, yft1.successor(0).getLeft().intValue());
    assertEquals(4, yft1.predecessor(6).getLeft().intValue());
    assertEquals(4, yft1.successor(3).getLeft().intValue());
    assertEquals(100, yft1.successor(4).getLeft().intValue());
    assertEquals(100, yft1.successor(50).getLeft().intValue());
    assertEquals(100, yft1.predecessor(200).getLeft().intValue());
  }
  
}
