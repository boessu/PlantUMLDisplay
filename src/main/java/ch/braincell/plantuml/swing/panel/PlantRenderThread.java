package ch.braincell.plantuml.swing.panel;

import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ch.braincell.plantuml.swing.svg.SVGPanel;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;

public class PlantRenderThread extends Thread {
	private volatile String newPlantScript = null;
	private String workingPlantScript = null;
	
	private String plantUMLSVG = null;

	private SVGPanel panel = null;

	public PlantRenderThread(SVGPanel panel) {
		this.panel = panel;
		this.start();
	}

	public void run() {
		try {
			do {
				if (null != newPlantScript && !newPlantScript.equals(workingPlantScript)) {
					workingPlantScript = newPlantScript;
					renderPlant(workingPlantScript);
				}
				sleep(200);
			} while (true);
		} catch (Throwable e) {
            StringBuilder sb = new StringBuilder("Error: ");
            sb.append(e.getMessage());
            sb.append("\n");
            for (StackTraceElement ste : e.getStackTrace()) {
                sb.append(ste.toString());
                sb.append("\n");
            }
            JTextArea jta = new JTextArea(sb.toString());
            JScrollPane jsp = new JScrollPane(jta){
 				private static final long serialVersionUID = 1L;

 				@Override
                public Dimension getPreferredSize() {
                    return new Dimension(480, 320);
                }
            };
            JOptionPane.showMessageDialog(
                null, jsp, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void setPlantScript(String plantScript) {
		newPlantScript = plantScript;
	}
	
	private void renderPlant(String plantUMLScript) {
		SourceStringReader reader = new SourceStringReader(plantUMLScript);

		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		// Write the first image to "os"
		try {
			DiagramDescription desc = reader.outputImage(os, new FileFormatOption(
					FileFormat.SVG));
			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		// The XML is stored into svg
		plantUMLSVG = new String(os.toByteArray(), Charset.forName("UTF-8"));
		panel.renderSVG(plantUMLSVG);
	}

}
