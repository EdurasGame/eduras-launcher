package de.illonis.eduras.launcher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import de.illonis.eduras.launcher.gui.DownloadProgressListener;
import de.illonis.eduras.launcher.gui.LauncherGui;
import de.illonis.eduras.launcher.network.UpdateDownloader;
import de.illonis.eduras.launcher.network.VersionCheckReceiver;
import de.illonis.eduras.launcher.network.VersionChecker;
import de.illonis.eduras.launcher.network.VersionInformation;

public class EdurasLauncher implements ActionListener, VersionCheckReceiver,
		DownloadProgressListener, ExtractProgressListener,
		RepairProgressListener {

	private final LauncherGui gui;
	private VersionInformation updateInfo;
	private ConfigParser config;

	public static void main(String[] args) {
		System.out.println("launched");
		EdurasLauncher launcher = new EdurasLauncher();
		launcher.startAndShowGui();
	}

	public EdurasLauncher() {
		gui = new LauncherGui(this);
	}

	private void startAndShowGui() {
		gui.show();
		checkLocal();
		check();
	}

	private void checkLocal() {
		// read local version

		config = new ConfigParser();
		try {
			config.load();
		} catch (ParseException e) {
		}
		gui.setVersion(getVersion());
	}

	private void check() {

		// compare local version with server version.
		VersionChecker vc = new VersionChecker(this);
		vc.checkVersion(getVersion());
	}

	@Override
	public void onUpdateRequired(VersionInformation info) {
		updateInfo = info;
		gui.setStatus("new version " + info.getVersion() + " - downloading "
				+ info.getNumFiles() + " file(s)");
		UpdateDownloader downloader = new UpdateDownloader(info, this);
		downloader.execute();
	}

	@Override
	public void onNoUpdateRequired() {
		gui.setStatus("No update required.");
		gui.ready();
	}

	@Override
	public void onUpdateError(Exception e) {
		gui.setStatus("Update error: " + e.getMessage());
		gui.setRepairButtonEnabled(true);
	}

	public double getVersion() {
		return config.getVersion();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton button = (JButton) e.getSource();
		if (button.getText().toLowerCase().contains("start")) {
			// start game
			launchGame();
		} else {
			// repair
			repair();
		}
	}

	private void repair() {
		gui.setStatus("Repairing...");
		gui.setButtonsEnabled(false);
		RepairTask task = new RepairTask(this);
		task.addPropertyChangeListener(this);
		task.execute();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			gui.propertyChange(evt);
		}
	}

	@Override
	public void onDownloadFinished() {
		gui.setStatus("Extracting files...");
		UpdateExtractor ex = new UpdateExtractor(updateInfo, this);
		ex.execute();
	}

	@Override
	public void onDownloadError(String msg) {
		JOptionPane.showMessageDialog(null, "Error while updating.\n"
				+ "Try again later or redownload Eduras manually.\n\n" + msg,
				"Download error", JOptionPane.ERROR_MESSAGE);
		gui.setStatus("An error occured while updating: " + msg);
		gui.setRepairButtonEnabled(true);
	}

	@Override
	public void onExtractingFinished() {
		checkLocal();
		if (getVersion() == updateInfo.getVersion()) {
			gui.setStatus("Update completed.");
			gui.ready();
			updateInfo = null;
		} else {
			check();
		}
	}

	private void launchGame() {
		gui.setStatus("Starting game...");
		gui.setButtonsEnabled(false);
		LaunchThread launcher = new LaunchThread();
		launcher.start();
	}

	private class LaunchThread extends Thread {

		public LaunchThread() {
			super("LaunchThread");
		}

		@Override
		public void run() {
			GameStarter starter = new GameStarter();
			starter.start();
			try {
				starter.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			gui.exit();
		}
	}

	@Override
	public void onExtractingFailed(String msg) {
		JOptionPane.showMessageDialog(null, "Error while updating.\n"
				+ "Make sure your game folder is writable.\n\n" + msg,
				"Extraction error", JOptionPane.ERROR_MESSAGE);
		gui.setStatus("An error occured while extracting: " + msg);
	}

	@Override
	public void onRepairCompleted() {
		gui.setStatus("Redownloading files...");
		checkLocal();
		check();
	}

	@Override
	public void onRepairFailed() {
		gui.setStatus("Repairing failed.");
	}
}
