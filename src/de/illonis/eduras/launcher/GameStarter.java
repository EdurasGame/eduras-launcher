package de.illonis.eduras.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import javax.swing.JOptionPane;

import de.illonis.eduras.launcher.tools.PathFinder;

public class GameStarter extends Thread {

	public GameStarter() {
		setName("GameStarter");

	}

	@Override
	public void run() {
		ConfigParser config = new ConfigParser();
		try {
			config.load();
		} catch (ParseException e1) {
			return;
		}

		URI path = PathFinder.findFile(config.getValue("gameJar").toString());

		try {
			Process proc = Runtime.getRuntime().exec(
					"java -jar " + path.getPath());

			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {

			}

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
