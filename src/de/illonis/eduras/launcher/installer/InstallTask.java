package de.illonis.eduras.launcher.installer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
		InputStream input = getClass().getResourceAsStream("/" + INSTALL_JAR);
		InputStream input2 = getClass().getResourceAsStream("icon.png");

		if (input == null || input2 == null) {
			System.out.println("internal setup file not found.");
			return false;
		}

		Path targetJar = target.resolve(INSTALL_JAR);
		Path targetIcon = target.resolve("icon.png");
		try {
			Files.copy(input, targetJar, StandardCopyOption.REPLACE_EXISTING);
			Files.copy(input2, targetIcon, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			error = e.getMessage();
			e.printStackTrace();
			return false;
		}

		createLauncherIcon();

		return true;
	}

	private void createLauncherIcon() {

		if (OsValidator.isWindows()) {
			/*
			 * copyDllsToTemp(); // Create a desktop icon for eduras JShellLink
			 * link = new JShellLink(); Path targetJar =
			 * target.resolve(INSTALL_JAR);
			 * 
			 * String filePath = "java -jar " + targetJar.toString();
			 * 
			 * try { link.setFolder(JShellLink.getDirectory("desktop"));
			 * link.setName("Eduras"); link.setPath(filePath); link.save(); }
			 * catch (Exception ex) { ex.printStackTrace(); }
			 */
		} else if (OsValidator.isUnix()) {
			// ubuntu case
			String dir = ".local/share/applications/";

			InputStream input = InstallTask.class
					.getResourceAsStream("eduras.desktop");

			if (input == null) {
				System.out.println("internal desktop file not found.");
				return;
			}

			String home = System.getProperty("user.home");
			Path parent = Paths.get(home, dir);
			Path target = parent.resolve("eduras.desktop");

			try {
				Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
				target.toFile().setExecutable(false);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private void copyDllsToTemp() {
		String[] dllFiles = { "jshortcut.dll", "jshortcut_x86.dll",
				"jshortcut_amd64.dll", "jshortcut_ia64.dll" };
		String tmpDir = System.getProperty("java.io.tmpdir");

		for (String file : dllFiles) {
			InputStream dllInput = getClass().getResourceAsStream("/" + file);

			if (dllInput == null) {
				System.out.println("internal dll file not found:" + file);
				return;
			}

			Path dllJar = Paths.get(tmpDir + File.separator + file);
			try {
				Files.copy(dllInput, dllJar,
						StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				error = e.getMessage();
				e.printStackTrace();
				return;
			}
		}

		System.setProperty("JSHORTCUT_HOME", tmpDir);
	}

	public static void main(String[] args) {
		new InstallTask(Paths.get("/home/illonis/eduras/"), null)
				.createLauncherIcon();
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
