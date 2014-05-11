package stringmatch.ds.yfasttrie;

import java.util.Collections;
import java.util.List;

import stringmatch.ds.yfasttrie.cuckoohash.CuckooHashMap;

public class XFastTrie {

  protected int universeSize;
  protected int numLevels;
  
  protected XFastInternalNode root;
  protected CuckooHashMap<Integer, XFastNode>[] hashTables;
  
  protected XFastTrie(Builder builder) {
    universeSize = builder.universeSize;
    numLevels = builder.numLevels;
    root = builder.root;
    hashTables = builder.hashTables;
  }
  
  public boolean hasKey(int key) {
    // Just search for it in the leaves (level 0) hash table.
    return hashTables[0].containsKey(key);
  }
  
  protected XFastInternalNode findLowestAncestor(int key) {
    // Do a binary search over levels between level (numLevels - 1)
    // and level 1 (not the leaves).
    int levelLo = 1;
    int levelHi = numLevels - 1;
    
    if (levelLo == levelHi) {
      // Just two levels.
      return root;
    }
    
    XFastNode lastFoundAncestor = null;
    
    while (levelLo < levelHi) {
      int levelMid = levelLo + (levelHi - levelLo) / 2;
      int prefixLengthAtLevel = (numLevels - 1) - levelMid;
      int keyPrefix = XFastInternalNode.prefixOfKey(key, prefixLengthAtLevel,
          numLevels);
      boolean levelHasPrefix = hashTables[levelMid].containsKey(keyPrefix);
      
      if (levelHasPrefix) {
        lastFoundAncestor = hashTables[levelMid].get(keyPrefix);
        // The lowest ancestor must be here or below, so search the current
        // level and below.
        levelHi = levelMid;
      } else {
        // The lowest ancestor must be higher, so search above the current level.
        levelLo = levelMid + 1;
      }
    }

    // Because we only search at levels 1 and above, lastFoundAncestor
    // should be an internal node.
    return (XFastInternalNode) lastFoundAncestor;
  }
  
  protected XFastLeaf findLowestAncestorDescendant(int key) {
    XFastInternalNode lowestAncestor = findLowestAncestor(key);
    // lowestAncestor must have one descendant pointer that points to a leaf,
    // which is either the predecessor or successor of key.
    XFastLeaf leaf;
    if (lowestAncestor.getLeftDescendantPointer() != null)
      leaf = lowestAncestor.getLeftDescendantPointer();
    else if (lowestAncestor.getRightDescendantPointer() != null)
      leaf = lowestAncestor.getRightDescendantPointer();
    else
      throw new RuntimeException("Something's not right..");
    return leaf;
  }
  
  // Returns the predecessor if it exists, or else null.
  public Integer predecessor(int key) {
    if (key >= universeSize) {
      if (hasKey(universeSize - 1))
        return universeSize - 1;
      else
        return predecessor(universeSize - 1);
    }
    
    if (hashTables[0].containsKey(key)) {
      // key is in the trie.
      XFastLeaf pred = (XFastLeaf) hashTables[0].get(key);
      if (pred.getPredecessor() != null)
        return pred.getPredecessor().getKey();
      else
        return null;
    }
    
    XFastLeaf leaf = findLowestAncestorDescendant(key);
    
    if (leaf.getKey() < key)
      return leaf.getKey();
    
    // leaf is a successor of key.
    if (leaf.getPredecessor() == null)
      return null;
    else
      return leaf.getPredecessor().getKey();
  }
  
  // Returns the successor if it exists, or else null.
  public Integer successor(int key) {
    if (key >= universeSize)
      return null;
    
    if (hashTables[0].containsKey(key)) {
      // key is in the trie.
      XFastLeaf pred = (XFastLeaf) hashTables[0].get(key);
      if (pred.getSuccessor() != null)
        return pred.getSuccessor().getKey();
      else
        return null;
    }
    
    XFastLeaf leaf = findLowestAncestorDescendant(key);
    if (leaf.getKey() > key)
       return leaf.getKey();

    // leaf is a predecessor of key.
    if (leaf.getSuccessor() == null)
      return null;
    else
      return leaf.getSuccessor().getKey();
  }
 
  public void verifyPointers() {
    verifyPointersAt(root);
  }
  
  protected void verifyPointersAt(XFastNode node) {
    node.verifyPointers();
    if (node.getLeftChild() != null)
      verifyPointersAt(node.getLeftChild());
    if (node.getRightChild() != null)
      verifyPointersAt(node.getRightChild());
  }
  
  public static class Builder {
    
    protected int universeSize;
    protected int numLevels;
    
    protected XFastInternalNode root;
    protected CuckooHashMap<Integer, XFastNode>[] hashTables;
    
    public Builder() {
      universeSize = 0;
      numLevels = 0;
      root = null;
      hashTables = null;
    }
    
    public static void processInputKeys(List<Integer> keys) {
      if (keys.size() == 0)
        throw new IllegalArgumentException("Can't make a trie out of nothing.");
      
      boolean isSorted = true;
      for (int i = 1; i < keys.size(); i++) {
        if (keys.get(i) < keys.get(i - 1)) {
          isSorted = false;
          break;
        }
      }
      if (!isSorted)
        Collections.sort(keys);
      for (int i = 1; i < keys.size(); i++) {
        if (keys.get(i) == keys.get(i - 1)) {
          throw new IllegalArgumentException("Keys must be distinct.");
        }
      }
    }
    
    public XFastTrie buildFromKeys(List<Integer> keys) {
      processInputKeys(keys);
      
      int maxKey = 0;
      for (int i = 0; i < keys.size(); i++) {
        if (keys.get(i) > maxKey) maxKey = keys.get(i);
      }
      universeSize = maxKey + 1;
      
      // We need an extra level for the root.
      numLevels = (int) Math.ceil(Math.log(universeSize) / Math.log(2)) + 1;
      if (universeSize == 1)
        numLevels++;

      // Build the trie. Takes O(n * log(universeSize)) time.
      root = new XFastInternalNode(-1, 0, numLevels);
      XFastLeaf previousLeaf = null;
      for (int i = 0; i < keys.size(); i++) {
        XFastLeaf leaf = new XFastLeaf(keys.get(i));
        if (i > 0) {
          leaf.setPredecessor(previousLeaf);
          previousLeaf.setSuccessor(leaf);
        }
        
        root.markInsertion(leaf);
        
        previousLeaf = leaf;
      }
      
      // Build the hash tables at each level.
      // Let the bottom level (leaves) be level 0.
      hashTables = new CuckooHashMap[numLevels];
      for (int i = 0; i < numLevels; i++) {
        hashTables[i] = new CuckooHashMap<Integer, XFastNode>();
      }
      addIntoHashTable(root, numLevels - 1);
      
      return new XFastTrie(this);
    }
    
    protected void addIntoHashTable(XFastNode node, int level) {
      hashTables[level].put(node.getKey(), node);
      if (node.getLeftChild() != null)
        addIntoHashTable(node.getLeftChild(), level - 1);
      if (node.getRightChild() != null)
        addIntoHashTable(node.getRightChild(), level - 1);
    }
    
  }
  
}
