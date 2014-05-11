package stringmatch.ds.yfasttrie;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import stringmatch.ds.yfasttrie.cuckoohash.CuckooHashMap;

public class YFastTrie {

  protected XFastTrie xft;
  protected CuckooHashMap<Integer, TreeSet<Integer>> bbsts;
  
  protected int universeSize;
  protected int bbstSize;
  protected int numBBSTs;
  
  protected YFastTrie(Builder builder) {
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
      return bbsts.get(repSuccessor).contains(key);
    if (repSuccessor == null)
      return bbsts.get(repPredecessor).contains(key);
    
    return bbsts.get(repSuccessor).contains(key) ||
        bbsts.get(repPredecessor).contains(key);
  }
  
  // Returns the predecessor if it exists, or else null.
  public Integer predecessor(int key) {
    // TODO: See hasKey(..) TODO.
    Integer repPredecessor = xft.predecessor(key);
    Integer repSuccessor;
    if (xft.hasKey(key))
      repSuccessor = key;
    else
      repSuccessor = xft.successor(key);
    
    if (repPredecessor == null)
      return bbsts.get(repSuccessor).lower(key);
    if (repSuccessor == null)
      return bbsts.get(repPredecessor).lower(key);
    
    Integer repSuccessorPred = bbsts.get(repSuccessor).lower(key);
    if (repSuccessorPred != null)
      return repSuccessorPred;
    else
      return bbsts.get(repPredecessor).lower(key);
  }
  
  // Returns the successor if it exists, or else null.
  public Integer successor(int key) {
    // TODO: See hasKey(..) TODO.
    Integer repSuccessor = xft.successor(key);
    Integer repPredecessor;
    if (xft.hasKey(key))
      repPredecessor = key;
    else
      repPredecessor = xft.predecessor(key);
    
    if (repPredecessor == null)
      return bbsts.get(repSuccessor).higher(key);
    if (repSuccessor == null)
      return bbsts.get(repPredecessor).higher(key);
    
    Integer repPrecessorSuc = bbsts.get(repPredecessor).higher(key);
    if (repPrecessorSuc != null)
      return repPrecessorSuc;
    else
      return bbsts.get(repSuccessor).higher(key);
  }
  
  public static class Builder {
    
    protected XFastTrie xft;
    protected CuckooHashMap<Integer, TreeSet<Integer>> bbsts;
    
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
    
    public YFastTrie buildFromKeys(List<Integer> keys) {
      XFastTrie.Builder.processInputKeys(keys);
      
      int numKeys = keys.size();
      int maxKey = 0;
      for (int i = 0; i < numKeys; i++) {
        if (keys.get(i) > maxKey) maxKey = keys.get(i);
      }
      universeSize = maxKey + 1;
      
      bbstSize = (int) Math.ceil(Math.log(universeSize) / Math.log(2));
      if (universeSize == 1)
        bbstSize++;
      numBBSTs = (int) Math.ceil(numKeys / bbstSize);
      
      bbsts = new CuckooHashMap<Integer, TreeSet<Integer>>();
      
      List<Integer> bbstRepresentatives = new ArrayList<Integer>(numBBSTs);
      
      TreeSet<Integer> currentBBST = new TreeSet<Integer>();
      for (int i = 0; i < numKeys; i++) {
        if (currentBBST.size() == bbstSize) {
          // Find a representative element, which can be the key inserted
          // bbstSize/2 iterations ago.
          int rep = keys.get(i - bbstSize/2);
          
          // Store the BBST and start with a new one.
          bbsts.put(rep, currentBBST);
          bbstRepresentatives.add(rep);
          currentBBST = new TreeSet<Integer>();
        }
        
        currentBBST.add(keys.get(i));
      }
      if (currentBBST.size() > 0) {
        // Find a representative element, which can be the key inserted
        // currentBBST.size()/2 iterations before the end.
        int rep = keys.get(numKeys - 1 - currentBBST.size()/2);
        
        // Store the BBST and start with a new one.
        bbsts.put(rep, currentBBST);
        bbstRepresentatives.add(rep);
      }
      
      // Build the X-Fast trie.
      XFastTrie.Builder xftBuilder = new XFastTrie.Builder();
      xft = xftBuilder.buildFromKeys(bbstRepresentatives);
      
      return new YFastTrie(this);
    }
    
  }
  
}
