package de.illonis.eduras.launcher.network;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Holds dataset for a downloaded file.
 * 
 * @author illonis
 * 
 */
public final class DownloadFile {

	private final String fileName;
	private final long fileSize;
	private final String sha256;

	/**
	 * Creates a new object.
	 * 
	 * @param name
	 *            the filename (without path).
	 * @param size
	 *            the filesize in bytes.
	 * @param hash
	 *            the SHA-256 hash value of that file.
	 */
	public DownloadFile(String name, long size, String hash) {
		this.fileName = name;
		this.fileSize = size;
		this.sha256 = hash;
	}

	public String getFileName() {
		return fileName;
	}

	/**
	 * @return filesize in bytes.
	 */
	public long getFileSize() {
		return fileSize;
	}

	public String getHash() {
		return sha256;
	}

	/**
	 * Computes SHA-256 hash for given file
	 * 
	 * @param file
	 *            the file to compute hash from.
	 * @return SHA-256 hash in hexadecimal format.
	 */
	public static String computeHash(Path file) {
		try {
			InputStream in = Files.newInputStream(file);
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] md = new byte[8192];

			for (int n = 0; (n = in.read(md)) > -1;)
				digest.update(md, 0, n);

			byte[] hashed = digest.digest();
			StringBuffer sb = new StringBuffer();
			for (byte b : hashed) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (IOException | NoSuchAlgorithmException e) {
			return "";
		}
	}

	/**
	 * Validates a file against given hash.
	 * 
	 * @param file
	 *            the file on local file system.
	 * @param hash
	 *            the SHA-256 hash in hexadecimal format.
	 * @return true if validation succeeded, false otherwise.
	 */
	public static boolean validateHash(Path file, String hash) {
		return hash.equals(computeHash(file));
	}
}
