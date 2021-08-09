package analyzer.GUI;

public class PrintSomeValue {

	public  void run() throws Exception{
		int i = 0;
		while(i++ < 5) {
			System.out.println("Hello World!!");
			Thread.sleep(5000);
		}
	}

}
