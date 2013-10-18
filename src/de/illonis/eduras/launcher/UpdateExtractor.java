package de.illonis.eduras.launcher;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import de.illonis.eduras.launcher.info.ChangeSet;
import de.illonis.eduras.launcher.network.ConfigChange;
import de.illonis.eduras.launcher.tools.PathFinder;

public class UpdateExtractor extends SwingWorker<Boolean, Void> {

	private final ExtractProgressListener listener;
	private final ChangeSet info;
	private String error = "";

	public UpdateExtractor(ChangeSet info, ExtractProgressListener listener) {
		this.listener = listener;
		this.info = info;
	}

	@Override
	protected Boolean doInBackground() {

		// to make it at least last a while...
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {

		}

		// delete old files
		LinkedList<String> deleteFiles = info.getDeleteFiles();
		for (String string : deleteFiles) {
			URI url = PathFinder.findFile(string);
			File f = new File(url);
			f.delete();
		}

		// change config

		EdurasLauncher.CONFIG.setVersion(info.getTarget());

		for (ConfigChange change : info.getConfigChanges()) {
			EdurasLauncher.CONFIG.set(change.getKey(), change.getValue());
		}

		EdurasLauncher.CONFIG
				.set(EdurasLauncher.KEY_CLIENTNOTE, info.getNote());
		try {
			EdurasLauncher.CONFIG.save();
		} catch (IOException e) {
			error = e.getMessage();
			return false;
		}
		return true;
	}

	@Override
	public void done() {
		try {
			boolean b = get();
			if (b)
				listener.onExtractingFinished();
			else
				listener.onExtractingFailed(error);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

}
