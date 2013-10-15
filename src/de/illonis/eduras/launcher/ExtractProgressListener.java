package de.illonis.eduras.launcher;

public interface ExtractProgressListener {

	/**
	 * Fired when extraction of update is completed.
	 */
	void onExtractingFinished();

	void onExtractingFailed(String msg);

}
