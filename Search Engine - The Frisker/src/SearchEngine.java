import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

@SuppressWarnings("serial")
public class SearchEngine extends BaseServlet {
	
	private static final String TITLE = "FRISK";
	
    private final InvertedIndex index;
    private static ArrayList<SearchResult> results = null;
    private long timeItTook;
    
	public SearchEngine(WorkQueue workers, InvertedIndex index) {
		super();
		this.index = index;
		this.timeItTook = 0;
	}
    
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		
		PrintWriter out = response.getWriter();
		
		out.printf("THE FRISKER");
		
		out.printf("<html>%n");
		out.printf("<body>%n");
		out.printf("<center>%n");
		out.printf("<head><title>%s</title><link rel = \"shortcut icon\" href = \"http://oi44.tinypic.com/169q174.jpg\"></head>%n", TITLE);
		
		out.printf("<a href = \"http://localhost:8080/search\"> <img src = \"http://oi40.tinypic.com/2mrtiio.jpg\" alt = \"FRISK\" height = \"133\" width = \"291\"></a>%n%n");
		
		printForm(request, response);
		
		out.printf("</center>%n");
		out.printf("</body>%n");
		
		
		out.printf("</html>%n");
		printResults(request, response);

		response.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		
		long start = System.nanoTime();
		
		String query = request.getParameter("query");
		query = (query == null || query.isEmpty()) ? "" : query;
		query = StringEscapeUtils.escapeHtml4(query);
		
		if(query == null || query.isEmpty()) {
			response.sendRedirect("http://localhost:8080/search");
		}
		else {
			ArrayList<String> lineWords = new ArrayList<>();

			for(String prefixWord : query.split(" ")) {

				if(prefixWord.isEmpty()) {
					continue;
				}

				lineWords.add(prefixWord);
			}

			results = index.search(lineWords);

			long end = System.nanoTime();

			timeItTook = end - start;

			if(lineWords.size() == 1) {
				Cookie cookie = new Cookie(query, getDate());
				response.addCookie(cookie);
			}
			else if(lineWords.size() > 1) {
				
				String searchterm = "";
				for(String word: lineWords) {
					searchterm = searchterm.concat("_" + word);
				}
				
				Cookie cookie = new Cookie(searchterm, getDate());
				response.addCookie(cookie);
			}
		}
		
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.sendRedirect(request.getServletPath());
	}
	
	private void printForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		PrintWriter out = response.getWriter();
		out.printf("<form method=\"post\" action=\"%s\">%n", request.getServletPath());
		out.printf("<table cellspacing=\"0\" cellpadding=\"2\"%n");
		out.printf("<tr>%n");
		out.printf("\t\t<input type=\"text\" name=\"query\" maxlength=\"75\" size=\"75\">%n");
		out.printf("\t</td>%n");
		out.printf("</tr>%n%n");
		out.printf("<p><input type=\"submit\" value=\"STOP AND FRISK\"></p>");
		out.printf("</form>\n%n");
	}
	
	private void printResults(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		PrintWriter out = response.getWriter();
		
		if(results != null) {
			
			String numResults = String.valueOf(results.size());
			String time = String.valueOf(timeItTook);
			
			out.printf("<p> %s results found in %s nanoseconds</p>", numResults, time);
			
			for(SearchResult result: results) {
				
				String link = result.getPath();
//				link = link.replaceAll(".*?/", "").replaceAll("[.]+?[A-Za-z]+", "");
				out.printf("<p><a href=\"%s\"> %s </a></p>", link, link);
			}
		}
	}
}
