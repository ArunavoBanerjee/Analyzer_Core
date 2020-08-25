package analyzer.CustomException;

public class DataStructureException extends Exception {
	private static final long serialVersionUID = 1L;

	public DataStructureException(String msg) {
		super("DataFormat Exception: " + msg);
	}
}
