package stringmatch.ds.suffixtree;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.TextSubstring;

/*
 * Stores an edge that consists *solely* of a wildcard character.
 */
public class WildcardEdge extends Edge {

  protected SuffixTreeWithWildcards wildcardTree;
  
  public WildcardEdge(Node fromNode,
      SuffixTreeWithWildcards wildcardTree) {
    super(fromNode, null);
    this.wildcardTree = wildcardTree;
    toNode = wildcardTree.root;
  }
  
  public AlphabetCharacter getCharAt(int i) {
    return AlphabetCharacter.WILDCARD;
  }
  
  public boolean isWildcardEdge() {
    return true;
  }
  
  protected void setTextSubstring(TextSubstring textSubstring) {
    throw new RuntimeException("This is a wildcard edge! There's no textSubstring.");
  }
  
  protected TextSubstring getTextSubstring() {
    return TextSubstring.WILDCARD;
  }
  
  public String toString() {
    return AlphabetCharacter.WILDCARD.toString();
  }
  
  public Edge clone(Node fromNode) {
    throw new RuntimeException("We shouldn't be cloning a wildcard edge!");
  }
  
  @Override
  public int compareTo(Edge o) {
    return 0;
  }
  
}
