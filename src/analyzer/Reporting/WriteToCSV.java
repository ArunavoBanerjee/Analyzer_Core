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

import analyzer.Base.Splitter;

public class WriteToCSV extends CSVConfiguration {
	List<String> header = new ArrayList<String>();
	String[] header_row = null;
	public ArrayList<String> keyMaster = new ArrayList<String>();
	List<String[]> allRows_matched = new ArrayList<String[]>();
	List<String[]> allRows_unmatched = new ArrayList<String[]>();
	TarArchiveEntry out_tarEntry = null;
	String fileName = "";
	File csvout = null;
	CSVWriter cwriter = null;
	int file_i_matched = 0, file_i_unmatched = 0;

	public WriteToCSV() throws Exception {
	}

	public boolean loadReportHeader() {
		if (!field_to_write.isEmpty()) {
			field_to_write.remove(ID);
			header.add(ID);
			header.addAll(field_to_write);
		} else {
			header.add(0, ID);
			header.addAll(keyMaster);
		}
		header_row = header.toArray(new String[0]);
		return true;
	}

	public void csvloader(HashMap<String, HashSet<String>> sourceDict, Boolean writetomatch) throws Exception {
		String[] row;
		row = new String[header.size()];
		for (Map.Entry<String, HashSet<String>> entry : sourceDict.entrySet()) {
			if (header.contains(entry.getKey())) {
				String data = "";
				int index = entry.getValue().size();
				for (String value : entry.getValue()) {
					--index;
					if (index == 0)
						data += value;
					else
						data += value + multivalue_seperator;
				}
				row[header.indexOf(entry.getKey())] = data;
			}
		}
		if (writetomatch && !Splitter.report_matched.isEmpty()) {
			allRows_matched.add(row);
			if (allRows_matched.size() == rowlimit)
				csvwriter_matched();
		} else if (!(writetomatch || Splitter.report_unmatched.isEmpty())) {
			allRows_unmatched.add(row);
			if (allRows_unmatched.size() == rowlimit)
				csvwriter_unmatched();
		}
	}

	public boolean csvwriter_matched() throws Exception {
		if (allRows_matched.size() == 0)
			return false;
		else {
			Date writeDate = new Date("yyyy_MM_dd-HH_mm_ss");
			Splitter.report_matched += File.separatorChar + writeDate.getDate();
			List<String[]> filerows = new ArrayList<String[]>();
			boolean createTGT = true;
			for (int row_i = 0; row_i < allRows_matched.size(); row_i++) {
				if (createTGT) {
					createTGT = false;
					if (!new File(Splitter.report_matched).exists())
						new File(Splitter.report_matched).mkdirs();
				}
				filerows.add(allRows_matched.get(row_i));
				if (row_i / rowlimit != file_i_matched) {
					fileName = file_i_matched + ".csv";
					csvout = new File(Splitter.report_matched + File.separatorChar + fileName);
					cwriter = new CSVWriter(new FileWriter(csvout));
					cwriter.writeNext(header_row);
					cwriter.writeAll(filerows);
					filerows.clear();
					cwriter.close();
					file_i_matched = row_i / rowlimit;
				}
			}
			if (!filerows.isEmpty()) {
				fileName = file_i_matched + ".csv";
				csvout = new File(Splitter.report_matched + File.separatorChar + fileName);
				cwriter = new CSVWriter(new FileWriter(csvout));
				cwriter.writeNext(header_row);
				cwriter.writeAll(filerows);
				filerows.clear();
				cwriter.close();
			}
			return true;
		}
	}

	public boolean csvwriter_unmatched() throws Exception {
		if (allRows_unmatched.size() == 0)
			return false;
		else {
			Date writeDate = new Date("yyyy_MM_dd-HH_mm_ss");
			Splitter.report_unmatched += File.separatorChar + writeDate.getDate();
			List<String[]> filerows = new ArrayList<String[]>();
			boolean createTGT = true;
			for (int row_i = 0; row_i < allRows_unmatched.size(); row_i++) {
				if (createTGT) {
					createTGT = false;
					if (!new File(Splitter.report_unmatched).exists())
						new File(Splitter.report_unmatched).mkdirs();
				}
				filerows.add(allRows_unmatched.get(row_i));
				if (row_i / rowlimit != file_i_unmatched) {
					fileName = file_i_unmatched + ".csv";
					csvout = new File(Splitter.report_unmatched + File.separatorChar + fileName);
					cwriter = new CSVWriter(new FileWriter(csvout));
					cwriter.writeNext(header_row);
					cwriter.writeAll(filerows);
					filerows.clear();
					cwriter.close();
					file_i_unmatched = row_i / rowlimit;
				}
			}
			if (!filerows.isEmpty()) {
				fileName = file_i_unmatched + ".csv";
				csvout = new File(Splitter.report_unmatched + File.separatorChar + fileName);
				cwriter = new CSVWriter(new FileWriter(csvout));
				cwriter.writeNext(header_row);
				cwriter.writeAll(filerows);
				filerows.clear();
				cwriter.close();
			}
			return true;
		}
	}
}
