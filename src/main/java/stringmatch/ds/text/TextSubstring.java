package stringmatch.ds.text;

public class TextSubstring {

  public Text text;
  public int start;
  public int length;
  
  public TextSubstring(Text text, int start, int length) {
    if (start < 0 || start + length > text.getLength())
      throw new IllegalArgumentException();
    
    this.text = text;
    this.start = start;
    this.length = length;
  }
  
  public String toString() {
	  return getSubstringAsText().toString();
  }
  
  public Text getSubstringAsText() {
    return text.extractSubstring(start, start + length);
  }
  
  public AlphabetCharacter getFirstChar() {
    return text.getCharAtIndex(start);
  }
  
  public AlphabetCharacter getLastChar() {
    int index = start + length - 1;
    return text.getCharAtIndex(index);
  }
  
  public AlphabetCharacter getIthChar(int i) {
    return text.getCharAtIndex(start + i);
  }
  
  public int getStartIndex() {
    return start;
  }
  
  public int getEndIndex() {
    return start + length;
  }
  
  public int getLength() {
    return length;
  }
  
  public Text getText() {
		return text;
  }
  
  // Returns a concatenation of two TextSubstrings.
  // Because we work with a compressed trie, they don't necessarily need
  // to be adjacent! (the strings themselves are, but not necessarily the indices)
  // In terms of strings, the string represented by this must be immediately
  // before the string represented by other.
  public TextSubstring mergeWith(TextSubstring other) {
    int mergedLength = length + other.length;
    int mergedStart = other.getStartIndex() - length;
    return new TextSubstring(text, mergedStart, mergedLength);
  }
  
  public boolean equals(Object obj) {
    if (obj instanceof TextSubstring) {
      TextSubstring ts = (TextSubstring)obj;
      // Only compare *pointer* to text, rather than the actual contents!
      return (ts.text == text && ts.start == start && ts.length == length);
    } else {
      return false;
    }
  }
  
  public int hashCode() {
    // Forget the text; just compare the indices.
    return (start + 31*length);
  }

  
}
