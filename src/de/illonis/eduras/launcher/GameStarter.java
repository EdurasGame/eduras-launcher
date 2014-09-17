package de.illonis.eduras.launcher;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

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

	public GameStarter(LauncherGui gui, URI jarFile) {
		super("GameStarter");
		this.gui = gui;
		String path = new File(jarFile).getAbsolutePath();
		args.add(path);
	}

	public GameStarter(LauncherGui gui, String targetName) {
		this(gui, PathFinder.findFile(targetName));
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
		String[] cmdargs = new String[args.size() + 2];
		cmdargs[0] = "java";
		cmdargs[1] = "-jar";
		int i = 2;
		for (String arg : args) {
			cmdargs[i++] = arg;
		}

		try {
			System.out.println("executing: " + Arrays.toString(cmdargs));
			Process p = Runtime.getRuntime().exec(cmdargs);
			gui.setStatus("Game is running...");
			p.waitFor();
			if (p.exitValue() != 0) {
				Scanner s = new Scanner(new BufferedInputStream(
						p.getErrorStream()));
				StringBuilder error = new StringBuilder();
				while (s.hasNextLine()) {
					error.append(s.nextLine());
				}
				s.close();
				gui.showError("Error starting game", error.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
