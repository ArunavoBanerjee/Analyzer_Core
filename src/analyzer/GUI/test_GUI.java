package analyzer.GUI;

import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class test_GUI {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		test_GUI expr_GUI = new test_GUI();
		expr_GUI.create_a_frame();
	}

	
	JFrame create_a_frame() {
		
		JFrame newFrame = new JFrame();
		newFrame.setVisible(true);
		newFrame.setSize(400,400);
		newFrame.setLayout(new FlowLayout());
		newFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		newFrame.add(create_a_label("some text"));
		newFrame.add(create_a_label("some more text"));
		return newFrame;
	}
	
	
	JLabel create_a_label(String text) {
		
		return new JLabel(text);
		
	}
	
	
	
}
