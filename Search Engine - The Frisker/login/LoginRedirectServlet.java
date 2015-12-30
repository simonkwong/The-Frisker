import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class LoginRedirectServlet extends LoginBaseServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		PrintWriter out = response.getWriter();
		
		out.printf("<head><link rel = \"shortcut icon\" href = \"http://oi44.tinypic.com/169q174.jpg\"></head>%n");
		out.printf("<a href = \"http://localhost:8080/search\"> <img src = \"http://oi40.tinypic.com/2mrtiio.jpg\" alt = \"FRISK\" height = \"133\" width = \"291\"></a>%n%n");
		
		if (getUsername(request) != null) {
			response.sendRedirect("/welcome");
		}
		else {
			response.sendRedirect("/login");
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		doGet(request, response);
	}
}
