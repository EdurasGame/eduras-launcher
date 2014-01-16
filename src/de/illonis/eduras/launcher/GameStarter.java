package de.illonis.eduras.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import de.illonis.eduras.launcher.tools.PathFinder;

public class GameStarter extends Thread {

	private String execString;
	private LinkedList<String> args = new LinkedList<String>();

	public GameStarter(URI jarFile) {
		String path = new File(jarFile).getAbsolutePath();
		execString = "java -jar " + path;
	}

	public GameStarter(String targetName) {
		super("GameStarter");
		URI uri = PathFinder.findFile(targetName);
		execString = "java -jar " + new File(uri).getAbsolutePath();
	}

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
			Process proc = Runtime.getRuntime().exec(cmd);

			try {
				// wait some time until game launched
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				return;
			}

			// check if game launch failed
			InputStream err = proc.getErrorStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					err));
			if (reader.ready()) {
				String line = reader.readLine();
				JOptionPane.showMessageDialog(null, line,
						"Error starting game", JOptionPane.ERROR_MESSAGE);
			}
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
