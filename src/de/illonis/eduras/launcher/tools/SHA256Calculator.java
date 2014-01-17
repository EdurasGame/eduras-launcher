package de.illonis.eduras.launcher.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.illonis.eduras.launcher.network.DownloadFile;

/**
 * A tool for calculating a SHA-256 hash in hex-format of a file.<br>
 * You can either use the GUI and drag&drop a file onto it or pass a filename as
 * first argument on console.
 * 
 * @author illonis
 * 
 */
public final class SHA256Calculator extends DropTarget {

	private static final long serialVersionUID = 1L;
	private final JTextField target;
	private final JFrame frame;

	private SHA256Calculator(JTextField target, JFrame frame) {
		this.target = target;
		this.frame = frame;
	}

	/**
	 * @param args
	 *            filename (optional)
	 */
	public static void main(String[] args) {
		if (args.length == 1) {
			// calculate from console
			Path path = Paths.get(args[0]);
			String hash = DownloadFile.computeHash(path);
			System.out.println(hash);
			return;
		}
		JFrame frame = new JFrame("SHA-256 calculator");
		frame.getContentPane().setLayout(new BorderLayout());
		JTextField hashText = new JTextField();
		hashText.setEditable(false);
		frame.setDropTarget(new SHA256Calculator(hashText, frame));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(450, 200));
		frame.setLocationRelativeTo(null);
		JLabel label = new JLabel(
				"<html><div style=\"text-align: center;\">Drag&Drop a file here<br>to calculate it's SHA-256 hash.<br>Hash value will appear below.</div></html>",
				JLabel.CENTER);

		label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		label.setHorizontalTextPosition(JLabel.CENTER);
		frame.getContentPane().add(label, BorderLayout.CENTER);
		frame.getContentPane().add(hashText, BorderLayout.SOUTH);
		frame.setVisible(true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void drop(DropTargetDropEvent dtde) {
		// Check the drop action

		dtde.acceptDrop(DnDConstants.ACTION_COPY);
		List<File> droppedFiles;
		try {
			droppedFiles = (List<File>) dtde.getTransferable().getTransferData(
					DataFlavor.javaFileListFlavor);

			File file = droppedFiles.get(0);
			Path path = file.toPath();
			String hash = DownloadFile.computeHash(path);
			target.setText(hash);
			target.requestFocusInWindow();
			target.setSelectionStart(0);
			target.setSelectionEnd(target.getText().length());
			frame.toFront();

		} catch (UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
		}
	}
}
