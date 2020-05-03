package ch.braincell.plantuml.tools;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;

public class PlantUtil {
	static Logger log = Logger.getLogger(PlantUtil.class.getCanonicalName());

	/**
	 * Creates an SVG picture out of a plant script.
	 * 
	 * @param plantUMLScript the script to convert.
	 * @return SVG XML in a String. Null if the script was null or any exception
	 *         happened (that's basically unlikely).
	 */
	public static String getSVGPlant(String plantUMLScript) {
		String result = null;

		if (plantUMLScript != null) {
			SourceStringReader reader = new SourceStringReader(plantUMLScript);

			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			// Write the first image to "os"
			try {
				DiagramDescription desc = reader.outputImage(os, new FileFormatOption(FileFormat.SVG));
				os.close();
				log.info("Parsed plant: " + desc.getDescription());
				result = new String(os.toByteArray(), Charset.forName("UTF-8"));
			} catch (IOException e) {
				log.severe("IO Exceptions on Strings: source " + plantUMLScript);
			} finally {
				closeIO(os);
			}
		}

		return result;
	}

	/**
	 * Copies the (first) bitmap picture from a plant script into the clipboard.
	 * 
	 * @param plantUMLScript plantuml script to convert. If it is null, nothing
	 *                       happens.
	 */
	public static void copyPlantToClipboard(String plantUMLScript) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		if (plantUMLScript != null) {
			ByteArrayOutputStream os = null;
			ByteArrayInputStream is = null;
			Image image = null;
			try {
				SourceStringReader reader = new SourceStringReader(plantUMLScript);

				os = new ByteArrayOutputStream();
				reader.outputImage(os, new FileFormatOption(FileFormat.PNG));
				is = new ByteArrayInputStream(os.toByteArray());
				image = ImageIO.read(is);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			} finally {
				closeIO(is);
				closeIO(os);
			}
			if (null != image) {
				clipboard.setContents(new ImageSelection(image), null);
			}
		}
	}

	/**
	 * Saves PlantUML Script as a SVG or PNG picture. If the format is not SVG, a
	 * png will be saved. The filename-extension will not be changed. If any
	 * exception happens, a {@link RuntimeException} will be re-thrown.
	 * 
	 * @param plantUMLScript plantuml script to convert.
	 * @param file           File to store the picture in it.
	 * @param fileFormat     Fileformat to store. Currently only SVG and PNG is
	 *                       supported. If the format is anything else than SVG, a
	 *                       PNG will be saved.
	 */
	public static void savePicture(String plantUMLScript, File file, FileFormat fileFormat) {
		if (plantUMLScript != null && file != null && fileFormat != null) {
			log.info("File will be written... " + file.getAbsolutePath());

			SourceStringReader reader = new SourceStringReader(plantUMLScript);

			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			ByteArrayInputStream is = null;
			OutputStreamWriter writer = null;
			// Write the first image to "os"
			try {
				DiagramDescription desc = reader.outputImage(os, new FileFormatOption(fileFormat));
				os.close();
				log.info("Parsed plant: " + desc.getDescription());

				if (fileFormat == FileFormat.SVG) {
					// The XML is stored into svg
					String plantUMLSVG = new String(os.toByteArray(), Charset.forName("UTF-8"));
					writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
					writer.write(plantUMLSVG);
				} else {
					// png will be saved.
					is = new ByteArrayInputStream(os.toByteArray());
					BufferedImage image = ImageIO.read(is);
					ImageIO.write(image, "png", file);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				closeIO(is);
				closeIO(os);
				closeIO(writer);
			}
		}
	}

	/**
	 * Save way to close a resource. A {@link RuntimeException} will be thrown if
	 * the closing fails (it's very unlikely that this fails. That would be a Runtime
	 * problem).
	 * 
	 * @param closing Resource to close.
	 */
	private static void closeIO(Closeable closing) {
		if (closing != null) {
			try {
				closing.close();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

}
