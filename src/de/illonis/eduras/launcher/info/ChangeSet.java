package de.illonis.eduras.launcher.info;

import java.util.LinkedList;

import de.illonis.eduras.launcher.network.ConfigChange;
import de.illonis.eduras.launcher.network.DownloadFile;

public class ChangeSet {

	private final VersionNumber from;
	private final LinkedList<DownloadFile> files;
	private final String baseUrl;
	private final LinkedList<String> deleteFiles;
	private final LinkedList<ConfigChange> configChanges;
	private final VersionNumber target;
	private final long fileSize;
	private final String note;

	public ChangeSet(String note, VersionNumber from, VersionNumber target,
			long fileSize, String baseUrl, LinkedList<DownloadFile> newFiles,
			LinkedList<String> deleteFiles,
			LinkedList<ConfigChange> configChanges) {
		this.from = from;
		this.note = note;
		this.target = target;
		this.files = new LinkedList<DownloadFile>(newFiles);
		this.deleteFiles = new LinkedList<String>(deleteFiles);
		this.baseUrl = baseUrl;
		this.fileSize = fileSize;
		this.configChanges = new LinkedList<ConfigChange>(configChanges);
	}

	public VersionNumber getFrom() {
		return from;
	}

	public String getNote() {
		return note;
	}

	public long getFileSize() {
		return fileSize;
	}

	public LinkedList<DownloadFile> getFiles() {
		return new LinkedList<DownloadFile>(files);
	}

	public int getNumFiles() {
		return files.size();
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public LinkedList<String> getDeleteFiles() {
		return new LinkedList<String>(deleteFiles);
	}

	public LinkedList<ConfigChange> getConfigChanges() {
		return configChanges;
	}

	public VersionNumber getTarget() {
		return target;
	}
}