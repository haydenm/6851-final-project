import java.util.ArrayList;
import java.util.List;

public class Node {

	
	public String s;
	public int index;
	public ArrayList<Node> childs;

	int offset;

	public Node() {
	};

	public Node(String content) {
		s = content;
		index = -1;
	}

	public Node extend(String ext) {
		if (childs == null)
			childs = new ArrayList<Node>();
		Node n2 = new Node(ext);
		childs.add(n2);
		return n2;
	}

	public Node extend(char charAt) {
		String s = new String(new char[] { charAt });
		return extend(s);
	}

	public boolean isLeaf() {
		return childs == null ? true : (childs.size() == 0);
	}

	public String toString() {
		if (s == null)
			return "";
		else if (isLeaf())
			return s + " " + offset;
		else
			return s;
	}

	public char toChar() {
		if (s != null && s.length() >= 1)
			return s.charAt(0);
		else
			throw new InternalError(s);
	}

	public Node follow(char charAt) {
		if (childs != null)
			for (Node n : childs)
				if (n.toChar() == charAt)
					return n;
		return extend(charAt);
	}

	public Node get(char charAt) {
		if (childs != null) {
			for (Node n : childs) {
				if (n.toChar() == charAt) {
					return n;
				}
			}
		}
		return null;
	}
	
	public List<Pair<String, Integer>> getStrings() {
		List<Pair<String, Integer>> strings = new ArrayList<Pair<String, Integer>>();
		if (childs == null || childs.size() == 0) {
			strings.add(new Pair<String, Integer>(s, offset));
		} else {
			for (Node n: childs) {
				n.getStringsHelper(strings, "");
			}
		}
		return strings;
	}
	
	public void getStringsHelper(List<Pair<String, Integer>> strings, String current) {
		if (childs == null || childs.size() == 0) {
			strings.add(new Pair<String, Integer>(current + s, offset));
		} else {
			for (Node n: childs) {
				n.getStringsHelper(strings, current + s);
			}
		}
	}
	
	public List<Integer> leafIndices() {
		List<Integer> leaves = new ArrayList<Integer>();
		if (childs == null || childs.size() == 0) {
			leaves.add(offset);
		} else {
			for (Node n: childs) {
				n.getDescendants(leaves);
			}
		}
		return leaves;
	}
	
	public void getDescendants(List<Integer> leaves) {
		if (childs == null || childs.size() == 0) {
			leaves.add(offset);
		} else {
			for (Node n: childs) {
				n.getDescendants(leaves);
			}
		}
	}

	public boolean hasOneChild() {
		return childs != null && childs.size() == 1;
	}

	public Node onlyChild() {
		if (childs == null || childs.size() > 1)
			throw new InternalError("Childs: " + childs);
		return childs.get(0);
	}
}