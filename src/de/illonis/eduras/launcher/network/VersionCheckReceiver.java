package de.illonis.eduras.launcher.network;

public interface VersionCheckReceiver {

	void onUpdateRequired(VersionInformation serverVersion);

	void onNoUpdateRequired();

	void onLauncherOutdated(double newVersion);

	void onUpdateError(Exception e);
}
