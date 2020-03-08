package ch.braincell.plantuml.swing.panel;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

import ch.braincell.plantuml.swing.svg.SVGPanel;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;

public class PlantPanel extends JPanel {
	Logger log = Logger.getLogger(PlantPanel.class.getCanonicalName());

	private static final long serialVersionUID = 1L;

	private SVGPanel svg = null;
	private JButton copyButton = null;
	private JButton saveButton = null;
	private JScrollPane scrollPane = null;
	private JToolBar toolBar = null;
	private static final int VERTICAL_SCROLL_UNITS = 16;

	protected String plantUMLScript = null;
	protected String plantUMLSVG = null;

	private PlantRenderThread thread = null;

	public PlantPanel() {
		super();
		initComponents();
	}

	protected void initComponents() {
		setLayout(new BorderLayout());
		svg = new SVGPanel();
		scrollPane = new JScrollPane(svg);
		scrollPane.getVerticalScrollBar().setUnitIncrement(VERTICAL_SCROLL_UNITS);
		add(scrollPane, BorderLayout.CENTER);

		toolBar = new JToolBar();
		add(toolBar, BorderLayout.NORTH);

		copyButton = new JButton();
		copyButton.setText("Copy");
		copyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyPlantToClipboard();
			}
		});
		copyButton.setEnabled(false);
		toolBar.add(copyButton);

		saveButton = new JButton();
		saveButton.setText("Save");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				savePlantPicture();
			}
		});
		saveButton.setEnabled(false);
		toolBar.add(saveButton);

		thread = new PlantRenderThread(svg); // Thread
	}

	/**
	 * Displays the PlantUML source.
	 * @param plantUml	String with PlantUML syntax.
	 */
	public void renderPlant(String plantUml) {
		copyButton.setEnabled(true);
		saveButton.setEnabled(true);
		plantUMLScript = plantUml;
		thread.setPlantScript(plantUMLScript);
	}

	/**
	 * Returns the Toolbar. In such the case you need to add something like a button or so.
	 * @return the toolbar of the panel.
	 */
	public JToolBar getToolBar() {
		return toolBar;
	}

	protected void copyPlantToClipboard() {
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
				closeStream(is);
				closeStream(os);
			}
			if (null != image) {
				clipboard.setContents(new ImageSelection(image), null);
			}
		}
	}

	protected void savePlantPicture() {
		if (plantUMLScript != null) {
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter svgFilter = new FileNameExtensionFilter("SVG", "svg");
			FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG", "png");
			chooser.addChoosableFileFilter(svgFilter);
			chooser.addChoosableFileFilter(pngFilter);
			chooser.setAcceptAllFileFilterUsed(false);
			int returnVal = chooser.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = null;
				String fileName = chooser.getSelectedFile().getName();
				FileFormat ff;
				String postfix;
				if (chooser.getFileFilter() == svgFilter) {
					log.info("svg is choosen");
					ff = FileFormat.SVG;
					postfix = ".svg";
				} else {
					log.info("png is choosen");
					ff = FileFormat.PNG;
					postfix = ".png";
				}
				if (fileName.endsWith(postfix))
					file = new File(chooser.getSelectedFile().getAbsolutePath());
				else
					file = new File(chooser.getSelectedFile().getAbsolutePath() + postfix);

				log.info("File will be written... " + file.getAbsolutePath());

				SourceStringReader reader = new SourceStringReader(plantUMLScript);

				final ByteArrayOutputStream os = new ByteArrayOutputStream();
				ByteArrayInputStream is = null;
				OutputStreamWriter writer = null;
				// Write the first image to "os"
				try {
					DiagramDescription desc = reader.outputImage(os, new FileFormatOption(ff));
					os.close();
					log.info("Parsed plant: " + desc.getDescription());

					if (ff == FileFormat.SVG) {
						// The XML is stored into svg
						plantUMLSVG = new String(os.toByteArray(), Charset.forName("UTF-8"));
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
					closeStream(is);
					closeStream(os);
					closeWriter(writer);
				}

			}
		}
	}

	private void closeStream(InputStream stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private void closeStream(OutputStream stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	private void closeWriter(Writer writer) {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
}
