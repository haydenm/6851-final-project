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
	char[] alphabet = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
								  'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
	
	public SuffixTree(String s) {
		this.string = s;
		this.root = build(s, false);
		order = Utils.eulerTour(0, root);
		Utils.updateIndices(this);
		chunks = new ArrayList<Pair<Chunk, Pair<Integer, Integer>>>();
		chunkify();
		chunkTable = Utils.buildChunkTable(this);
		table = Utils.buildMap(this);
	}
	
	public Node build(String s, boolean compress) {
		s = s + "$";
		root = new Node();
		for (int i = 0; i < s.length() - 1; i++)
			insert(i, s.substring(i), root);
		if (compress) {
			collapse(root);	
		}
		return root;
	}

	private void insert(int offset, String s, Node node) {
		Node c = node;
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
	
	/*public List<Integer> queryWithoutWildcards(String p) {
		Node current = root;
		int i = 0;
		while (i < p.length() && current != null) {
			current = current.get(p.charAt(i));
			if (current != null) {
				for (int j = i; j < Math.min(current.s.length() + i, p.length()); j++) {
					if (current.s.charAt(j - i) != p.charAt(j)) {
						return null;
					}
				}
				i += current.s.length();
			}
		}
		if (current == null) {
			return new ArrayList<Integer>();
		} else {
			return current.leafIndices();
		}
	}*/
	
	public List<Integer> queryWithoutWildcards(String p) {
		return check(p, 0, root);
	}
	
	public List<Integer> check(String p, int start, Node current) {
		if (start >= p.length()) {
			return current.leafIndices();
		}
		Node c = current.get(p.charAt(start));
		if (c != null) {
			for (int j = start; j < Math.min(c.s.length() + start, p.length()); j++) {
				if (c.s.charAt(j - start) != p.charAt(j)) {
					return new ArrayList<Integer>();
				}
			}
			return check(p, start + c.s.length(), c);
		}
		return new ArrayList<Integer>();
	}
	
	public List<Integer> slowQueryWithWildcards(String p) {
		return slowCheck(p, 0, root);
	}
	
	public List<Integer> slowCheck(String p, int start, Node current) {
		List<Integer> results = new ArrayList<Integer>();
		if (start >= p.length()) {
			//System.out.println(current);
			return current.leafIndices();
		}
		Node c = current.get(p.charAt(start));
		boolean match = true;
		if (c != null) {
			for (int j = start; j < Math.min(c.s.length() + start, p.length()); j++) {
				if (c.s.charAt(j - start) != p.charAt(j) && !(p.charAt(j) == '*' && c.s.charAt(j - start) != '$')) {
					match = false;
				}
			}
			if (match) {
				System.out.println(c);
				results.addAll(slowCheck(p, start + c.s.length(), c));
			}
		}
		// Check if wildcard, if so then explore all branches
		if (p.charAt(start) == '*') {
			for (char next: alphabet) {
				match = true;
				Node nextNode = current.get(next);
				if (nextNode != null) {
					for (int j = start + 1; j < Math.min(nextNode.s.length() + start, p.length()); j++) {
						if (nextNode.s.charAt(j - start) != p.charAt(j) && !(p.charAt(j) == '*' && nextNode.s.charAt(j - start) != '$')) {
							match = false;
						}
					}
					if (match) {
						results.addAll(slowCheck(p, start + nextNode.s.length(), nextNode));
					}
				}
			}
		}
		return results;
	}
	
	public void addWildcardSubtrees(int k) {
		Node node = root;
		
	}
	
	/*public Node buildMergedTree(Node n) {
		System.out.println(n.getStrings());
		Node wild = wildcardBuild(n.getStrings());
		System.out.println(wild);
		System.out.println(wild.childs);
		return wild;
	}*/
	
	public void addAllWildcardTrees(int k) {
		addWildcardTreesBelowNode(root, k);
	}
	
	public void addWildcardTreesBelowNode(Node node, int k) {
		if (!node.isLeaf() && k > 0) {
			if (node.s != null && node.s.charAt(0) == '*') {
				k -= 1;
			}
			if (k > 0 && !(node.childs.size() == 1 && node.childs.get(0).s.charAt(0) == '$')) {
				addWildcardTree(node);
			}
			for (int i = 0; i < node.childs.size(); i++) {
				Node c = node.childs.get(i);
				addWildcardTreesBelowNode(c, k);
			}
		}
	}
	
	
	public Node addWildcardTree(Node node) {
		List<Pair<String, Integer>>strings = node.getStrings();
		for (int i = 0; i < strings.size(); i++) {
			int offset = strings.get(i).getRight();
			String s = strings.get(i).getLeft();
			s = "*" + s.substring(1, s.length());
			insert(offset, s, node);
		}
		return node;
	}

	public void printTree(Node node) {
		if (!node.isLeaf()) {
			System.out.print(node.childs);
			System.out.print("\n");
			for (Node c: node.childs) {
				//System.out.print(c.childs);
				printTree(c);
			}
		}
	}
	
	private void printTree(String prefix, Node n, boolean leaf) {
        System.out.println(prefix + (leaf ? "|-- " : "|-- ") + n);
        if (n.childs != null) {
        for (int i = 0; i < n.childs.size() - 1; i++) {
            printTree(prefix + (leaf ? "    " : "|   "), n.childs.get(i), false);
        }
        if (n.childs.size() >= 1) {
            printTree(prefix + (leaf ?"    " : "|   "), n.childs.get(n.childs.size() - 1), true);
        }
        }
    }
	

}
