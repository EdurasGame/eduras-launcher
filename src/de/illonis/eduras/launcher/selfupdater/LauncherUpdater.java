package de.illonis.eduras.launcher.selfupdater;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.swing.JOptionPane;

import de.illonis.eduras.launcher.tools.PathFinder;

/**
 * An updater tool that replaces the launcher with an updated version and starts
 * launcher again.
 * 
 * @author illonis
 * 
 */
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
			String path = new File(PathFinder.findFile(targetName))
					.getAbsolutePath();
			String[] cmdargs = new String[3];
			cmdargs[0] = "java";
			cmdargs[1] = "-jar";
			cmdargs[2] = path;
			try {
				Runtime.getRuntime().exec(cmdargs);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
