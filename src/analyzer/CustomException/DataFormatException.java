package analyzer.CustomException;

public class DataFormatException extends Exception {
	private static final long serialVersionUID = 1L;

	public DataFormatException(String msg) {
		super("DataFormat Exception: " + msg);
	}
}
