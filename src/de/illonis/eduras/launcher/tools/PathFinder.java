package de.illonis.eduras.launcher.tools;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;

import de.illonis.eduras.launcher.EdurasLauncher;

/**
 * Simplifies resource locating.
 * 
 * @author illonis
 * 
 */
public class PathFinder {

	/**
	 * Retrieves the path where the programm jar is located.
	 * 
	 * @return the jar's location.
	 */
	public static URI getBaseDir() {
		try {
			// try another way
			CodeSource source = PathFinder.class.getProtectionDomain()
					.getCodeSource();
			if (source != null) {
				URI url2 = source.getLocation().toURI();
				if (url2.toString().endsWith(".jar")) {
					return url2.resolve(".");
				}
				return url2;
			} else {
				URI url = ClassLoader.getSystemClassLoader().getResource(".")
						.toURI();
				if (url != null) {
					URI parent = url.resolve("../");
					return parent;
				} else {
					throw new RuntimeException(
							"Base directory could not be resolved.");
				}
			}
		} catch (URISyntaxException e) {
			return null;
		}
	}

	public static Path getDataPath() {
		Path p = Paths.get(getBaseDir());
		return p.resolve(EdurasLauncher.DATA_PATH);
	}

	/**
	 * Returns an url that points to a file relative to program folder.<br>
	 * The path is built by combining {@link #getBaseDir()} with the fileName.
	 * 
	 * @param fileName
	 *            the file name.
	 * @return an uri pointing to that file.
	 */
	public static URI findFile(String fileName) {
		return PathFinder.getBaseDir().resolve(fileName);
	}
}
