package de.illonis.eduras.launcher.installer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.swing.SwingWorker;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class InstallTask extends SwingWorker<Boolean, Void> {
	private final static String DOWNLOAD_URL_RES = "file:///home/illonis/git/eduras-res/master.zip";
	private final static String DOWNLOAD_URL_CLIENT = "file:///home/illonis/git/eduras/dist/jars/eduras-client.jar";

	public final static String CLIENT_JAR = "eduras.jar";

	private final InstallFinishedListener listener;
	private final Path target;
	private String error = "";

	public InstallTask(Path target, InstallFinishedListener listener) {
		this.listener = listener;
		this.target = target;
	}

	@Override
	protected Boolean doInBackground() {
		try {
			Path tmpFile = Files.createTempFile("eduras_data", ".zip");
			URL resUrl = new URL(DOWNLOAD_URL_RES);

			ReadableByteChannel rbc = Channels.newChannel(resUrl.openStream());
			try (FileOutputStream fos = new FileOutputStream(tmpFile.toFile())) {
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			}

			try {
				ZipFile f = new ZipFile(tmpFile.toFile());
				f.extractAll(target.toString());
				deleteFileOrFolder(target.resolve("src"));
			} catch (ZipException e) {
				e.printStackTrace();
				error = e.getMessage();
				return false;
			}
			URL clientUrl = new URL(DOWNLOAD_URL_CLIENT);
			rbc = Channels.newChannel(clientUrl.openStream());
			Path clientPath = target.resolve(CLIENT_JAR);

			try (FileOutputStream fos = new FileOutputStream(clientPath.toFile())) {
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			}
			
			Files.move(target.resolve("starters/start.sh"), target.resolve("start.sh"));
			Files.move(target.resolve("starters/start.bat"), target.resolve("start.bat"));

		} catch (IOException e) {
			e.printStackTrace();
			error = e.getMessage();
			return false;
		}
		return true;
	}

	@Override
	protected void done() {
		listener.onSetupFinished();
	}

	public String getError() {
		return error;
	}

	
	// from Sean Patrick Floyd - http://stackoverflow.com/a/3775893
	public static void deleteFileOrFolder(final Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(final Path file, final IOException e) {
				return handleException(e);
			}

			private FileVisitResult handleException(final IOException e) {
				e.printStackTrace(); // replace with more robust error handling
				return FileVisitResult.TERMINATE;
			}

			@Override
			public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
				if (e != null)
					return handleException(e);
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	};
}
