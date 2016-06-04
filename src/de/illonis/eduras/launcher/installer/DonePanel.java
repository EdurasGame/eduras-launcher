package de.illonis.eduras.launcher.installer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DonePanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private final JLabel infoLabel;
	private final JButton startButton;
	private Path target;

	public DonePanel() {
		super(new BorderLayout());
		startButton = new JButton("Close");
		add(startButton, BorderLayout.SOUTH);
		startButton.addActionListener(this);
		infoLabel = new JLabel("Setup completed. Start game by running " + InstallTask.CLIENT_JAR);
		infoLabel.setHorizontalAlignment(JLabel.CENTER);
		add(infoLabel, BorderLayout.CENTER);
	}

	public void setFailed(String message) {
		infoLabel.setText("Setup failed: " + message);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		System.exit(0);
	}

	public void setPath(Path targetPath) {
		this.target = targetPath.resolve(InstallTask.CLIENT_JAR);
	}

}
