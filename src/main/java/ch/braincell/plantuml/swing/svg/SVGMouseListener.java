package ch.braincell.plantuml.swing.svg;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JViewport;

public class SVGMouseListener extends MouseAdapter {
	private SVGPanel panel;
	private final Point pp = new Point();
	
	public SVGMouseListener(SVGPanel panel) {
		this.panel = panel;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
			panel.setScaleFactor( panel.getScaleFactor() + (e.getWheelRotation() * 0.1));
			panel.repaint();
			panel.revalidate();
		} else
			e.getComponent().getParent().dispatchEvent(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == 3) {
			panel.setScaleFactor(panel.getDefaultScaleFactor());
			panel.repaint();
			panel.revalidate();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == 1) {
			panel.setCursor(Cursor
					.getPredefinedCursor(Cursor.HAND_CURSOR));
			pp.setLocation(e.getLocationOnScreen());
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == 1) {
			panel.setCursor(Cursor
					.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (panel.getViewport() != null) {
			JViewport vport = panel.getViewport();
			Point cp = e.getLocationOnScreen();
			Point vp = vport.getViewPosition();
			vp.translate(pp.x - cp.x, pp.y - cp.y);
			panel.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
			pp.setLocation(cp);
		}
	}
}