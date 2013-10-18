package de.illonis.eduras.launcher.network;

import java.util.Date;
import java.util.LinkedList;

import de.illonis.eduras.launcher.info.ChangeSet;
import de.illonis.eduras.launcher.info.VersionNumber;

public class VersionInformation {

	private final VersionNumber version;
	private final Date releaseDate;
	private final LauncherUpdateInfo launcherInfo;
	private final LinkedList<ChangeSet> changeSets;
	private final String metaServer;
	private final String homepage;
	private final String updateUrl;
	private final String releaseName;
	private final String gameJar;

	public VersionInformation(VersionNumber version, Date release,
			String gameJar, String metaServer, String homepage,
			String updateUrl, String releaseName,
			LauncherUpdateInfo launcherVersion, LinkedList<ChangeSet> changeSets) {
		this.version = version;
		this.releaseDate = release;
		this.changeSets = changeSets;
		this.launcherInfo = launcherVersion;
		this.metaServer = metaServer;
		this.homepage = homepage;
		this.updateUrl = updateUrl;
		this.releaseName = releaseName;
		this.gameJar = gameJar;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public VersionNumber getVersion() {
		return version;
	}

	public LauncherUpdateInfo getLauncherInfo() {
		return launcherInfo;
	}

	public LinkedList<ChangeSet> getChangeSets() {
		return changeSets;
	}

	public ChangeSet getChangeSetFor(VersionNumber versionNumber) {
		ChangeSet nullSet = null;
		for (ChangeSet set : changeSets) {
			if (set.getFrom().equals(versionNumber))
				return set;
			else if (set.getFrom().isNull())
				nullSet = set;
		}
		return nullSet;
	}

	public String getHomepage() {
		return homepage;
	}

	public String getMetaServer() {
		return metaServer;
	}

	public String getReleaseName() {
		return releaseName;
	}

	public String getUpdateUrl() {
		return updateUrl;
	}

	public String getGameJar() {
		return gameJar;
	}
}