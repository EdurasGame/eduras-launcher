package de.illonis.eduras.launcher.gui.images;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Provides utility features to load or interact with images stored in game
 * package. All images are stored in image-package or subpackages.
 * 
 * @author illonis
 * 
 */
public class ImageFiler {

	/**
	 * Loads an image from internal filesystem and returns its
	 * {@link BufferedImage}.
	 * 
	 * @param fileName
	 *            file name of image. Must be relative to images-package.
	 * @return image.
	 * @throws IOException
	 *             when image could not be loaded.
	 * @throws IllegalArgumentException
	 *             if there is no file with given filename.
	 */
	public static BufferedImage load(String fileName) throws IOException,
			IllegalArgumentException {
		return ImageIO.read(ImageFiler.class.getResource(fileName));
	}

	/**
	 * Loads an icon from internal filesystem.
	 * 
	 * @param path
	 *            file name of icon. Must be relative to images-package.
	 * @return an {@link ImageIcon} from given file.
	 */
	public static ImageIcon loadIcon(String path) {
		URL imgURL = ImageFiler.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.out.println("Image not found: " + path);
			return null;
		}
	}
}
