package stringmatch.ds.suffixtree;

import java.util.HashMap;
import java.util.Map;

import stringmatch.ds.util.Pair;

public class Path {
  
  protected Map<Integer, Node> nodes;
  protected Pair<Integer, Node> last;
  protected Pair<Integer, Node> first;
  
  protected Path() {
    nodes = new HashMap<Integer, Node>();
  }
  
  protected int addNode(Node node) {
    if (last == null) {
      nodes.put(node.maxHeight, node);
      Pair<Integer, Node> p = new Pair<Integer, Node>(node.maxHeight, node);
      first = p;
      last = p;
      return node.maxHeight;
    } else if (node.incomingEdge.getFromNode() == last.getRight()) {
      int height = last.getLeft() - node.incomingEdge.getLength();
      nodes.put(height, node);
      last = new Pair<Integer, Node>(height, node);
      return height;
    } else {
      throw new RuntimeException("Appending node to path which doesn't follow");
    }
  }
  
  protected int prependNode(Node node) {
    if (first == null) {
      nodes.put(node.maxHeight, node);
      Pair<Integer, Node> p = new Pair<Integer, Node>(node.maxHeight, node);
      first = p;
      return node.maxHeight;
    } else if (first.getRight().incomingEdge.getFromNode() == node) {
      int height = first.getLeft() + first.getRight().incomingEdge.getLength();
      nodes.put(height, node);
      first = new Pair<Integer, Node>(height, node);
      return height;
    } else {
      throw new RuntimeException("Prepending node to path which doesn't follow");
    }
    
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("PATH: ");
    for(int i: nodes.keySet()) {
      sb.append("[" + i + ", " + nodes.get(i).toString() + " ]");
    }
    return sb.toString();
  }
  
  public int getLength() {
    return first.getLeft();
  }
}
