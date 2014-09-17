package de.illonis.eduras.launcher.tools;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
	public static URL getBaseDir() {
		try {
			// try another way
			CodeSource source = PathFinder.class.getProtectionDomain()
					.getCodeSource();
			if (source != null) {
				URL url2 = source.getLocation();
				return url2;
			} else {
				URL url = ClassLoader.getSystemClassLoader().getResource(".");
				if (url != null) {
					URL parent = new URL(url, "../");
					return parent;
				} else {
					throw new RuntimeException(
							"Base directory could not be resolved.");
				}
			}
		} catch (MalformedURLException e) {
			System.out.println("base dir not found.");
			return null;
		}
	}

	public static Path getDataPath() throws URISyntaxException {
		Path p = Paths.get(getBaseDir().toURI());
		return p.resolve(EdurasLauncher.DATA_PATH);
	}

	/**
	 * Returns an url that points to a file relative to program folder.<br>
	 * The path is built by combining {@link #getBaseDir()} with the fileName.
	 * 
	 * @param fileName
	 *            the file name.
	 * @return
	 */
	public static URI findFile(String fileName) {
		try {
			URI uri = new URL(PathFinder.getBaseDir(), fileName).toURI();
			return uri;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
}
