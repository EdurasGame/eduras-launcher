package de.illonis.eduras.launcher.installer;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class InstallPanel extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	private final JProgressBar progressBar;

	public InstallPanel(EdurasInstaller installer) {
		super(new BorderLayout());
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		add(progressBar, BorderLayout.SOUTH);
		add(new JLabel("installing..."), BorderLayout.CENTER);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			if (progress == 0) {
				progressBar.setIndeterminate(true);
			} else {
				progressBar.setString(null);
				progressBar.setStringPainted(true);
				progressBar.setIndeterminate(false);
				progressBar.setValue(progress);
			}
		}
	}

}
