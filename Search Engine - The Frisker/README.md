# LoginDemo

This demo demonstrates how to create a simple webapp that supports user registration and login. It uses SQL and JDBC, Jetty and Servlets, Cookies, and Enums. Videos for some of these classes may be found on Canvas.

## Configuration Files

These files you must download and modify with your own login information. Specifically:

- `database.properties` - Enter your MySQL username and password. Use `sql.cs.usfca.edu` as the hostname if you are running locally on the CS Network, or `localhost:3307` and an SSH tunnel if you are running remotely. Make sure this is in your root project directory.

- `tunnel.command` - Enter your CS username. On a Mac, you can make this file executable. Otherwise, copy/paste the command into a Terminal or Console window. On Windows, you will need to use Putty to setup the SSH Tunnel.

- `log4j2.xml` - No changes necessary. Make sure this is in a source code folder.

## Database Files

Make sure these files run properly before trying the entire example. Specifically:

- `DatabaseConnector.java` - When run, this will test whether you are able to successfully connect to the database using your `database.properties` file.

- `LoginDatabaseHandler.java` - Will actually setup the proper tables in your database for this example. Used by all the servlets.

- `LoginDatabaseTester.java` - Will test that the `LoginDatabaseHandler.java` class is able to query the database.

There are videos available for all of these classes (although they do not cover every single method). I suggest you place these in a separate source code folder (e.g. named `db`) in your project.

## Utility Files

The following files demonstrate enum types, and are used in various places by the servlets and database handler. 

- `Month.java` and `MonthTester.java` - Demonstrate basic enum types.

- `Status.java` and `StatusTester.java` - Demonstrate more complex enum types, used by servlets to avoid XSS attacks.

- `StringUtilities.java` - Used by servlets. Contains many helper methods to get current date and time, check the validity of a string, and handle calculating password hashes. The password hash functionality could be replaced by a third-party library.

There are videos available for all of these classes. I suggest you place these in a separate source code folder (e.g. named `util`) in your project.

## Servlet Files

The following files actually startup the Jetty server, and handle requests. These you should try to run after everything else is working.

- `LoginServer.java` - Starts the server and configures the servlets.

- `LoginBaseServlet.java` - Extended by all other servlets, contains some base methods for dealing with cookies and querying the database.

- `LoginRedirectServlet.java` - When a user visits the server, this servlet determines where the user should be redirected (e.g. `/login` if the user is not logged in, or `/welcome` if the user is logged in).

- `LoginUserServlet.java` - Handles processing user login and logout.

- `LoginRegisterServlet.java` - Handles processing user registration.

- `LoginWelcomeServlet.java` - Displays user information. This servlet should eventually be replaced with one or more servlets that do something more... useful.

These classes are discussed in class, and do not currently have videos.
