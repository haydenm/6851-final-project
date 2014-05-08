package stringmatch.ds.text;

import java.util.ArrayList;
import java.util.List;

public class Text {

  private final List<AlphabetCharacter> text;
  
  private Text(Builder builder) {
    this.text = builder.text;
  }
  
  public Text(AlphabetCharacter ac) {
    Builder builder = new Builder();
    builder.addAlphabetCharacter(ac);
    this.text = builder.text;
  }
  
  public Text(String s, boolean endChar) {
	  Builder builder = new Builder();
	  for (char c : s.toCharArray()) {
	    builder.addAlphabetCharacter(new AlphabetCharacter(new Character(c)));
	  }
	  if (endChar) {
	    builder.addAlphabetCharacter(AlphabetCharacter.END_CHAR);
	  }
	  this.text = builder.text;
  }
  
  public List<AlphabetCharacter> getList() {
    return text;
  }
  
  public int getLength() {
    return text.size();
  }
  
  public AlphabetCharacter getCharAtIndex(int index) {
    if (index < 0 || index >= text.size())
      throw new IllegalArgumentException();
    return text.get(index);
  }
  
  public Text extractSubstring(int start, int end) {
    if (text.size() == 0 || start >= text.size() || end <= start
        || end > text.size())
      throw new IllegalArgumentException();
    
    return (new Builder()).addAlphabetCharacters(
        text.subList(start, end)).build();
  }
  
  public boolean equals(Object obj) {
    if (obj instanceof Text)
      return ((Text)obj).text.equals(text);
    else
      return false;
  }
  
  public int hashCode() {
    return text.hashCode();
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (AlphabetCharacter ac : text) {
      sb.append(ac.toString());
    }
    return sb.toString();
  }
  
  public int getSize() {
	  return text.size();
  }
  
  public static class Builder {
    private final List<AlphabetCharacter> text;
    
    public Builder() {
      text = new ArrayList<AlphabetCharacter>();
    }
    
    public Builder addAlphabetCharacter(AlphabetCharacter ac) {
      text.add(ac);
      return this;
    }
    
    public Builder addAlphabetCharacters(List<AlphabetCharacter> acs) {
      text.addAll(acs);
      return this;
    }
    
    public Text build() {
      return new Text(this);
    }
  }
  
}
