package de.illonis.eduras.launcher.network;

public class HashTestException extends Exception {

	private static final long serialVersionUID = 1L;

	public HashTestException(String filename) {
		super("Hash test failed for " + filename + ".");
	}
}
