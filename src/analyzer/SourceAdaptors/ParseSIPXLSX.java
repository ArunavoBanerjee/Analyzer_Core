package analyzer.SourceAdaptors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.opencsv.CSVReader;

/**
 * XLSX input data parser. WIP
 * @author arunavo.banerjee.cse16@gmail.com
 *
 */

public class ParseSIPXLSX extends Parser {
	String _xlsxName = "", multiValueSep = "";
	static ArrayList<String> header = new ArrayList<String>();
	Sheet xlsxDataSheet = null;
	Iterator<Row> allRows = null;
	KVPExtraction kvp = null;
	DataFormatter formatter = new DataFormatter();
	Row row = null;
	int row_index = 0;
	
	public ParseSIPXLSX(String _xlsxPath, String muliValueSep) throws Exception {
		// TODO Auto-generated constructor stub
		File _xlsxFile = new File(_xlsxPath);
		_xlsxName = _xlsxFile.getName();
		this.multiValueSep = muliValueSep;
		Workbook xlsxData = new XSSFWorkbook(new FileInputStream(_xlsxFile));
		xlsxDataSheet = xlsxData.getSheetAt(0);
		allRows = xlsxDataSheet.iterator();
		allRows.next();
		xlsxData.close();
		kvp = new KVPExtraction();
//		if(!testforheader(header))
//			throw new Exception("CSV File does not contain a header column.");
	}
	
	public String getSourceName() {
		return _xlsxName;
	}
	
	public boolean clean() throws Exception{
		return true;
	}

	public void loadKeys(ArrayList<String> keyMaster) throws Exception {
		try {
			Iterator<Row> allRows = xlsxDataSheet.iterator();
			while (allRows.hasNext()) {
				Row thisrow = allRows.next();
				for (Cell eachColumn : thisrow) {
					if (eachColumn.getCellType() == CellType.FORMULA)
						throw new Exception("Data File header cannot have formula.");
					if (formatter.formatCellValue(eachColumn).strip().isBlank())
						throw new Exception("Data File header can not be empty.");
					header.add(formatter.formatCellValue(eachColumn).strip());
				}
				break;
			}
		while (allRows.hasNext()) {
			Row thisrow = allRows.next();
			int i = 0;
			for (Cell eachColumn : thisrow) {
				String cellValue = formatter.formatCellValue(eachColumn).strip();
				String field_name = header.get(i).strip();
				keyMaster.add(field_name);
				HashSet<String> field_value_list = new HashSet<String>();
				if(multiValueSep.isBlank())
					field_value_list.add(cellValue);
				else
					for (String eachValue : cellValue.split(multiValueSep))
						field_value_list.add(eachValue.strip());
				for(String field_value : field_value_list) {
					kvp.KVPextractKeys(field_name, field_value, keyMaster);
				}
				i++;
			}
		}
		} catch (NoSuchElementException e) {
			throw e;
		} catch(Exception e) {
			throw e;
		}
	}

	public boolean next() throws Exception {
		dataDict.clear();
		boolean nextExists = false;
		Row thisrow = null;
		try { 
		if ((thisrow = allRows.next()) != null) {
			int i = 0;
			nextExists = true;
			for (Cell eachColumn : thisrow) {
				String cellValue = formatter.formatCellValue(eachColumn).strip();
				String field_name = header.get(i).strip();
				HashSet<String> field_value_list = new HashSet<String>();
				if(multiValueSep.isBlank())
					field_value_list.add(cellValue);
				else
					for (String eachValue : cellValue.split(multiValueSep))
						field_value_list.add(eachValue.strip());			
				for(String field_value : field_value_list){
					kvp.KVPextractAll(field_name, field_value, dataDict);
			}
				i++;
			}
		}
	} catch (NoSuchElementException e) {
		// System.out.println(dataDict);
	}
		return nextExists;
	}
}