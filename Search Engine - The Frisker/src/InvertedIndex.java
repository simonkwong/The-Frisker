import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/** Class that handles the creation of the inverted index map.
 *  Checks first if the map is empty, it will put the first elements
 *  as is. 
 *  If the map doesn't contain that key, it will also put the 
 *  given information for that key as is.
 *  If the map does contain the key, checks if it contains a certain 
 *  key path, if not it adds the path and location to the inside map.
 *  	If it does contain the path, it adds the locations to the 
 *  	already existing location list.
 *  Will return a search result object depending on the query given.
 */

public class InvertedIndex {

	private static final Charset UTF8 = Charset.forName("UTF-8");
	private final MultiReadWriteLock lock;
	private final TreeMap<String, TreeMap<String, ArrayList<Integer>>> wordMap;
//	private static final Logger logger = LogManager.getLogger();

	/** Constructor for initializing wordMap. */
	public InvertedIndex() {
		lock = new MultiReadWriteLock();
		wordMap = new TreeMap<>();
	}
	
	/** Adds individual components into the invertedindex 
	 * 	as mentioned above. 
	 */
	public void add(String path, String word, Integer pos) {
		
//		logger.debug("Adding to invertedindex.");
		
		lock.lockWrite();
		
		if(wordMap.isEmpty() || !wordMap.containsKey(word)) {
			
			TreeMap<String, ArrayList<Integer>> locations = createLocMap(word, path, pos);
			wordMap.put(word, locations);
		}
		
		else if(wordMap.containsKey(word)) {

			TreeMap<String, ArrayList<Integer>> locMap = wordMap.get(word);
			
			if(wordMap.get(word).containsKey(path)) {
			
				ArrayList<Integer> locations = locMap.get(path);
				locations.add(pos);
			}
			
			else if(!wordMap.get(word).containsKey(path)) {
				
				ArrayList<Integer> locations = new ArrayList<Integer>();
				locations.add(pos);
				locMap.put(path, locations);
			}
		}
		lock.unlockWrite();
	}

	/** The helper method for creating the inner map for 
	 * the path and locations.
	 */
	private TreeMap<String, ArrayList<Integer>> createLocMap(String word, String path, Integer index) {

		TreeMap<String, ArrayList<Integer>> locMap = new TreeMap<>();
		ArrayList<Integer> location = new ArrayList<Integer>();

		location.add(index);
		locMap.put(path, location);
		
		return locMap;
	}
	
	/** Methods that checks for related words and keeps track of the comparable objects. */
	public ArrayList<SearchResult> search(ArrayList<String> lineWords) {
		
//		logger.debug("Creating arraylist of comparables.");
		
		HashMap<String, SearchResult> resultMap = new HashMap<>();
		
		lock.lockRead();
			
		for(String prefix : lineWords) {
			
			for(Map.Entry<String, TreeMap<String, ArrayList<Integer>>> word: wordMap.tailMap(prefix).entrySet()) {

				if(!word.getKey().startsWith(prefix)) {
					break;
				}

				for(Map.Entry<String, ArrayList<Integer>> filePath: word.getValue().entrySet()) {
			
					String path = filePath.getKey();
					Integer first = filePath.getValue().get(0);
					Integer freq = filePath.getValue().size();
					
					if(!resultMap.containsKey(path)) {
						
						resultMap.put(path, new SearchResult(path, freq, first));
					}
					else if(resultMap.containsKey(path)) {

						resultMap.get(path).update(freq, first);
					}
				}
			}
		}
		lock.unlockRead();
		
		ArrayList<SearchResult> resultList = new ArrayList<>();
		resultList.addAll(resultMap.values());
		Collections.sort(resultList);
		return resultList;
	}
	
	public void addAll(InvertedIndex localIndex) {
		
		lock.lockWrite();
		
		for(Map.Entry<String, TreeMap<String, ArrayList<Integer>>> word: localIndex.wordMap.entrySet()) {
			
			if(wordMap.isEmpty() || !wordMap.containsKey(word.getKey())) {
				
				wordMap.put(word.getKey(), word.getValue());
			}
			else if(wordMap.containsKey(word.getKey())) {
				
				TreeMap<String, ArrayList<Integer>> paths = wordMap.get(word.getKey());

				if(!wordMap.get(word.getKey()).containsKey(word.getValue().firstKey())) {

					paths.put(word.getValue().firstKey(), word.getValue().get(word.getValue().firstKey()));
				}
				else if(wordMap.get(word.getKey()).containsKey(word.getValue().firstKey())) {

					ArrayList<Integer> locations = paths.get(word.getValue().firstKey());

					locations.addAll(word.getValue().get(word.getValue().firstKey()));
				}
			}
		}
		lock.unlockWrite();
	}
	
	/** Method for writing the invertedindex into an output file. */
	public void print(Path output) {

//		logger.debug("Printing invertedindex.");
		
		lock.lockRead();
		
		try(BufferedWriter bw = Files.newBufferedWriter(output, UTF8)) {
			
			for(Map.Entry<String, TreeMap<String, ArrayList<Integer>>> word: wordMap.entrySet()) {
				
				bw.write(word.getKey());
				bw.newLine();
				
				for(Map.Entry<String, ArrayList<Integer>> path: word.getValue().entrySet()) {
					
					bw.write('"' + path.getKey() + '"');
					ArrayList<Integer> locations = path.getValue();

					for(int i = 0; i < locations.size(); i++) {
						
						bw.write(", " + locations.get(i));
					}
					bw.newLine();
				}
				bw.newLine();
			}
			bw.flush();
		} 
		catch (IOException IOE) {
//			logger.catching(Level.DEBUG, IOE);
//			logger.warn("Unable to print to file {}", output);			
		}
		lock.unlockRead();
	}
}