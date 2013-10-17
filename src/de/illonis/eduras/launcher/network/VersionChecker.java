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

import de.illonis.eduras.launcher.ConfigParser;
import de.illonis.eduras.launcher.info.ChangeSet;
import de.illonis.eduras.launcher.info.VersionNumber;

public class VersionChecker {
	public final static String DEFAULT_VERSION_URL = "http://illonis.dyndns.org/eduras/update/version.xml";

	private final VersionCheckReceiver receiver;

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

		ConfigParser p = new ConfigParser();
		try {
			p.load();
		} catch (de.illonis.eduras.launcher.ParseException e) {
		}
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new URL(p.getValue("updateUrl").toString())
				.openStream());
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

		VersionNumber launcherVersion = null;

		// TODO: change
		try {
			launcherVersion = new VersionNumber(getNodeValue(docElement,
					"launcher"));
			// handle launcher
		} catch (NodeNotFoundException e) {

		}

		String releaseDate = getNodeValue(docElement, "releaseDate");
		DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date release;
		try {
			release = f.parse(releaseDate);
		} catch (ParseException e) {
			throw new UpdateException(e);
		}

		VersionInformation info = new VersionInformation(version, release,
				metaserver, homePage, updateUrl, releaseName, launcherVersion,
				getChangeSets(docElement, version));

		return info;
	}

	private LinkedList<ChangeSet> getChangeSets(Element docElement,
			VersionNumber target) throws NodeNotFoundException, UpdateException {
		ConfigParser p = new ConfigParser();
		try {
			p.load();
		} catch (de.illonis.eduras.launcher.ParseException e1) {
		}
		LinkedList<ChangeSet> sets = new LinkedList<ChangeSet>();
		NodeList changeSets = docElement.getElementsByTagName("changeset");
		for (int i = 0; i < changeSets.getLength(); i++) {
			Node n = changeSets.item(i);
			if (n instanceof Element) {
				System.out.println(n.getNodeName());
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
						if (n.getNodeName().equals("file")) {
							String name = child.getFirstChild().getNodeValue();
							long size = Long.parseLong(child
									.getAttribute("size"));
							files.add(new DownloadFile(name, size));
						}
					}
				}
				Node deleteNode = elem.getElementsByTagName("delete").item(0);

				NodeList deleteNodes = deleteNode.getChildNodes();

				LinkedList<String> deletes = new LinkedList<String>();
				for (i = 0; i < deleteNodes.getLength(); i++) {
					Node delNode = deleteNodes.item(i);
					if (delNode instanceof Element) {
						Element child = (Element) delNode;
						if (n.getNodeName().equals("file")) {
							String name = child.getFirstChild().getNodeValue();
							deletes.add(name);
						}
					}
				}

				Node configNode = elem.getElementsByTagName("config").item(0);

				NodeList configNodes = configNode.getChildNodes();

				LinkedList<ConfigChange> configs = new LinkedList<ConfigChange>();
				for (i = 0; i < configNodes.getLength(); i++) {
					Node cNode = configNodes.item(i);
					if (cNode instanceof Element) {
						Element child = (Element) cNode;
						if (n.getNodeName().equals("option")) {
							String value = child.getFirstChild().getNodeValue();
							String key = child.getAttribute("key");
							configs.add(new ConfigChange(key, value));
						}
					}
				}
				ChangeSet set = new ChangeSet(p.getVersion(), target, fileSize,
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
		return node.getFirstChild().getNodeValue();
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

			if (null != serverVersion.getLauncherVersion()
					&& launcherVersion.compareTo(serverVersion
							.getLauncherVersion()) < 0)
				receiver.onLauncherOutdated(serverVersion.getLauncherVersion());
			else if (clientVersion.compareTo(serverVersion.getVersion()) < 0)
				receiver.onUpdateRequired(serverVersion);
			else
				receiver.onNoUpdateRequired();

		}
	}

}
