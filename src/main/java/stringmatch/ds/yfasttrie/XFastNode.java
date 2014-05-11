package stringmatch.ds.yfasttrie;

public abstract class XFastNode {

  // In a leaf, the key is the actual key.
  // In an internal node, the key represents a prefix.
  protected int key;
    
  protected XFastNode(int key) {
    this.key = key;
  }
  
  public int getKey() {
    return key;
  }
  
  public abstract boolean isLeaf();
  
  public abstract void markInsertion(XFastLeaf leaf);
  
  public abstract XFastNode getLeftChild();
  public abstract XFastNode getRightChild();
  
  public abstract void verifyPointers();
  
}
