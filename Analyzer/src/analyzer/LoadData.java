package analyzer;

import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.opencsv.CSVReader;
import analyzer.CustomException.DataFormatException;
import analyzer.CustomException.DataStructureException;
import analyzer.Engine.Data;
import analyzer.Validators.DataValidator;
import analyzer.Validators.MatchPropValidator;
import analyzer.Validators.Validator;

public class LoadData {
	MatchPropValidator mpv = new MatchPropValidator();
	DataValidator dv = new DataValidator();
	String splitDataPath;
	String[] dataProp = new String[5];
	String split_field, header_field;

	public LoadData(String in_path) {
		// TODO Auto-generated constructor stub
		this.splitDataPath = in_path;
	}

	public void loadDataCSV() throws Exception {
		CSVReader cr = new CSVReader(new FileReader(splitDataPath));
		List<String[]> allRows = cr.readAll();
		List<String> header = Arrays.asList(allRows.iterator().next());
		List<String> splitID = new ArrayList<String>();
		HashMap<String, Integer> distinctID = new HashMap<String, Integer>();
		for (int i = 0; i < header.size(); i++) {
			if (i != 0 && header.get(i).equalsIgnoreCase("matchType") && header.get(i - 1).equalsIgnoreCase("matchType"))
				throw new Exception("Datafile cannot have consequtive field descriptor matchType.");
			if (header.get(i).equalsIgnoreCase("matchType"))
				continue;
			if (!distinctID.containsKey(header.get(i))) {
				distinctID.put(header.get(i), 0);
				if (!header.get(i).matches(".*\\[\\s*\\d+\\s*\\]$"))
					splitID.add(header.get(i) + "[0]");
				else
					splitID.add(header.get(i));
			} else {
				int ID_idx = distinctID.get(header.get(i)) + 1;
				if (!header.get(i).matches(".*\\[\\s*\\d+\\s*\\]$"))
					splitID.add(header.get(i) + "[" + ID_idx + "]");
				else
					splitID.add(header.get(i));
			}
		}
		for (String keys : distinctID.keySet())
			splitID.add(keys);
		for (Map.Entry<String, Data> entry : Validator.exprfieldList.entrySet()) {
			if (!(entry.getValue() == null || splitID.contains(entry.getKey())))
				throw new Exception("Expression field " + entry.getKey() + " is not contained in filterdata. No Split performed.");
		}
		for (String eachID : splitID)
			if (!Validator.exprfieldList.containsKey(eachID))
				splitID.remove(eachID);
		loadDataObject(allRows, header, splitID);
		cr.close();
	}

	public void loadDataXLSX() throws Exception {
		FileInputStream in_ft = new FileInputStream(splitDataPath);
		Workbook workbook = new XSSFWorkbook(in_ft);
		Sheet datasheet = workbook.getSheetAt(0);
		workbook.close();
		DataFormatter formatter = new DataFormatter();
		Iterator<Row> eachrow = datasheet.iterator();
		List<String> header = new ArrayList<String>();
		while (eachrow.hasNext()) {
			Row thisrow = eachrow.next();
			for (Cell eachColumn : thisrow) {
				if (eachColumn.getCellType() == CellType.FORMULA)
					throw new Exception("Data File header cannot have formula.");
				if (formatter.formatCellValue(eachColumn).strip().isBlank())
					throw new Exception("Data File header can not be empty.");
				header.add(formatter.formatCellValue(eachColumn).strip());
			}
			break;
		}
		List<String> splitID = new ArrayList<String>();
		HashMap<String, Integer> distinctID = new HashMap<String, Integer>();
		for (int i = 0; i < header.size(); i++) {
			if (i != 0 && header.get(i).equalsIgnoreCase("matchType") && header.get(i - 1).equalsIgnoreCase("matchType"))
				throw new Exception("Datafile cannot have consequtive field descriptor matchType.");
			if (header.get(i).equalsIgnoreCase("matchType"))
				continue;
			// TODO to add conditions for catastrophic consequences for a header coming with same [.*] appended multiple times.
			if (!distinctID.containsKey(header.get(i))) {
				distinctID.put(header.get(i), 0);
				if (!header.get(i).matches(".*\\[\\s*\\d+\\s*\\]$"))
					splitID.add(header.get(i) + "[0]");
				else
					splitID.add(header.get(i));
			} else {
				int ID_idx = distinctID.get(header.get(i)) + 1;
				distinctID.put(header.get(i), ID_idx);
				if (!header.get(i).matches(".*\\[\\s*\\d+\\s*\\]$"))
					splitID.add(header.get(i) + "[" + ID_idx + "]");
				else
					splitID.add(header.get(i));
			}
		}
		for (String keys : distinctID.keySet())
			splitID.add(keys);
		for (Map.Entry<String, Data> entry : Validator.exprfieldList.entrySet()) {
			if (!(entry.getValue() == null || splitID.contains(entry.getKey())))
				throw new Exception("Expression field '" + entry.getKey() + "' not contained in filterdata. No Split performed.");
		}
		List<String[]> allRows = new ArrayList<String[]>();
		while (eachrow.hasNext()) {
			Row thisrow = eachrow.next();
			int row_len = thisrow.getLastCellNum();
			String[] row = new String[row_len];
			for (int col_idx = 0; col_idx < row_len; col_idx++) {
				Cell eachColumn = thisrow.getCell(col_idx);
				if (eachColumn != null) {
					if (eachColumn.getCellType() == CellType.FORMULA) {
						switch (eachColumn.getCachedFormulaResultType()) {
						case BOOLEAN:
							row[col_idx] = String.valueOf(eachColumn.getBooleanCellValue());
							break;
						case NUMERIC:
							row[col_idx] = String.valueOf(eachColumn.getNumericCellValue());
							break;
						case STRING:
							if (eachColumn.getRichStringCellValue() != null)
								row[col_idx] = eachColumn.getRichStringCellValue().getString().strip();
							else
								row[col_idx] = "";
							break;
						default:
							throw new Exception("Cell Formula Evaluation contains error.");
						}
					} else
						row[col_idx] = formatter.formatCellValue(eachColumn).strip();
				} else
					row[col_idx] = "";
			}
			boolean blankrowcheck = true;
			for (String data : row) {
				if (!data.isBlank())
					blankrowcheck = false;
			}
			if (blankrowcheck)
				continue;
			allRows.add(row);
		}
		loadDataObject(allRows, header, splitID);
	}

	void loadDataObject(List<String[]> allRows, List<String> header, List<String> splitID) throws Exception {
//		System.out.println(allRows.size());
		String cellValue = "", data = "";
		for (int row_idx = 0; row_idx < allRows.size(); row_idx++) {
			int skipCount = 0;
			for (int i = 0; i < allRows.get(row_idx).length; i++) {
				cellValue = allRows.get(row_idx)[i].strip();
				header_field = header.get(i);
				if (header_field.equalsIgnoreCase("matchType")) {
					skipCount++;
					header_field = header.get(++i);
					split_field = splitID.get(i - skipCount);
					data = allRows.get(row_idx)[i].strip();
					if (cellValue.isEmpty()) {
						try {
							if (Validator.exprfieldList.containsKey(split_field) && Validator.exprfieldList.get(split_field) != null) {
								if (Validator.exprfieldList.get(split_field).genericDefinition != null) {
								data = dv.validateData(Validator.exprfieldList.get(split_field).genericDefinition, data);
									Validator.exprfieldList.get(split_field).loadConstPatterns(data);
								}
								else
									throw new DataStructureException("No generic definiton found.");
							}
							if (Validator.exprfieldList.containsKey(header_field) && Validator.exprfieldList.get(header_field) != null) {
								if (Validator.exprfieldList.get(header_field).genericDefinition != null) {
									data = dv.validateData(Validator.exprfieldList.get(header_field).genericDefinition, data);
									Validator.exprfieldList.get(header_field).loadConstPatterns(data);
								}
								else
									throw new DataStructureException("No generic definiton found.");
							}
						} catch (DataFormatException e) {
							System.out.println(e.getMessage());
							System.out.println("DataSet Value " + cellValue + " error at column " + i + " row " + row_idx);
						} catch (DataStructureException e) {
							System.out.println(e.getMessage());
							throw new Exception("DataSet MatchProperty Undefined for column '" + i + "'");
						}
					} else {
						ArrayList<String> matchProp = new ArrayList(Arrays.asList(cellValue.split(":")));
						try {
							mpv.validateMP(matchProp);
							data = dv.validateData(matchProp, data);
							if (Validator.exprfieldList.containsKey(split_field) && Validator.exprfieldList.get(split_field) != null)
								Validator.exprfieldList.get(split_field).loadVariablePatterns(matchProp, data);
							if (Validator.exprfieldList.containsKey(header_field) && Validator.exprfieldList.get(header_field) != null)
								Validator.exprfieldList.get(header_field).loadVariablePatterns(matchProp, data);
						} catch (DataFormatException e) {
							System.out.println(e.getMessage());
							System.out.println("DataSet Value " + cellValue + " error at column " + i + " row " + row_idx);
						} catch (DataStructureException e) {
							System.out.println(e.getMessage());
							throw new Exception("DataSet MatchProperty Undefined for column '" + i + "'");
						}
					}
				} else {
					split_field = splitID.get(i - skipCount);
					data = cellValue;
					try {
						if (Validator.exprfieldList.containsKey(split_field) && Validator.exprfieldList.get(split_field) != null) {
							if (Validator.exprfieldList.get(split_field).genericDefinition != null) {
							data = dv.validateData(Validator.exprfieldList.get(split_field).genericDefinition, data);
								Validator.exprfieldList.get(split_field).loadConstPatterns(data);
							}
							else
								throw new DataStructureException("No generic definiton found.");
						}
						if (Validator.exprfieldList.containsKey(header_field) && Validator.exprfieldList.get(header_field) != null) {
							if (Validator.exprfieldList.get(header_field).genericDefinition != null) {
								data = dv.validateData(Validator.exprfieldList.get(header_field).genericDefinition, data);
								Validator.exprfieldList.get(header_field).loadConstPatterns(data);
							}
							else
								throw new DataStructureException("No generic definiton found.");
						}
					} catch (DataFormatException e) {
						System.out.println(e.getMessage());
						System.out.println("DataSet Value " + cellValue + " error at column " + i + " row " + row_idx);
					} catch (DataStructureException e) {
						System.out.println(e.getMessage());
						throw new Exception("DataSet MatchProperty Undefined for column '" + i + "'");
					}
				}
			}
		}
	}
}
