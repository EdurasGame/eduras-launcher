package de.illonis.eduras.launcher.network;

public interface VersionCheckReceiver {

	void onUpdateRequired(VersionInformation serverVersion);

	void onNoUpdateRequired(VersionInformation serverVersion);

	void onLauncherOutdated(LauncherUpdateInfo newVersion);

	void onUpdateError(Exception e);
}
