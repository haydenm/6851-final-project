package stringmatch.ds.suffixtree;

import stringmatch.ds.text.Text;

public class SuffixTreeWithWildcards extends SuffixTree {
  
  public SuffixTreeWithWildcards(Builder builder) {
    root = builder.root;
  }

  public static class Builder {
    private Text inputText;
    private Node root;
    
    public Builder(Text inputText) {
      this.inputText = inputText;
    }
    
    public SuffixTreeWithWildcards build() {
      // Build regular suffix tree.
      root = (new SuffixTree.Builder(inputText)).build().getRoot();
      
      root.findCentroidPaths();
      
      return new SuffixTreeWithWildcards(this);
    }
  }
  
}
