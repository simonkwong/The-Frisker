import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class that handles the traversing of a given directory.
 *  If it is a directory, does through each sub-directory
 *  and checks whether it's a file or directory. 
 *  If any of the traversed directories are a file, it sends
 *  it through a file parser.
 */
public class MultithreadedIndexBuilder {
	
	private static final String PROTOCOL = "[A-Za-z][A-Za-z0-9+.-]://";
	private static final String DOMAIN = "[^/#?]+";
	private static final String RESOURCE = "(/[^?#]+)*$";
	private static final String URL = PROTOCOL + DOMAIN + RESOURCE;
	private static final String REGEX = "(?i)a\\s+href\\s*=\\s*?\"((" + URL + ")|(.*?))\\s*\"\\s*";
	private static final String UNIQUELINKINFO = "[?#].*";
	private static final int GROUP = 1;
	private final WorkQueue workers;
	private final HashSet<URI> uris;
	private final MultiReadWriteLock lock;
	private int pending;
//	private static final Logger logger = LogManager.getLogger();

	public MultithreadedIndexBuilder(WorkQueue workers) {
		this.pending = 0;
		this.workers = workers;   	
		this.uris = new HashSet<>();
		this.lock = new MultiReadWriteLock();
	}
	
	public void webCrawl(URI seedURI, InvertedIndex index, int maxLinks) {
		
		lock.lockWrite();
		uris.add(seedURI);
		lock.unlockWrite();
		
		workers.execute(new URIWorker(seedURI, maxLinks, index));
		
		finish();
	}
	
private class URIWorker implements Runnable {
		
		private URI uri;
		private int maxLinks;
		private final InvertedIndex index;
		
		public URIWorker(URI uri, int maxLinks, InvertedIndex index) {
			this.uri = uri;
			this.index = index;
			this.maxLinks = maxLinks;
			incrementPending();
		}
		
		@Override
		public void run() {
			
			HTMLParser httpfetcher = new HTMLParser(uri);
			
			httpfetcher.fetch();
			
			HashSet<URI> localURIS = listLinks(uri, maxLinks, httpfetcher, index);
			lock.lockRead();
			HashSet<URI> tempURIS = uris;
			lock.unlockRead();
			
			htmlParse(uri, index, httpfetcher.getHTML());
			
			// TODO Lock around the for loop so you can read and write to uris
			// Then would not need getSize()
			lock.lockRead();
			
			for(URI link : localURIS) {
				if(tempURIS.size() < maxLinks && !tempURIS.contains(link)) {
					
					tempURIS.add(link);
					
					workers.execute(new URIWorker(link, maxLinks, index));
				}
				else if(tempURIS.size() == maxLinks){
					break;
				}
			}
			lock.unlockRead();
			
			lock.lockWrite();
			uris.addAll(tempURIS);
			lock.unlockWrite();
			
			decrementPending();
		}
	}

	private HashSet<URI> listLinks(URI seedURI, int maxLinks, HTMLParser httpfetcher, InvertedIndex index) {

		String text = httpfetcher.getHTML();

		LinkedHashSet<URI> links = new LinkedHashSet<>();

		Pattern p = Pattern.compile(REGEX);

		Matcher m = p.matcher(text);

		while(m.find() && !uris.contains(m.group(GROUP).replaceAll(UNIQUELINKINFO, ""))) {

			URL url = null;

			try {

				url = new URL(seedURI.toURL(), m.group(GROUP).replaceAll(UNIQUELINKINFO, ""));

				URI uri = new URI(url.toString());
 
				String header = httpfetcher.getHeader();
				
				if(header.contains("text/html") && getSize() != maxLinks) {
					
					links.add(uri);
				}
				else {
					break;
				}
			}
			catch (URISyntaxException | MalformedURLException e){
			}
		}
		return links;
	}
	
	private void htmlParse(URI uri, InvertedIndex index, String html){
		
		InvertedIndex localIndex = new InvertedIndex();
		
		Integer pos = 1;

		ArrayList<String> words = HTMLParser.fetchWords(html);

		for(String word : words) {
			localIndex.add(uri.toString(), word, pos++);
		}

		index.addAll(localIndex);
	}

	public int getSize() {
		
		lock.lockRead();
		int count = uris.size();
		lock.unlockRead();
		
		return count;
	}
	
	/**
	 * Normalizes a word by converting it to lowercase, removing all non-word
	 * characters using the {@code "\\W"} regular expression, removing all
	 * {@code "_"} underscore characters, and removing any unnecessary extra
	 * whitespace at the start of end of the word.
	 *
	 * @param word to normalize
	 * @return normalized version of the word
	 */
	public String normalizeWord(String word) {

		if(word != null) {

			word = word.toLowerCase().trim();
			word = word.replaceAll("[\\W_]", "");

			return word;
		}
		return null;
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

	/** Method that returns the links of the HTML pages parsed.*/
	public Set<URI> getURIS() {
//		logger.debug("Getting links.");
		finish();
		lock.lockRead();
		Set<URI> unmodifiableURIS = Collections.unmodifiableSet(uris);
		lock.unlockRead();
		return unmodifiableURIS;
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
}
