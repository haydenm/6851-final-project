package stringmatch.ds.yfasttrie;

public class XFastInternalNode extends XFastNode {
  
  /*
   * An internal node may have just one of the below:
   * (a) both left and right child, but no descendant pointer
   * (b) a left child and a right descendant pointer
   * (c) a right child and a left descendant pointer
   * 
   * If a node does not have a particular child or descendant pointer,
   * it is stored as null.
   */
  protected XFastNode leftChild;
  protected XFastNode rightChild;
  protected XFastLeaf leftDescendantPointer;
  protected XFastLeaf rightDescendantPointer;
  
  protected int prefixLength;
  protected int treeNumLevels;
    
  public XFastInternalNode(int prefix, int prefixLength, int treeNumLevels) {
    super(prefix);
    leftChild = null;
    rightChild = null;
    leftDescendantPointer = null;
    rightDescendantPointer = null;
    this.prefixLength = prefixLength;
    this.treeNumLevels = treeNumLevels;
  }
  
  public boolean isRoot() {
    return prefixLength == 0;
  }
  
  @Override
  public XFastNode getLeftChild() {
    return leftChild;
  }
  
  @Override
  public XFastNode getRightChild() {
    return rightChild;
  }
  
  public XFastLeaf getLeftDescendantPointer() {
    return leftDescendantPointer;
  }
  
  public XFastLeaf getRightDescendantPointer() {
    return rightDescendantPointer;
  }
  
  public void setLeftChild(XFastNode node) {
    if (leftDescendantPointer != null)
      throw new RuntimeException("Already has left descendant pointer.");
    
    leftChild = node;
  }
  
  public void setRightChild(XFastNode node) {
    if (rightDescendantPointer != null)
      throw new RuntimeException("Already has right descendant pointer.");
    
    rightChild = node;
  }
  
  public void setLeftDescendantPointer(XFastLeaf leaf) {
    if (leftChild != null)
      throw new RuntimeException("Already has left child.");
    
    leftDescendantPointer = leaf;
  }
  
  public void setRightDescendantPointer(XFastLeaf leaf) {
    if (rightChild != null)
      throw new RuntimeException("Already has right child.");
    
    rightDescendantPointer = leaf;
  }
  
  /*
   * Returns the prefix of key of length prefixLength.
   * Specifically, returns the bits of key consisting of
   * the (treeNumLevels - 1)th bit, the (treeNumLevels - 2)th bit,
   * down to the (treeNumLevels - prefixLength)th bit.
   * (Note: the least significant bit is bit 1.)
   */
  public static int prefixOfKey(int key, int prefixLength, int treeNumLevels) {
    // All bits above the (treeNumLevels - 1)th bit should be 0 assuming
    // key is within the universe size. So we can just shift key right
    // by (treeNumLevels - prefixLength - 1) bits.
    return key >> (treeNumLevels - prefixLength - 1);
  }
  
  protected int leftChildPrefix() {
    if (isRoot())
      return 0;
    else
      return key << 1;
  }
  
  protected int rightChildPrefix() {
    if (isRoot())
      return 1;
    else
      return (key << 1) + 1;
  }
  
  /*
   * Recursively marks the insertion of leaf into the subtree rooted at this.
   */
  @Override
  public void markInsertion(XFastLeaf leaf) {
    int prefixLengthOfNextLevel = prefixLength + 1;
    int leafPrefixOnNextLevel = prefixOfKey(leaf.getKey(),
        prefixLengthOfNextLevel, treeNumLevels);
    
    if (leafPrefixOnNextLevel == leftChildPrefix()) {
      XFastNode l = markInsertionIntoLeftSubtree(leaf);
      l.markInsertion(leaf);
    } else if (leafPrefixOnNextLevel == rightChildPrefix()) {
      XFastNode r = markInsertionIntoRightSubtree(leaf);
      r.markInsertion(leaf);
    } else {
      throw new RuntimeException("Something's not right..");
    }
  }
  
  /*
   * Updates the right descendant pointer if needed.
   * If this node already has a left subtree it returns the left child node.
   * If this node does not have a left subtree it creates a left child node and
   * returns it.
   */
  protected XFastNode markInsertionIntoLeftSubtree(XFastLeaf leaf) {
    if (rightChild == null && rightDescendantPointer == null) {
      // Add a right descendant pointer.
      setRightDescendantPointer(leaf);
    }
    if (rightDescendantPointer != null) {
      // The pointer should point to the largest leaf in the left subtree.
      if (leaf.getKey() > rightDescendantPointer.getKey())
        setRightDescendantPointer(leaf);
    }
    
    if (leftChild != null) {
      return leftChild;
    } else {
      // Delete any left descendant pointer.
      leftDescendantPointer = null;
      
      // Make a new left child.
      
      if (prefixLength + 1 == treeNumLevels - 1) {
        // The left child is the leaf. So rather than creating a new left
        // child, update this node's left child to the leaf and return it.
        setLeftChild(leaf);
        return leaf;
      } else {
        // Make a new left child.
        XFastInternalNode l = new XFastInternalNode(leftChildPrefix(),
            prefixLength + 1, treeNumLevels);
        setLeftChild(l);
        return l;
      }
    }
  }
  
  /*
   * Updates the left descendant pointer if needed.
   * If this node already has a right subtree it returns the right child node.
   * If this node does not have a right subtree it creates a right child node and
   * returns it.
   */
  protected XFastNode markInsertionIntoRightSubtree(XFastLeaf leaf) {
    if (leftChild == null && leftDescendantPointer == null) {
      // Add a left descendant pointer.
      setLeftDescendantPointer(leaf);
    }
    if (leftDescendantPointer != null) {
      // The pointer should point to the smallest leaf in the right subtree.
      if (leaf.getKey() < leftDescendantPointer.getKey())
        setLeftDescendantPointer(leaf);
    }
    
    if (rightChild != null) {
      return rightChild;
    } else {
      // Delete any right descendant pointer.
      rightDescendantPointer = null;
      
      // Make a new right child.
      
      if (prefixLength + 1 == treeNumLevels - 1) {
        // The right child is the leaf. So rather than creating a new right
        // child, update this node's right child to the leaf and return it.
        setRightChild(leaf);
        return leaf;
      } else {
        // Make a new right child.
        XFastInternalNode r = new XFastInternalNode(rightChildPrefix(),
            prefixLength + 1, treeNumLevels);
        setRightChild(r);
        return r;
      }
    }
  }
  
  @Override
  public void verifyPointers() {
    if (leftChild != null && rightChild != null &&
        leftDescendantPointer == null && rightDescendantPointer == null)
      return;
    
    if (leftChild != null && rightChild == null &&
        leftDescendantPointer == null && rightDescendantPointer != null)
      return;
    
    if (leftChild == null && rightChild != null &&
        leftDescendantPointer != null && rightDescendantPointer == null)
      return;
    
    throw new RuntimeException("Node pointers are incorrect.");
  }

  @Override
  public boolean isLeaf() {
    return false;
  }
  
}
