package de.illonis.eduras.launcher;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;

import de.illonis.eduras.launcher.gui.LauncherGui;
import de.illonis.eduras.launcher.tools.PathFinder;

/**
 * Starts a runnable jar file.
 * 
 * @author illonis
 * 
 */
public class GameStarter extends Thread {

	private final LinkedList<String> args = new LinkedList<String>();
	private final LauncherGui gui;

	public GameStarter(LauncherGui gui) {
		super("GameStarter");
		this.gui = gui;
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
		gui.setStatus("Starting game..");
		String[] cmdargs = new String[args.size() + 4];
		cmdargs[0] = "java";
		Path currentPath = Paths.get(PathFinder.getBaseDir());
		String first = currentPath.resolve("game/eduras-client.jar").toString();
		String second = currentPath.resolve("game/data/lib").toString() + "/*";
		cmdargs[1] = "-classpath";
		cmdargs[2] = first + System.getProperty("path.separator") + second;
		cmdargs[3] = "de.illonis.eduras.gameclient.EdurasClient";
		int i = 4;
		for (String arg : args) {
			cmdargs[i++] = arg;
		}

		System.out.println("executing: " + Arrays.toString(cmdargs));
		try {
			Runtime.getRuntime().exec(cmdargs);
			gui.setStatus("Game is running...");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
