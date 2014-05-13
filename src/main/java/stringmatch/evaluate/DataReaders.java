package stringmatch.evaluate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import stringmatch.ds.text.AlphabetCharacter;
import stringmatch.ds.text.Text;
import stringmatch.ds.text.Text.Builder;

public class DataReaders {

  // fn is filename.
  // n is max number of characters to read.
  public static Text readFile(String fn, int n) {
    Text.Builder tb = new Text.Builder();
    
    try {
      BufferedReader br = new BufferedReader(new FileReader(fn));
      String line = br.readLine();
      
      // Skip line 1 (it just says the chr number in the genome, but
      // skip it for any other files too).
      br.readLine();
      
      while (line != null) {
        line = cleanString(line);
        if (line.length() > 0) {
          tb.addString(line);
        }
        
        if (tb.getCurrentSize() > n)
          break;
        
        line = br.readLine();
      }
      
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    tb.cutToSize(n-1);
    tb.addAlphabetCharacter(AlphabetCharacter.END_CHAR);
    
    return tb.build();
  }
  
  protected static String cleanString(String s) {
    s = s.replaceAll("[^a-zA-Z]", ""); // Delete all non-alphabet characters.
    s = s.toUpperCase(); // Make all uppercase.
    /*
    s = s.replaceAll("\\$", ""); // Delete '$'.
    s = s.replaceAll("\\*", ""); // Delete '*'.
    s = s.trim(); // Remove leading and trailing whitespace.
    s = s.replaceAll(" +", " "); // Replace multiple spaces with one.
    */
    return s;
  }
  
}
