package de.illonis.eduras.launcher.network;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.util.LinkedList;

import javax.swing.SwingWorker;

import de.illonis.eduras.launcher.gui.DownloadProgressListener;
import de.illonis.eduras.launcher.info.ChangeSet;

public class UpdateDownloader extends SwingWorker<Void, Void> {

	private final DownloadProgressListener listener;
	private final ChangeSet set;
	private int[] progressValues;
	private String error;

	public UpdateDownloader(ChangeSet set, DownloadProgressListener listener) {
		this.listener = listener;
		this.set = set;
		addPropertyChangeListener(listener);
	}

	@Override
	protected Void doInBackground() throws Exception {
		setProgress(0);

		LinkedList<DownloadFile> files = set.getFiles();

		int n = files.size();
		progressValues = new int[n];

		LinkedList<FileDownloader> downloaderList = new LinkedList<FileDownloader>();

		System.out.println("starting downloaders...");
		for (int i = 0; i < files.size(); i++) {
			DownloadFile f = files.get(i);
			FileDownloader dl = new FileDownloader(f, set.getBaseUrl());
			ChangeListener listener = new ChangeListener(i);
			dl.addPropertyChangeListener(listener);
			dl.execute();
			downloaderList.add(dl);
		}

		System.out.println("waiting for downloaders to finish...");
		for (int i = 0; i < downloaderList.size(); i++) {
			FileDownloader downloader = downloaderList.get(i);
			downloader.get();
			if (!downloader.isOk()) {
				handleError(files.get(i), downloader.getError());
				return null;
			}
		}
		System.out.println("all downloaders finished.");
		setProgress(99);
		return null;
	}

	private void handleError(DownloadFile file, Exception ex) {
		ex.printStackTrace();
		if (ex instanceof FileNotFoundException) {
			error = "File not found: " + file.getFileName();
		} else {
			error = ex.getClass().getName() + " in " + file.getFileName();
		}
	}

	@Override
	public void done() {
		if (error != null) {
			setProgress(1);
			listener.onDownloadError(error);
		} else
			listener.onDownloadFinished();
	}

	void onProgress(int id, int progress) {
		progressValues[id] = progress;
		recalculateProgress();
	}

	private void recalculateProgress() {
		int sum = 0;
		for (int i = 0; i < progressValues.length; i++) {
			sum += progressValues[i];
		}
		int max = progressValues.length * 100;
		int progress = Math.round((float) sum * 100 / max);
		setProgress(Math.min(99, progress));
	}

	private class ChangeListener implements PropertyChangeListener {

		private final int id;

		public ChangeListener(int id) {
			this.id = id;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("progress" == evt.getPropertyName()) {
				int value = (int) evt.getNewValue();
				onProgress(id, value);
			}
		}
	}
}
