package de.illonis.eduras.launcher.gui;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import de.illonis.eduras.launcher.EdurasLauncher;
import de.illonis.eduras.launcher.EdurasLauncher.ReleaseChannel;
import de.illonis.eduras.launcher.gui.images.ImageFiler;
import de.illonis.eduras.launcher.info.VersionNumber;

public class LauncherGui implements PropertyChangeListener {

	private JFrame frame;
	private JLabel status, versionLabel;
	private JButton startButton;
	private JButton repairButton;
	private JProgressBar updateBar;
	private JComboBox<ReleaseChannel> releaseSelect;

	public LauncherGui(EdurasLauncher launcher) {
		buildGui(launcher);
	}

	public void show() {
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private void buildGui(EdurasLauncher launcher) {

		frame = new JFrame("Eduras? Launcher");
		frame.setSize(500, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = (JPanel) frame.getContentPane();
		JLabel title = new JLabel(ImageFiler.loadIcon("logo.png"));
		title.setBorder(BorderFactory.createEmptyBorder(30, 5, 0, 5));

		versionLabel = new JLabel("checking installed version...");
		versionLabel.setHorizontalAlignment(JLabel.CENTER);
		versionLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5));

		status = new JLabel("checking for updates...");
		status.setHorizontalAlignment(JLabel.CENTER);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		updateBar = new JProgressBar();
		updateBar.setIndeterminate(true);
		startButton = new JButton("Start game");
		startButton.setEnabled(false);
		startButton.addActionListener(launcher);
		repairButton = new JButton("Repair");
		repairButton.addActionListener(launcher);
		repairButton.setEnabled(false);

		bottomPanel.add(updateBar, BorderLayout.CENTER);
		bottomPanel.add(startButton, BorderLayout.EAST);
		bottomPanel.add(repairButton, BorderLayout.WEST);

		JPanel configPanel = new JPanel(new BorderLayout());
		releaseSelect = new JComboBox<ReleaseChannel>(ReleaseChannel.values());
		releaseSelect.addActionListener(launcher);
		configPanel.add(releaseSelect, BorderLayout.EAST);
		releaseSelect
				.setSelectedItem(EdurasLauncher.CONFIG.getReleaseChannel());

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(configPanel, BorderLayout.NORTH);
		topPanel.add(title, BorderLayout.CENTER);
		JPanel labelPanel = new JPanel(new BorderLayout());
		labelPanel.add(versionLabel, BorderLayout.NORTH);

		labelPanel.add(status, BorderLayout.SOUTH);

		topPanel.add(labelPanel, BorderLayout.SOUTH);
		labelPanel.setBorder(BorderFactory.createEmptyBorder(30, 5, 30, 5));
		panel.add(topPanel, BorderLayout.CENTER);
		panel.add(bottomPanel, BorderLayout.SOUTH);
	}

	public void setStatus(String statusMessage) {
		status.setText(statusMessage);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		int progress = (Integer) evt.getNewValue();
		if (progress == 0) {
			updateBar.setIndeterminate(true);
		} else {
			updateBar.setString(null);
			updateBar.setStringPainted(true);
			updateBar.setIndeterminate(false);
			updateBar.setValue(progress);
		}
	}

	public void setStartButtonEnabled(boolean enabled) {
		startButton.setEnabled(enabled);
	}

	public void setRepairButtonEnabled(boolean enabled) {
		repairButton.setEnabled(enabled);
	}

	public void setButtonsEnabled(boolean enabled) {
		setStartButtonEnabled(enabled);
		setRepairButtonEnabled(enabled);
	}

	public void ready() {
		updateBar.setIndeterminate(false);
		updateBar.setValue(100);
		setButtonsEnabled(true);
		startButton.requestFocus();
	}

	public void setVersion(VersionNumber versionNumber) {
		String name = EdurasLauncher.CONFIG.getValue("releaseName", "");
		String vName = "version " + versionNumber.toString();
		if (!name.isEmpty()) {
			vName += " (" + name + ")";
		}
		versionLabel.setText(vName);
	}

	public void exit() {
		frame.dispose();
	}

	public void abortProgressBar() {
		updateBar.setIndeterminate(false);
		updateBar.setValue(100);
		updateBar.setStringPainted(true);
		updateBar.setString("error");
	}

	public void showMessage(String title, String message) {
		JOptionPane.showMessageDialog(frame, message, title,
				JOptionPane.INFORMATION_MESSAGE);
	}

	public void showError(String title, String message) {
		JOptionPane.showMessageDialog(frame, message, title,
				JOptionPane.ERROR_MESSAGE);

	}
}
