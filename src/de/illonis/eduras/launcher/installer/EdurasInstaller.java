package de.illonis.eduras.launcher.installer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.illonis.eduras.launcher.gui.images.ImageFiler;

public class EdurasInstaller implements InstallFinishedListener {

	private final JFrame frame;
	private JPanel stepPanel;

	final static String STARTPANEL = "Startpanel";
	final static String INSTALLPANEL = "Installpanel";
	final static String DONEPANEL = "Donepanel";
	private InstallPanel installCard;
	private DonePanel doneCard;
	private Path targetPath;
	private InstallTask task;

	private EdurasInstaller() {
		frame = new JFrame("Eduras installer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		buildGui();
	}

	private void buildGui() {

		frame.setSize(500, 300);
		JPanel panel = (JPanel) frame.getContentPane();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

		JLabel title = new JLabel(ImageFiler.loadIcon("logo_small.png"));
		title.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 5));
		panel.add(title, BorderLayout.NORTH);
		CardLayout layout = new CardLayout();
		stepPanel = new JPanel(layout);

		JPanel startCard = new StartPanel(this);
		installCard = new InstallPanel(this);
		doneCard = new DonePanel();

		stepPanel.add(startCard, STARTPANEL);
		stepPanel.add(installCard, INSTALLPANEL);
		stepPanel.add(doneCard, DONEPANEL);
		layout.show(stepPanel, STARTPANEL);
		panel.add(stepPanel, BorderLayout.CENTER);
	}

	private void showGui() {
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	void startInstalling(Path p) {
		CardLayout layout = (CardLayout) stepPanel.getLayout();
		layout.show(stepPanel, INSTALLPANEL);
		targetPath = p;
		task = new InstallTask(p, this);
		task.addPropertyChangeListener(installCard);
		task.execute();
	}

	public static void main(String[] args) {
		EdurasInstaller installer = new EdurasInstaller();
		installer.showGui();
	}

	@Override
	public void onSetupFinished() {
		boolean v = false;
		try {
			v = task.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		doneCard.setPath(targetPath);
		CardLayout layout = (CardLayout) stepPanel.getLayout();
		layout.show(stepPanel, DONEPANEL);
		if (!v)
			doneCard.setFailed(task.getError());
	}
}
