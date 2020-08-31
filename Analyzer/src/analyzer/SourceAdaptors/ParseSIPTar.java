package analyzer.SourceAdaptors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ParseSIPTar extends Parser {
	TarArchiveInputStream tis = null;
	String dataReadPath, trimPath;
	TarArchiveEntry in_tarEntry;

	public ParseSIPTar(String _tarPath, String _dataReadPath) throws Exception {
		// TODO Auto-generated constructor stub
		dataReadPath = _dataReadPath;
		dataReadPath = dataReadPath.replaceAll("^\\/", "");
		trimPath = dataReadPath.replaceAll("(\\/$)", "");
		trimPath = trimPath.substring(0, trimPath.lastIndexOf('/') == -1 ? 0 : trimPath.lastIndexOf('/'));
		File input_tar_gz = new File(_tarPath);
		String tarPath = input_tar_gz.getAbsolutePath();
		File tarFile = deCompressGZipFile(input_tar_gz, new File(tarPath.replace(".gz", "")));
		FileInputStream fis = new FileInputStream(tarFile);
		tis = new TarArchiveInputStream(fis);
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

	public boolean next() throws Exception {
		dataDict.clear();
		entryMap.clear();
		String parentDir="", root = "";
		boolean nextExists = false;
		do {
			if(in_tarEntry != null) {
				String tarEntryName = in_tarEntry.getName();
				if (!tarEntryName.contains(dataReadPath))
					continue;
				nextExists = true;
				if (root.isEmpty()) {
					int root_idx = tarEntryName.indexOf('/');
					if (root_idx != -1)
						root = tarEntryName.substring(0, root_idx);
					else
						root = tarEntryName;
				}
				String parent_entry = tarEntryName.replaceAll("/$", "").replaceAll("(.*)[/\\\\].*", "$1");
				if(parentDir.isBlank())
					parentDir = parent_entry;
				if (in_tarEntry.isFile()) {
					if (!parentDir.equals(parent_entry))
						break;
						byte[] content = new byte[(int) in_tarEntry.getSize()];
						int offset = 0;
						tis.read(content, offset, content.length - offset);
						tarEntryName = tarEntryName.replace(trimPath, "");
						entryMap.put(tarEntryName, content);
						if (tarEntryName.endsWith(".xml")) {
							String contentString = new String(content);
							ParseXMLtoDict.getSourceInfo(contentString, dataDict);
						} else if (tarEntryName.endsWith("handle")) {
							String handle = new String(content).strip();
							dataDict.put("handle_ID", new HashSet<String>() {
								{
									add(handle);
								}
							});
						}
				}
			}
		}
		while ((in_tarEntry = tis.getNextTarEntry()) != null);
		return nextExists;
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
}
