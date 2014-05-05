import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SuffixTree {

	
	public String string;
	public Node root;
	public List<Pair<Integer, Node>> order;
	public Map<Integer, Map<Integer, Integer>> table;
	public List<Pair<Chunk, Pair<Integer, Integer>>> chunks;
	Map<String, Pair<Integer, Integer>> chunkTable;
	
	public SuffixTree(String s) {
		this.string = s;
		this.root = build(s);
		order = Utils.eulerTour(0, root);
		Utils.updateIndices(this);
		chunks = new ArrayList<Pair<Chunk, Pair<Integer, Integer>>>();
		chunkify();
		chunkTable = Utils.buildChunkTable(this);
		table = Utils.buildMap(this);
	}
	
	public Node build(String s) {
		s = s + "$";
		root = new Node();
		for (int i = 0; i < s.length() - 1; i++)
			insert(i, s.substring(i));
		collapse(root);
		return root;
	}

	private void insert(int offset, String s) {
		Node c = root;
		for (int p = 0; p < s.length(); p++) {
			c = c.follow(s.charAt(p));
			c.offset = offset;
		}
	}
	
	private void collapse(Node n) {
		if (n.hasOneChild()) {
			Node c = n.onlyChild();
			n.childs.remove(c);
			while (c.hasOneChild()) {
					n.s += c.s;
					n.offset = c.offset;
					c = c.onlyChild();
			}
			if (c.isLeaf()) {
				n.s += c.s;
				n.offset = c.offset;
			} else {
				n.s += c.s;
				n.offset = c.offset;
				n.childs = c.childs;
				collapse(c);
			}
		} else {
			if (!n.isLeaf())
				for (Node c : n.childs)
					collapse(c);
		}
	}
	
	public void chunkify() {
		int size = (int) Math.floor(Utils.log2(order.size()) / 2);
		for (int i = 0; i < order.size(); i++) {
			if ((i % size) == 0) {
				if (i + size < order.size()) {
					Chunk c = new Chunk(order.subList(i, i + size));
					int min_index = Utils.findMin(order, i, i + size - 1) - i;
					int min = c.getValueAt(min_index);
					chunks.add(
							new Pair<Chunk, Pair<Integer, Integer>>(c, new Pair<Integer, Integer>(min_index, min)));
				} else {
					Chunk c = new Chunk(order.subList(i, order.size()));
					int min_index = Utils.findMin(order, i, order.size() - 1) - i;
					int min = c.getValueAt(min_index);
					chunks.add(
							new Pair<Chunk, Pair<Integer, Integer>>(c, new Pair<Integer, Integer>(min_index, min)));
				}
			}
		}
	}
	
	
	public static void main(String[] args) {
		SuffixTree s = new SuffixTree("BANANA");
		//printList(eulerTour(0, s.root));
		//buildMap(s.order);
		//System.out.println(Utils.RMQ(s, 5, 17));
		System.out.println(s.root.childs.get(1).childs.get(0).childs.get(0));
		System.out.println(s.root.childs.get(1).childs.get(1));
		System.out.println(Utils.LCA(s, s.root.childs.get(1).childs.get(0).childs.get(0), s.root.childs.get(1).childs.get(1)));
		//Utils.buildChunkTable(s);
		//System.out.println(s.order);
		//s.Chunkify();
		//Utils.buildChunkTable(s);
	}
}
