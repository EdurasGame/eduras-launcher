package de.illonis.eduras.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;

import de.illonis.eduras.launcher.EdurasLauncher.ReleaseChannel;
import de.illonis.eduras.launcher.info.VersionNumber;
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
	private final static String KEY_RELEASE_CHANNEL = "releaseChannel";

	// init default values
	private VersionNumber version = new VersionNumber("0");
	private final HashMap<String, String> otherConfigs;
	private final static HashMap<String, String> CONFIG_DEFAULTS;
	static {
		CONFIG_DEFAULTS = new HashMap<String, String>();
		CONFIG_DEFAULTS.put("gameJar", "eduras.jar");
		CONFIG_DEFAULTS.put("updateUrl", VersionChecker.STABLE_VERSION_URL);
		CONFIG_DEFAULTS.put("nightlyUrl", VersionChecker.NIGHTLY_VERSION_URL);
		CONFIG_DEFAULTS.put("betaUrl", VersionChecker.BETA_VERSION_URL);
		CONFIG_DEFAULTS.put(KEY_RELEASE_CHANNEL, ReleaseChannel.STABLE.name());
		CONFIG_DEFAULTS.put("metaserver", "http://illonis.dyndns.org:4324");
	}

	public ConfigParser() {
		otherConfigs = new HashMap<String, String>(CONFIG_DEFAULTS);
	}

	void setVersion(VersionNumber versionNumber) {
		this.version = versionNumber;
	}

	public VersionNumber getVersion() {
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
		otherConfigs.clear();
		version = new VersionNumber("0");
		otherConfigs.putAll(CONFIG_DEFAULTS);
		try {
			File f = new File(PathFinder.findFile(CONFIG_FILE));
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] kv = line.split("=");
				String key = kv[0];
				String val = (kv.length == 1) ? "" : kv[1];
				switch (key) {
				case "version":
					try {
						version = new VersionNumber(val);
					} catch (NumberFormatException e) {
						throw new ParseException(e);
					}
					break;
				default:
					otherConfigs.put(key, val);
					break;
				}
			}
			Date d;
			try {
				d = VersionChecker.DATE_FORMAT.parse(getValue("releaseDate",
						"0"));

				version.setReleaseDate(d);
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}

			reader.close();
		} catch (IOException e) {
			throw new ParseException(e);
		}
	}

	public void set(String key, String value) {
		otherConfigs.put(key, value);
	}

	public String getValue(String key, String defaultValue) {
		String val = getValue(key);
		if (val.equals("-"))
			return defaultValue;
		return val;
	}

	public String getValue(String key) {
		String val = otherConfigs.get(key);
		if (val == null) {
			return "-";
		}
		return val;
	}

	public void setRelease(ReleaseChannel type) {
		otherConfigs.put(KEY_RELEASE_CHANNEL, type.name());
	}

	public ReleaseChannel getReleaseChannel() {
		return ReleaseChannel.valueOf(otherConfigs.get(KEY_RELEASE_CHANNEL));
	}
}
