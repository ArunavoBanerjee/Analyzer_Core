package analyzer.SourceAdaptors;

public class FactoryTest {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
SourceParserFactory spf = new SourceParserFactory();
Parser parser = spf.getParser("C:\\Users\\user\\Desktop\\KGP\\NDL\\Research\\Analyzer\\testing_modules\\CAP05_ana_test1.tar.gz", "");
int count = 0;
while(parser.next()) {
	System.out.println("Item : " + count++);
	System.out.println(parser.dataDict);
	if(count == 2)
	break;
}
	}

}
