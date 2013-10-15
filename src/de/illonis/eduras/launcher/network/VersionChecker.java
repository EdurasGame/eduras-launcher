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

	public void checkVersion(double version) {

		Runnable r = new VersionCheckRunner(version);
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

	private VersionInformation getServerVersion() throws UpdateException {
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
				release, baseUrl, files, deletes, configs);
		return info;
	}

	private String getNodeValue(Document document, String nodeName) {
		return document.getDocumentElement().getElementsByTagName(nodeName)
				.item(0).getFirstChild().getNodeValue();
	}

	private class VersionCheckRunner implements Runnable {

		private final double clientVersion;

		public VersionCheckRunner(double version) {
			clientVersion = version;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				return;
			}
			VersionInformation serverVersion;
			try {
				serverVersion = getServerVersion();
			} catch (UpdateException e) {
				receiver.onUpdateError(e.getDetailException());
				return;
			}

			if (clientVersion < serverVersion.getVersion())
				receiver.onUpdateRequired(serverVersion);
			else
				receiver.onNoUpdateRequired();

		}
	}

}
