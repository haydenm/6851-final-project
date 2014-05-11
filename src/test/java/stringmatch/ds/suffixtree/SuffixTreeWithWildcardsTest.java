package stringmatch.ds.suffixtree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import stringmatch.ds.text.Text;

public class SuffixTreeWithWildcardsTest {

  // Generates all suffixes, excluding '$', with <= k wildcards.
  // Does not put a wildcard in the spot of '$'.
  public static List<String> generateAllSuffixesWithKWildcards(Text t, int k) {
    List<String> suffixes = new ArrayList<String>();
    for (String suffix : SuffixTreeTest.generateAllSuffixesOfText(t)) {
      suffixes.add(suffix);
      for (int j = 1; j <= k; j++) {
        // Find all subsets of k indices between 0 and the '$' at the end (but not
        // including the '$').
        Set<Set<Integer>> setOfIndicesToReplace = makeAllSubsets(0, suffix.length() - 2, j);
        for (Set<Integer> indicesToReplace : setOfIndicesToReplace) {
          StringBuilder suffixSB = new StringBuilder(suffix);
          for (int i: indicesToReplace) {
            suffixSB.replace(i, i+1, "*");
          }
          suffixes.add(suffixSB.toString());
        }
      }
    }
    
    return suffixes;
  }
  
  // Returns all subsets of size k in { i, i+1, ..., j }.
  protected static Set<Set<Integer>> makeAllSubsets(int i, int j, int k) {
    if (k == 0) {
      Set<Set<Integer>> h = new HashSet<Set<Integer>>();
      h.add(new HashSet<Integer>());
      return h;
    }
    if (i > j || k > j - i + 1)
      return new HashSet<Set<Integer>>();
    
    Set<Set<Integer>> subsets = new HashSet<Set<Integer>>();
    for (int x = i; x <= j; x++) {
      // Don't take x.
      subsets.addAll(makeAllSubsets(x+1, j, k));
      
      // Take x.
      for (Set<Integer> s : makeAllSubsets(x+1, j, k-1)) {
        s.add(x);
        subsets.add(s);
      }
    }
    return subsets;
  }
  
}
