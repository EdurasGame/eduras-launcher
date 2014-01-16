package de.illonis.eduras.launcher.installer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import de.illonis.eduras.launcher.network.DownloadFile;
import de.illonis.eduras.launcher.network.FileDownloader;
import de.illonis.eduras.launcher.tools.PathFinder;

public class InstallTask extends SwingWorker<Boolean, Void> {

	public final static String INSTALL_JAR = "eduras.jar";

	private final static String LAUNCHER_BASE = "http://illonis.dyndns.org/eduras/update/";

	private final InstallFinishedListener listener;
	private final Path target;
	private String error = "";

	public InstallTask(Path target, InstallFinishedListener listener) {
		this.listener = listener;
		this.target = target;
	}

	@Override
	protected Boolean doInBackground() {

		DownloadFile f = new DownloadFile(INSTALL_JAR, Long.MAX_VALUE);
		try {
			FileDownloader dl = new FileDownloader(f, LAUNCHER_BASE);
			dl.execute();
			dl.get();
			if (!dl.isOk()) {
				error = dl.getError().getMessage();
				return false;
			}
		} catch (MalformedURLException | InterruptedException
				| ExecutionException e) {
			error = e.getMessage();
			e.printStackTrace();
			return false;
		}
		Path sourceJar = Paths.get(PathFinder.findFile(INSTALL_JAR));
		Path targetJar = target.resolve(INSTALL_JAR);

		try {
			Files.move(sourceJar, targetJar,
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			error = e.getMessage();
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	protected void done() {
		super.done();
		listener.onSetupFinished();
	}

	public String getError() {
		return error;
	}
}
