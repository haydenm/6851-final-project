package stringmatch.ds.suffixtree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import stringmatch.ds.util.Pair;
import stringmatch.ds.yfasttrie.YFastTrie;

public class Path {
  
  protected Map<Integer, Node> nodes;
  protected LinkedList<Integer> order;
  protected YFastTrie steps;
  
  protected Path() {
    nodes = new HashMap<Integer, Node>();
    order = new LinkedList<Integer>();
  }
  
  protected int addNode(Node node) {
    if (order.size() == 0) {
      nodes.put(node.maxHeight, node);
      order.addLast(node.maxHeight);
      return node.maxHeight;
    } else {
      int lastHeight = order.peekLast();
      Node last = nodes.get(lastHeight);
      if (node.incomingEdge.getFromNode() == last) {
        int height = lastHeight - node.incomingEdge.getLength();
        nodes.put(height, node);
        order.addLast(height);
        return height;
      } else {
        throw new RuntimeException("Appending node to path which doesn't follow");
      }
    }
  }
  
  protected int prependNode(Node node) {
    if (order.size() == 0) {
      nodes.put(node.maxHeight, node);
      order.addFirst(node.maxHeight);
      return node.maxHeight;
    } else {
      int firstHeight = order.peekFirst();
      Node first = nodes.get(firstHeight);
      if (first.incomingEdge.getFromNode() == node) {
        int height = firstHeight + first.incomingEdge.getLength();
        nodes.put(height, node);
        order.addFirst(height);
        return height;
      } else {
        throw new RuntimeException("Prepending node to path which doesn't follow");
      }
    }
    
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("PATH: ");
    for(int i: nodes.keySet()) {
      sb.append("[" + i + ", " + nodes.get(i).toString() + " ]");
    }
    return sb.toString();
  }
  
  public void buildYFastTrie() {
    List<Integer> keys = new ArrayList<Integer>();
    Iterator<Integer> i = order.descendingIterator();
    while (i.hasNext()) {
      keys.add(i.next());
    }
    YFastTrie.Builder builder = new YFastTrie.Builder();
    steps = builder.buildFromKeys(keys);
  }
  
  public int getLength() {
    return order.peekFirst();
  }
  
  public Pair<Node, Integer> jump(int start, int query) {
    int goal = start + query;
    if (nodes.containsKey(goal)) {
      return new Pair<Node, Integer>(nodes.get(goal), 0);
    } else {
      Integer succ = steps.successor(goal);
      if (succ == null) {
        return null;
        // return the root, or maybe this means our ladders are wrong?
      } else {
        Node next = nodes.get(succ);
        int offset = succ - goal;
        return new Pair<Node, Integer>(next, offset);
      }
    }
  }
}
