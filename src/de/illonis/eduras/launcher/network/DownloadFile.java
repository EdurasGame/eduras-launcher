package de.illonis.eduras.launcher.network;

public final class DownloadFile {

	private String fileName;
	private long fileSize;

	public DownloadFile(String name, long size) {
		this.fileName = name;
		this.fileSize = size;
	}

	public String getFileName() {
		return fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

}
