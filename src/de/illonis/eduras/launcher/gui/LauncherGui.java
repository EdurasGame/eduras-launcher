package de.illonis.eduras.launcher.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import de.illonis.eduras.launcher.EdurasLauncher;
import de.illonis.eduras.launcher.gui.images.ImageFiler;

public class LauncherGui extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JLabel progressLabel, statusLabel;
	private JButton startButton;
	private JProgressBar updateBar;
	private JComboBox<String> releaseSelect;
	private DefaultComboBoxModel<String> releases;
	private final EdurasLauncher launcher;
	private JButton websiteButton;

	public LauncherGui(EdurasLauncher launcher) {
		super();
		this.launcher = launcher;
		buildGui();
	}

	@Override
	public void setVisible(boolean b) {
		setLocationRelativeTo(null);
		super.setVisible(b);
	}

	private void buildGui() {
		setTitle("Eduras? Launcher");
		setSize(600, 400);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = (JPanel) getContentPane();
		JLabel title = new JLabel(ImageFiler.loadIcon("logo.png"));
		title.setBorder(BorderFactory.createEmptyBorder(30, 5, 0, 5));

		statusLabel = new JLabel("");
		statusLabel.setHorizontalAlignment(JLabel.CENTER);
		statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));

		progressLabel = new JLabel("");
		progressLabel.setHorizontalAlignment(JLabel.CENTER);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		updateBar = new JProgressBar();
		updateBar.setIndeterminate(true);
		startButton = new JButton("Start game");
		startButton.setEnabled(false);
		startButton.addActionListener(this);

		bottomPanel.add(updateBar, BorderLayout.CENTER);
		bottomPanel.add(startButton, BorderLayout.EAST);

		JPanel configPanel = new JPanel(new BorderLayout());
		releases = new DefaultComboBoxModel<String>();
		releaseSelect = new JComboBox<String>(releases);
		configPanel.add(releaseSelect, BorderLayout.EAST);
		releaseSelect.addActionListener(this);
		websiteButton = new JButton("Website");
		websiteButton.addActionListener(this);
		JLabel label = new JLabel("Launcher version "
				+ EdurasLauncher.LAUNCHER_VERSION);
		configPanel.add(label, BorderLayout.CENTER);
		label.setFont(label.getFont().deriveFont(10f));
		label.setHorizontalAlignment(JLabel.CENTER);
		configPanel.add(websiteButton, BorderLayout.WEST);
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(configPanel, BorderLayout.NORTH);
		topPanel.add(title, BorderLayout.CENTER);
		JPanel labelPanel = new JPanel(new BorderLayout());
		labelPanel.add(statusLabel, BorderLayout.NORTH);

		labelPanel.add(progressLabel, BorderLayout.SOUTH);

		topPanel.add(labelPanel, BorderLayout.SOUTH);
		labelPanel.setBorder(BorderFactory.createEmptyBorder(30, 5, 30, 5));
		panel.add(topPanel, BorderLayout.CENTER);
		panel.add(bottomPanel, BorderLayout.SOUTH);
		disableControls();
	}

	public void setStatus(String text) {
		statusLabel.setText(text);
	}

	public void setProgressLabel(String statusMessage) {
		progressLabel.setText(statusMessage);
	}

	public void setProgress(int progress, String note) {
		if (progress == 0) {
			updateBar.setIndeterminate(true);
		} else {
			setProgressLabel(note);
			updateBar.setString(null);
			updateBar.setStringPainted(true);
			updateBar.setIndeterminate(false);
			updateBar.setValue(progress);
		}
	}

	public void ready() {
		updateBar.setIndeterminate(false);
		updateBar.setValue(100);
		startButton.requestFocus();
	}

	public void exit() {
		dispose();
	}

	public void abortProgressBar() {
		updateBar.setIndeterminate(false);
		updateBar.setValue(100);
		updateBar.setStringPainted(true);
		updateBar.setString("error");
	}

	public void showMessage(String title, String message) {
		JOptionPane.showMessageDialog(this, message, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	public int ask(String title, String message, int optionType) {
		return JOptionPane.showConfirmDialog(this, message, title, optionType);
	}

	public void showError(String title, String message) {
		JOptionPane.showMessageDialog(this, message, title,
				JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == startButton) {
			launcher.launchGame();
		} else if (e.getSource() == releaseSelect && releaseSelect.isEnabled()) {
			String release = (String) releaseSelect.getSelectedItem();
			if (release != null)
				launcher.setRelease(release);
		} else if (e.getSource() == websiteButton) {
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(new URI(launcher.getWebsite()));
				} catch (IOException | URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public void disableControls() {
		startButton.setEnabled(false);
		releaseSelect.setEnabled(false);
	}

	public void enableControls() {
		startButton.setEnabled(true);
		releaseSelect.setEnabled(true);

		startButton.requestFocus();
	}

	public void setChannelList(Collection<String> channels, String current) {
		releases.removeAllElements();
		for (String channel : channels) {
			releases.addElement(channel);
		}
		if (releases.getIndexOf(current) >= 0) {
			releaseSelect.setSelectedItem(current);
		} else
			releaseSelect.setSelectedIndex(-1);
	}
}
