package de.illonis.eduras.launcher.network;

import de.illonis.eduras.launcher.info.VersionNumber;

public interface VersionCheckReceiver {

	void onUpdateRequired(VersionInformation serverVersion);

	void onNoUpdateRequired();

	void onLauncherOutdated(VersionNumber newVersion);

	void onUpdateError(Exception e);
}
