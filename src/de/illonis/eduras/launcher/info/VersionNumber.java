package de.illonis.eduras.launcher.info;

/**
 * Represents a version number.<br>
 * <p>
 * Version numbers consist of a maximum of {@value #MAX_NUMBERS} parts where
 * every part is a decimal number greater or equal than 0. Parts are
 * concatenated by points to build the full version number. Version numbers can
 * easily be compared using {@link #compareTo(VersionNumber)}.
 * </p>
 * <p>
 * Call the {@link #toString()} method to receive a short string representation.
 * Use {@link #toString(boolean)} to also receive a long version.
 * </p>
 * <b>Examples:</b>
 * <ul>
 * <li>1.2 (1.2.0)</li>
 * <li>1.3.1</li>
 * <li>2 (2.0.0)</li>
 * </ul>
 * 
 * @author illonis
 * 
 */
public class VersionNumber implements Comparable<VersionNumber> {

	private final static int MAX_NUMBERS = 3;

	private int[] numbers;

	/**
	 * Creates a new version number that parses given string.
	 * 
	 * @param stringValue
	 *            the string to parse.
	 * @throws NumberFormatException
	 *             if the string could not be parsed.
	 */
	public VersionNumber(String stringValue) {
		numbers = new int[MAX_NUMBERS];
		parseString(stringValue);
	}

	private void parseString(String stringValue) {
		String[] digits = stringValue.trim().split("\\.");
		if (digits.length > MAX_NUMBERS)
			throw new NumberFormatException(
					"String has to many version digits: " + digits.length);

		int i;
		for (i = 0; i < digits.length; i++) {
			numbers[i] = Integer.parseInt(digits[i]);
		}
		for (; i < MAX_NUMBERS; i++) {
			numbers[i] = 0;
		}
	}

	@Override
	public String toString() {
		return toString(true);
	}

	/**
	 * Returns the string representation of this version number.
	 * 
	 * @param omitTrailingZeros
	 *            if true, trailing zeros will be removed. 1.0.0 becomes 1,
	 *            3.2.0 becomes 3.2 etc.
	 * @return version string.
	 */
	public String toString(boolean omitTrailingZeros) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < MAX_NUMBERS; i++) {
			b.append(numbers[i]);
			b.append('.');
		}
		String string = b.substring(0, b.length() - 1);
		if (omitTrailingZeros) {
			while (string.endsWith(".0")) {
				string = string.substring(0, string.length() - 2);
			}
		}
		return string;
	}

	@Override
	public int compareTo(VersionNumber o) {
		for (int i = 0; i < MAX_NUMBERS; i++) {
			if (numbers[i] == o.numbers[i])
				continue;
			else if (numbers[i] < o.numbers[i])
				return -1;
			else
				return 1;
		}
		return 0;
	}

	public boolean isNull() {
		for (int i = 0; i < MAX_NUMBERS; i++) {
			if (numbers[i] != 0)
				return false;
		}
		return true;
	}
}
