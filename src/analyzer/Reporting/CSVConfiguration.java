package analyzer.Reporting;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import analyzer.Base.Splitter;

public class CSVConfiguration {
	int rowlimit = 0;
	String multivalue_seperator = "", ID = "";
	public Set<String> field_to_write = new HashSet<String>();
	public CSVConfiguration() throws Exception {
		Properties prop = new Properties();
		try {
			InputStream input = null;
			if(Splitter.csvconfigPath.isBlank()) {
				input = CSVConfiguration.class.getResourceAsStream("/analyzer/DefaultConfig/csvconfig");
			} else {
				input = new FileInputStream(new File(Splitter.csvconfigPath));
			}
			prop.load(input);
			if (prop.getProperty("rowlimit") == null)
				throw new Exception("rowlimit param missing in CSV config.");
			else
				rowlimit = Integer.parseInt(prop.getProperty("rowlimit"));
			if (prop.getProperty("multivalue_seperator") == null)
				throw new Exception("param multivalue_seperator missing in CSV config.");
			else
				multivalue_seperator = prop.getProperty("multivalue_seperator");
			if (prop.getProperty("fieldList") != null) {
				String fieldList = prop.getProperty("fieldList").replaceAll(",\s*", ",");
				field_to_write.addAll(Arrays.asList(fieldList.split(",")));
			}
			if (prop.getProperty("ID") != null)
				ID=prop.getProperty("ID");
			else {
				System.out.println("ID field is not provided in the CSV Configuration File."
						+ "\nDo you want to default it to Handle_ID or set new value(case-sensitive) or terminate?[Yes/New/No]");
				Scanner in = new Scanner(System.in);
				if(in.next().equalsIgnoreCase("yes"))
					ID = "Handle_ID";
				else if (in.next().equalsIgnoreCase("new"))
					ID = in.next();
				else if (in.next().equalsIgnoreCase("no")) {
					in.close();
					System.out.println("Program terminating with input option \"No\"");
					System.exit(0);
				}
				else {
					in.close();
					System.out.println("Input value Wrong. Terminating Program...");
					System.exit(0);
				}
			}
		} catch (NumberFormatException e) {
			System.out.println("Number format error in rowlimit param.\nrowlimit is an Integer value.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("CSV configuration file missing.");
		}
	}
}
