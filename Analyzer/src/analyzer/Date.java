package analyzer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Date {
	String date_format = "";

	public Date(String in_format) {
		this.date_format = in_format;
	}

	public String getDate() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(date_format);
		LocalDateTime now = LocalDateTime.now();
		return dtf.format(now);
	}

	public static void main(String[] args) {
		String format = "yyyy/MM/dd-HH:mm:ss";
		Date newDate = new Date(format);
		System.out.println(newDate.getDate());

	}
}
