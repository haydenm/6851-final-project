package stringmatch.ds.util;

import stringmatch.ds.text.TextSubstring;

public class Pair<L, R> {

    private L left;
    private R right;


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
    
    public boolean equals(Object obj) {
      if (obj instanceof Pair) {
        Pair<L, R> pair = (Pair<L, R>) obj;
        return this.left.equals(pair.getLeft()) && this.right.equals(pair.getRight());
      } else {
        return false;
      }
    }

}