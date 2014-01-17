package de.illonis.eduras.launcher;

/**
 * Listens and reacts on events fired by the download extraction process.
 * 
 * @author illonis
 * 
 */
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
