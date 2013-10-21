package de.illonis.eduras.launcher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;

import de.illonis.eduras.launcher.gui.DownloadProgressListener;
import de.illonis.eduras.launcher.gui.LauncherGui;
import de.illonis.eduras.launcher.info.ChangeSet;
import de.illonis.eduras.launcher.info.VersionNumber;
import de.illonis.eduras.launcher.network.LauncherUpdateDownloader;
import de.illonis.eduras.launcher.network.LauncherUpdateInfo;
import de.illonis.eduras.launcher.network.LauncherUpdateListener;
import de.illonis.eduras.launcher.network.UpdateDownloader;
import de.illonis.eduras.launcher.network.VersionCheckReceiver;
import de.illonis.eduras.launcher.network.VersionChecker;
import de.illonis.eduras.launcher.network.VersionInformation;
import de.illonis.eduras.launcher.tools.PathFinder;

public class EdurasLauncher implements ActionListener, VersionCheckReceiver,
		DownloadProgressListener, ExtractProgressListener,
		LauncherUpdateListener, RepairProgressListener {

	public enum ReleaseChannel {
		STABLE, BETA, NIGHTLY;

		public String toString() {
			return name().toLowerCase();
		}
	}

	public final static VersionNumber LAUNCHER_VERSION = new VersionNumber(
			"2.2");
	public final static ConfigParser CONFIG = new ConfigParser();
	public final static String KEY_LAUNCHERNOTE = "launchernote";
	public final static String KEY_CLIENTNOTE = "clientnote";

	private final LauncherGui gui;
	private VersionInformation updateInfo;
	private ReleaseChannel releaseChannel = ReleaseChannel.STABLE;

	public static void main(String[] args) {
		System.out.println("launched");
		System.out.println("Launcher v " + LAUNCHER_VERSION);
		try {
			CONFIG.load();
		} catch (ParseException e) {
		}
		EdurasLauncher launcher = new EdurasLauncher();
		launcher.startAndShowGui();
	}

	public EdurasLauncher() {
		releaseChannel = CONFIG.getReleaseChannel();
		gui = new LauncherGui(this);
	}

	private void startAndShowGui() {
		gui.show();
		checkLocal();
		check();
	}

	private void checkLocal() {
		// read local version
		gui.setVersion(getVersion());

		String val = CONFIG.getValue(KEY_LAUNCHERNOTE, "");
		if (!val.isEmpty()) {
			gui.showMessage("Launcher updated", val);
			CONFIG.set(KEY_LAUNCHERNOTE, "");
			try {
				CONFIG.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void check() {
		gui.setButtonsEnabled(false);
		// compare local version with server version.
		VersionChecker vc = new VersionChecker(this);
		vc.checkVersion(getVersion(), LAUNCHER_VERSION, releaseChannel);
	}

	@Override
	public void onUpdateRequired(VersionInformation info) {
		updateInfo = info;
		ChangeSet updateSet = updateInfo.getChangeSetFor(CONFIG.getVersion());

		gui.setStatus("new version " + info.getVersion() + " - downloading "
				+ updateSet.getNumFiles() + " file(s)");
		UpdateDownloader downloader = new UpdateDownloader(updateSet, this);
		downloader.execute();
	}

	@Override
	public void onNoUpdateRequired(VersionInformation info) {
		gui.setStatus("No update required.");
		String updater = info.getLauncherInfo().getUpdaterName();
		if (!updater.isEmpty()) {
			File f = new File(PathFinder.findFile(updater));
			f.delete();
		}
		checkLocal();
		gui.ready();
	}

	@Override
	public void onUpdateError(Exception e) {
		gui.setStatus("Update error: " + e.getMessage());
		e.printStackTrace();
		gui.setRepairButtonEnabled(true);
	}

	public VersionNumber getVersion() {
		return CONFIG.getVersion();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JButton button = (JButton) e.getSource();
			if (button.getText().toLowerCase().contains("start")) {
				// start game
				launchGame();
			} else {
				// repair
				repair();
			}
		} else {
			JComboBox<?> cb = (JComboBox<?>) e.getSource();
			ReleaseChannel release = ((ReleaseChannel) cb.getSelectedItem());
			if (release != null)
				setRelease(release);
		}
	}

	private void setRelease(ReleaseChannel releaseChannel) {
		this.releaseChannel = releaseChannel;
		System.out.println("Set release channel to " + releaseChannel);
		CONFIG.setRelease(releaseChannel);
		try {
			CONFIG.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
		check();
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
		ChangeSet updateSet = updateInfo.getChangeSetFor(CONFIG.getVersion());
		gui.setStatus("Extracting files...");
		UpdateExtractor ex = new UpdateExtractor(updateSet, this);
		ex.execute();
	}

	@Override
	public void onDownloadError(String msg) {
		gui.showMessage("Download error", "Error while updating.\n"
				+ "Try again later or redownload Eduras manually.\n\n" + msg);
		gui.setStatus("An error occured while updating: " + msg);
		gui.setRepairButtonEnabled(true);
	}

	@Override
	public void onExtractingFinished() {
		checkLocal();
		if (getVersion() == updateInfo.getVersion()) {
			gui.setStatus("Update completed.");
			gui.ready();
			String val = CONFIG.getValue(KEY_CLIENTNOTE, "");
			if (!val.isEmpty()) {
				gui.showMessage("Client updated", val);
				CONFIG.set(KEY_CLIENTNOTE, "");
				try {
					CONFIG.save();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
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
			Object jarObj = EdurasLauncher.CONFIG.getValue("gameJar");
			if (jarObj == null)
				return;
			GameStarter starter = new GameStarter(jarObj.toString());
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
		gui.showMessage("Extraction error", "Error while updating.\n"
				+ "Make sure your game folder is writable.\n\n" + msg);
		gui.setStatus("An error occured while extracting: " + msg);
	}

	@Override
	public void onRepairCompleted() {
		try {
			CONFIG.load();
		} catch (ParseException e) {
		}
		gui.setStatus("Redownloading files...");
		checkLocal();
		check();
	}

	@Override
	public void onRepairFailed() {
		gui.setStatus("Repairing failed.");
		gui.abortProgressBar();
		gui.setButtonsEnabled(true);
	}

	@Override
	public void onLauncherOutdated(LauncherUpdateInfo newVersion) {

		gui.setButtonsEnabled(false);
		gui.setStatus("Updating game launcher...");
		CONFIG.set(KEY_LAUNCHERNOTE, newVersion.getNote());
		try {
			CONFIG.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LauncherUpdateDownloader l = new LauncherUpdateDownloader(this,
				newVersion);
		l.execute();
	}

	@Override
	public void exitLauncher() {
		gui.setStatus("Restarting launcher...");
		gui.exit();
	}
}
