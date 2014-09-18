package de.illonis.eduras.launcher.selfupdater;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.swing.JOptionPane;

import de.illonis.eduras.launcher.tools.PathFinder;

/**
 * An updater tool that replaces the launcher with an updated version and starts
 * launcher again. This should be placed as "lupdater.jar" in the game folder as
 * long with the new launcher file.
 * 
 * @author illonis
 * 
 */
public class LauncherUpdater {

	public static void main(String[] args) {
		if (args.length != 1) {
			JOptionPane.showMessageDialog(null,
					"This file cannot be started manually.");
			return;
		}

		Path downloadedPath = Paths.get(PathFinder.findFile("launcher.jar"));
		Path targetPath = Paths.get(PathFinder.findFile("../eduras.jar"));

		try {
			Files.copy(downloadedPath, targetPath,
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Replacing old launcher failed.");
			e.printStackTrace();
			return;
		}

		String[] cmdargs = new String[3];
		cmdargs[0] = "java";
		cmdargs[1] = "-jar";
		cmdargs[2] = targetPath.toString();
		try {
			Runtime.getRuntime().exec(cmdargs);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Could not start new launcher. Please try manually.");
			e.printStackTrace();
		}
	}
}
