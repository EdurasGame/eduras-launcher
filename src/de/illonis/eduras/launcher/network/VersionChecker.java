package de.illonis.eduras.launcher.network;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.illonis.eduras.launcher.EdurasLauncher;
import de.illonis.eduras.launcher.EdurasLauncher.ReleaseChannel;
import de.illonis.eduras.launcher.info.ChangeSet;
import de.illonis.eduras.launcher.info.VersionNumber;

/**
 * Checks for new versions on server and updates urls to server.
 * 
 * @author illonis
 * 
 */
public class VersionChecker {
	public final static String NIGHTLY_VERSION_URL = "http://illonis.dyndns.org/eduras/update/nightly.php";
	public final static String STABLE_VERSION_URL = "http://illonis.dyndns.org/eduras/update/version.xml";
	public final static String BETA_VERSION_URL = "http://illonis.dyndns.org/eduras/update/beta.xml";
	public final static String LAUNCHER_URL = "http://illonis.dyndns.org/eduras/update/launcher.xml";

	private final static DocumentBuilderFactory docFactory = DocumentBuilderFactory
			.newInstance();

	private final VersionCheckReceiver receiver;
	public final static DateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm");

	public VersionChecker(VersionCheckReceiver receiver) {
		this.receiver = receiver;
	}

	/**
	 * Checks the update server for updates.
	 * 
	 * @param clientVersion
	 *            the currently installed gameclient version.
	 * @param launcherVersion
	 *            the currently installed launcher version.
	 * @param releaseChannel
	 *            the client's release channel.
	 */
	public void checkVersion(VersionNumber clientVersion,
			VersionNumber launcherVersion, ReleaseChannel releaseChannel) {

		Runnable r = new VersionCheckRunner(clientVersion, launcherVersion,
				releaseChannel);
		Thread t = new Thread(r);
		t.setName("VersionCheckRunner");
		t.start();
	}

	private Document receiveVersionDocument(ReleaseChannel channel)
			throws ParserConfigurationException, MalformedURLException,
			SAXException, IOException {

		String updateUrl;
		switch (channel) {
		case BETA:
			updateUrl = BETA_VERSION_URL;
			break;
		case NIGHTLY:
			updateUrl = NIGHTLY_VERSION_URL;
			break;
		default:
			updateUrl = STABLE_VERSION_URL;
			break;

		}

		return getUpdateInfo(updateUrl);
	}

	private Document getUpdateInfo(String updateUrl)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder db = docFactory.newDocumentBuilder();
		return db.parse(new URL(updateUrl).openStream());
	}

	private VersionInformation getServerVersion(ReleaseChannel channel)
			throws UpdateException, NodeNotFoundException {
		Document document;
		try {
			document = receiveVersionDocument(channel);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new UpdateException(e);
		}
		Element docElement = document.getDocumentElement();

		String homePage = getNodeValue(docElement, "homepage");
		String releaseName = getNodeValue(docElement, "releaseName");
		String metaserver = getNodeValue(docElement, "metaserver");
		String updateUrl = getNodeValue(docElement, "updateUrl");
		String gameJar = getNodeValue(docElement, "gameJar");
		String releaseDate = getNodeValue(docElement, "releaseDate");

		Date release;
		try {
			release = DATE_FORMAT.parse(releaseDate);
		} catch (ParseException e) {
			throw new UpdateException(e);
		}
		VersionNumber version;
		try {
			version = new VersionNumber(getNodeValue(docElement, "version"),
					release);
		} catch (NumberFormatException e) {
			throw new UpdateException(e);
		}

		VersionInformation info = new VersionInformation(version, release,
				gameJar, metaserver, homePage, updateUrl, releaseName,
				getChangeSets(docElement, version));

		return info;
	}

	private LinkedList<ChangeSet> getChangeSets(Element docElement,
			VersionNumber target) throws NodeNotFoundException, UpdateException {

		LinkedList<ChangeSet> sets = new LinkedList<ChangeSet>();
		NodeList changeSets = docElement.getElementsByTagName("changeset");
		for (int i = 0; i < changeSets.getLength(); i++) {
			Node n = changeSets.item(i);
			if (n instanceof Element) {
				Element elem = (Element) n;

				long fileSize;
				try {
					fileSize = Long.parseLong(getNodeValue(elem, "size"));
				} catch (NumberFormatException e) {
					throw new UpdateException(e);
				}

				String baseUrl = getNodeValue(elem, "baseUrl");
				Node fileNode = elem.getElementsByTagName("files").item(0);
				NodeList fileNodes = fileNode.getChildNodes();

				LinkedList<DownloadFile> files = new LinkedList<DownloadFile>();
				for (int j = 0; j < fileNodes.getLength(); j++) {
					Node node = fileNodes.item(j);
					if (node instanceof Element) {
						Element child = (Element) node;
						if (child.getNodeName().equals("file")) {
							String name = child.getFirstChild().getNodeValue();
							long size = Long.parseLong(child
									.getAttribute("size"));
							String hash = child.getAttribute("sha256");
							files.add(new DownloadFile(name, size, hash));
						}
					}
				}
				Node deleteNode = elem.getElementsByTagName("delete").item(0);

				LinkedList<String> deletes = new LinkedList<String>();
				if (deleteNode != null) {
					NodeList deleteNodes = deleteNode.getChildNodes();

					for (i = 0; i < deleteNodes.getLength(); i++) {
						Node delNode = deleteNodes.item(i);
						if (delNode instanceof Element) {
							Element child = (Element) delNode;
							if (child.getNodeName().equals("file")) {
								String name = child.getFirstChild()
										.getNodeValue();
								deletes.add(name);
							}
						}
					}
				}
				LinkedList<ConfigChange> configs = new LinkedList<ConfigChange>();

				Node configNode = elem.getElementsByTagName("config").item(0);
				if (configNode != null) {
					NodeList configNodes = configNode.getChildNodes();

					for (i = 0; i < configNodes.getLength(); i++) {
						Node cNode = configNodes.item(i);
						if (cNode instanceof Element) {
							Element child = (Element) cNode;
							if (child.getNodeName().equals("option")) {
								String value = child.getFirstChild()
										.getNodeValue();
								String key = child.getAttribute("key");
								configs.add(new ConfigChange(key, value));
							}
						}
					}
				}
				String note;
				try {
					note = getNodeValue(elem, "note");
				} catch (NodeNotFoundException ne) {
					note = "";
				}
				ChangeSet set = new ChangeSet(note,
						EdurasLauncher.CONFIG.getVersion(), target, fileSize,
						baseUrl, files, deletes, configs);
				sets.add(set);
			}

		}
		return sets;
	}

	private String getNodeValue(Element element, String nodeName)
			throws NodeNotFoundException {
		Node node = element.getElementsByTagName(nodeName).item(0);
		if (node == null)
			throw new NodeNotFoundException(nodeName);
		try {
			return node.getFirstChild().getNodeValue();
		} catch (NullPointerException nl) {
			throw new NodeNotFoundException(nodeName);
		}
	}

	private class VersionCheckRunner implements Runnable {

		private final VersionNumber clientVersion;
		private final VersionNumber launcherVersion;
		private final ReleaseChannel channel;

		public VersionCheckRunner(VersionNumber clientVersion,
				VersionNumber launcherVersion, ReleaseChannel channel) {
			this.clientVersion = clientVersion;
			this.launcherVersion = launcherVersion;
			this.channel = channel;
		}

		@Override
		public void run() {
			try {
				// delay for testing on local network, so it does not complete
				// "immediately".
				Thread.sleep(500);
			} catch (InterruptedException e) {
				return;
			}
			VersionInformation serverVersion;
			LauncherUpdateInfo launcherNewVersion;
			try {
				launcherNewVersion = getNewLauncherInfo();
				if (launcherVersion.compareTo(launcherNewVersion.getVersion()) < 0) {
					receiver.onLauncherOutdated(launcherNewVersion);
					return;
				}

			} catch (NodeNotFoundException | UpdateException e) {
				receiver.onUpdateError(e);
				return;
			}
			try {
				serverVersion = getServerVersion(channel);
			} catch (NodeNotFoundException | UpdateException e) {
				receiver.onUpdateError(e);
				return;
			}
			updateBasics(serverVersion);

			if (channel == ReleaseChannel.NIGHTLY) {
				if (clientVersion.getReleaseDate().before(
						serverVersion.getReleaseDate())) {
					receiver.onUpdateRequired(serverVersion);
				} else {
					receiver.onNoUpdateRequired(serverVersion);
				}
			} else if (clientVersion.compareTo(serverVersion.getVersion()) < 0)
				receiver.onUpdateRequired(serverVersion);
			else
				receiver.onNoUpdateRequired(serverVersion);

		}

		private LauncherUpdateInfo getNewLauncherInfo()
				throws NodeNotFoundException, UpdateException {
			Document doc;
			try {
				doc = getUpdateInfo(LAUNCHER_URL);
			} catch (ParserConfigurationException | SAXException | IOException e) {
				throw new UpdateException(e);
			}
			Element docElement = doc.getDocumentElement();

			String note;
			try {
				note = getNodeValue(docElement, "note");
			} catch (NodeNotFoundException ne) {
				note = "";
			}
			String baseUrl = getNodeValue(docElement, "baseUrl");

			VersionNumber newLauncherVersion = new VersionNumber(getNodeValue(
					docElement, "version"));

			// launcher node
			Node node = docElement.getElementsByTagName("launcher").item(0);
			if (node == null)
				throw new NodeNotFoundException("launcher");
			Node launcherNode = node.getFirstChild();
			if (launcherNode == null)
				throw new NodeNotFoundException("launcher");

			long launcherSize = Long.parseLong(node.getAttributes()
					.getNamedItem("size").getNodeValue());

			String launcherHash = node.getAttributes().getNamedItem("sha256")
					.getNodeValue();
			DownloadFile launcherFile = new DownloadFile(
					launcherNode.getNodeValue(), launcherSize, launcherHash);

			// updater node
			Node updaterNode = docElement.getElementsByTagName("updater").item(
					0);
			if (updaterNode == null)
				throw new NodeNotFoundException("launcher");
			Node updatersNode = updaterNode.getFirstChild();
			if (updatersNode == null)
				throw new NodeNotFoundException("launcher");
			long updaterSize = Long.parseLong(updaterNode.getAttributes()
					.getNamedItem("size").getNodeValue());
			String updaterHash = updaterNode.getAttributes()
					.getNamedItem("sha256").getNodeValue();
			DownloadFile updaterFile = new DownloadFile(
					updatersNode.getNodeValue(), updaterSize, updaterHash);

			return new LauncherUpdateInfo(note, newLauncherVersion, baseUrl,
					launcherFile, updaterFile);
		}

		private void updateBasics(VersionInformation serverVersion) {
			EdurasLauncher.CONFIG.set("gameJar", serverVersion.getGameJar());
			EdurasLauncher.CONFIG.set("metaserver",
					serverVersion.getMetaServer());
			EdurasLauncher.CONFIG.set("homepage", serverVersion.getHomepage());
			EdurasLauncher.CONFIG
					.set("updateUrl", serverVersion.getUpdateUrl());
			EdurasLauncher.CONFIG.set("releaseDate",
					DATE_FORMAT.format(serverVersion.getReleaseDate()));

			EdurasLauncher.CONFIG.set("releaseName",
					serverVersion.getReleaseName());
		}
	}

}
