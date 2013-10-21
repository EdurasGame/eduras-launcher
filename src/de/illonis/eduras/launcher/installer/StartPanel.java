package de.illonis.eduras.launcher.installer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class StartPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private final static String INSTALL_FOLDER = "eduras";
	private JButton chooseButton;
	private JButton installButton;
	private JTextField installLocation;
	private JFileChooser fileChooser;
	private final EdurasInstaller installer;

	public StartPanel(EdurasInstaller installer) {
		super(new BorderLayout());
		chooseButton = new JButton("change");
		this.installer = installer;
		chooseButton.addActionListener(this);
		installButton = new JButton("install");
		installButton.addActionListener(this);
		fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose install location");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
		JPanel targetPanel = new JPanel(new BorderLayout());
		JPanel bottomPanel = new JPanel(new BorderLayout());

		targetPanel.add(chooseButton, BorderLayout.EAST);
		JLabel locationLabel = new JLabel("Install location:");
		targetPanel.add(locationLabel, BorderLayout.WEST);
		targetPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		locationLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		installLocation = new JTextField();
		targetPanel.add(installLocation, BorderLayout.CENTER);
		bottomPanel.add(targetPanel, BorderLayout.NORTH);
		bottomPanel.add(targetPanel, BorderLayout.NORTH);
		bottomPanel.add(installButton, BorderLayout.SOUTH);
		JLabel infoLabel = new JLabel(
				"<html>Welcome to Eduras?!<br>"
						+ "It is recommended that you install the game client to an empty folder.<br>"
						+ "Not existing folders will be created autmatically.</html>");
		add(infoLabel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
		installLocation.setText(findDefaultFolder().toString());
	}

	private Path findDefaultFolder() {
		String homeDir = System.getProperty("user.home");
		if (OsValidator.isWindows() || homeDir == null) {
			homeDir = "C:/Spiele/";
		}

		Path p = Paths.get(homeDir, "eduras/");

		return p;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton source = (JButton) e.getSource();
		if (source == installButton) {
			install();
		} else if (source == chooseButton) {
			chooseLocation();
		}
	}

	private void install() {
		Path p = Paths.get(installLocation.getText());
		if (Files.exists(p)) {
			int result = JOptionPane.showConfirmDialog(this, "The directory "
					+ p.toString() + " already exists.\n"
					+ "Existing files might be overwritten.",
					"install warning", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE);

			if (result == JOptionPane.CANCEL_OPTION)
				return;
		}
		try {
			Files.createDirectories(p);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error creating directory: "
					+ e.getMessage(), "install error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		installer.startInstalling(p);
	}

	private void chooseLocation() {
		fileChooser.setSelectedFile(new File(installLocation.getText()));
		if (fileChooser.showDialog(this, "choose location") == JFileChooser.APPROVE_OPTION) {
			Path p = fileChooser.getSelectedFile().toPath();
			if (!p.endsWith(INSTALL_FOLDER)) {
				p = p.resolve(INSTALL_FOLDER);
			}
			installLocation.setText(p.toString());
		}
	}
}
