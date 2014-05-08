package stringmatch.ds.suffixtree;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.TextSubstring;

public class Edge implements Comparable<Edge> {

  protected Node fromNode;
  protected Node toNode;
  
  protected TextSubstring textSubstring;
  
  /* Used for building! */
  private int textStart;
  private boolean textEndsAtTreeEnd;
  private SuffixTree.Builder treeBuilder;
  /* Used for building! */
    
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
  
  // Why do we need this?
  /*protected Edge(Node fromNode, int textStart, TextSubstring textSubstring) {
	    this.fromNode = fromNode;
	    toNode = null;
	    this.textStart = textStart;
	    textEndsAtTreeEnd = false;
	    treeBuilder = null;
	    this.textSubstring = textSubstring;
  }*/
  
  protected void setToNode(Node node) {
    toNode = node;
  }
 
  public boolean isWildcardEdge() {
    return false;
    // it shouldn't be! if it is, this call should be overriden in WildcardEdge
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
  
  public void fixTextSubstringAfterBuild() {
    if (textEndsAtTreeEnd) {
      textSubstring = getTextSubstring();
      textEndsAtTreeEnd = false;
    }
  }
  
  protected Node getToNode() {
    return toNode;
  }
  
  protected Node getFromNode() {
    return fromNode;
  }
  
  protected int getLength() {
    return textSubstring.length;
  }
  
  public AlphabetCharacter getCharAt(int i) {
		return getTextSubstring().getIthChar(i);
  }
  
  public String toString() {
	  return getTextSubstring().getSubstringAsText().toString();
  }

  public int getTextStart() {
    return getTextSubstring().getStartIndex();
  }
  
  public Edge clone(Node fromNode) {
    Edge copy = new Edge(fromNode, textSubstring.clone());
    copy.toNode = toNode.clone(false, copy);
    return copy;
  }

  @Override
  public int compareTo(Edge o) {
    return getTextSubstring().compareTo(o.getTextSubstring());
  }
  
}
