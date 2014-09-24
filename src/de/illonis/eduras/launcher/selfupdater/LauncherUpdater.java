package de.illonis.eduras.launcher.selfupdater;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

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
		int n = 1;
		while (n < 10) {
			try {
				System.out.println("replacing old launcher (Try " + n
						+ " of 10)");
				Files.copy(downloadedPath, targetPath,
						StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Replaced old launcher.");
				break;
			} catch (IOException e) {
				e.printStackTrace();
				try {
					Thread.sleep(300);
				} catch (InterruptedException ex) {
				}
			}
			n++;
		}
		if (n == 10) {
			JOptionPane.showMessageDialog(null,
					"Replacing old launcher failed after 10 attempts.");
		}

		String[] cmdargs = new String[3];
		cmdargs[0] = "java";
		cmdargs[1] = "-jar";
		cmdargs[2] = targetPath.toString();
		System.out.println("Starting new launcher with "
				+ Arrays.toString(cmdargs));
		try {
			Runtime.getRuntime().exec(cmdargs);
			System.exit(0);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Could not start new launcher. Please try manually.");
			e.printStackTrace();
		}
	}
}
