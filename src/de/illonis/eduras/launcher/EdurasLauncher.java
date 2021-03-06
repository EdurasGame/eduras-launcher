package de.illonis.eduras.launcher;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import javax.swing.SwingUtilities;

import de.illonis.eduras.launcher.gui.LauncherGui;
import de.illonis.eduras.launcher.tools.PathFinder;
import de.illonis.newup.client.ChannelListener;
import de.illonis.newup.client.NeWUpClient;
import de.illonis.newup.client.UpdateException;
import de.illonis.newup.client.UpdateException.ErrorType;
import de.illonis.newup.client.UpdateListener;
import de.illonis.newup.client.UpdateResult;

/**
 * A game launcher that updates the game automatically at startup.
 * 
 * @author illonis
 * 
 */
public class EdurasLauncher implements UpdateListener, ChannelListener {

	public final static int LAUNCHER_VERSION = 10;
	public final static String DATA_PATH = "game/";
	private final static String SERVER_URL = "http://illonis.de/newup/";
	private static final String DEFAULT_RELEASE_CHANNEL = "beta";

	private final LauncherGui gui;
	private String releaseChannel;
	private final URL serverURL;
	private final Path localPath;
	private String website;
	private NeWUpClient client;

	public static void main(String[] args) {

		System.out.println("Launcher v " + LAUNCHER_VERSION);

		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					EdurasLauncher launcher = new EdurasLauncher();
					launcher.startAndShowGui();
				} catch (MalformedURLException | URISyntaxException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private EdurasLauncher() throws MalformedURLException, URISyntaxException {
		serverURL = new URL(SERVER_URL);
		localPath = PathFinder.getDataPath();
		gui = new LauncherGui(this);
		releaseChannel = "";
		website = "http://www.eduras.de";
	}

	public String getWebsite() {
		return website;
	}

	private void startAndShowGui() {
		gui.addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent arg0) {
				gui.setStatus("Retrieving available release channels...");
				NeWUpClient.queryChannels(serverURL, EdurasLauncher.this);
			}

			@Override
			public void componentResized(ComponentEvent arg0) {
			}

			@Override
			public void componentMoved(ComponentEvent arg0) {
			}

			@Override
			public void componentHidden(ComponentEvent arg0) {
			}
		});
		gui.setVisible(true);
	}

	protected void startUpdateCheck(String channel) {
		gui.disableControls();
		if (!Files.exists(localPath)) {
			try {
				Files.createDirectories(localPath);
			} catch (IOException e) {
				gui.showError("Error", "Could not create data directory.");
				e.printStackTrace();
				return;
			}
		}

		gui.setStatus("Checking for updates...");
		gui.setButtonAbort();
		client = new NeWUpClient(serverURL, localPath, channel);
		client.addUpdateListener(this);
		client.checkForUpdates(true);
	}

	public void abort() {
		if (client != null)
			client.abort();
	}

	public void setRelease(String newChannel) {
		this.releaseChannel = newChannel;
		System.out.println("Set release channel to " + newChannel);
		startUpdateCheck(newChannel);
	}

	public void launchGame() {
		gui.setStatus("Starting game...");
		gui.disableControls();
		LaunchThread launcher = new LaunchThread();
		launcher.start();
	}

	private class LaunchThread extends Thread {

		public LaunchThread() {
			super("LaunchThread");
		}

		@Override
		public void run() {
			GameStarter starter = new GameStarter(gui);
			starter.start();
			try {
				starter.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			gui.setStatus("Ready to play");
			gui.enableControls();
		}
	}

	public void exitLauncher() {
		gui.setStatus("Restarting launcher...");
		gui.exit();
	}

	@Override
	public void onUpdateCompleted(UpdateResult result) {
		releaseChannel = result.getChannel();
		if (result.getNewFilesAmount() == 0) {
			gui.setStatus("No update required.");
		} else {
			gui.setStatus("Update completed.");
			afterEdurasUpdate();
			if (!result.getNotice().isEmpty())
				gui.showMessage("Update information", result.getNotice());
		}
		gui.setProgress(100,
				result.getServerVersion() + " (" + result.getServerTag() + ")");
		gui.setButtonStart();
		gui.enableControls();
	}

	private void afterEdurasUpdate() {
		EdurasConfigFile file = new EdurasConfigFile();
		int launcherVersion = 0;
		try {
			file.load();
			website = file.getValue(EdurasConfigFile.KEY_WEBSITE,
					"http://www.eduras.de");
			launcherVersion = Integer.parseInt(file.getValue(
					EdurasConfigFile.KEY_LAUNCHERVERSION, "0").trim());
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		if (launcherVersion > LAUNCHER_VERSION) {
			// update required
			try {
				updateLauncher();
			} catch (IOException | URISyntaxException e) {
				gui.showError("Update failed", "Updating launcher failed.");
				e.printStackTrace();
			}
		}
	}

	private void updateLauncher() throws IOException, URISyntaxException {
		Path updater = PathFinder.getDataPath().resolve("lupdater.jar");
		if (!Files.exists(updater)) {
			return;
		}
		gui.setStatus("Starting launcher update...");
		String[] cmdargs = new String[4];
		cmdargs[0] = "java";
		cmdargs[1] = "-jar";
		cmdargs[2] = updater.toString();
		cmdargs[3] = "update";
		Runtime.getRuntime().exec(cmdargs);
		System.exit(0);
	}

	@Override
	public void onUpdateInfoReceived(UpdateResult result) {
		// unused
	}

	@Override
	public void onUpdateError(UpdateException e) {
		e.printStackTrace();
		if (e.getType() == ErrorType.INVALID_CHANNEL) {
			gui.showError(
					"Invalid channel",
					"Releasechannel \""
							+ releaseChannel
							+ "\" is not available. Please select another one from list.");
			gui.setStatus("");
			gui.setProgress(-1, "No channel selected.");
		} else {
			gui.showError("Update error", e.getClass().getSimpleName() + ": "
					+ e.getMessage());
			gui.setStatus("Update error");
			gui.setProgress(-1, e.getMessage());
		}
		gui.enableControls();
		gui.disableStart();
	}

	@Override
	public void onNetworkError(IOException e) {
		e.printStackTrace();
		gui.showError("Network error",
				e.getClass().getSimpleName() + ": " + e.getMessage());
		gui.setStatus("Network error");
		gui.setProgress(-1, e.getMessage());
	}

	@Override
	public void updateProgress(int progress, String note) {
		gui.setProgress(progress, note);
	}

	@Override
	public void onUpdateCancelled() {
		gui.setProgress(1, "Update cancelled");
		gui.setStatus("Please select a release to start downloading.");
		gui.setButtonStart();
		gui.enableControls();
		gui.disableStart();
	}

	@Override
	public void onChannelListReceived(Collection<String> channels) {
		gui.setStatus("Reading local version...");
		try {
			String localRelease = NeWUpClient.getLocalChannel(localPath);
			if (!localRelease.isEmpty() && localRelease != "n/a") {
				releaseChannel = localRelease;
			} else {
				releaseChannel = DEFAULT_RELEASE_CHANNEL;
			}
		} catch (IOException e) {
			e.printStackTrace();
			releaseChannel = DEFAULT_RELEASE_CHANNEL;
		}
		gui.setChannelList(channels, releaseChannel);
		startUpdateCheck(releaseChannel);
	}

	@Override
	public void onError(Exception e) {
		gui.showError("Error", "Could not get available release channels.");
	}
}
