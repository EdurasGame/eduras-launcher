package de.illonis.eduras.launcher.network;

import java.net.MalformedURLException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import de.illonis.eduras.launcher.GameStarter;

public class LauncherUpdateDownloader extends SwingWorker<Void, Void> {

	private final LauncherUpdateInfo info;
	private final LauncherUpdateListener l;

	public LauncherUpdateDownloader(LauncherUpdateListener l,
			LauncherUpdateInfo info) {
		this.info = info;
		this.l = l;
	}

	@Override
	protected Void doInBackground() throws Exception {
		DownloadFile f = new DownloadFile(info.getDownloadName(),
				info.getFileSize());

		DownloadFile f2 = new DownloadFile(info.getUpdaterName(),
				info.getUpdaterFileSize());
		FileDownloader dl = null;
		FileDownloader dl2 = null;
		try {
			dl = new FileDownloader(f, info.getBaseUrl(), f.getFileName()
					+ ".new");
			dl.addPropertyChangeListener(l);
			dl2 = new FileDownloader(f2, info.getBaseUrl());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (dl != null) {
			dl.execute();
			dl2.execute();
			dl.get();
			dl2.get();
			if (!dl.isOk()) {
				JOptionPane.showMessageDialog(null,
						"An error occured while updating launcher.");
				dl.getError().printStackTrace();
				return null;
			}
		}
		return null;
	}

	@Override
	protected void done() {

		l.exitLauncher();
		String updater = info.getUpdaterName();

		GameStarter st = new GameStarter(updater);
		st.setArguments(info.getDownloadName(), info.getDownloadName() + ".new");
		st.start();
	}

}
