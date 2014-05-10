package stringmatch.ds.yfasttrie;

public class XFastLeaf extends XFastNode {

  protected XFastLeaf predecessor;
  protected XFastLeaf successor;
  
  public XFastLeaf(int key) {
    super(key);
  }
  
  public void setPredecessor(XFastLeaf leaf) {
    predecessor = leaf;
  }
  
  public void setSuccessor(XFastLeaf leaf) {
    successor = leaf;
  }
  
  public XFastLeaf getPredecessor() {
    return predecessor;
  }
  
  public XFastLeaf getSuccessor() {
    return successor;
  }

  @Override
  public void markInsertion(XFastLeaf leaf) {
    return;
  }

  @Override
  public XFastNode getLeftChild() {
    return null;
  }

  @Override
  public XFastNode getRightChild() {
    return null;
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public void verifyPointers() {
    return;
  }
  
}
