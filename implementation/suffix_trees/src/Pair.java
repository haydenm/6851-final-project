public class Pair<L, R> {

    public L left;
    public R right;


    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }
    
    public String toString() {
    	return "(" + left.toString() + ", " + right.toString() + ")";
    }

}