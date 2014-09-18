package de.illonis.eduras.launcher;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import de.illonis.eduras.launcher.tools.PathFinder;

/**
 * Abstraction layer for the Eduras? configuration file that contains basic
 * information like website url and metaserver url.
 * 
 * @author illonis
 * 
 */
public class EdurasConfigFile {

	public final static String KEY_WEBSITE = "websiteUrl";
	public final static String KEY_METASERVER = "metaUrl";
	public static final String KEY_LAUNCHERVERSION = "launcherVersion";

	private final static String CONFIG_FILENAME = "config.ini";
	private final static HashMap<String, String> CONFIG_DEFAULTS;
	private final HashMap<String, String> configuration;

	static {
		CONFIG_DEFAULTS = new HashMap<String, String>();
		CONFIG_DEFAULTS.put(KEY_WEBSITE, "http://www.eduras.de/");
		CONFIG_DEFAULTS.put(KEY_METASERVER, "http://84.201.34.226:4324");
	}

	/**
	 * Creates a new config-file abstraction layer that is filled with default
	 * values.
	 * 
	 * @see #load()
	 */
	public EdurasConfigFile() {
		configuration = new HashMap<String, String>();
	}

	/**
	 * Saves values to config file.<br>
	 * <b>Important:</b> You must call {@link #load()} before to make sure all
	 * parameters are loaded.
	 * 
	 * @throws IOException
	 *             if file could not be saved.
	 * @throws URISyntaxException
	 */
	public void save() throws IOException, URISyntaxException {
		Path configFile = PathFinder.getDataPath().resolve(CONFIG_FILENAME);
		List<String> values = new LinkedList<String>();
		for (Entry<String, String> entry : configuration.entrySet()) {
			values.add(entry.getKey() + "=" + entry.getValue());
		}

		Files.write(configFile, values, StandardCharsets.UTF_8,
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
	}

	/**
	 * Loads values from config file. This will clear all unsaved configurations
	 * added/changed.
	 * 
	 * @see #save()
	 * 
	 * @throws IOException
	 *             if file could not be read.
	 * @throws URISyntaxException
	 */
	public void load() throws IOException, URISyntaxException {
		configuration.clear();
		configuration.putAll(CONFIG_DEFAULTS);
		Path configFile = PathFinder.getDataPath().resolve(CONFIG_FILENAME);
		List<String> entries = Files.readAllLines(configFile,
				StandardCharsets.UTF_8);
		for (String string : entries) {
			if (string.isEmpty())
				continue;
			String[] data = string.split("=");
			configuration.put(data[0], data[1]);
		}
	}

	/**
	 * Puts a configuration value. This replaces an existing value with given
	 * key. You should only put new values after loading the configuration.
	 * 
	 * @see #load()
	 * 
	 * @param key
	 *            the key.
	 * @param value
	 *            the new value.
	 */
	public void put(String key, String value) {
		configuration.put(key, value);
	}

	/**
	 * Returns a configuration value or given default value is key is not set.
	 * 
	 * @param key
	 *            the key.
	 * @param defaultValue
	 *            the default value returned if key not specified.
	 * @return the saved value or default value.
	 */
	public String getValue(String key, String defaultValue) {
		String val = getValue(key);
		if (val == null)
			return defaultValue;
		return val;
	}

	/**
	 * Returns a configuration value.
	 * 
	 * @param key
	 *            the key.
	 * @return the value for given key or <i>null</i> if key is not set.
	 */
	public String getValue(String key) {
		return configuration.get(key);
	}

}
