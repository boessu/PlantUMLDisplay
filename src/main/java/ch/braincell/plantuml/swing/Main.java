package ch.braincell.plantuml.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;

import ch.braincell.plantuml.swing.panel.PlantPanel;

public class Main {
	
	public static void main(String[] args) {
		
		String source = "@startuml\n";
		source += "Bob -> ƒ÷‹‰ˆ¸liceﬂ : hello & all\n";
		source += "@enduml\n";

		JFrame jf = new JFrame("PlantUMLPanel");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setPreferredSize(new Dimension(640,480));
		PlantPanel panel = new PlantPanel();
		jf.add(panel, BorderLayout.CENTER);
		jf.pack();

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				jf.setVisible(true);
			}
		});
		
		panel.renderPlant(source);
	}

}
