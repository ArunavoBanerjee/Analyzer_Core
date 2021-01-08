package analyzer.Base;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.Scanner;

import analyzer.Validators.Validator;

/**
 * Analyzer Wrapper. Wraps calls to all other underlying classes and methods.
 * 
 * @author arunavo.banerjee.cse16@gmail.com
 *
 */
public class Analyzer {

	static Scanner in = new Scanner(System.in);

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		long time = System.currentTimeMillis();
		String reportType = args[0];
		if (reportType.equalsIgnoreCase("-d"))
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

		/**
		 * Set Validator parameters and verify.
		 */
		if (prop.getProperty("dataType") != null)
			Validator.dataType = prop.getProperty("dataType").strip();
		if (prop.getProperty("matchType") != null)
			Validator.matchType = prop.getProperty("matchType").strip();
		if (prop.getProperty("matchCase") != null)
			Validator.matchCase = prop.getProperty("matchCase").strip();
		if (prop.getProperty("leftTokenizer") != null)
			Validator.left_token = prop.getProperty("leftTokenizer").strip();
		if (prop.getProperty("rightTokenizer") != null)
			Validator.right_token = prop.getProperty("rightTokenizer").strip();
		if (prop.getProperty("splitExpression") != null)
			Validator.expr_str = prop.getProperty("splitExpression").strip().replaceAll("\\s*:\\s*", ":")
					.replaceAll("\\s+", " ");
		if (!Splitter.isReport && Validator.expr_str.isBlank())
			throw new Exception("Split Expression is mandatory for data splitting.");
		if (prop.getProperty("splitListFile") != null)
			Validator.splitlistPath = prop.getProperty("splitListFile").strip();

		Validator new_validator = null;
		if (!(reportType.equals("-r") && Validator.expr_str.isBlank())) {
			new_validator = new Validator();
		}

		Splitter.sourceList = prop.getProperty("sourceFile").split(",");
		if (prop.getProperty("targetFileMatched") != null)
			Splitter.dest_matched = prop.getProperty("targetFileMatched").strip();
		if (prop.getProperty("targetFileUnMatched") != null)
			Splitter.dest_unmatched = prop.getProperty("targetFileUnMatched").strip();
		if (prop.getProperty("reportDestination") != null)
			Splitter.reportDest = prop.getProperty("reportDestination").strip();
		if (prop.getProperty("keepSourceHier") != null)
			Splitter.keepsrchier = Boolean.valueOf(prop.getProperty("keepSourceHier").strip());

		if (prop.getProperty("batchSize") != null) {
			String _batchsize = prop.getProperty("batchSize").strip();
			if (!_batchsize.isEmpty()) {
				try {
					Splitter.batchSize = Integer.parseInt(_batchsize);
				} catch (Exception e) {
					System.out.println("batchSize value '" + prop.getProperty("batchSize") + "' is not a number.");
					throw e;
				}
			}
		}

		// Running options validation and control settings.
		validate_input_conditions(new_validator);

		if (prop.getProperty("dataReadPath") != null)
			Splitter.dataReadPath = prop.getProperty("dataReadPath");
		if (prop.getProperty("csvConfigPath") != null)
			Splitter.csvconfigPath = prop.getProperty("csvConfigPath");

		Splitter newSplit = new Splitter(new_validator);
		newSplit.churnData();

		long elapsed_time = (System.currentTimeMillis() - time);
		if (elapsed_time / 60000 < 1) {
			elapsed_time = elapsed_time / 1000;
			System.out.println("Total Processing Time " + Math.round(elapsed_time * 100) / 100 + " seconds.");
		} else {
			elapsed_time = elapsed_time / 1000;
			float elapsed_min = elapsed_time / 60;
			float elapsed_sec = elapsed_time % 60;
			System.out.println("Total Processing Time " + Math.round(elapsed_min) + " mins "
					+ Math.round(elapsed_sec * 100) / 100 + " seconds.");
		}
	}

	/**
	 * Validate the input condition variations and set the variables accordingly.
	 * 
	 * @param new_validator
	 * @throws Exception
	 */
	static void validate_input_conditions(Validator new_validator) throws Exception {
		if (Splitter.isReport) {
			if (Splitter.dest_matched.isEmpty() && Splitter.dest_unmatched.isEmpty() && Splitter.reportDest.isEmpty()) {
				throw new Exception("Report destinations mandatory for -r Flag.");
			}
			if (new_validator == null) {
				if (!Splitter.reportDest.isEmpty())
					Splitter.report_matched = Splitter.reportDest + File.separatorChar + "data-report";
				else
					throw new Exception(
							"Default Data report need to have mandatory reportDestination parameter value in properties file.\nOther destination parameter values are not considered.");
			} else if (!(Splitter.dest_matched.isBlank() && Splitter.dest_unmatched.isBlank())) {
				if (Splitter.dest_matched.endsWith("tar.gz"))
					Splitter.report_matched = Splitter.dest_matched.replace(".tar.gz", "") + "_report";
				else
					Splitter.report_matched = Splitter.dest_matched;
				if (Splitter.dest_unmatched.endsWith("tar.gz"))
					Splitter.report_unmatched = Splitter.dest_unmatched.replace(".tar.gz", "") + "_report";
				else
					Splitter.report_unmatched = Splitter.dest_unmatched;
			} else {
				Splitter.report_matched = Splitter.reportDest + File.separatorChar + "matched-data-report";
				Splitter.report_unmatched = Splitter.reportDest + File.separatorChar + "unmatched-data-report";
			}
		} else {
			if (Splitter.dest_matched.isEmpty() && Splitter.dest_unmatched.isEmpty()) {
				throw new Exception("Data destinations mandatory for -d Flag.");
			} else {
				System.out.println("Do you want to generate a data report? (Yes/No)");
				String reportFlag = in.next();
				if (reportFlag.equalsIgnoreCase("yes")) {
					System.out.println("Generate report in the data location? (Yes/No)");
					reportFlag = in.next();
					if (reportFlag.equalsIgnoreCase("yes")) {
						if (!Splitter.dest_matched.isEmpty())
							Splitter.report_matched = Splitter.dest_matched.replace(".tar.gz", "") + "_report";
						if (!Splitter.dest_unmatched.isEmpty())
							Splitter.report_unmatched = Splitter.dest_unmatched.replace(".tar.gz", "") + "_report";
					} else if (reportFlag.equalsIgnoreCase("no")) {
						if (Splitter.reportDest.isEmpty()) {
							System.out.println(
									"Report Destination not provided. No report will be generated. Continue? (Yes/No)");
							reportFlag = in.next();
							if (reportFlag.equalsIgnoreCase("yes")) {
								Splitter.dataOnly = true;
							} else if (reportFlag.equalsIgnoreCase("no")) {
								in.close();
								throw new Exception("Program terminated as per user's request");
							} else {
								in.close();
								throw new Exception("Invalid entry : '" + reportFlag + "'");
							}
						} else {
							Splitter.report_matched = Splitter.reportDest + File.separatorChar
									+ "Analyzer_matched-report";
							Splitter.report_unmatched = Splitter.reportDest + File.separatorChar
									+ "Analyzer_unmatched-report";
						}
					} else if (reportFlag.equalsIgnoreCase("no")) {
						Splitter.dataOnly = true;
					}
				}
				in.close();
			}
		}
	}
}
