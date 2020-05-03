package ch.braincell.plantuml.swing.panel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;

import ch.braincell.plantuml.swing.svg.SVGPanel;
import ch.braincell.plantuml.tools.PlantUtil;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.sequencediagram.Newpage;

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
	 * 
	 * @param plantUml String with PlantUML syntax.
	 */
	public void renderPlant(String plantUml) {
		copyButton.setEnabled(true);
		saveButton.setEnabled(true);
		plantUMLScript = plantUml;
		thread.setPlantScript(plantUMLScript);
	}

	/**
	 * Returns the Toolbar. In such the case you need to add something like a button
	 * or so.
	 * 
	 * @return the toolbar of the panel.
	 */
	public JToolBar getToolBar() {
		return toolBar;
	}

	/**
	 * Returns the raw SVG XML which is actually displayed. Yes, you also get "The
	 * plant error display" that way. This is actually the best bet if you try to
	 * find and interpret syntax errors (really).
	 * 
	 * @deprecated You should use the new {@link PlantUtil#getSVGPlant(String)}
	 *             instead. Have a look.
	 * @return the SVG as String, null if there is actually no code to render.
	 */
	public String getSVGPlant() {
		return PlantUtil.getSVGPlant(plantUMLScript);
	}

	protected void copyPlantToClipboard() {
		PlantUtil.copyPlantToClipboard(plantUMLScript);
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

				PlantUtil.savePicture(plantUMLScript, file, ff);
			}
		}
	}

}
