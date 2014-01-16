package de.illonis.eduras.launcher;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import de.illonis.eduras.launcher.tools.NoJarFileException;
import de.illonis.eduras.launcher.tools.PathFinder;

public class RepairTask extends SwingWorker<Boolean, Void> {

	private final RepairProgressListener listener;

	public RepairTask(RepairProgressListener listener) {
		this.listener = listener;
	}

	@Override
	protected Boolean doInBackground() throws URISyntaxException {
		String jar;
		try {
			jar = PathFinder.getJarName();
		} catch (NoJarFileException e1) {
			e1.printStackTrace();
			return false;
		}
		Path p = Paths.get(PathFinder.getBaseDir().toURI());
		// try {
		// FIXME: repair is buggy
		// Files.walkFileTree(p, new DeleteFileWalker(jar));
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// return false;
		// }

		return true;
	}

	@Override
	public void done() {
		try {
			if (get())
				listener.onRepairCompleted();
			else
				listener.onRepairFailed();
		} catch (InterruptedException | ExecutionException e) {
			listener.onRepairFailed();
		}
	}
}
