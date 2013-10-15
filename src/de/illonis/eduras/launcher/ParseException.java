package de.illonis.eduras.launcher;

public class ParseException extends Exception {

	public ParseException(Exception e) {
		super(e.getMessage());
	}
}
