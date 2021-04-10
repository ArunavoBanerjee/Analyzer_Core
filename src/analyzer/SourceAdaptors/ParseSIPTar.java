package analyzer.SourceAdaptors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
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
	FileInputStream fis = null;
	TarArchiveInputStream tis = null;
	String dataReadPath, trimPath;
	TarArchiveEntry in_tarEntry;
	String tarName = "";
	File tarFile = null;
	HashSet<String> parentsRead = new HashSet<String>();
	ParseXMLtoDict toDict = new ParseXMLtoDict();
	
	public ParseSIPTar(String _tarPath, String _dataReadPath) throws Exception {
		// TODO Auto-generated constructor stub
		dataReadPath = _dataReadPath;
		dataReadPath = dataReadPath.replaceAll("^\\/", "");
		trimPath = dataReadPath.replaceAll("(\\/$)", "");
		trimPath = trimPath.substring(0, trimPath.lastIndexOf('/') == -1 ? 0 : trimPath.lastIndexOf('/'));
		File input_tar_gz = new File(_tarPath);
		tarName = input_tar_gz.getName().replaceAll("\\.tar\\.gz$","");
		String tarPath = input_tar_gz.getAbsolutePath();
		tarFile = deCompressGZipFile(input_tar_gz, new File(tarPath.replace(".gz", "")));
		fis = new FileInputStream(tarFile);
		tis = new TarArchiveInputStream(fis);
//		while(tis.getNextTarEntry()!=null) {
//		//for(int i=0;i<5;i++)
//		if(tis.getCurrentEntry().getName().contains("/1/"))	
//		System.out.println(tis.getCurrentEntry().getName() );
//		}
//		System.out.println(tis.getCurrentEntry().getName() );
//		System.exit(0);
	}
	
	public void loadKeys(ArrayList<String> keyMaster) throws Exception {
		TarArchiveInputStream tis_iterKeys = new TarArchiveInputStream(fis);
		while(tis_iterKeys.getNextTarEntry() != null) {
			String tarEntryName = in_tarEntry.getName();
			if (!(tarEntryName.contains(dataReadPath) && in_tarEntry.isFile()))
				continue;
			else if(tarEntryName.endsWith(".xml")){
				byte[] content = new byte[(int) in_tarEntry.getSize()];
				int offset = 0;
				tis.read(content, offset, content.length - offset);
				String contentString = new String(content);
				toDict.getSourceFields(contentString, keyMaster);
			}
		}
		tis_iterKeys.close();
	}
	
	public String getSourceName() {
		return tarName;
	}
	
	public boolean clean() throws Exception{
		tis.close();
		return tarFile.getAbsoluteFile().delete();
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
//				System.out.println(dataReadPath+":" + tarEntryName);
				if (!(tarEntryName.contains(dataReadPath) && in_tarEntry.isFile()))
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
							toDict.getSourceInfo(contentString, dataDict);
						} else if (tarEntryName.endsWith("handle")) {
							String handle = new String(content).strip();
							dataDict.put("Handle_ID", new HashSet<String>() {
								{
									add(handle);
								}
							});
						}
				}
			}
		}
		while ((in_tarEntry = tis.getNextTarEntry()) != null);
		//System.out.println(dataDict);
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
