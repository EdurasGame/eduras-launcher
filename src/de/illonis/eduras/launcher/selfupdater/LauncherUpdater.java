package de.illonis.eduras.launcher.selfupdater;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.swing.JOptionPane;

import de.illonis.eduras.launcher.GameStarter;
import de.illonis.eduras.launcher.tools.PathFinder;

public class LauncherUpdater {

	public static void main(String[] args) {
		if (args.length < 2) {
			JOptionPane.showMessageDialog(null,
					"This file cannot be started manually.");
			return;
		}

		String targetName = args[0];
		String downloadedName = args[1];

		Path downloadedPath = Paths.get(PathFinder.findFile(downloadedName));
		Path targetPath = Paths.get(PathFinder.findFile(targetName));

		if (Files.exists(downloadedPath)) {
			System.out.println("Waiting 2sek...");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				return;
			}
			System.out.println("Updating launcherfile...");
			try {
				Files.move(downloadedPath, targetPath,
						StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Starting launcher");
			GameStarter st = new GameStarter(targetName);
			st.start();
			try {
				st.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
