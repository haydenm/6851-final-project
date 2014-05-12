package stringmatch.ds.suffixtree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import stringmatch.ds.util.Pair;
import stringmatch.ds.yfasttrie.YFastTrie;

/*
 * Represents a path within the tree
 */
public class Path {

  // Invariants: order always consists of the keys of nodes. Once the y-fast trie is constructed,
  // no further additions can be made to the path, and the y-fast trie will consist of all the
  // keys in nodes.
  protected Map<Integer, Node> nodes;
  protected LinkedList<Integer> order;
  protected YFastTrie<Object> steps;
  
  protected Path() {
    nodes = new HashMap<Integer, Node>();
    order = new LinkedList<Integer>();
  }
  
  /*
   * Add a node to the end of the path. This node must be a child of the previous end node in the path.
   */
  protected int addNode(Node node) {
    System.out.println(nodes);
    System.out.println(order);
    System.out.println(node);
    if (order.size() == 0) {
      nodes.put(node.maxHeight, node);
      order.addLast(node.maxHeight);
      return node.maxHeight;
    } else {
      int lastHeight = order.peekLast();
      Node last = nodes.get(lastHeight);
      System.out.println(node.incomingEdge.getFromNode());
      System.out.println(last);
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
  
  /*
   * Add a node to the beginning of the path. This node must have the previous beginning of the path
   * as a child.
   */
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
  
  /*
   * Build the y-fast tree of the keys in nodes.
   */
  public void buildYFastTrie() {
    List<Pair<Integer, Object>> keys = new ArrayList<Pair<Integer, Object>>();
    Iterator<Integer> i = order.descendingIterator();
    Object o = new Object();
    while (i.hasNext()) {
      keys.add(new Pair<Integer, Object>(i.next(), o));
    }
    YFastTrie.Builder<Object> builder = new YFastTrie.Builder<Object>();
    steps = builder.buildFromPairs(keys);
  }
  
  // The length of the path is the max height of the first node
  public int getLength() {
    return order.peekFirst();
  }
  
  /*
   * Jump from starting position start in the path to position query. Return a pair. The first element
   * is the node above (or equal to) the resulting position. The second element is the amount that the
   * query position lies below the node.
   */
  public Pair<Node, Integer> jump(int start, int query) {
    int goal = start + query;
    if (nodes.containsKey(goal)) {
      return new Pair<Node, Integer>(nodes.get(goal), 0);
    } else {
      Integer succ = steps.successor(goal).getLeft();
      if (succ == null) {
        throw new RuntimeException("A jump query in a ladder exceeded the height of the ladder");
      } else {
        Node next = nodes.get(succ);
        int offset = succ - goal;
        return new Pair<Node, Integer>(next, offset);
      }
    }
  }
}
