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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.opencsv.CSVReader;



public class LoadData {
	MatchPropValidator mpv = new MatchPropValidator(); 
	
	void loadDataCSV(HashMap<String, HashSet<String[]>> triplet, HashMap<String[], HashSet<String>> triplet_equals, String splitDataPath) throws Exception {
		CSVReader cr = new CSVReader(new FileReader(splitDataPath));
		List<String[]> allRows = cr.readAll();
		List<String> header = Arrays.asList(allRows.iterator().next());
		List<String> splitID = new ArrayList<String>();
		HashMap<String, Integer> distinctID = new HashMap<String, Integer>();
		for (int i = 0; i < header.size(); i++) {
			if (i != 0 && header.get(i).equalsIgnoreCase("matchType")
					&& header.get(i - 1).equalsIgnoreCase("matchType"))
				throw new Exception("datafile cannot have consequtive field descriptor matchType.");
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
		for (Map.Entry<String, String[]> entry : Validator.exprfieldList.entrySet()) {
			if (!splitID.contains(entry.getKey()))
				throw new Exception(
						"Expression field " + entry.getKey() + " is not contained in filterdata. No Split performed.");
		}
		for (String eachID : splitID)
			if (!Validator.exprfieldList.containsKey(eachID))
				splitID.remove(eachID);
		loadDataTriplet(allRows, header, splitID, triplet, triplet_equals);
		cr.close();
	}

	void loadDataXLS(HashMap<String, HashSet<String[]>> triplet, HashMap<String[], HashSet<String>> triplet_equals, String splitDataPath) throws Exception {
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
					throw new Exception("Header Cells cannot have formulas within.");
				if (formatter.formatCellValue(eachColumn).strip().isBlank())
					throw new Exception("Data File header can not be empty");
				header.add(formatter.formatCellValue(eachColumn).strip());
			}
			break;
		}
		List<String> splitID = new ArrayList<String>();
		HashMap<String, Integer> distinctID = new HashMap<String, Integer>();
		for (int i = 0; i < header.size(); i++) {
			if (i != 0 && header.get(i).equalsIgnoreCase("matchType")
					&& header.get(i - 1).equalsIgnoreCase("matchType"))
				throw new Exception("datafile cannot have consequtive field descriptor matchType.");
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
		for (Map.Entry<String, String[]> entry : Validator.exprfieldList.entrySet()) {
			if (!(splitID.contains(entry.getKey()) || entry.getValue()[0].equals("exists")))
				throw new Exception(
						"Expression field " + entry.getKey() + " is not contained in filterdata. No Split performed.");
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
				break;
			allRows.add(row);
		}
		loadDataTriplet(allRows, header, splitID, triplet, triplet_equals);
	}

	void loadDataTriplet(List<String[]> allRows, List<String> header, List<String> splitID,
			HashMap<String, HashSet<String[]>> triplet, HashMap<String[], HashSet<String>> triplet_equals) throws Exception {
//		System.out.println(allRows.size());
		for (int row_idx = 0; row_idx < allRows.size(); row_idx++) {
			int skipCount = 0;
			for (int i = 0; i < allRows.get(row_idx).length; i++) {
				String value = allRows.get(row_idx)[i].strip();
				String split_field = "", header_field = header.get(i);
				if (header_field.equalsIgnoreCase("matchType")) {
					skipCount++;
					header_field = header.get(++i);
					split_field = splitID.get(i - skipCount);
					boolean load_splitID = Validator.exprfieldList.containsKey(split_field)
							&& !Validator.exprfieldList.get(split_field)[0].equals("exists");
					boolean load_headerID = Validator.exprfieldList.containsKey(header_field)
							&& !Validator.exprfieldList.get(header_field)[0].equals("exists");
					if (!(load_splitID || load_headerID))
						continue;
					String data = allRows.get(row_idx)[i].strip();
					String[] dataProp = new String[5];
					if (value.isEmpty()) {
						try {
							dataProp = mpv.valdateMP(Validator.exprfieldList.get(header_field), value);
							} catch (Exception e) {
								System.out.println(e.getMessage());
								System.err.println("Need to correct error display.. Originiating from config matchType resolution module.");
								throw new Exception("DataSet Value " + value + " error at column " + i + " row " + row_idx);
							}
						if (load_splitID) {
							if (!triplet.containsKey(split_field)) {
								HashSet<String[]> values = new HashSet<String[]>();
								values.add(dataProp);
								triplet.put(split_field, values);
							} else {
								triplet.get(split_field).add(dataProp);
							}
						}
						if (load_headerID) {
							if (!triplet.containsKey(header_field)) {
								HashSet<String[]> values = new HashSet<String[]>();
								values.add(dataProp);
								triplet.put(header_field, values);
							} else {
								triplet.get(header_field).add(dataProp);
							}
						}
					} else {
						String[] matchProp = value.split(":");
						for(int m_i = 0; m_i < matchProp.length; m_i++)
							matchProp[m_i] = matchProp[m_i].strip().toLowerCase();
						try {
						dataProp = mpv.valdateMP(matchProp, data);
						System.out.println(dataProp.length);
						} catch (Exception e) {
							System.out.println(e.getMessage());
							throw new Exception("DataSet Value " + value + " error at column " + i + " row " + row_idx);
						}
						if (load_splitID) {
							if (dataProp[1].equalsIgnoreCase("equals")) {
								if (!triplet_equals.isEmpty()) {
									boolean keyMatch = false;
									for (String[] key : triplet_equals.keySet()) {
										if (key[0].equalsIgnoreCase(split_field)
												&& key[1].equalsIgnoreCase(dataProp[2])) {
											keyMatch = true;
											triplet_equals.get(key).add(dataProp[0]);
											break;
										}
										if (!keyMatch) {
											HashSet<String> values = new HashSet<String>();
											values.add(dataProp[0]);
											triplet_equals.put(new String[] { split_field, dataProp[2] }, values);
										}
									}
								} else {
									HashSet<String> values = new HashSet<String>();
									values.add(dataProp[0]);
									triplet_equals.put(new String[] { split_field, dataProp[2] }, values);
								}
							}
							else {
								if (!triplet.containsKey(split_field)) {
									HashSet<String[]> values = new HashSet<String[]>();
									values.add(dataProp);
									triplet.put(split_field, values);
								} else {
									triplet.get(split_field).add(dataProp);
								}
							}
						}
						if (load_headerID) {
							if (dataProp[1].equalsIgnoreCase("equals")) {
								if (!triplet_equals.isEmpty()) {
									boolean keyMatch = false;
									for (String[] key : triplet_equals.keySet()) {
										if (key[0].equalsIgnoreCase(split_field)
												&& key[1].equalsIgnoreCase(dataProp[2])) {
											keyMatch = true;
											triplet_equals.get(key).add(dataProp[0]);
											break;
										}
										if (!keyMatch) {
											HashSet<String> values = new HashSet<String>();
											values.add(dataProp[0]);
											triplet_equals.put(new String[] { split_field, dataProp[2] }, values);
										}
									}
								} else {
									HashSet<String> values = new HashSet<String>();
									values.add(dataProp[0]);
									triplet_equals.put(new String[] { split_field, dataProp[2] }, values);
								}
							}
							else {
								if (!triplet.containsKey(split_field)) {
									HashSet<String[]> values = new HashSet<String[]>();
									values.add(dataProp);
									triplet.put(split_field, values);
								} else {
									triplet.get(split_field).add(dataProp);
								}
							}
						}
					}
				} else {
					split_field = splitID.get(i - skipCount);
					boolean load_splitID = Validator.exprfieldList.containsKey(split_field)
							&& !Validator.exprfieldList.get(split_field)[0].equals("exists");
					boolean load_headerID = Validator.exprfieldList.containsKey(header_field)
							&& !Validator.exprfieldList.get(header_field)[0].equals("exists");
					if (!(load_splitID || load_headerID))
						continue;
					String[] dataProp = new String[5];;
					try {
						dataProp = mpv.valdateMP(Validator.exprfieldList.get(header_field), value);
						} catch (Exception e) {
							System.out.println(e.getMessage());
							System.err.println("Need to correct error display.. Originiating from config matchType resolution module.");
							throw new Exception("DataSet Value " + value + " error at column " + i + " row " + row_idx);
						}
					if (load_splitID) {
						if (dataProp[1].equalsIgnoreCase("equals")) {
							if (!triplet_equals.isEmpty()) {
								boolean keyMatch = false;
								for (String[] key : triplet_equals.keySet()) {
									if (key[0].equalsIgnoreCase(split_field)
											&& key[1].equalsIgnoreCase(dataProp[2])) {
										keyMatch = true;
										triplet_equals.get(key).add(dataProp[0]);
										break;
									}
									if (!keyMatch) {
										HashSet<String> values = new HashSet<String>();
										values.add(dataProp[0]);
										triplet_equals.put(new String[] { split_field, dataProp[2] }, values);
									}
								}
							} else {
								HashSet<String> values = new HashSet<String>();
								values.add(dataProp[0]);
								triplet_equals.put(new String[] { split_field, dataProp[2] }, values);
							}
						}
						else {
							if (!triplet.containsKey(split_field)) {
								HashSet<String[]> values = new HashSet<String[]>();
								values.add(dataProp);
								triplet.put(split_field, values);
							} else {
								triplet.get(split_field).add(dataProp);
							}
						}
					}
					if (load_headerID) {
						if (dataProp[1].equalsIgnoreCase("equals")) {
							if (!triplet_equals.isEmpty()) {
								boolean keyMatch = false;
								for (String[] key : triplet_equals.keySet()) {
									if (key[0].equalsIgnoreCase(header_field)
											&& key[1].equalsIgnoreCase(dataProp[2])) {
										keyMatch = true;
										triplet_equals.get(key).add(dataProp[0]);
										break;
									}
									if (!keyMatch) {
										HashSet<String> values = new HashSet<String>();
										values.add(dataProp[0]);
										triplet_equals.put(new String[] { header_field, dataProp[2] }, values);
									}
								}
							} else {
								HashSet<String> values = new HashSet<String>();
								values.add(dataProp[0]);
								triplet_equals.put(new String[] { header_field, dataProp[2] }, values);
							}
						}
						else {
							if (!triplet.containsKey(header_field)) {
								HashSet<String[]> values = new HashSet<String[]>();
								values.add(dataProp);
								triplet.put(header_field, values);
							} else {
								triplet.get(header_field).add(dataProp);
							}
						}
					}
				}
			}
		}
	}

}
