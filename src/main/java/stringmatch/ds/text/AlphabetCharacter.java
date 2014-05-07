package stringmatch.ds.text;

public class AlphabetCharacter {

  public static final AlphabetCharacter END_CHAR
    = new AlphabetCharacter(new Character('$'));
  public static final AlphabetCharacter WILDCARD
    = new AlphabetCharacter(new Character('*'));
  
  private final Character character;
  
  public AlphabetCharacter(Character character) {
    this.character = character;
  }
  
  public boolean equals(Object obj) {
    if (obj instanceof AlphabetCharacter)
      return (((AlphabetCharacter) obj).character.equals(character));
    else
      return false;
  }
  
  public int hashCode() {
    return character.hashCode();
  }
  
  public String toString() {
    return character.toString();
  }
  
}