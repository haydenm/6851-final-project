package stringmatch.evaluate;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.github.jamm.MemoryMeter;

import stringmatch.ds.suffixtree.SuffixTree;
import stringmatch.ds.suffixtree.SuffixTreeNaiveBigSpace;
import stringmatch.ds.suffixtree.SuffixTreeWithCPD;
import stringmatch.ds.text.Text;
import stringmatch.ds.util.Pair;

public class Evaluator {

  public static final int AMOUNT_INPUT_EXTRACTED = 1000000;
  public static final int NUM_TRIALS = 5;
  public static final int NUM_QUERIES = 10000;
  public static final Random random = new Random();
  public static MemoryMeter meter = new MemoryMeter();
  
  public static void evaluateSlowQuery(Text inputText,
      String runtimeOutputFilename, String spaceOutputFilename)
      throws FileNotFoundException {
    List<Pair<Integer, Integer>> paramVals = new ArrayList<Pair<Integer, Integer>>();
    for (int n = 10000; n <= 50000; n += 10000) {
      // k doesn't matter in building this tree, but does affect runtime of
      // queries.
      for (int k = 0; k <= 30; k += 1) {
        paramVals.add(new Pair<Integer, Integer>(n, k));
      }
    }
    
    // To control for cache issues, take the first few values in paramVals
    // and put them in front (so they get run twice).
    List<Pair<Integer, Integer>> beginningVals = new ArrayList<Pair<Integer, Integer>>();
    for (int i = 0; i < Math.min(3, paramVals.size()); i++) {
      beginningVals.add(paramVals.get(i));
    }
    for (int i = 0; i < beginningVals.size(); i++) {
      paramVals.add(i, beginningVals.get(i));
    }
    
    Map<Pair<Integer, Integer>, Double> runtime = new HashMap<Pair<Integer, Integer>, Double>();
    Map<Pair<Integer, Integer>, Long> space = new HashMap<Pair<Integer, Integer>, Long>();
    
    System.gc();
    
    for (Pair<Integer, Integer> paramVal : paramVals) {
      int n = paramVal.getLeft();
      int k = paramVal.getRight();
      int p = Math.max(50, k);
      System.out.println("Running n=" + n + ", k=" + k + ", p=" + p);
      
      long totalTime = 0;
      for (int t = 0; t < NUM_TRIALS; t++) {
        int offset = random.nextInt(AMOUNT_INPUT_EXTRACTED - n - 1);
        Text inputTextPortion = inputText.extractSubstring(offset, offset + n).addEndCharIfNeeded();
        List<Text> queries = makeQueries(inputTextPortion, NUM_QUERIES/NUM_TRIALS, p, k);
        
        SuffixTree.Builder stb = new SuffixTree.Builder(inputTextPortion);
        SuffixTree st = stb.build();
        if (t == 0)
          space.put(new Pair<Integer, Integer>(n, k), meter.measureDeep(st));
      
        long startTime = System.currentTimeMillis();
        for (Text query : queries) {
          List<Integer> matches = st.naiveWildcardQueryIndices(query);
          if (matches.size() < 1)
            throw new RuntimeException("Didn't find match but should have!");
          //verifyQueryIndices(inputTextPortion, query, matches);
        }
        long endTime = System.currentTimeMillis();
        totalTime += endTime - startTime;
        
        // Clean up between calls.
        inputTextPortion = null;
        queries = null;
        stb = null;
        st = null;
        System.gc();
      }
      double timePerQuery = ((double) totalTime) / NUM_QUERIES;
      runtime.put(paramVal, timePerQuery);
    }
    
    outputResults(paramVals, runtime, space, runtimeOutputFilename, spaceOutputFilename);
  }
  
  public static void evaluateBigSpace(Text inputText,
      String runtimeOutputFilename, String spaceOutputFilename)
      throws FileNotFoundException {
    List<Pair<Integer, Integer>> paramVals = new ArrayList<Pair<Integer, Integer>>();
    for (int n = 1600; n >= 400; n -= 400) {
      // k doesn't matter in building this tree, but does affect runtime of
      // queries.
      for (int k = 10; k >= 0; k -= 1) {
        paramVals.add(new Pair<Integer, Integer>(n, k));
      }
    }
    
    // To control for cache issues, take the first few values in paramVals
    // and put them in front (so they get run twice).
    List<Pair<Integer, Integer>> beginningVals = new ArrayList<Pair<Integer, Integer>>();
    for (int i = 0; i < Math.min(3, paramVals.size()); i++) {
      beginningVals.add(paramVals.get(i));
    }
    for (int i = 0; i < beginningVals.size(); i++) {
      paramVals.add(i, beginningVals.get(i));
    }
    
    // Runs out of space at n=1000, k=12.
        
    Map<Pair<Integer, Integer>, Double> runtime = new HashMap<Pair<Integer, Integer>, Double>();
    Map<Pair<Integer, Integer>, Long> space = new HashMap<Pair<Integer, Integer>, Long>();
    
    System.gc();
    
    for (Pair<Integer, Integer> paramVal : paramVals) {
      int n = paramVal.getLeft();
      int k = paramVal.getRight();
      int p = Math.max(25, k);
      System.out.println("Running n=" + n + ", k=" + k + ", p=" + p);
      
      long totalTime = 0;
      for (int t = 0; t < NUM_TRIALS; t++) {
        int offset = random.nextInt(AMOUNT_INPUT_EXTRACTED - n - 1);
        Text inputTextPortion = inputText.extractSubstring(offset, offset + n).addEndCharIfNeeded();
        List<Text> queries = makeQueries(inputTextPortion, NUM_QUERIES/NUM_TRIALS, p, k);
        
        SuffixTreeNaiveBigSpace.Builder stb = new SuffixTreeNaiveBigSpace.Builder(inputTextPortion, k);
        SuffixTreeNaiveBigSpace st = stb.build();
        if (t == 0)
          space.put(new Pair<Integer, Integer>(n, k), meter.measureDeep(st));
      
        long startTime = System.currentTimeMillis();
        for (Text query : queries) {
          List<Integer> matches = st.queryForIndices(query);
          if (matches.size() < 1)
            throw new RuntimeException("Didn't find match but should have!");
          //verifyQueryIndices(inputTextPortion, query, matches);
        }
        long endTime = System.currentTimeMillis();
        totalTime += endTime - startTime;
        
        // Clean up between calls.
        inputTextPortion = null;
        queries = null;
        stb = null;
        st = null;
        System.gc();
      }
      double timePerQuery = ((double) totalTime) / NUM_QUERIES;
      runtime.put(paramVal, timePerQuery);
    }
    
    outputResults(paramVals, runtime, space, runtimeOutputFilename, spaceOutputFilename);
  }
  
  public static void evaluateCPDSlow(Text inputText,
      String runtimeOutputFilename, String spaceOutputFilename)
      throws FileNotFoundException {
    List<Pair<Integer, Integer>> paramVals = new ArrayList<Pair<Integer, Integer>>();
    for (int n = 10000; n >= 2000; n -= 2000) {
      // k doesn't matter in building this tree, but does affect runtime of
      // queries.
      for (int k = 16; k >= 0; k -= 1) {
        paramVals.add(new Pair<Integer, Integer>(n, k));
      }
    }
    
    // To control for cache issues, take the first few values in paramVals
    // and put them in front (so they get run twice).
    List<Pair<Integer, Integer>> beginningVals = new ArrayList<Pair<Integer, Integer>>();
    for (int i = 0; i < Math.min(3, paramVals.size()); i++) {
      beginningVals.add(paramVals.get(i));
    }
    for (int i = 0; i < beginningVals.size(); i++) {
      paramVals.add(i, beginningVals.get(i));
    }
    
    // Note: we use less space than bigspace around n=1000,k=9
    
    Map<Pair<Integer, Integer>, Double> runtime = new HashMap<Pair<Integer, Integer>, Double>();
    Map<Pair<Integer, Integer>, Long> space = new HashMap<Pair<Integer, Integer>, Long>();
    
    System.gc();
    
    for (Pair<Integer, Integer> paramVal : paramVals) {
      int n = paramVal.getLeft();
      int k = paramVal.getRight();
      int p = Math.max(25, k);
      System.out.println("Running n=" + n + ", k=" + k + ", p=" + p);
      
      long totalTime = 0;
      for (int t = 0; t < NUM_TRIALS; t++) {
        int offset = random.nextInt(AMOUNT_INPUT_EXTRACTED - n - 1);
        Text inputTextPortion = inputText.extractSubstring(offset, offset + n).addEndCharIfNeeded();
        List<Text> queries = makeQueries(inputTextPortion, NUM_QUERIES/NUM_TRIALS, p, k);

        SuffixTreeWithCPD.Builder stb = new SuffixTreeWithCPD.Builder(inputTextPortion, k);
        SuffixTreeWithCPD st = stb.build(true);
        if (t == 0)
          space.put(new Pair<Integer, Integer>(n, k), meter.measureDeep(st));
        //space.put(new Pair<Integer, Integer>(n, k), 0L);
            
        long startTime = System.currentTimeMillis();
        for (Text query : queries) {
          List<Integer> matches = st.slowSmartQueryIndices(query);
          if (matches.size() < 1)
            throw new RuntimeException("Didn't find match but should have!");
          //verifyQueryIndices(inputTextPortion, query, matches);
        }
        long endTime = System.currentTimeMillis();
        totalTime += endTime - startTime;
        
        // Clean up between calls.
        inputTextPortion = null;
        queries = null;
        stb = null;
        st = null;
        System.gc();
      }
      double timePerQuery = ((double) totalTime) / NUM_QUERIES;
      runtime.put(paramVal, timePerQuery);
    }
    
    outputResults(paramVals, runtime, space, runtimeOutputFilename, spaceOutputFilename);
  }
  
  public static void evaluateCPD(Text inputText,
      String runtimeOutputFilename, String spaceOutputFilename)
      throws FileNotFoundException {
    List<Pair<Integer, Integer>> paramVals = new ArrayList<Pair<Integer, Integer>>();
    for (int n = 1600; n >= 400; n -= 400) {
      // k doesn't matter in building this tree, but does affect runtime of
      // queries.
      for (int k = 10; k >= 0; k -= 1) {
        paramVals.add(new Pair<Integer, Integer>(n, k));
      }
    }
    
    // To control for cache issues, take the first few values in paramVals
    // and put them in front (so they get run twice).
    List<Pair<Integer, Integer>> beginningVals = new ArrayList<Pair<Integer, Integer>>();
    for (int i = 0; i < Math.min(3, paramVals.size()); i++) {
      beginningVals.add(paramVals.get(i));
    }
    for (int i = 0; i < beginningVals.size(); i++) {
      paramVals.add(i, beginningVals.get(i));
    }
    
    Map<Pair<Integer, Integer>, Double> runtime = new HashMap<Pair<Integer, Integer>, Double>();
    Map<Pair<Integer, Integer>, Long> space = new HashMap<Pair<Integer, Integer>, Long>();
    
    System.gc();
    
    for (Pair<Integer, Integer> paramVal : paramVals) {
      int n = paramVal.getLeft();
      int k = paramVal.getRight();
      int p = Math.max(25 , k);
      System.out.println("Running n=" + n + ", k=" + k + ", p=" + p);
      
      long totalTime = 0;
      for (int t = 0; t < NUM_TRIALS; t++) {
        int offset = random.nextInt(AMOUNT_INPUT_EXTRACTED - n - 1);
        Text inputTextPortion = inputText.extractSubstring(offset, offset + n).addEndCharIfNeeded();
        List<Text> queries = makeQueries(inputTextPortion, NUM_QUERIES/NUM_TRIALS, p, k);  

        SuffixTreeWithCPD.Builder stb = new SuffixTreeWithCPD.Builder(inputTextPortion, k);
        SuffixTreeWithCPD st = stb.build();
      
        if (t == 0)
          //space.put(new Pair<Integer, Integer>(n, k), meter.measureDeep(st));
          space.put(new Pair<Integer, Integer>(n, k), 0L);
        
        long startTime = System.currentTimeMillis();
        for (Text query : queries) {
          List<Integer> matches = st.smartQueryIndices(query);
          if (matches.size() < 1)
            throw new RuntimeException("Didn't find match but should have!");
          //verifyQueryIndices(inputTextPortion, query, matches);
        }
        long endTime = System.currentTimeMillis();
        totalTime += endTime - startTime;
        
        // Clean up between calls.
        inputTextPortion = null;
        queries = null;
        stb = null;
        st = null;
        System.gc();
      }
      double timePerQuery = ((double) totalTime) / NUM_QUERIES;
      runtime.put(paramVal, timePerQuery);
    }
    
    outputResults(paramVals, runtime, space, runtimeOutputFilename, spaceOutputFilename);
  }
  
  public static void outputResults(List<Pair<Integer, Integer>> paramVals,
      Map<Pair<Integer, Integer>, Double> runtime,
      Map<Pair<Integer, Integer>, Long> space,
      String runtimeOutputFilename, String spaceOutputFilename)
          throws FileNotFoundException {
    // Output with rows as k and columns as n
    Set<Integer> kVals = new HashSet<Integer>();
    for (Pair<Integer, Integer> p : paramVals) {
      kVals.add(p.getRight());
    }
    List<Integer> kValsSorted = new ArrayList<Integer>(kVals);
    Collections.sort(kValsSorted);
    
    Set<Integer> nVals= new HashSet<Integer>();
    for (Pair<Integer, Integer> p : paramVals) {
      nVals.add(p.getLeft());
    }
    List<Integer> nValsSorted = new ArrayList<Integer>(nVals);
    Collections.sort(nValsSorted);
    
    PrintWriter pwTime = new PrintWriter(runtimeOutputFilename);
    PrintWriter pwSpace = new PrintWriter(spaceOutputFilename);
    
    String nValsStr = "k";
    for (int n : nValsSorted) {
      nValsStr += "\t" + String.valueOf(n);
    }
    pwTime.println(nValsStr);
    pwSpace.println(nValsStr);
    
    for (int k : kValsSorted) {
      String timeRow = String.valueOf(k);
      String spaceRow = String.valueOf(k);
      
      for (int n : nValsSorted) {
        double rt = runtime.get(new Pair<Integer, Integer>(n, k));
        timeRow += "\t" + String.valueOf(rt);
        long s = space.get(new Pair<Integer, Integer>(n, k));
        spaceRow += "\t" + String.valueOf((double)s/1048576.0);
      }
      pwTime.println(timeRow);
      pwSpace.println(spaceRow);
    }
    
    pwTime.close();
    pwSpace.close();
  }
  
  public static void verifyQueryIndices(Text inputText, Text query, List<Integer> matches) {
    for (Integer match : matches) {
      Text queryMatch = inputText.extractSubstring(match, match + query.getLength());
      if (!queryMatch.equalsIgnoreWildcards(query))
        throw new RuntimeException("Invalid query match.");
    }
  }
 
  public static List<Text> makeQueries(Text inputText, int numQueries,
      int queryLength, int numWildcards) {
    List<Text> queries = new ArrayList<Text>();
    for (int i = 0; i < numQueries; i++) {
      int startIndex = random.nextInt(inputText.getLength() - queryLength);
      Text queryBasic = inputText.extractSubstring(startIndex, startIndex + queryLength);
      StringBuilder queryBasicStr = new StringBuilder(queryBasic.toString());
      List<Integer> pos = new ArrayList<Integer>();
      for (int j = 0; j < queryLength; j++) {
        pos.add(j);
      }
      Collections.shuffle(pos);
      for (int j = 0; j < numWildcards; j++) {
        int wildcardPos = pos.get(j);
        queryBasicStr.replace(wildcardPos, wildcardPos+1, "*");
      }
      Text query = new Text(queryBasicStr.toString(), false);
      queries.add(query);
    }
    return queries;
  }
  
  public static void main(String[] args) throws FileNotFoundException {
    String chr1Filename = args[0];
    String englishCorpusFilename = args[1];
    
    Text chr = DataReaders.readFile(chr1Filename, AMOUNT_INPUT_EXTRACTED);
    Text eng = DataReaders.readFile(englishCorpusFilename, AMOUNT_INPUT_EXTRACTED);
    
    String outputPath = args[2];
    
    //evaluateSlowQuery(chr, outputPath + "slowquery_littlealph_time.txt", outputPath + "slowquery_littlealph_space.txt");
    //evaluateSlowQuery(eng, outputPath + "slowquery_bigalph_time.txt", outputPath + "slowquery_bigalph_space.txt");
    
    //evaluateBigSpace(chr, outputPath + "bigspace_littlealph_time.txt", outputPath + "bigspace_littlealph_space.txt");
    //evaluateBigSpace(eng, outputPath + "bigspace_bigalph_time.txt", outputPath + "bigspace_bigalph_space.txt");
    
    //evaluateCPDSlow(chr, outputPath + "cpdslow_littlealph_time.txt", outputPath + "cpdslow_littlealph_space.txt");
    //evaluateCPDSlow(eng, outputPath + "cpdslow_bigalph_time.txt", outputPath + "cpdslow_bigalph_space.txt");
    
    //evaluateCPD(chr, outputPath + "cpd_littlealph_time.txt", outputPath + "cpd_littlealph_space.txt");
    evaluateCPD(eng, outputPath + "cpd_bigalph_time.txt", outputPath + "cpd_bigalph_space.txt");
  }
  
}
