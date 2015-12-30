import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Class that handles parsing an array of arguments into flag/value pairs. A
 * flag is considered to be a non-null String that starts with a "-" dash
 * symbol. A value optionally follows a flag, and must not start with a "-"
 * dash symbol.
 *
 */
public class ArgumentParser {

	/** Stores flag/value pairs of arguments. */
	private final HashMap<String, String> argumentMap;

	public ArgumentParser(String[] args) {
		
		argumentMap = new HashMap<>();
		parseArgs(args, args.length);
	}
	
	/**
	 * Parses the provided array of arguments into flag/value pairs, storing
	 * the results in an internal map.
	 *
	 * @param arguments to parse
	 */
	public void parseArgs(String[] args, int size) {

		int i = 0;

		if((size == 1) && isFlag(args[0])) {
			insert(args[0], null);
		}

		for(; i < size - 1; i++) {

			int j = i + 1;

			String arg1 = args[i];
			String arg2 = args[j];

			if(isFlag(arg1) && isFlag(arg2)) {
				insert(arg1, null);
			}
			
			if(isFlag(arg1) && (arg2 == null)) {
				insert(arg1, null);
			}

			else if(isFlag(arg1) && isValue(arg2)) {
				insert(arg1, arg2);
			}

			else if(isFlag(arg2)) {
				insert(arg2, null);
			}
		}
	}

	/** Helper method for parseArgs for adding to map. */
	private void insert(String flag, String value) {

		argumentMap.put(flag, value);
	}

	/** Checks if the given string is in fact a flag. */
	private static boolean isFlag(String text) {

		if((text == null) || text.isEmpty()) {
			return false;
		}

		if(text.startsWith("-") && (text.length() > 1)) {
			return true;
		}

		return false;
	}

	/** Checks if the given string is a value or not. */
	public static boolean isValue(String text) {

		if((text == null) || text.isEmpty() || text.startsWith("-")) {
			return false;
		}

		return true;
	}
	
	/** Checks if the map has a specific flag. */
	public boolean hasFlag(String flag) {
		return argumentMap.containsKey(flag);
	}
	
	/** Gets the value associated with the given flag. */
	public String getValue(String flag) {
		return argumentMap.get(flag);
	}
	
	/**
	 * Returns the number of flags stored in the argument map.
	 *
	 * @return number of flags stored in the argument map
	 */
	public int numFlags() {

		return argumentMap.size();
	}
	
	/** Returns the URL given for the given flag.*/
	public URI getURI(String flag) {
		
		if(hasFlag(flag)) {
			URI link = null;

			try {
				link = new URI(getValue(flag));
			}
			catch (URISyntaxException e) {
			}
			return link;
		} 
		return null;
	}
	
	/** Returns true if the given arguments allows parsing.
	 *  And that the given values for the flags are valid. 
	 *  Returns false otherwise.
	 */
	public Path getPath(String flag) {

		if(hasFlag(flag)) {
			
			String path = getValue(flag);
			
			if(path != null) {
				Path p = Paths.get(path);
				return p;
			}
		}
		return null;
	}
	
	/** Method for checking if string can be parsed into an integer.*/
	public boolean isInteger(String string) {
	    
		if(string == null || string.isEmpty()) {
			return false;
		}
		
		else {
			try { 
				int threads = Integer.parseInt(string); 
				if(threads > 0) {
					return true;
				}
				return false;
			}
			catch(NumberFormatException nfe) { 
				return false; 
			}
		}
	}
}
