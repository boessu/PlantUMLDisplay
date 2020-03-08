package ch.braincell.plantuml.swing.svg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.io.StringReader;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.KeyStroke;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;

public class SVGPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private SVGUniverse svgUniverse = SVGCache.getSVGUniverse();
	private SVGDiagram diagram = null;
	private URI svgURI;
	private static final String svgName = "simpleStringSVG";

	private AffineTransform transXform = new AffineTransform();

	private static final double DEFAULTSCALE = 1.0;
	
	private double scaleFactor = DEFAULTSCALE;
	private double moveGraphX = 0;
	private double moveGraphY = 0;

	private JViewport viewport = null;

	public SVGPanel() {
		super();
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		setBackground(Color.white);
		final SVGPanel self = this;
		SVGMouseListener listener = new SVGMouseListener(this);
		addMouseWheelListener(listener);
		addMouseListener(listener);
		addMouseMotionListener(listener);

		// Change mouse cursor on pressed Control to indicate scroll mode.
		getInputMap().put(
				KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL,
						InputEvent.CTRL_DOWN_MASK, false), "ZoomMouse");
		getActionMap().put("ZoomMouse", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				self.setCursor(Cursor
						.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			}
		});
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0, true),
				"DefaultMouse");
		getActionMap().put("DefaultMouse", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				self.setCursor(Cursor
						.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
	}

	public JViewport getViewport() {
		if (viewport == null && getParent() instanceof JViewport) {
			viewport = (JViewport) getParent();
		}
		return viewport;
	}

	@Override
	protected void paintComponent(Graphics gg) {
		super.paintComponent(gg);

		Graphics2D g = (Graphics2D) gg.create();
		paintComponent(g);
		g.dispose();
	}

	private void paintComponent(Graphics2D g) {
		if (diagram == null)
			return;

		// store old setting before painting
		Object oldAliasHint = g
				.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		AffineTransform oldXform = g.getTransform();

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		Dimension dim = getSize();
		final double width = dim.width;
		final double height = dim.height;
		double diaWidth = diagram.getWidth() * scaleFactor;
		double diaHeight = diagram.getHeight() * scaleFactor;
		double translateX = 0 + moveGraphX;
		double translateY = 0 + moveGraphY;

		if (width > diaWidth)
			translateX = (width - diaWidth) / 2 / scaleFactor;
		if (height > diaHeight)
			translateY = (height - diaHeight) / 2 / scaleFactor;
		transXform.setToScale(scaleFactor, scaleFactor);
		transXform.translate(translateX, translateY);

		g.transform(transXform);

		try {
			diagram.render(g);

		} catch (SVGException e) {
			throw new RuntimeException(e);
		}

		g.setTransform(oldXform);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAliasHint);
	}

	protected double getDefaultScaleFactor() {
		double defaultScale = getParent().getSize().width / diagram.getWidth();
		defaultScale = defaultScale > DEFAULTSCALE ? DEFAULTSCALE : defaultScale;
		return defaultScale;
	}

	public void renderSVG(String svgstring) {
		svgUniverse.clear();
		svgURI = svgUniverse.loadSVG(new StringReader(svgstring), svgName);
		diagram = svgUniverse.getDiagram(svgURI);
		repaint();
		revalidate();
	}

	@Override
	public Dimension getPreferredSize() {
		if (diagram != null)
			return new Dimension((int) (diagram.getWidth() * scaleFactor),
					(int) (diagram.getHeight() * scaleFactor));
		return new Dimension();
	}
	
	public double getScaleFactor() {
		return scaleFactor;
	}
	
	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}
}
