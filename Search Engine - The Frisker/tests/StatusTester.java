
/**
 * Demonstrates how to use the {@link Status} enum type. Includes how to
 * create a {@link Status} object from the name and ordinal.
 *
 * @see Status
 * @see StatusTester
 */
public class StatusTester {
	/**
	 * Shows all of the possible {@link Status} enum types, and how to
	 * create a {@link Status} enum type from its name or ordinal.
	 *
	 * @param args - unused
	 */
	public static void main(String[] args) {
		/*
		 * Format string for outputting the Status enum name, ordinal,
		 * and message.
		 */
		String format = "%s (%d): %s%n";

		// Prints all of the defined Status enum types.
		for (Status s : Status.values()) {
			System.out.printf(format, s.name(), s.ordinal(), s.message());
		}

		System.out.println();

		// Create a Status enum type directly and display its info.
		Status s = Status.OK;
		System.out.printf(format, s.name(), s.ordinal(), s.message());

		// Create a Status enum type from its name.
		String name = "ERROR";
		s = Status.valueOf(name);
		System.out.printf(format, s.name(), s.ordinal(), s.message());

		// Create a Status enum type from its ordinal.
		int ordinal = 2;
		s = Status.values()[ordinal];
		System.out.printf(format, s.name(), s.ordinal(), s.message());
	}
}
