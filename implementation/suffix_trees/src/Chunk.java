import java.util.List;


public class Chunk {

	public int start;
	public String type;
	
	public Chunk(List<Pair<Integer, Node>> order) {
		start = order.get(0).getLeft();
		StringBuilder s = new StringBuilder("");
		for (int i = 1; i < order.size(); i++) {
			if (order.get(i).getLeft() > order.get(i - 1).getLeft()) {
				s.append("1");
			} else {
				s.append("0");
			}
		}
		type = s.toString();
	}
	
	public String toString() {
		return "[" + start + "] " + type;
	}
	
	public int getValueAt(int index) {
		int val = start;
		for (int i = 0; i < index; i++) {
			if (type.charAt(i) == '1') {
				val += 1;
			} else {
				val -= 1;
			}
		}
		return val;
	}
	
}
