package analyzer.Reporting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import com.opencsv.CSVWriter;

import analyzer.Splitter;

public class WriteToCSV extends CSVConfiguration {
	List<String> header = new ArrayList<String>();
	List<String[]> allRows_matched = new ArrayList<String[]>();
	List<String[]> allRows_unmatched = new ArrayList<String[]>();
	TarArchiveEntry out_tarEntry = null;
	String fileName = "";
	File csvout = null;
	CSVWriter cwriter = null;

	public WriteToCSV() throws Exception {
	}

	public void csvloader(HashMap<String, HashSet<String>> sourceDict, Boolean writetomatch) throws Exception {
		String[] row;
		if (!field_to_write.isEmpty()) {
			if (!field_to_write.contains(ID))
				field_to_write.add(ID);
			for (String key : sourceDict.keySet()) {
				if (!header.contains(key) && field_to_write.contains(key)) {
					if (key.equals(ID)) {
						header.add(0, key);
					} else {
						header.add(key);
					}
				}
			}
			row = new String[header.size()];
			for (Map.Entry<String, HashSet<String>> entry : sourceDict.entrySet()) {
				if (field_to_write.contains(entry.getKey())) {
					String data = "";
					for (String value : entry.getValue())
						data += value + multivalue_seperator;
					data = data.substring(0, data.length() - 1);
					row[header.indexOf(entry.getKey())] = data;
				}
			}
		} else {
			for (String key : sourceDict.keySet()) {
				if (!header.contains(key)) {
					if (key.equals(ID)) {
						header.add(0, key);
					} else {
						header.add(key);
					}
				}
			}
			row = new String[header.size()];
			for (Map.Entry<String, HashSet<String>> entry : sourceDict.entrySet()) {
				String data = "";
				for (String value : entry.getValue())
					data += value + multivalue_seperator;
				data = data.substring(0, data.length() - 1);
				row[header.indexOf(entry.getKey())] = data;
			}
		}
		if (writetomatch && !Splitter.report_matched.isEmpty())
			allRows_matched.add(row);
		else if (!Splitter.report_unmatched.isEmpty())
			allRows_unmatched.add(row);
	}

	public void csvwriter_matched() throws Exception {
		Date writeDate = new Date("yyyy_MM_dd-HH_mm_ss");
		Splitter.report_matched += File.separatorChar + writeDate.getDate();
		int file_i = 0;
		List<String[]> filerows = new ArrayList<String[]>();
		String[] headerrow = new String[header.size()];
		for (int i = 0; i < header.size(); i++)
			headerrow[i] = header.get(i);
		boolean createTGT = true;
		for (int row_i = 0; row_i < allRows_matched.size(); row_i++) {
			if (createTGT) {
				createTGT = false;
				if (!new File(Splitter.report_matched).exists())
					new File(Splitter.report_matched).mkdirs();
			}
			filerows.add(allRows_matched.get(row_i));
			if (row_i / rowlimit != file_i) {
				fileName = file_i + ".csv";
				csvout = new File(Splitter.report_matched + File.separatorChar + fileName);
				cwriter = new CSVWriter(new FileWriter(csvout));
				cwriter.writeNext(headerrow);
				cwriter.writeAll(filerows);
				filerows.clear();
				cwriter.close();
				file_i = row_i / rowlimit;
			}
		}
		if (!filerows.isEmpty()) {
			fileName = file_i + ".csv";
			csvout = new File(Splitter.report_matched + File.separatorChar + fileName);
			cwriter = new CSVWriter(new FileWriter(csvout));
			cwriter.writeNext(headerrow);
			cwriter.writeAll(filerows);
			filerows.clear();
			cwriter.close();
		}
	}

	public void csvwriter_unmatched() throws Exception {
		Date writeDate = new Date("yyyy_MM_dd-HH_mm_ss");
		Splitter.report_unmatched += File.separatorChar + writeDate.getDate();
		int file_i = 0;
		List<String[]> filerows = new ArrayList<String[]>();
		String[] headerrow = new String[header.size()];
		for (int i = 0; i < header.size(); i++)
			headerrow[i] = header.get(i);
		boolean createTGT = true;
		for (int row_i = 0; row_i < allRows_unmatched.size(); row_i++) {
			if (createTGT) {
				createTGT = false;
				if (!new File(Splitter.report_unmatched).exists())
					new File(Splitter.report_unmatched).mkdirs();
			}
			filerows.add(allRows_unmatched.get(row_i));
			if (row_i / rowlimit != file_i) {
				fileName = file_i + ".csv";
				csvout = new File(Splitter.report_unmatched + File.separatorChar + fileName);
				cwriter = new CSVWriter(new FileWriter(csvout));
				cwriter.writeNext(headerrow);
				cwriter.writeAll(filerows);
				filerows.clear();
				cwriter.close();
				file_i = row_i / rowlimit;
			}
		}
		if (!filerows.isEmpty()) {
			fileName = file_i + ".csv";
			csvout = new File(Splitter.report_unmatched + File.separatorChar + fileName);
			cwriter = new CSVWriter(new FileWriter(csvout));
			cwriter.writeNext(headerrow);
			cwriter.writeAll(filerows);
			filerows.clear();
			cwriter.close();
		}
	}
}
