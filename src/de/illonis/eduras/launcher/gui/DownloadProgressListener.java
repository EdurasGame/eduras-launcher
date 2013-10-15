package de.illonis.eduras.launcher.gui;

import java.beans.PropertyChangeListener;

public interface DownloadProgressListener extends PropertyChangeListener {

	void onDownloadFinished();

	void onDownloadError(String msg);
}
