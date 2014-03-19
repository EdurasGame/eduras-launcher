package de.illonis.eduras.launcher;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;

import de.illonis.eduras.launcher.tools.PathFinder;

/**
 * Starts a runnable jar file.
 * 
 * @author illonis
 * 
 */
public class GameStarter extends Thread {

	private final String execString;
	private final LinkedList<String> args = new LinkedList<String>();

	public GameStarter(URI jarFile) {
		super("GameStarter");
		String path = new File(jarFile).getAbsolutePath();
		execString = "java -jar " + path;
	}

	public GameStarter(String targetName) {
		this(PathFinder.findFile(targetName));
	}

	/**
	 * Sets the command line arguments used when executing the jar. This has to
	 * be set before thread is started.
	 * 
	 * @param args
	 *            any number of arguments.
	 */
	public void setArguments(String... args) {
		for (int i = 0; i < args.length; i++) {
			this.args.add(args[i]);
		}
	}

	@Override
	public void run() {
		String cmd = execString;
		for (String arg : args) {
			cmd += " " + arg;
		}

		try {
			System.out.println("executing: " + cmd);
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
