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
import analyzer.Validators.Validator;

public class Splitter {
	static String reportDest = "", dest_matched = "", dest_unmatched = "", report_matched = "", report_unmatched = "", dataReadPath = "", csvconfigPath = "",
			matched_tarPath = "", unmatched_tarPath = "";
	ArrayList<String> matchedNameList = new ArrayList<String>();
	ArrayList<String> unmatchedNameList = new ArrayList<String>();
	static String[] sourceList = null;
	static boolean isReport = false, dataOnly = false;
	boolean writetomatch = true;
	HashMap<String, HashSet<String>> sourceDict = new HashMap<String, HashSet<String>>();
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
		dataReadPath = dataReadPath.replaceAll("^\\/", "");
		String trimPath = dataReadPath.replaceAll("(\\/$)", "");
		trimPath = trimPath.substring(0, trimPath.lastIndexOf('/') == -1 ? 0 : trimPath.lastIndexOf('/'));
		reportWriter = new WriteToCSV();
		dest_matched = dest_matched.replaceAll("\\.tar\\.gz$", "");
		dest_unmatched = dest_unmatched.replaceAll("\\.tar\\.gz$", "");
		for (String source : sourceList) {
			File input_tar_gz = new File(source);
			String tarPath = input_tar_gz.getAbsolutePath();
			File tarFile = deCompressGZipFile(input_tar_gz, new File(tarPath.replace(".gz", "")));
			FileInputStream fis = new FileInputStream(tarFile);
			TarArchiveInputStream tis = new TarArchiveInputStream(fis);
			TarArchiveEntry in_tarEntry = null;
			String parentDir = "", root = "", _tarEntryName = "", _path = "";
			HashMap<String, byte[]> entryMap = new HashMap<String, byte[]>();
			long st_time = System.currentTimeMillis();
			while ((in_tarEntry = tis.getNextTarEntry()) != null) {
				String tarEntryName = in_tarEntry.getName();
				if (!tarEntryName.contains(dataReadPath))
					continue;
				if (root.isEmpty()) {
					int root_idx = tarEntryName.indexOf('/');
					if (root_idx != -1)
						root = tarEntryName.substring(0, root_idx);
					else
						root = tarEntryName;
				}
				String parent_entry = tarEntryName.replaceAll("/$", "").replaceAll("(.*)[/\\\\].*", "$1");
				if (in_tarEntry.isFile()) {
					if (!parentDir.equals(parent_entry)) {
						if (!sourceDict.isEmpty()) {
							writetomatch = true;
							if (new_validator != null)
								writetomatch = new_validator.validate(sourceDict);
							if (!dataOnly)
								reportWriter.csvloader(sourceDict, writetomatch);
							if (!isReport) {
								for (Map.Entry<String, byte[]> dataentry : entryMap.entrySet()) {
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
							entryMap.clear();
							sourceDict.clear();
							count_item++;
							if (count_item % 10000 == 0) {
								long time = System.currentTimeMillis();
								float elapsed_time = (time - st_time);
								if (elapsed_time / 60000 < 1) {
									elapsed_time = elapsed_time / 1000;
									System.out.println("Processed (" + input_tar_gz.getName() + ") " + count_item + " records in " + Math.round(elapsed_time * 100) / 100
											+ " seconds.");
								} else {
									elapsed_time = elapsed_time / 1000;
									float elapsed_min = elapsed_time / 60;
									float elapsed_sec = elapsed_time % 60;
									System.out.println("Processed (" + input_tar_gz.getName() + ") " + count_item + " records in " + Math.round(elapsed_min * 100) / 100
											+ " mins " + Math.round(elapsed_sec * 100) / 100 + " seconds.");
								}
							}
						}
						parentDir = parent_entry;
					}
					byte[] content = new byte[(int) in_tarEntry.getSize()];
					int offset = 0;
					tis.read(content, offset, content.length - offset);
					tarEntryName = tarEntryName.replace(trimPath, "");
					if (!isReport)
						entryMap.put(tarEntryName, content);
					if (tarEntryName.endsWith(".xml")) {
						String contentString = new String(content);
						getSourceInfo(contentString);
					} else if (tarEntryName.endsWith("handle")) {
						String handle = new String(content).strip();
						sourceDict.put("handle_ID", new HashSet<String>() {
							{
								add(handle);
							}
						});
					}
				}
			}
			if (!sourceDict.isEmpty()) {
				writetomatch = true;
				if (new_validator != null)
					writetomatch = new_validator.validate(sourceDict);
				if (!dataOnly)
					reportWriter.csvloader(sourceDict, writetomatch);
				if (!isReport) {
					for (Map.Entry<String, byte[]> dataentry : entryMap.entrySet()) {
						if (writetomatch) {
							if (!dest_matched.isEmpty()) {
								if (batchSize != 0) {
									_path = dest_matched + "_batch_" + (match_count / batchSize) + ".tar.gz";
									if (!_path.equals(matched_tarPath)) {
										matched_tarPath = _path;
										matchedNameList.add(matched_tarPath);
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
							_tarEntryName = _tarEntryName.replace(root, new File(unmatched_tarPath).getName().replace(".tar.gz", ""));
							TarArchiveEntry out_tarEntry = new TarArchiveEntry(_tarEntryName);
							out_tarEntry.setSize(dataentry.getValue().length);
							tos_unmatch.putArchiveEntry(out_tarEntry);
							tos_unmatch.write(dataentry.getValue());
							tos_unmatch.closeArchiveEntry();
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
			if (writetomatch)
				match_count++;
			else
				unmatch_count++;
			entryMap.clear();
			sourceDict.clear();
			count_item++;
			if (count_item % 10000 != 0) {
				long time = System.currentTimeMillis();
				float elapsed_time = (time - st_time);
				if (elapsed_time / 60000 < 1) {
					elapsed_time = elapsed_time / 1000;
					System.out.println("Processed (" + input_tar_gz.getName() + ") " + count_item + " records in " + Math.round(elapsed_time * 100) / 100 + " seconds.");
				} else {
					elapsed_time = elapsed_time / 1000;
					float elapsed_min = elapsed_time / 60;
					float elapsed_sec = elapsed_time % 60;
					System.out.println("Processed (" + input_tar_gz.getName() + ") " + count_item + " records in " + Math.round(elapsed_min * 100) / 100 + " mins "
							+ Math.round(elapsed_sec * 100) / 100 + " seconds.");
				}
			}
			tis.close();
			if (!report_matched.isEmpty())
				reportWriter.csvwriter_matched();
			if (!report_unmatched.isEmpty())
				reportWriter.csvwriter_unmatched();
			tarFile.delete();
		}
		print_output();
	}

	void getSourceInfo(String itemContent) throws Exception {
		JsonParser parser = new JsonParser();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		dbf.setValidating(false);
		documentBuilder = dbf.newDocumentBuilder();
		Document inputDoc = documentBuilder.parse(new InputSource(new StringReader(itemContent)));
		Element root = inputDoc.getDocumentElement();
		String schema = root.getAttribute("schema");
		NodeList docNodes = root.getChildNodes();
		for (int i = 0; i < docNodes.getLength(); i++) {
			Node docNode = docNodes.item(i);
			if (docNode.getNodeType() != Node.ELEMENT_NODE)
				continue;
			String nodeNameNDL = formReadable(docNode, schema);
			String textContent = docNode.getTextContent().trim();
			try {
				JsonObject jsonObject = parser.parse(textContent).getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					nodeNameNDL = nodeNameNDL + "@" + entry.getKey();
					textContent = entry.getValue().getAsString();
				}
			} catch (Exception e) {

			}
			if (!sourceDict.containsKey(nodeNameNDL)) {
				HashSet<String> values = new HashSet<String>();
				values.add(textContent);
				sourceDict.put(nodeNameNDL, values);
			} else
				sourceDict.get(nodeNameNDL).add(textContent);
		}
	}

	String formReadable(Node thisNode, String schema) {
		NamedNodeMap attrs = thisNode.getAttributes();
		String element = "", qualifier = "", read;
		for (int i = 0; i < attrs.getLength(); i++) {
			switch (attrs.item(i).getNodeName()) {
			case "element":
				element = attrs.getNamedItem("element").getNodeValue();
				break;
			case "qualifier":
				qualifier = attrs.getNamedItem("qualifier").getNodeValue();
				break;
			}
		}

		read = (schema + "." + element + "." + qualifier);
		read = read.replaceAll("[.]$", "");

		return read;
	}

	File deCompressGZipFile(File gZippedFile, File tarFile) throws IOException {
		FileInputStream fis = new FileInputStream(gZippedFile);
		GZIPInputStream gZIPInputStream = new GZIPInputStream(fis);
		FileOutputStream fos = new FileOutputStream(tarFile);

		byte[] buffer = new byte[1024];
		int len;
		while ((len = gZIPInputStream.read(buffer)) > 0) {
			fos.write(buffer, 0, len);
		}

		fos.close();
		gZIPInputStream.close();
		return tarFile;

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
