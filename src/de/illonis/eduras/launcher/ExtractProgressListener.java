package de.illonis.eduras.launcher;

public interface ExtractProgressListener {

	/**
	 * Fired when extraction of update is completed.
	 */
	void onExtractingFinished();

	/**
	 * Fired when extraction of update failed.
	 * 
	 * @param msg
	 *            the error message.
	 */
	void onExtractingFailed(String msg);

}
