package stringmatch.ds.suffixtree;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.TextSubstring;

public class Edge implements Comparable<Edge> {

  private Node fromNode;
  private Node toNode;
  
  private int textStart;
  private boolean textEndsAtTreeEnd;
  private SuffixTree.Builder treeBuilder;
  private TextSubstring textSubstring;
  boolean wildcard;
    
  protected Edge(Node fromNode, TextSubstring textSubstring) {
    this.fromNode = fromNode;
    toNode = null;
    textEndsAtTreeEnd = false;
    treeBuilder = null;
    this.textSubstring = textSubstring;
    wildcard = false;
  }
  
  protected Edge(Node fromNode, int textStart,
      SuffixTree.Builder treeBuilder) {
    this.fromNode = fromNode;
    toNode = null;
    this.textStart = textStart;
    textEndsAtTreeEnd = true;
    this.treeBuilder = treeBuilder;
    textSubstring = null;
    wildcard = false;
  }
  
  protected Edge(Node fromNode, int textStart, TextSubstring textSubstring, boolean wildcard) {
	    this.fromNode = fromNode;
	    toNode = null;
	    this.textStart = textStart;
	    textEndsAtTreeEnd = false;
	    treeBuilder = null;
	    this.textSubstring = textSubstring;
	    this.wildcard = wildcard;
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
  
  protected Node getFromNode() {
    return fromNode;
  }
  
  protected int getLength() {
    return textSubstring.length;
  }
  
  public AlphabetCharacter getCharAt(int i) {
	  if (wildcard && i == 0) {
		  return AlphabetCharacter.WILDCARD;
	  } else {
		  return getTextSubstring().getIthChar(i);
	  }
  }
  
  public String toString() {
	  if (wildcard) {
		  String original = getTextSubstring().getSubstringAsText().toString();
		  return "*" + original.substring(1, original.length());
	  }
	  return getTextSubstring().getSubstringAsText().toString();
  }

public int getTextStart() {
	return textStart;
}


  @Override
  public int compareTo(Edge o) {
    return getTextSubstring().compareTo(o.getTextSubstring());
  }
  
}
