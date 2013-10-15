package de.illonis.eduras.launcher.network;

import java.util.Date;
import java.util.LinkedList;

public class VersionInformation {

	private final double version;
	private final long filesize;
	private final Date releaseDate;
	private final LinkedList<DownloadFile> files;
	private final String baseUrl;
	private final LinkedList<String> deleteFiles;
	private final LinkedList<ConfigChange> configChanges;
	private final double launcherVersion;

	public VersionInformation(double version, long filesize, Date release,
			String baseUrl, LinkedList<DownloadFile> newFiles,
			LinkedList<String> deleteFiles,
			LinkedList<ConfigChange> configChanges, double launcherVersion) {
		this.version = version;
		this.filesize = filesize;
		this.releaseDate = release;
		this.files = new LinkedList<DownloadFile>(newFiles);
		this.deleteFiles = new LinkedList<String>(deleteFiles);
		this.baseUrl = baseUrl;
		this.configChanges = new LinkedList<ConfigChange>(configChanges);
		this.launcherVersion = launcherVersion;
	}

	public long getFilesize() {
		return filesize;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public double getVersion() {
		return version;
	}

	public LinkedList<DownloadFile> getFiles() {
		return new LinkedList<DownloadFile>(files);
	}

	public int getNumFiles() {
		return files.size();
	}

	public LinkedList<String> getDeleteFiles() {
		return new LinkedList<String>(deleteFiles);
	}

	public LinkedList<ConfigChange> getConfigChanges() {
		return configChanges;
	}

	public double getLauncherVersion() {
		return launcherVersion;
	}
}
