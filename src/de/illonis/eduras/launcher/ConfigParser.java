package de.illonis.eduras.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import de.illonis.eduras.launcher.network.VersionChecker;
import de.illonis.eduras.launcher.tools.PathFinder;

/**
 * Handles access to config file.
 * 
 * @author illonis
 * 
 */
public class ConfigParser {
	private final static String CONFIG_FILE = "config.ini";

	// init default values
	private double version = 0;
	private final HashMap<String, Object> otherConfigs;

	public ConfigParser() {
		otherConfigs = new HashMap<String, Object>();
		otherConfigs.put("gameJar", "eduras.jar");
		otherConfigs.put("updateUrl", VersionChecker.DEFAULT_VERSION_URL);
		otherConfigs.put("metaServer", "eduras.jar");
	}

	void setVersion(double version) {
		this.version = version;
	}

	public double getVersion() {
		return version;
	}

	private void saveValue(String key, Object value, PrintWriter writer) {
		writer.println(key + "=" + value);
	}

	/**
	 * Saves values to config file.<br>
	 * <b>Important:</b> You must call {@link #load()} before to make sure all
	 * parameters are loaded.
	 * 
	 * @throws IOException
	 */
	public void save() throws IOException {

		File f = new File(PathFinder.findFile(CONFIG_FILE));
		PrintWriter writer = new PrintWriter(f);
		saveValue("version", version, writer);
		for (String k : otherConfigs.keySet()) {
			Object v = otherConfigs.get(k);
			saveValue(k, v, writer);
		}
		writer.flush();
		writer.close();
	}

	/**
	 * Loads values from config file.
	 * 
	 * @throws ParseException
	 */
	public void load() throws ParseException {
		try {
			File f = new File(PathFinder.findFile(CONFIG_FILE));
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] kv = line.split("=");
				switch (kv[0]) {
				case "version":
					try {
						version = Double.parseDouble(kv[1]);
					} catch (NumberFormatException e) {
						throw new ParseException(e);
					}
					break;
				default:
					otherConfigs.put(kv[0], kv[1]);
					break;
				}
			}

			reader.close();
		} catch (IOException e) {
			throw new ParseException(e);
		}
	}

	public void set(String key, String value) {
		otherConfigs.put(key, value);
	}

	public Object getValue(String key) {
		Object val = otherConfigs.get(key);
		if (val == null) {
			return "!" + key + "!";
		}
		return val;
	}
}
