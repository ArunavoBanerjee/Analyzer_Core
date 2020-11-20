package analyzer;

import java.io.BufferedOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import analyzer.Engine.BooleanParser;
import analyzer.Reporting.WriteToCSV;
import analyzer.SourceAdaptors.Parser;
import analyzer.SourceAdaptors.SourceParserFactory;
import analyzer.Validators.Validator;

public class Splitter {
	static String reportDest = "", dest_matched = "", dest_unmatched = "", dataReadPath = "",
			matched_tarPath = "", unmatched_tarPath = "";
	public static String  csvconfigPath = "", report_unmatched = "", report_matched = "";
	ArrayList<String> matchedNameList = new ArrayList<String>();
	ArrayList<String> unmatchedNameList = new ArrayList<String>();
	static String[] sourceList = null;
	static boolean isReport = false, dataOnly = false;
	boolean writetomatch = true;
	WriteToCSV reportWriter = null;
	Validator new_validator = null;
	static int batchSize = 0;
	int match_count = 0, unmatch_count = 0, count_item = 0;
	TarArchiveOutputStream tos_match = null;
	TarArchiveOutputStream tos_unmatch = null;

	public Splitter(Validator in) throws Exception {
		this.new_validator = in;
	}

	protected void churnData() throws Exception {
		long st_time = System.currentTimeMillis();
		dataReadPath = dataReadPath.replaceAll("^\\/", "");
		String root = "", _tarEntryName = "", _path = "";
		reportWriter = new WriteToCSV();
		dest_matched = dest_matched.replaceAll("\\.tar\\.gz$", "");
		dest_unmatched = dest_unmatched.replaceAll("\\.tar\\.gz$", "");
		SourceParserFactory factory = new SourceParserFactory();
		for (String source : sourceList) {
			Parser parser = factory.getParser(source, "");
			while (parser.next()) {
				writetomatch = true;
				if (new_validator != null)
					writetomatch = new_validator.validate(parser.dataDict);
				if (!dataOnly)
					reportWriter.csvloader(parser.dataDict, writetomatch);
				if (!isReport) {
					for (Map.Entry<String, byte[]> dataentry : parser.entryMap.entrySet()) {
						if (writetomatch) {
							if (!dest_matched.isEmpty()) {
								if (batchSize != 0) {
									_path = dest_matched + "_batch_" + (match_count / batchSize) + ".tar.gz";
									if (!_path.equals(matched_tarPath)) {
										matched_tarPath = _path;
										matchedNameList.add(matched_tarPath);
										if (tos_match != null)
											tos_match.close();
										tos_match = get_tos(matched_tarPath);
									}
								} else {
									_path = dest_matched + ".tar.gz";
									if (!_path.equals(matched_tarPath)) {
										matched_tarPath = _path;
										tos_match = get_tos(matched_tarPath);
									}
								}
								_tarEntryName = dataentry.getKey();
								//TODO Can be replaced with a Parser variable. Needs Analysis.
								root = _tarEntryName.substring(0,_tarEntryName.indexOf('/'));
								_tarEntryName = _tarEntryName.replace(root, new File(matched_tarPath).getName().replace(".tar.gz", ""));
								TarArchiveEntry out_tarEntry = new TarArchiveEntry(_tarEntryName);
								out_tarEntry.setSize(dataentry.getValue().length);
								tos_match.putArchiveEntry(out_tarEntry);
								tos_match.write(dataentry.getValue());
								tos_match.closeArchiveEntry();
							}
						} else if (!dest_unmatched.isEmpty()) {
							if (batchSize != 0) {
								_path = dest_unmatched + "batch_" + (unmatch_count / batchSize) + ".tar.gz";
								if (!_path.equals(unmatched_tarPath)) {
									unmatched_tarPath = _path;
									unmatchedNameList.add(unmatched_tarPath);
									if (tos_unmatch != null)
										tos_unmatch.close();
									tos_unmatch = get_tos(unmatched_tarPath);
								}
							} else {
								_path = dest_unmatched + ".tar.gz";
								if (!_path.equals(unmatched_tarPath)) {
									unmatched_tarPath = _path;
									tos_unmatch = get_tos(unmatched_tarPath);
								}
							}
							_tarEntryName = dataentry.getKey();
							//TODO Can be replaced with a Parser variable. Needs Analysis.
							root = _tarEntryName.substring(0,_tarEntryName.indexOf('/'));
							_tarEntryName = _tarEntryName.replace(root, new File(unmatched_tarPath).getName().replace(".tar.gz", ""));
							TarArchiveEntry out_tarEntry = new TarArchiveEntry(_tarEntryName);
							out_tarEntry.setSize(dataentry.getValue().length);
							tos_unmatch.putArchiveEntry(out_tarEntry);
							tos_unmatch.write(dataentry.getValue());
							tos_unmatch.closeArchiveEntry();
						}
					}
				}
				if (writetomatch)
					match_count++;
				else
					unmatch_count++;
				count_item++;
				if (count_item % 10000 == 0) {
					long time = System.currentTimeMillis();
					float elapsed_time = (time - st_time);
					if (elapsed_time / 60000 < 1) {
						elapsed_time = elapsed_time / 1000;
						System.out.println(
								"Processed (" + new File(source).getName() + ") " + count_item + " records in " + Math.round(elapsed_time * 100) / 100 + " seconds.");
					} else {
						elapsed_time = elapsed_time / 1000;
						float elapsed_min = elapsed_time / 60;
						float elapsed_sec = elapsed_time % 60;
						System.out.println("Processed (" + new File(source).getName() + ") " + count_item + " records in " + Math.round(elapsed_min * 100) / 100
								+ " mins " + Math.round(elapsed_sec * 100) / 100 + " seconds.");
					}
				}
			}
		}
		if (!isReport) {
			if (tos_match != null)
				tos_match.close();
			if (tos_unmatch != null)
				tos_unmatch.close();
		}
		if (!report_matched.isEmpty())
			reportWriter.csvwriter_matched();
		if (!report_unmatched.isEmpty())
			reportWriter.csvwriter_unmatched();
		print_output();
	}

	TarArchiveOutputStream get_tos(String _tarName) throws Exception {
		File _tarFile = new File(_tarName);
		if (_tarFile.exists())
			_tarFile.delete();
		FileOutputStream fos = new FileOutputStream(_tarName);
		GZIPOutputStream gos = new GZIPOutputStream(new BufferedOutputStream(fos));
		TarArchiveOutputStream tos = new TarArchiveOutputStream(gos);
		tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
		return tos;
	}

	void print_output() {
		System.out.println("Matched #:" + match_count + "  UnMatched #:" + unmatch_count);
		if (new_validator == null)
			System.out.println("Report Destination: " + report_matched);
		else {
			if (!(dest_matched.isEmpty() || isReport)) {
				if (batchSize == 0)
					System.out.println("Matched Data Destination: " + matched_tarPath);
				else {
					System.out.println("Matched Data Destination:");
					for (String _fileLocation : matchedNameList)
						System.out.println(_fileLocation);
				}
			}
			if (!(dest_unmatched.isEmpty() || isReport)) {
				if (batchSize == 0)
					System.out.println("UnMatched Data Destination: " + unmatched_tarPath);
				else {
					System.out.println("UnMatched Data Destination:");
					for (String _fileLocation : unmatchedNameList)
						System.out.println(_fileLocation);
				}
			}
			if (!report_matched.isEmpty())
				System.out.println("Matched Report Destination: " + report_matched);
			if (!report_unmatched.isEmpty())
				System.out.println("UnMatched Report Destination: " + report_unmatched);
		}
	}

}
