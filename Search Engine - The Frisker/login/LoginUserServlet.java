import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class LoginUserServlet extends LoginBaseServlet {

	LoginDatabaseHandler database = LoginDatabaseHandler.getInstance();
	
	@Override
	public void doGet(
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		prepareResponse("Login", response);

		PrintWriter out = response.getWriter();
		String error = request.getParameter("error");
		out.printf("<head><link rel = \"shortcut icon\" href = \"http://oi44.tinypic.com/169q174.jpg\"></head>%n");
		out.printf("<a href = \"http://localhost:8080/search\"> <img src = \"http://oi40.tinypic.com/2mrtiio.jpg\" alt = \"FRISK\" height = \"133\" width = \"291\"></a>%n%n");
		
		int code = 0;

		if (error != null) {
			try {
				code = Integer.parseInt(error);
			}
			catch (Exception ex) {
				code = -1;
			}

			String errorMessage = StringUtilities.getStatus(code).message();
			out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}

		if (request.getParameter("newuser") != null) {
			out.println("<p>Registration was successful!");
			out.println("Login with your new username and password below.</p>");
		}

		if (request.getParameter("logout") != null) {
			clearCookies(request, response);
			out.println("<p>Successfully logged out.</p>");
		}

		printForm(out);
		finishResponse(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		String user = request.getParameter("user");
		String pass = request.getParameter("pass");

		Status status = database.authenticateUser(user, pass);

		try {
			if (status == Status.OK) {
				// should eventually change this to something more secure
				response.addCookie(new Cookie("login", "true"));
				response.addCookie(new Cookie("name", user));
				response.sendRedirect(response.encodeRedirectURL("/welcome"));
			}
			else {
				response.addCookie(new Cookie("login", "false"));
				response.addCookie(new Cookie("name", ""));
				response.sendRedirect(response.encodeRedirectURL("/login?error=" + status.ordinal()));
			}
		}
		catch (Exception ex) {
//			log.error("Unable to process login form.", ex);
		}
	}

	private void printForm(PrintWriter out) {
		out.println("<form action=\"/login\" method=\"post\">");
		out.println("<table border=\"0\">");
		out.println("\t<tr>");
		out.println("\t\t<td>Usename:</td>");
		out.println("\t\t<td><input type=\"text\" name=\"user\" size=\"30\"></td>");
		out.println("\t</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>Password:</td>");
		out.println("\t\t<td><input type=\"password\" name=\"pass\" size=\"30\"></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" value=\"Login\"></p>");
		out.println("</form>");

		out.println("<p>(<a href=\"/register\">new user? register here.</a>)</p>");
	}
}
