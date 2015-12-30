import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/** Class that creates a map containing the given search terms.
 *  Calls the inverted index for all the information, which 
 *  returns an arraylist of comparable objects, which is inserted
 *  into the map and later returned.
 */

@SuppressWarnings("serial")
public class QueryParser extends BaseServlet{

	private static final Charset UTF8 = Charset.forName("UTF-8");
	private final MultiReadWriteLock lock;
	private final LinkedHashMap<String, ArrayList<SearchResult>> queryMap;
//    private static final Logger logger = LogManager.getLogger();
    private final WorkQueue workers;
	private int pending;
	
	/** Constructor for initializing queryMap. */
	public QueryParser(WorkQueue workers) {
		this.workers = workers;
		this.lock = new MultiReadWriteLock();
		this.queryMap = new LinkedHashMap<>();
		this.pending = 0;
	}	
	
	public void multiThreadQueryParse(Path file, InvertedIndex index){
		
//		logger.debug("Starting Query Parsing.");
		
		try(BufferedReader bfReader = Files.newBufferedReader(file, UTF8)) {

			String line;

			while((line = bfReader.readLine()) != null) {
				
				lock.lockWrite();
				queryMap.put(line, null);
				lock.unlockWrite();
				
				workers.execute(new ParseQueryFile(line, index));

			}
		}
		catch(IOException IOE) {
//			logger.catching(Level.DEBUG, IOE);
//			logger.warn("Unable to open query file {}", file);
		}
		
		finish();
//		logger.debug("Ending Query Parsing.");
	}
	
	/** Class that handles the parsing of the query file
	 *  and passing in those words to the inverted index.
	 *  and adding to the queryMap.
	 */
	private class ParseQueryFile implements Runnable {
		
		private String line;
		private InvertedIndex index;
		
		public ParseQueryFile(String line, InvertedIndex index) {
			this.line = line;
			this.index = index;
			incrementPending();
		}
		
		@Override
		public void run() {
			
			ArrayList<String> lineWords = new ArrayList<>();
			
			for (String prefixWord : line.split(" ")) {

				if(prefixWord.isEmpty()) {
					continue;
				}

				lineWords.add(prefixWord.toLowerCase());
			}

			ArrayList<SearchResult> results = index.search(lineWords);

			lock.lockWrite();
			queryMap.put(line, results);
			lock.unlockWrite();
			decrementPending();
		}
	}
	
	/**
	 * Indicates that we now have additional "pending" work to wait for. We
	 * need this since we can no longer call join() on the threads. (The
	 * threads keep running forever in the background.)
	 *
	 * We made this a synchronized method in the outer class, since locking
	 * on the "this" object within an inner class does not work.
	 */
	private synchronized void incrementPending() {
		pending++;
//		logger.debug("Pending is now {}", pending);
	}

	/**
	 * Indicates that we now have one less "pending" work, and will notify
	 * any waiting threads if we no longer have any more pending work left.
	 */
	private synchronized void decrementPending() {
		pending--;
//		logger.debug("Pending is now {}", pending);

		if (pending <= 0) {
			this.notifyAll();
		}
	}
	
	/**
	 * Helper method, that helps a thread wait until all of the current
	 * work is done. This is useful for resetting the counters or shutting
	 * down the work queue.
	 */
	public synchronized void finish() {
		try {
			while (pending > 0) {
//				logger.debug("Waiting until threads finish.");
				this.wait();
			}
		}
		catch (InterruptedException e) {
//			logger.debug("Finish interrupted.", e);
		}
	}
	
	/**
	 * Will shutdown the work queue after all the current pending work is
	 * finished. Necessary to prevent our code from running forever in the
	 * background.
	 */
	public void shutdown() {
//		logger.debug("Shutting down");
		finish();
		workers.shutdown();
	}

	/** Class that handles writing the new query map into a file. */
	public void print(Path output) {

		lock.lockRead();
		
		try(BufferedWriter bw = Files.newBufferedWriter(output, UTF8)) {

			for(String line: queryMap.keySet()) {

				bw.write(line);
				bw.newLine();				
				ArrayList<SearchResult> results = queryMap.get(line);

				for(SearchResult result: results) {

					bw.write(result.toString());
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