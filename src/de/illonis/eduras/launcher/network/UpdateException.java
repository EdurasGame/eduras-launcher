package de.illonis.eduras.launcher.network;

public class UpdateException extends Exception {

	private Exception detailException;

	public UpdateException(Exception e) {
		super(e.getMessage());
		this.detailException = e;
	}

	public Exception getDetailException() {
		return detailException;
	}
}
