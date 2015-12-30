import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;

/**
 * An abstract class designed to make fetching the results of different HTTP
 * operations easier.
 */
public class HTMLParser {

	private static final int PORT = 80;
	private final URI uri;
	private final StringBuilder html;
	private final StringBuilder header;
	
	/**
	 * Initializes this fetcher. Must call {@link #fetch()} to actually start
	 * the process.
	 *
	 * @param url - the link to fetch from the webserver
	 * @throws MalformedURLException if unable to parse URL
	 */
	public HTMLParser(URI uri) {
		this.uri = uri;
		this.html = new StringBuilder();
		this.header = new StringBuilder();
	}

	public String getHTML() {
		return html.toString();
	}
	
	public String getHeader() {
		return header.toString();
	}
	
	/**
	 * Returns the port being used to fetch URLs.
	 *
	 * @return port number
	 */
	public int getPort() {
		return this.PORT;
	}

	/**
	 * Returns the URL being used by this fetcher.
	 *
	 * @return URL
	 */
	public URI getURI() {
		return this.uri;
	}
	
	/**
	 * Fetches the webpage at the provided URL, cleans up the HTML tags,
	 * and parses the resulting plain text into words.
	 *
	 * THIS METHOD IS PROVIDED FOR YOU. DO NOT MODIFY.
	 *
	 * @param url - webpage to download
	 * @return list of parsed words
	 */
	public static ArrayList<String> fetchWords(String html) {

		html = stripElement("script", html);
		html = stripElement("style", html);
		html = stripTags(html);
		html = stripEntities(html);
		
		return parseWords(html);
	}

	/**
	 * Parses the provided plain text (already cleaned of HTML tags) into
	 * individual words.
	 *
	 * THIS METHOD IS PROVIDED FOR YOU. DO NOT MODIFY.
	 *
	 * @param text - plain text without html tags
	 * @return list of parsed words
	 */
	public static ArrayList<String> parseWords(String text) {
		
		ArrayList<String> words = new ArrayList<String>();

		for (String word : text.split("\\s+")) {
			word = word.toLowerCase().replaceAll("[\\W_]+", "").trim();

			if (!word.isEmpty()) {
				words.add(word);
			}
		}

		return words;
	}

	/**
	 * Removes everything between the element tags, and the element
	 * tags themselves. For example, consider the html code:
	 *
	 * <pre>
	 * &lt;style type="text/css"&gt;body { font-size: 10pt; }&lt;/style&gt;
	 * </pre>
	 *
	 * If removing the "style" element, all of the above code will
	 * be removed, and replaced with the empty string.
	 *
	 * @param name - name of the element to strip, like style or script
	 * @param html - html code to parse
	 * @return html code without the element specified
	 */
	public static String stripElement(String name, String html) {
		
		html = html.replaceAll("\n", " ");
		
		String style = "<" + name + ".*?>.*?</" + name + ".*?>";
		html = html.replaceAll("(?i)" + style, " ");
		
		return html;
	}
	
	/**
	 * Removes all HTML tags, which is essentially anything between
	 * the < and > symbols. The tag will be replaced by the
	 * empty string.
	 *
	 * @param html - html code to parse
	 * @return text without any html tags
	 */
	public static String stripTags(String html) {
		
		html = html.replaceAll(" \n", " ");
		String tags = "<.*?>";
		html = html.replaceAll(tags, " ");
		
		return html;
	}

	/**
	 * Replaces all HTML entities in the text with the empty string.
	 * For example, "2010&ndash;2012" will become "20102012".
	 *
	 * @param html - the text with html code being checked
	 * @return text with HTML entities replaced by a space
	 */
	public static String stripEntities(String html) {

		String entity = "&.*?;";
		html = html.replaceAll(entity, " ");
		
		return html;
	}

	/**
	 * Crafts the HTTP request from the URL. Must be overridden.
	 *
	 * @return HTTP request
	 */
	private String craftRequest() {
		String host = this.getURI().getHost();
		String resource = this.getURI().getPath().isEmpty() ? "/" : this.getURI().getPath();

		StringBuffer output = new StringBuffer();
		output.append("GET " + resource + " HTTP/1.0\n");
		output.append("Host: " + host + "\n");
		output.append("\r\n");

		return output.toString();
	}

	/**
	 * Will connect to the web server and fetch the URL using the HTTP request
	 * from {@link #craftRequest()}, and then call {@link #processLine(String)}
	 * on each of the returned lines.
	 */
	public void fetch() {

		try (
			Socket socket = new Socket(uri.getHost(), PORT);
				
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(socket.getOutputStream());
			) {
			String request = craftRequest();

			writer.println(request);
			writer.flush();
			
			String line = reader.readLine();

			while(line != null) {
				if(line.isEmpty()) {
					break;
				}
				else {
					header.append(line + " ");
					line = reader.readLine();
				}
			}
			
			while (line != null) {
				html.append(line + " ");
				line = reader.readLine();
			}
		}
		catch (IOException e) {
		}
	}
}