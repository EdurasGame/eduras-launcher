package de.illonis.eduras.launcher.installer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.swing.SwingWorker;

public class InstallTask extends SwingWorker<Boolean, Void> {

	public final static String INSTALL_JAR = "eduras.jar";

	private final InstallFinishedListener listener;
	private final Path target;
	private String error = "";

	public InstallTask(Path target, InstallFinishedListener listener) {
		this.listener = listener;
		this.target = target;
	}

	@Override
	protected Boolean doInBackground() {
		InputStream input = getClass().getResourceAsStream(
				"/eduras-launcher.jar");

		if (input == null) {
			System.out.println("internal setup file not found.");
			return false;
		}

		Path targetJar = target.resolve(INSTALL_JAR);
		try {
			Files.copy(input, targetJar, StandardCopyOption.REPLACE_EXISTING);
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
