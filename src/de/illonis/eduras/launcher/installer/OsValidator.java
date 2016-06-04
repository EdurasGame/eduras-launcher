package de.illonis.eduras.launcher.installer;

import java.nio.file.Path;
import java.nio.file.Paths;

public class OsValidator {

	private static String OS = System.getProperty("os.name").toLowerCase();

	public static boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

	public static boolean isMac() {
		return (OS.indexOf("mac") >= 0);
	}

	public static boolean isUnix() {
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
	}

	public static boolean isSolaris() {
		return (OS.indexOf("sunos") >= 0);
	}

	public static Path findDefaultFolder() {
		String homeDir = System.getProperty("user.home");
		if (OsValidator.isWindows() || homeDir == null) {
			homeDir = "C:/Spiele/";
		}

		Path p = Paths.get(homeDir, "eduras/");

		return p;
	}

}
