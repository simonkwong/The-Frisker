import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Driver {

//	private static final Logger logger = LogManager.getLogger();	
	
	public static void main(String[] args) throws Exception{

		if(args.length > 0) {

			InvertedIndex index = new InvertedIndex();
			ArgumentParser ap = new ArgumentParser(args);

			Integer threads = 5;
			Integer maxLinks = 100;
			Integer PORT = null;

			if(ap.hasFlag("-p") && ap.isInteger(ap.getValue("-p"))) {
				PORT = Integer.parseInt(ap.getValue("-p"));
			}
			
			if((ap.hasFlag("-u")) && (ap.getValue("-u") != null) && (PORT != null)) {

				URI uri = ap.getURI("-u");
				
				if(ap.hasFlag("-t") && ap.isInteger(ap.getValue("-t"))) {
					threads = Integer.parseInt(ap.getValue("-t"));
				}

				WorkQueue workers = new WorkQueue(threads);

				MultithreadedIndexBuilder mtib = new MultithreadedIndexBuilder(workers);
				mtib.webCrawl(uri, index, maxLinks);

				Server server = new Server(PORT);
				
				SearchEngine se = new SearchEngine(workers, index);
				
				ServletHandler handler = new ServletHandler();

				handler.addServletWithMapping(new ServletHolder(se), "/search");
				
			/*	handler.addServletWithMapping(LoginUserServlet.class, "/login");
				handler.addServletWithMapping(LoginRegisterServlet.class, "/register");
				handler.addServletWithMapping(LoginWelcomeServlet.class, "/welcome");
				handler.addServletWithMapping(LoginRedirectServlet.class, "/*");
				handler.addServletWithMapping(ChangePasswordServlet.class, "/change");
				handler.addServletWithMapping(MessageServlet.class, "/settings");
			*/	
				
				Path output = Paths.get("InvertedIndex.txt");
				
				index.print(output);
				
				server.setHandler(handler);

				try {
					server.start();
					server.join();
				}
				catch (Exception ex) {
//					logger.fatal("Interrupted while running server.", ex);
					
					System.out.println("FAILED!");
					System.exit(-1);
				}				

				workers.shutdown();
			}
		}
		else {
//			logger.debug("No valid arguments were given.");
			System.out.println("Please enter valid arguments.");
			System.out.println("USAGE: <arguments>");
			System.out.println("<-u url> <-p port> <-t threads>");
		}
	}
}
