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
	public void checkVersion(double clientVersion, double launcherVersion) {

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

		double version;
		try {
			version = Double.parseDouble(getNodeValue(document, "version"));
		} catch (NumberFormatException e) {
			throw new UpdateException(e);
		}

		long fileSize;
		try {
			fileSize = Long.parseLong(getNodeValue(document, "fileSize"));
		} catch (NumberFormatException e) {
			throw new UpdateException(e);
		}

		double launcherVersion;

		launcherVersion = Double.parseDouble(getNodeValue(document,
				"launcherVersion"));

		String releaseDate = getNodeValue(document, "releaseDate");
		String baseUrl = getNodeValue(document, "baseUrl");
		DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date release;
		try {
			release = f.parse(releaseDate);
		} catch (ParseException e) {
			throw new UpdateException(e);
		}

		Node fileNode = document.getDocumentElement()
				.getElementsByTagName("files").item(0);

		NodeList fileNodes = fileNode.getChildNodes();

		LinkedList<DownloadFile> files = new LinkedList<DownloadFile>();
		for (int i = 0; i < fileNodes.getLength(); i++) {
			Node n = fileNodes.item(i);
			if (n instanceof Element) {
				Element child = (Element) n;
				if (n.getNodeName().equals("file")) {
					String name = child.getFirstChild().getNodeValue();
					long size = Long.parseLong(child.getAttribute("size"));
					files.add(new DownloadFile(name, size));
				}
			}
		}

		Node deleteNode = document.getDocumentElement()
				.getElementsByTagName("delete").item(0);

		NodeList deleteNodes = deleteNode.getChildNodes();

		LinkedList<String> deletes = new LinkedList<String>();
		for (int i = 0; i < deleteNodes.getLength(); i++) {
			Node n = deleteNodes.item(i);
			if (n instanceof Element) {
				Element child = (Element) n;
				if (n.getNodeName().equals("file")) {
					String name = child.getFirstChild().getNodeValue();
					deletes.add(name);
				}
			}
		}

		Node configNode = document.getDocumentElement()
				.getElementsByTagName("config").item(0);

		NodeList configNodes = configNode.getChildNodes();

		LinkedList<ConfigChange> configs = new LinkedList<ConfigChange>();
		for (int i = 0; i < configNodes.getLength(); i++) {
			Node n = configNodes.item(i);
			if (n instanceof Element) {
				Element child = (Element) n;
				if (n.getNodeName().equals("option")) {
					String value = child.getFirstChild().getNodeValue();
					String key = child.getAttribute("key");
					configs.add(new ConfigChange(key, value));
				}
			}
		}

		VersionInformation info = new VersionInformation(version, fileSize,
				release, baseUrl, files, deletes, configs, launcherVersion);

		return info;
	}

	private String getNodeValue(Document document, String nodeName)
			throws NodeNotFoundException {
		Node node = document.getDocumentElement()
				.getElementsByTagName(nodeName).item(0);
		if (node == null)
			throw new NodeNotFoundException(nodeName);
		return node.getFirstChild().getNodeValue();
	}

	private class VersionCheckRunner implements Runnable {

		private final double clientVersion;
		private final double launcherVersion;

		public VersionCheckRunner(double clientVersion, double launcherVersion) {
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

			if (launcherVersion < serverVersion.getLauncherVersion())
				receiver.onLauncherOutdated(serverVersion.getLauncherVersion());
			else if (clientVersion < serverVersion.getVersion())
				receiver.onUpdateRequired(serverVersion);
			else
				receiver.onNoUpdateRequired();

		}
	}

}
