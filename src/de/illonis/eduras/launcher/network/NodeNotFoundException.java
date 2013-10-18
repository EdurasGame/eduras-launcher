package de.illonis.eduras.launcher.network;

public class NodeNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public NodeNotFoundException(String nodeName) {
		super("Could not find node: " + nodeName);
	}
}
