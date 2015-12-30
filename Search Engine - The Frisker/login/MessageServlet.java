import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

@SuppressWarnings("serial")
public class MessageServlet extends BaseServlet{
	private static final String TITLE = "Messages";
//	private static Logger log = Log.getRootLogger();
	private static String date;
	
	private LinkedList<String> values;
	private Map<String, String> cookies;
	
	public MessageServlet() {
		super();
		values = new LinkedList<>();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		
		String delete = request.getParameter("delete");
		cookies = getCookieMap(request);

//		log.info("MessageServlet ID " + this.hashCode() + " handling GET request.");

		PrintWriter out = response.getWriter();
		out.printf("<html>%n");
		out.printf("<head><title>%s</title></head>%n", TITLE);
		out.printf("<head><link rel = \"shortcut icon\" href = \"http://oi44.tinypic.com/169q174.jpg\"></head>%n");		
		out.printf("<a href = \"http://localhost:8080/search\"> <img src = \"http://oi40.tinypic.com/2mrtiio.jpg\" alt = \"FRISK\" height = \"133\" width = \"291\"></a>%n%n");

		out.printf("<body>%n");
		out.printf("<center>%n");
		out.printf("<fieldset>");
		out.printf("<h1>Saved Cookies</h1>%n%n");
		
		if(cookies.isEmpty() || cookies == null) {
			String noCookies = "No cookies saved.";
			out.printf("%s", noCookies);
		}
		else if ((cookies.size() > 0) && (delete == null)) {

			if(cookies != null) {
				synchronized (cookies) {
					for (String cookie : cookies.keySet()) {
						
						out.printf("<p><b> %s </b> at %s </p>%n%n", cookie, cookies.get(cookie));
					}
				}
			}
		}
		
		out.printf("<div style=\"width:500px;height:275px;border:6px double black;\">");
		out.printf("<h1>Edit Cookies: </h1>%n%n");
		printForm(request, response);
		
		if(date != null) {
			out.printf("<p><i><font size=\"1\">Last updated at </font> <b> <font size = \"2\">%s.</font></p>%n", date);
		}

		out.printf("</div>%n");
		out.printf("</fieldset>");
		out.println("<p><a href=\"/change\">change password</a></p>");
		out.printf("</center>%n");
		out.printf("</body>%n");
		out.printf("</html>%n");
		
		
		response.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

//		log.info("MessageServlet ID " + this.hashCode() + " handling POST request.");

		String name = request.getParameter("name");
		String value = request.getParameter("value");
		String delete = request.getParameter("delete");
		String clear = request.getParameter("clear");
		
		name = (name == null || name.isEmpty()) ? "anonymous" : name;
		value = (value == null || value.isEmpty()) ? "" : value;
		
		name = StringEscapeUtils.escapeHtml4(name);
		value = StringEscapeUtils.escapeHtml4(value);
		
		if(clear != null) {
			clearCookies(request, response);
		}
		if((delete != null) && cookies.containsKey(name)) {
			deleteCookie(name, response);
		}
		
		String formatted = String.format("<b>%s : </b> %s", name, value);
		date = getDate();

		synchronized (values) {
			values.push(formatted);

			if (values.size() > 20) {
				values.pop();
			}
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.sendRedirect(request.getServletPath());
	}

	private static void printForm(HttpServletRequest request, HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		out.printf("<form method=\"post\" action=\"%s\">%n", request.getServletPath());
		out.printf("<table cellspacing=\"0\" cellpadding=\"2\"%n");
		out.printf("<tr>%n");
		out.printf("\t<td nowrap>Name:</td>%n");
		out.printf("\t<td>%n");
		out.printf("\t\t<input type=\"text\" name=\"name\" maxlength=\"50\" size=\"50\">%n");
		out.printf("\t</td>%n");
		out.printf("</tr>%n");
		out.printf("<tr>%n");
		out.printf("\t<td nowrap>Value:</td>%n");
		out.printf("\t<td>%n");
		out.printf("\t\t<input type=\"text\" name=\"value\" maxlength=\"100\" size=\"50\">%n");
		out.printf("\t</td>%n");
		out.printf("</tr>%n");
		out.printf("</table>%n");
		out.printf("<p><input type=\"checkbox\" name=\"delete\" value=\"delete\"> Delete this cookie </p>%n%n");
		out.printf("<p><input type=\"checkbox\" name=\"clear\" value=\"clear\"> Clear all cookies </p>%n%n");
		out.printf("<p><input type=\"submit\" value=\"Submit\"></p>");
		out.printf("</form>\n%n");
	}
}