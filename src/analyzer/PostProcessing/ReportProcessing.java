package analyzer.PostProcessing;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class ReportProcessing {

	class Value {
		String value;
		int coverage;

		Value(String value, int coverage) {
			this.value = value;
			this.coverage = coverage;
		}
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		ReportProcessing rp = new ReportProcessing();

		CSVReader cr = new CSVReader(new FileReader(new File(
				"C:\\Users\\srvcs\\Research_stuffs_local\\Analyzer_experiments\\data-report\\LOC_Manuscript\\ExportData\\matched-data-report\\2021_08_03-19_13_43\\0.csv")));
		String[] readHeader = cr.readNext();
		HashMap<String, ArrayList<Value>> valueMap = new HashMap<String, ArrayList<Value>>();
		String[] row = null;
		while ((row = cr.readNext()) != null) {
			for (int i = 1; i < row.length; i++)
				if (valueMap.containsKey(readHeader[i])) {
					for (String eachInput : row[i].split("\\|\\|")) {
						boolean addValue = true;
						for (Value eachValue : valueMap.get(readHeader[i]))
							if (eachInput.equalsIgnoreCase(eachValue.value)) {
								addValue = false;
								eachValue.coverage++;
								break;
							}
						if (addValue)
							valueMap.get(readHeader[i]).add(rp.new Value(eachInput, 1));
					}
				} else {
					ArrayList<Value> values = new ArrayList<Value>();
					for (String eachInput : row[i].split("\\|\\|")) {
						boolean addValue = true;
						for (Value eachValue : values)
							if (eachInput.equalsIgnoreCase(eachValue.value)) {
								addValue = false;
								break;
							}
						if (addValue)
							values.add(rp.new Value(eachInput, 1));
					}
					valueMap.put(readHeader[i], values);
				}
		}
		CSVWriter cw = new CSVWriter(new FileWriter(new File(
				"C:\\Users\\srvcs\\Research_stuffs_local\\Analyzer_experiments\\data-report\\LOC_Manuscript\\ExportData\\matched-data-report\\2021_08_03-19_13_43\\1.csv")));
		List<String> headerList = new ArrayList<String>(Arrays.asList(readHeader));
		headerList.remove(0);
		String[] writeHeader = new String[headerList.size() * 2];
		for (String colName : headerList) {
			int i = headerList.indexOf(colName);
			writeHeader[i * 2] = colName;
			writeHeader[i * 2 + 1] = "freq(" + colName + ")";
		}
		cw.writeNext(writeHeader);
		String[] writeRow = new String[valueMap.size() * 2];
		int j = 0;
		listempty: while (true) {
			boolean nullity = true;
			int i = 0;
			for (Map.Entry<String, ArrayList<Value>> entry : valueMap.entrySet()) {
				try {
					writeRow[i * 2] = entry.getValue().get(j).value;
					writeRow[i * 2 + 1] = String.valueOf(entry.getValue().get(j).coverage);
					nullity &= false;
				} catch (IndexOutOfBoundsException e) {
					writeRow[i * 2] = "";
					writeRow[i * 2 + 1] = "";
				}
				i++;
			}
			if (nullity)
				break listempty;
			else {
//				System.out.println("Writing row : " + j);
				cw.writeNext(writeRow);
			}
			j++;
		}
		cw.close();
	}
}
