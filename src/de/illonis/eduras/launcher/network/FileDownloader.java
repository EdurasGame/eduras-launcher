package de.illonis.eduras.launcher.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.swing.SwingWorker;

import de.illonis.eduras.launcher.tools.PathFinder;

public class FileDownloader extends SwingWorker<Void, Void> {

	private static final int BUFFER_SIZE = 2048;

	private final URL source;
	private final URI target;
	private final long fileSize;
	private boolean ok = false;
	private Exception error;

	public FileDownloader(DownloadFile f, String baseUrl)
			throws MalformedURLException {
		source = new URL(baseUrl + f.getFileName());
		target = PathFinder.findFile(f.getFileName());
		fileSize = f.getFileSize();
	}

	@Override
	protected Void doInBackground() {
		System.out.println("Started downloader for " + source.toString()
				+ " to " + target.getPath());
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
				double p = (double) totalBytesRead * 100 / fileSize;
				setProgress((int) p);
			}
			ok = true;
		} catch (IOException e) {
			error = e;
			ok = false;
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

		return null;
	}

	public boolean isOk() {
		return ok;
	}

	public Exception getError() {
		return error;
	}

}
