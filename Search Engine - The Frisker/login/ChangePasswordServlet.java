import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ChangePasswordServlet extends LoginBaseServlet {

	LoginDatabaseHandler database = LoginDatabaseHandler.getInstance();
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		prepareResponse("change", response);

		PrintWriter out = response.getWriter();
		String error = request.getParameter("error");

		out.printf("<head><link rel = \"shortcut icon\" href = \"http://oi44.tinypic.com/169q174.jpg\"></head>%n");
		
		out.printf("<a href = \"http://localhost:8080/search\"> <img src = \"http://oi40.tinypic.com/2mrtiio.jpg\" alt = \"FRISK\" height = \"133\" width = \"291\"></a>%n%n");
		
		if(error != null) {
			String errorMessage = StringUtilities.getStatus(error).message();
			out.println("<p style=\"color: red;\">" + errorMessage + "</p>");
		}

		printForm(out);
		finishResponse(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		prepareResponse("change", response);
		response.setStatus(HttpServletResponse.SC_OK);

		String newuser = request.getParameter("user");
		String newpass = request.getParameter("pass");
		Status status = database.changePass(newuser, newpass);

		if(status == Status.OK) {
			response.sendRedirect(response.encodeRedirectURL("/change?newpass==true"));
		}
		else {
			String url = "/change?error=" + status.name();
			response.sendRedirect(response.encodeRedirectURL(url));
		}
	}

	private void printForm(PrintWriter out) {
		out.println("<form action=\"change\" method=\"post\">");
		out.println("<table border=\"0\">");
		out.println("\t<tr>");
		out.println("\t\t<td>Username:</td>");
		out.println("\t\t<td><input type=\"text\" name=\"user\" size=\"30\"></td>");
		out.println("\t</tr>");
		out.println("\t<tr>");
		out.println("\t\t<td>Password:</td>");
		out.println("\t\t<td><input type=\"password\" name=\"pass\" size=\"30\"></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<p><input type=\"submit\" value=\"Change\"></p>");
		out.println("</form>");
	}
}