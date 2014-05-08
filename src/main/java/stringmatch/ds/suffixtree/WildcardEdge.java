package stringmatch.ds.suffixtree;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.TextSubstring;

/*
 * Stores an edge that consists *solely* of a wildcard character.
 */
public class WildcardEdge extends Edge {

  public WildcardEdge(Node fromNode) {
    super(fromNode, null);
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
    Edge copy = new WildcardEdge(fromNode);
    copy.toNode = toNode.clone(false, copy);
    return copy;
  }
  
  @Override
  public int compareTo(Edge o) {
    return 0;
  }
  
}
