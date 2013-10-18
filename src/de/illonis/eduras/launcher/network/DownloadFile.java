package de.illonis.eduras.launcher.network;

public final class DownloadFile {

	private final String fileName;
	private final long fileSize;
	private final String md5;

	public DownloadFile(String name, long size) {
		this.fileName = name;
		this.fileSize = size;
		this.md5 = "";
	}

	public String getFileName() {
		return fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public String getMd5() {
		return md5;
	}

}
