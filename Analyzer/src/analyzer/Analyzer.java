package analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.Scanner;

/**
 * Analyzer Wrapper. Wraps calls to all other underlying classes and methods. 
 * @author user
 *
 */
public class Analyzer {
	static Scanner in = new Scanner(System.in);
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		long time = System.currentTimeMillis();
		String reportType = args[0];
		if(reportType.equalsIgnoreCase("-d"))
			Splitter.isReport = false;
		else if (reportType.equalsIgnoreCase("-r"))
			Splitter.isReport = true;
		else
			throw new Exception("Splitter runType flag to be set to -d/-r.\nPlease refer documentation.");
		String propPath = args[1];
		if (propPath.isEmpty())
			throw new Exception("Properties File not set.");
		Properties prop = new Properties();
		File properties = new File(propPath);
		InputStream input = new FileInputStream(properties);
		prop.load(input);
		if (prop.getProperty("matchType") != null) {
			Validator.matchType = prop.getProperty("matchType").strip().toLowerCase();
			if (!(Validator.matchType.isBlank() || Validator.matchType.matches("(?i)startsWith|endsWith|contains|matches|equals|exists|dupl")))
				throw new Exception("Property matchType has wrong definition : " + Validator.matchType
						+ "\nValid values are startsWith|endsWith|contains|matches|exists|dupl");
		}
		if (prop.getProperty("matchCase") != null) {
			Validator.matchCase = prop.getProperty("matchCase").strip().toLowerCase();
			if (!(Validator.matchCase.isBlank() || Validator.matchCase.matches("(?i)fold|no-fold")))
				throw new Exception("Property matchCase has wrong definition : " + Validator.matchCase 
						+ "\nValid Values are fold|no-fold");
		}
		if (prop.getProperty("dataType") != null) {
			Validator.dataType = prop.getProperty("dataType").strip().toLowerCase();
			if (!(Validator.dataType.isBlank() || Validator.dataType.matches("(?i)str|regx")))
				throw new Exception("Property dataType has wrong definition : " + Validator.dataType 
						+ "\nValid Values are str|regx");
		}
		if(Validator.matchType.equals("equals") && Validator.dataType.equals("regx"))
			throw new Exception("Analyser condition conflict.\nmatchType=equals can not be combined with dataType=regx. Please refer documentation.");
		if (prop.getProperty("splitExpression") != null)
			Validator.expr_str = prop.getProperty("splitExpression").replaceAll("\\s*:\\s*", ":");
		else if (!Splitter.isReport && (prop.getProperty("splitExpression") == null || prop.getProperty("splitExpression").strip().isEmpty()))
			throw new Exception("Split Expression is mandatory for data splitting.");
		if (prop.getProperty("splitListFile") != null)
			Validator.splitlistPath = prop.getProperty("splitListFile");
		Validator new_validator = null;
		if(!(reportType.equals("-r") && Validator.expr_str.isBlank())) {
			new_validator = new Validator();
		}
		Splitter.sourceList = prop.getProperty("sourceFile").split(",");
		if(prop.getProperty("targetFileMatched") != null)
			Splitter.dest_matched = prop.getProperty("targetFileMatched").strip();
		if(prop.getProperty("targetFileUnMatched") != null)
			Splitter.dest_unmatched = prop.getProperty("targetFileUnMatched").strip();
		if (new File(Splitter.dest_matched).exists())
			new File(Splitter.dest_matched).delete();
		if (new File(Splitter.dest_unmatched).exists())
			new File(Splitter.dest_unmatched).delete();
		if(prop.getProperty("reportDestination") == null || prop.getProperty("reportDestination").isBlank()) {
			if(Splitter.isReport) {
				throw new Exception("Report destination mandatory for -r Flag.");
			}
			else {
				System.out.println("Report Destination not provided. No report will be generated. Continue? (Y/N)");
				String runFlag = in.next();
				in.close();
				if(runFlag.equalsIgnoreCase("y")) {
					Splitter.dataOnly = true;
				}
				else if (runFlag.equalsIgnoreCase("n")) {
					throw new Exception("Program terminated as per user's request");
				}
			}
		} else
			Splitter.reportDest = prop.getProperty("reportDestination").strip();
		if (prop.getProperty("dataReadPath") != null)
			Splitter.dataReadPath = prop.getProperty("dataReadPath");
		if (prop.getProperty("csvconfigPath") != null)
			Splitter.csvconfigPath = prop.getProperty("csvconfigPath");
		Splitter newSplit = new Splitter(new_validator);
		newSplit.churnData();
		long elapsed_time = (System.currentTimeMillis() - time);
		if (elapsed_time / 60000 < 1) {
			elapsed_time = elapsed_time / 1000;
			System.out.println("Total Processing Time " + Math.round(elapsed_time*100)/100 + " seconds.");
		} else {
			elapsed_time = elapsed_time / 1000;
			float elapsed_min = elapsed_time / 60;
			float elapsed_sec = elapsed_time % 60;
			System.out.println("Total Processing Time " + Math.round(elapsed_min) + " mins "
					+ Math.round(elapsed_sec*100)/100 + " seconds.");
		}
	}
}
