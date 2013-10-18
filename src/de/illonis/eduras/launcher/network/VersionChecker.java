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
import de.illonis.eduras.launcher.info.ChangeSet;
import de.illonis.eduras.launcher.info.VersionNumber;

/**
 * Checks for new versions on server and updates urls to server.
 * 
 * @author illonis
 * 
 */
public class VersionChecker {
	public final static String DEFAULT_VERSION_URL = "http://illonis.dyndns.org/eduras/update/version.xml";

	private final VersionCheckReceiver receiver;
	private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

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
	 */
	public void checkVersion(VersionNumber clientVersion,
			VersionNumber launcherVersion) {

		Runnable r = new VersionCheckRunner(clientVersion, launcherVersion);
		Thread t = new Thread(r);
		t.setName("VersionCheckRunner");
		t.start();
	}

	private Document receiveVersionDocument()
			throws ParserConfigurationException, MalformedURLException,
			SAXException, IOException {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new URL(EdurasLauncher.CONFIG.getValue(
				"updateUrl").toString()).openStream());
		return doc;
	}

	private VersionInformation getServerVersion() throws UpdateException,
			NodeNotFoundException {
		Document document;
		try {
			document = receiveVersionDocument();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new UpdateException(e);
		}
		Element docElement = document.getDocumentElement();

		VersionNumber version;
		try {
			version = new VersionNumber(getNodeValue(docElement, "version"));
		} catch (NumberFormatException e) {
			throw new UpdateException(e);
		}

		String homePage = getNodeValue(docElement, "homepage");
		String releaseName = getNodeValue(docElement, "releaseName");
		String metaserver = getNodeValue(docElement, "metaserver");
		String updateUrl = getNodeValue(docElement, "updateUrl");
		String gameJar = getNodeValue(docElement, "gameJar");

		VersionNumber launcherVersion = null;

		LauncherUpdateInfo lui = new LauncherUpdateInfo();
		try {
			NodeList launcherData = docElement.getElementsByTagName("launcher");
			Node launcherNode = launcherData.item(0);
			if (launcherNode instanceof Element) {
				Element e = (Element) launcherNode;
				String launcherName = getNodeValue(e, "name");
				String updater = getNodeValue(e, "updater");
				String note;
				try {
					note = getNodeValue(e, "note");
				} catch (NodeNotFoundException ne) {
					note = "";
				}

				long size = Long.parseLong(getNodeValue(e, "size"));
				long updatersize = Long
						.parseLong(getNodeValue(e, "updatersize"));
				String baseUrl = getNodeValue(e, "baseUrl");

				launcherVersion = new VersionNumber(getNodeValue(e, "version"));
				lui = new LauncherUpdateInfo(note, launcherVersion, updater,
						baseUrl, launcherName, size, updatersize);
			}
		} catch (NodeNotFoundException e) {
		}

		String releaseDate = getNodeValue(docElement, "releaseDate");

		Date release;
		try {
			release = format.parse(releaseDate);
		} catch (ParseException e) {
			throw new UpdateException(e);
		}

		VersionInformation info = new VersionInformation(version, release,
				gameJar, metaserver, homePage, updateUrl, releaseName, lui,
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
							files.add(new DownloadFile(name, size));
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

		public VersionCheckRunner(VersionNumber clientVersion,
				VersionNumber launcherVersion) {
			this.clientVersion = clientVersion;
			this.launcherVersion = launcherVersion;
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
			try {
				serverVersion = getServerVersion();
			} catch (NodeNotFoundException | UpdateException e) {
				receiver.onUpdateError(e);
				return;
			}

			updateBasics(serverVersion);

			if (null != serverVersion.getLauncherInfo()
					&& launcherVersion.compareTo(serverVersion
							.getLauncherInfo().getVersion()) < 0)
				receiver.onLauncherOutdated(serverVersion.getLauncherInfo());
			else if (clientVersion.compareTo(serverVersion.getVersion()) < 0)
				receiver.onUpdateRequired(serverVersion);
			else
				receiver.onNoUpdateRequired(serverVersion);

		}

		private void updateBasics(VersionInformation serverVersion) {
			EdurasLauncher.CONFIG.set("gameJar", serverVersion.getGameJar());
			EdurasLauncher.CONFIG.set("metaserver",
					serverVersion.getMetaServer());
			EdurasLauncher.CONFIG.set("homepage", serverVersion.getHomepage());
			EdurasLauncher.CONFIG
					.set("updateUrl", serverVersion.getUpdateUrl());
			EdurasLauncher.CONFIG.set("releaseDate",
					format.format(serverVersion.getReleaseDate()));

			EdurasLauncher.CONFIG.set("releaseName",
					serverVersion.getReleaseName());
		}
	}

}
