package de.illonis.eduras.launcher.tools;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;

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
		URI uri;
		try {
			URL url = ClassLoader.getSystemClassLoader().getResource(".");
			if (url != null) {
				uri = url.toURI();
				// (PathFinder.class.getProtectionDomain().getCodeSource().getLocation().getPath());
				URI parent = uri.resolve(".");
				return parent.toURL();
			} else {
				// try another way
				CodeSource source = PathFinder.class.getProtectionDomain()
						.getCodeSource();
				if (source != null) {
					URL url2 = source.getLocation();
					return url2;
				} else
					throw new RuntimeException(
							"Base directory could not be resolved.");
			}
		} catch (URISyntaxException | MalformedURLException e) {
			System.out.println("base dir not found.");
			return null;
		}
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

	/**
	 * Retrieves the name of the currently running jar file.
	 * 
	 * @return the name of the jar file.
	 * @throws NoJarFileException
	 *             if the program is running from eclipse or not from a jar.
	 */
	public static String getJarName() throws NoJarFileException {
		String path;
		String s = PathFinder.class.getName().replace('.', '/') + ".class";
		URL url = PathFinder.class.getClassLoader().getResource(s);

		try {
			path = URLDecoder.decode(url.getPath(), "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new NoJarFileException();
		}
		if (path.startsWith("file:")) {
			int end = path.lastIndexOf(".jar!");
			int begin = path.lastIndexOf("/", end) + 1;
			return path.substring(begin, end + 4);
		} else {
			throw new NoJarFileException();
		}
	}
}
