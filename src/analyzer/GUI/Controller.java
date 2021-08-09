package analyzer.GUI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.filechooser.FileNameExtensionFilter;

import analyzer.Base.Analyzer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class Controller {

	@FXML
	Label runTypeLabel;
	
	@FXML
	TextField fcPath;
	
	@FXML
	TextArea fcBody, console;

	@FXML
	Button fcButton, runButton;
	
	@FXML
	RadioButton radioData, radioReport;
	
	String runType;
	File analyzer_run_properties;
	
    private PrintStream ps;
	
    public void initialize() {
        ps = new PrintStream(new Console(console),true);
    }
    public class Console extends OutputStream {
        private TextArea console;
 
        public Console(TextArea console) {
            this.console = console;
        }
 
        public void appendText(String valueOf) {
            Platform.runLater(() -> console.appendText(valueOf));
        }
 
        public void write(int b) throws IOException {
            appendText(String.valueOf((char)b));
        }
    }
	
	public void fileChooser(ActionEvent event) throws Exception{
		FileChooser fc = new FileChooser();
		Properties gui_properties = new Properties();
		String last_opened_location = "";
		try {
			gui_properties.load(new FileInputStream("user_prefs.properties"));
			last_opened_location = gui_properties.getProperty("last_opened_location","");
		} catch (FileNotFoundException e) {
			
		}
		if(!last_opened_location.isBlank())
			fc.setInitialDirectory(new File(last_opened_location));
		fc.getExtensionFilters().addAll(new ExtensionFilter("properties files", "*.properties"));
		analyzer_run_properties = fc.showOpenDialog(null);
		if(analyzer_run_properties != null) {
			BufferedReader br = new BufferedReader(new FileReader(analyzer_run_properties));
			StringBuilder brString = new StringBuilder();
			String out = ""; 
			while( (out = br.readLine()) != null)
				brString.append(out+"\n");
			fcPath.clear();
			fcBody.clear();
			fcPath.appendText(analyzer_run_properties.getAbsolutePath());
			fcBody.appendText(brString.toString());
			gui_properties.setProperty("last_opened_location", analyzer_run_properties.getParentFile().getAbsolutePath());
			gui_properties.store(new FileOutputStream("user_prefs.properties"), null);
		}
		else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Choose a properties File.");
		}
	}
	
	public void fileSaver(ActionEvent event) throws Exception {
		FileChooser fc = new FileChooser();
		Properties gui_properties = new Properties();
		String last_opened_location = "";
		try {
			gui_properties.load(new FileInputStream("user_prefs.properties"));
			last_opened_location = gui_properties.getProperty("last_opened_location","");
		} catch (FileNotFoundException e) {
			
		}
		if(!last_opened_location.isBlank())
			fc.setInitialDirectory(new File(last_opened_location));
		fc.getExtensionFilters().addAll(new ExtensionFilter("properties files", "*.properties"));
		String textSave = fcBody.getText();
		analyzer_run_properties = fc.showSaveDialog(null);
		if(analyzer_run_properties != null) {
			saveTextToFile(textSave, analyzer_run_properties);
			fcPath.clear();
			fcPath.appendText(analyzer_run_properties.getAbsolutePath());
			gui_properties.setProperty("last_opened_location", analyzer_run_properties.getParentFile().getAbsolutePath());
			gui_properties.store(new FileOutputStream("user_prefs.properties"), null);
		}
		else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Choose a properties File.");
		}
	}
	
    private void saveTextToFile(String content, File file) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);
            writer.println(content);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    		
		public void runAnalyzer(ActionEvent event) throws Exception {
			console.clear();
	        System.setOut(ps);
	        System.setErr(ps);
			new Thread() {
			    public void run() {
			    	try {
		                runButton.setDisable(true);
		                Analyzer analyzer = new Analyzer();
						analyzer.runAnalysis(runType, analyzer_run_properties);
						runButton.setDisable(false);
				    	} catch(Exception e) {
				    		e.printStackTrace();
				    	}	        
			    }
			}.start();
		}
		
		public void getRunType(ActionEvent event) {
			if(radioData.isSelected()) {
				runTypeLabel.setText("You have choosed data splitting.");
				runType = "-d";
			}
			else if(radioReport.isSelected()) {
				runTypeLabel.setText("You have choosed data reporting.");
				runType = "-r";
			}
		}
		
	}
