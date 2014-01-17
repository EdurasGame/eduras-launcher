package de.illonis.eduras.launcher.network;

/**
 * A config change dataset.
 * 
 * @author illonis
 * 
 */
public class ConfigChange {

	private final String key;
	private final String value;

	public ConfigChange(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

}
