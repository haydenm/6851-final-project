package stringmatch.ds.suffixtree;

import stringmatch.ds.text.TextSubstring;

public class Edge implements Comparable<Edge> {

  private Node fromNode;
  private Node toNode;
  
  private int textStart;
  private boolean textEndsAtTreeEnd;
  private SuffixTree.Builder treeBuilder;
  private TextSubstring textSubstring;
    
  protected Edge(Node fromNode, TextSubstring textSubstring) {
    this.fromNode = fromNode;
    toNode = null;
    textEndsAtTreeEnd = false;
    treeBuilder = null;
    this.textSubstring = textSubstring;
  }
  
  protected Edge(Node fromNode, int textStart,
      SuffixTree.Builder treeBuilder) {
    this.fromNode = fromNode;
    toNode = null;
    this.textStart = textStart;
    textEndsAtTreeEnd = true;
    this.treeBuilder = treeBuilder;
    textSubstring = null;
  }
  
  protected void setToNode(Node node) {
    toNode = node;
  }
  
  protected void setTextSubstring(TextSubstring textSubstring) {
    this.textSubstring = textSubstring;
    textStart = textSubstring.getStartIndex();
    textEndsAtTreeEnd = false;
  }
  
  protected TextSubstring getTextSubstring() {
    if (!textEndsAtTreeEnd)
      return textSubstring;
    
    int substringLength = treeBuilder.getEndPosition() - textStart;
    TextSubstring substr = new TextSubstring(treeBuilder.getInputText(),
        textStart, substringLength);
    return substr;
  }
  
  protected Node getToNode() {
    return toNode;
  }
  
  public String toString() {
	  return getTextSubstring().getSubstringAsText().toString();
  }

  @Override
  public int compareTo(Edge o) {
    return getTextSubstring().compareTo(o.getTextSubstring());
  }
  
}
