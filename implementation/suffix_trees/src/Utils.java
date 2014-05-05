import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Utils {

	public static List<Pair<Integer, Node>> eulerTour(int depth, Node start) {
		Pair<Integer, Node> pair = new Pair<Integer, Node>(depth, start);
		List<Pair<Integer, Node>> order = new ArrayList<Pair<Integer, Node>>();
		order.add(pair);
		if (!start.isLeaf()) {
			for (int i = 0; i < start.childs.size(); i++) {
				List<Pair<Integer, Node>> l = eulerTour(depth + 1, start.childs.get(i));
				order.addAll(l);
				order.add(pair);
			}
		}
		return order;
	}
	
	public static void updateIndices(SuffixTree suffix) {
		for (int i = 0; i < suffix.order.size(); i++) {
			if (suffix.order.get(i).getRight().index == -1) {
				suffix.order.get(i).getRight().index = i;
			}
		}
	}
	
	public static Map<Integer, Map<Integer, Integer>> buildMap(SuffixTree suffix) {
		Map<Integer, Map<Integer, Integer>> table = new HashMap<Integer, Map<Integer, Integer>>();
		
		for (int index = 0; index < suffix.chunks.size(); index++) {
			Map<Integer, Integer> inner = new HashMap<Integer, Integer>();
			for (int i = 1; i < log2(suffix.chunks.size()); i++) {
				int end = Math.min(suffix.chunks.size() - 1, (int) Math.pow(2, i) + index);
				inner.put((int) Math.pow(2, i), findMin(index, end, suffix.chunks));
			}
			table.put(index, inner);
		}
		return table;
	}
	
	public static int findMin(List<Pair<Integer, Node>> order, int start, int end) {
		int min = order.get(start).getLeft();
		int index = start;
		for (int i = start + 1; i <= end; i++) {
			int val = order.get(i).getLeft();
			if (val < min) {
				min = val;
				index = i;
			}
		}
		return index;
	}
	
	public static int findMin(int start, int end, List<Pair<Chunk, Pair<Integer, Integer>>> order) {
		int min = order.get(start).getRight().getRight();
		int index = start;
		for (int i = start + 1; i <= end; i++) {
			int val = order.get(i).getRight().getRight();
			if (val < min) {
				min = val;
				index = i;
			}
		}
		return index;
	}
	
	public static int RMQTopLevel(SuffixTree suffix, int i, int j) {
		int width = (int) Math.pow(2, Math.floor(log2(j - i)));
		int min1;
		int min2;
		if (width <= 1) {
			min1 = i;
			min2 = j;
		} else {
			min1 = suffix.table.get(i).get(width);
			min2 = suffix.table.get(j - width).get(width);
		}
		if (suffix.chunks.get(min1).getRight().getRight() <= suffix.chunks.get(min2).getRight().getRight()) {
			return min1;
		} else {
			return min2;
		}
	}
	
	public static int RMQ(SuffixTree suffix, int i, int j) {
		int size = (int) Math.floor(Utils.log2(suffix.order.size()) / 2);
		
		// Get the minimum of all the middle chunks
		int left_chunk = (int) Math.floor(i / size);
		int right_chunk = (int) Math.floor(j / size);
		int minChunkIndex = RMQTopLevel(suffix, left_chunk + 1, right_chunk - 1);
		int topIndex = minChunkIndex * size + suffix.chunks.get(minChunkIndex).getRight().getLeft();
		int topValue = suffix.chunks.get(minChunkIndex).getRight().getRight();
		
		// Get the minimum of the left chunk
		Chunk left = suffix.chunks.get(left_chunk).getLeft();
		Pair<Integer, Integer> leftMin = suffix.chunkTable.get(left.type.substring((i % size), left.type.length()));
		int leftIndex = leftMin.getLeft() + i;
		int leftValue = leftMin.getRight() + left.getValueAt(leftMin.getLeft() + (i % size));
		
		// Get the minimum of the right chunk
		Chunk right = suffix.chunks.get(right_chunk).getLeft();
		Pair<Integer, Integer> rightMin = suffix.chunkTable.get(right.type.substring(0, (j % size)));
		int rightIndex = rightMin.getLeft() + j - (j % size);
		int rightValue = rightMin.getRight() + right.start;
		
		// Return the smallest
		int overallMin = leftValue;
		int minIndex = leftIndex;
		if (topValue < overallMin) {
			overallMin = topValue;
			minIndex = topIndex;
		}
		if (rightValue < overallMin) {
			overallMin = rightValue;
			minIndex = rightIndex;
		}
		return minIndex;
	}

	public static Node LCA(SuffixTree suffix, Node n1, Node n2) {
		int min_index = RMQ(suffix, n1.index, n2.index);
		return suffix.order.get(min_index).getRight();
	}
	
	public static double log2(int x) {
		return Math.log(x) / Math.log(2);
	}
	
	public static Map<String, Pair<Integer, Integer>> buildChunkTable(SuffixTree suffix) {
		Map<String, Pair<Integer, Integer>> chunkTable = new HashMap<String, Pair<Integer, Integer>>();
		int size = (int) Math.floor(Utils.log2(suffix.order.size()) / 2);
		for (int i = 0; i < (int) Math.pow(2, size); i++) {
			String s = Integer.toBinaryString(i);
			while (s.length() < size) {
				int cumulative = 0;
				int min = 0;
				int min_index = 0;
				for (int j = 0; j < s.length(); j++) {
					if (s.charAt(j) == '1') {
						cumulative += 1;
					} else {
						cumulative -= 1;
					}
					if (cumulative < min) {
						min = cumulative;
						min_index = j + 1;
					}
				}
				chunkTable.put(s, new Pair<Integer, Integer>(min_index, min));
				s = "0" + s;
			}
			int cumulative = 0;
			int min = 0;
			int min_index = 0;
			for (int j = 0; j < s.length(); j++) {
				if (s.charAt(j) == '1') {
					cumulative += 1;
				} else {
					cumulative -= 1;
				}
				if (cumulative < min) {
					min = cumulative;
					min_index = j + 1;
				}
			}
			chunkTable.put(s, new Pair<Integer, Integer>(min_index, min));
		}
		chunkTable.put("", new Pair<Integer, Integer>(0, 0));
		return chunkTable;
	}
	
}
