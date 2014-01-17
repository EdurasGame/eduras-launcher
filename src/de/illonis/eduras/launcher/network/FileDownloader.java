package de.illonis.eduras.launcher.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;

import javax.swing.SwingWorker;

import de.illonis.eduras.launcher.tools.PathFinder;

public class FileDownloader extends SwingWorker<Void, Void> {

	private static final int BUFFER_SIZE = 2048;

	private final URL source;
	private final URI target;
	private boolean ok = false;
	private final DownloadFile file;
	private Exception error;

	public FileDownloader(DownloadFile f, String baseUrl, String newFileName)
			throws MalformedURLException {
		System.out.println(f.getFileName());
		this.file = f;
		source = new URL(baseUrl + f.getFileName());
		target = PathFinder.findFile(newFileName);
	}

	public FileDownloader(DownloadFile f, String baseUrl)
			throws MalformedURLException {
		this(f, baseUrl, f.getFileName());

	}

	@Override
	protected Void doInBackground() {
		System.out.println("Started downloader for " + source.toString()
				+ " to " + target.getPath());
		// download file and validate if download succeeded
		ok = download() && validate();
		return null;
	}

	private boolean download() {
		long totalBytesRead = 0;
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			inputStream = source.openStream();
			File f = new File(target);
			f.createNewFile();
			outputStream = new FileOutputStream(f);

			int bytesRead = -1;
			byte[] buffer = new byte[BUFFER_SIZE];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
				totalBytesRead += bytesRead;
				double p = (double) totalBytesRead * 100 / file.getFileSize();
				setProgress((int) p);
			}
			return true;
		} catch (IOException e) {
			error = e;
			return false;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private boolean validate() {
		boolean ok = DownloadFile.validateHash(Paths.get(target),
				file.getHash());
		if (!ok)
			error = new HashTestException(file.getFileName());
		return ok;
	}

	public boolean isOk() {
		return ok;
	}

	public Exception getError() {
		return error;
	}

}
