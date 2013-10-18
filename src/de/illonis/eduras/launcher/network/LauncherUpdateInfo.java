package de.illonis.eduras.launcher.network;

import de.illonis.eduras.launcher.info.VersionNumber;

public class LauncherUpdateInfo {

	private final String downloadName;
	private final String note;
	private final String updaterName;
	private final String baseUrl;
	private final VersionNumber version;
	private final long fileSize;
	private final long updaterFileSize;

	public LauncherUpdateInfo() {
		this("", new VersionNumber("0"), "", "", "", 0, 0);
	}

	public LauncherUpdateInfo(String note, VersionNumber launcherVersion,
			String updaterName, String baseUrl, String downloadedLauncher,
			long fileSize, long updaterFileSize) {
		this.downloadName = downloadedLauncher;
		this.note = note;
		this.updaterName = updaterName;
		this.version = launcherVersion;
		this.fileSize = fileSize;
		this.baseUrl = baseUrl;
		this.updaterFileSize = updaterFileSize;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public VersionNumber getVersion() {
		return version;
	}

	public String getDownloadName() {
		return downloadName;
	}

	public String getUpdaterName() {
		return updaterName;
	}

	public String getNote() {
		return note;
	}

	public long getFileSize() {
		return fileSize;
	}

	public long getUpdaterFileSize() {
		return updaterFileSize;
	}

}
