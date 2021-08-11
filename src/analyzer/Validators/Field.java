package analyzer.Validators;

public class Field {
	String fieldName;
	int patternIndexPos;
	
	public Field(String fieldName) {
		// TODO Auto-generated constructor stub
		this.fieldName = fieldName;
		this.patternIndexPos = 0;
	}
	public Field(String fieldName, int indexPos) {
		// TODO Auto-generated constructor stub
		this.fieldName = fieldName;
		this.patternIndexPos = indexPos;
	}
	
	public String getField() {
		return fieldName;
	}
	
	public int getPOS() {
		return patternIndexPos;
	}
}
