package de.illonis.eduras.launcher;

public class ParseException extends Exception {

	private static final long serialVersionUID = 1L;
	private final Exception exception;

	public ParseException(Exception e) {
		super(e.getMessage());
		this.exception = e;
	}

	@Override
	public void printStackTrace() {
		exception.printStackTrace();
	}
}
