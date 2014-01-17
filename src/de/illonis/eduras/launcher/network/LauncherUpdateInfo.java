package de.illonis.eduras.launcher.network;

import de.illonis.eduras.launcher.info.VersionNumber;

/**
 * Holds information about current launcher version on server.
 * 
 * @author illonis
 * 
 */
public class LauncherUpdateInfo {

	private final String note;
	private final VersionNumber version;
	private final String baseUrl;
	private final DownloadFile newLauncherFile, updaterFile;

	public LauncherUpdateInfo(String note, VersionNumber launcherVersion,
			String baseUrl, DownloadFile newLauncherFile,
			DownloadFile updaterFile) {
		this.note = note;
		this.version = launcherVersion;
		this.baseUrl = baseUrl;
		this.newLauncherFile = newLauncherFile;
		this.updaterFile = updaterFile;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public VersionNumber getVersion() {
		return version;
	}

	public String getNote() {
		return note;
	}

	public DownloadFile getNewLauncherFile() {
		return newLauncherFile;
	}

	public DownloadFile getUpdaterFile() {
		return updaterFile;
	}
}
