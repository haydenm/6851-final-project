package stringmatch.ds.yfasttrie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import stringmatch.ds.util.Pair;
import stringmatch.ds.yfasttrie.cuckoohash.CuckooHashMap;

public class YFastTrie<T> {

  protected XFastTrie xft;
  protected CuckooHashMap<Integer, TreeMap<Integer, T>> bbsts;
  
  protected int universeSize;
  protected int bbstSize;
  protected int numBBSTs;
  
  protected YFastTrie(Builder<T> builder) {
    xft = builder.xft;
    bbsts = builder.bbsts;
    universeSize = builder.universeSize;
    bbstSize = builder.bbstSize;
    numBBSTs = builder.numBBSTs;
  }
  
  public boolean hasKey(int key) {
    // key must be in the BBST corresponding to either the representative
    // predecessor or successor of key.
    
    if (xft.hasKey(key))
      return true;
    
    // TODO: Can improve constant factors by only running predecessor,
    // and using the leaf returned to find repSuccessor in O(1) time.
    // It's not necessary to call xft.successor. The same applies
    // to the predecessor(..) and successor(..) methods below.
    Integer repPredecessor = xft.predecessor(key);
    Integer repSuccessor = xft.successor(key);
    
    if (repPredecessor == null)
      return bbsts.get(repSuccessor).containsKey(key);
    if (repSuccessor == null)
      return bbsts.get(repPredecessor).containsKey(key);
    
    return bbsts.get(repSuccessor).containsKey(key) ||
        bbsts.get(repPredecessor).containsKey(key);
  }
  
  // Returns the predecessor if it exists, or else null.
  public Pair<Integer, T> predecessor(int key) {
    // TODO: See hasKey(..) TODO.
    Integer repPredecessor = xft.predecessor(key);
    Integer repSuccessor;
    if (xft.hasKey(key))
      repSuccessor = key;
    else
      repSuccessor = xft.successor(key);
    
    if (repPredecessor == null)
      return pairFromEntry(bbsts.get(repSuccessor).lowerEntry(key));
    if (repSuccessor == null)
      return pairFromEntry(bbsts.get(repPredecessor).lowerEntry(key));
    
    Pair<Integer, T> repSuccessorPred = 
        pairFromEntry(bbsts.get(repSuccessor).lowerEntry(key));
    if (repSuccessorPred != null)
      return repSuccessorPred;
    else
      return pairFromEntry(bbsts.get(repPredecessor).lowerEntry(key));
  }
  
  // Returns the successor if it exists, or else null.
  public Pair<Integer, T> successor(int key) {
    // TODO: See hasKey(..) TODO.
    Integer repSuccessor = xft.successor(key);
    Integer repPredecessor;
    if (xft.hasKey(key))
      repPredecessor = key;
    else
      repPredecessor = xft.predecessor(key);
    
    if (repPredecessor == null)
      return pairFromEntry(bbsts.get(repSuccessor).higherEntry(key));
    if (repSuccessor == null)
      return pairFromEntry(bbsts.get(repPredecessor).higherEntry(key));
    
    Pair<Integer, T> repPrecessorSuc =
        pairFromEntry(bbsts.get(repPredecessor).higherEntry(key));
    if (repPrecessorSuc != null)
      return repPrecessorSuc;
    else
      return pairFromEntry(bbsts.get(repSuccessor).higherEntry(key));
  }
  
  protected Pair<Integer, T> pairFromEntry(Map.Entry<Integer, T> entry) {
    if (entry == null)
      return null;
    return new Pair<Integer, T>(entry.getKey(), entry.getValue());
  }
  
  public static class Builder<T> {
    
    protected XFastTrie xft;
    protected CuckooHashMap<Integer, TreeMap<Integer, T>> bbsts;
    
    protected int universeSize;
    protected int bbstSize;
    protected int numBBSTs;
    
    public Builder() {
      xft = null;
      bbsts = null;
      universeSize = 0;
      bbstSize = 0;
      numBBSTs = 0;
    }
    
    protected void processInputKeys(List<Pair<Integer, T>> keys) {
      if (keys.size() == 0)
        throw new IllegalArgumentException("Can't make a trie out of nothing.");
      
      boolean isSorted = true;
      for (int i = 1; i < keys.size(); i++) {
        if (keys.get(i).getLeft() < keys.get(i - 1).getLeft()) {
          isSorted = false;
          break;
        }
      }
      if (!isSorted) {
        Collections.sort(keys, new Comparator<Pair<Integer, T>>() {
          public int compare(Pair<Integer, T> p1, Pair<Integer, T> p2) {
            return p1.getLeft().compareTo(p2.getLeft());
          }
        });
      }
      for (int i = 1; i < keys.size(); i++) {
        if (keys.get(i).getLeft() == keys.get(i - 1).getLeft()) {
          throw new IllegalArgumentException("Keys must be distinct.");
        }
      }
    }
    
    public YFastTrie<T> buildFromPairs(List<Pair<Integer, T>> keys) {
      processInputKeys(keys);
      
      int numKeys = keys.size();
      int maxKey = 0;
      for (int i = 0; i < numKeys; i++) {
        if (keys.get(i).getLeft() > maxKey) maxKey = keys.get(i).getLeft();
      }
      universeSize = maxKey + 1;
      
      bbstSize = (int) Math.ceil(Math.log(universeSize) / Math.log(2));
      if (universeSize == 1)
        bbstSize++;
      numBBSTs = (int) Math.ceil(numKeys / bbstSize);
      
      bbsts = new CuckooHashMap<Integer, TreeMap<Integer, T>>();
      
      List<Integer> bbstRepresentatives = new ArrayList<Integer>(numBBSTs);
      
      TreeMap<Integer, T> currentBBST = new TreeMap<Integer, T>();
      for (int i = 0; i < numKeys; i++) {
        if (currentBBST.size() == bbstSize) {
          // Find a representative element, which can be the key inserted
          // bbstSize/2 iterations ago.
          int rep = keys.get(i - bbstSize/2).getLeft();
          
          // Store the BBST and start with a new one.
          bbsts.put(rep, currentBBST);
          bbstRepresentatives.add(rep);
          currentBBST = new TreeMap<Integer, T>();
        }
        
        currentBBST.put(keys.get(i).getLeft(), keys.get(i).getRight());
      }
      if (currentBBST.size() > 0) {
        // Find a representative element, which can be the key inserted
        // currentBBST.size()/2 iterations before the end.
        int rep = keys.get(numKeys - 1 - currentBBST.size()/2).getLeft();
        
        // Store the BBST and start with a new one.
        bbsts.put(rep, currentBBST);
        bbstRepresentatives.add(rep);
      }
      
      // Build the X-Fast trie.
      XFastTrie.Builder xftBuilder = new XFastTrie.Builder();
      xft = xftBuilder.buildFromKeys(bbstRepresentatives);
      
      return new YFastTrie<T>(this);
    }
    
  }
  
}
