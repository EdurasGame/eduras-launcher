package de.illonis.eduras.launcher.tools;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class DeleteFileWalker extends SimpleFileVisitor<Path> {

	private final String jar;

	public DeleteFileWalker(String jar) {
		super();
		this.jar = jar;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
		if (file.getFileName().toString().equalsIgnoreCase(jar))
			return FileVisitResult.CONTINUE;
		System.out.println("Deleting " + file.toString());
		try {
			Files.deleteIfExists(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
		try {
			Files.deleteIfExists(dir);
		} catch (DirectoryNotEmptyException e) {
			System.out.println("Not empty: " + dir.toString());
		} catch (IOException e) {
			System.out.println("couldnt delete " + dir.toString());
			e.printStackTrace();
		}
		return FileVisitResult.CONTINUE;
	}

	// If there is some error accessing
	// the file, let the user know.
	// If you don't override this method
	// and an error occurs, an IOException
	// is thrown.
	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) {
		System.err.println(exc);
		return FileVisitResult.CONTINUE;
	}
}
