package git;

public class GitTest {
	public static void main(String[] args){
		GenASTString g = new GenASTString();
		g.genCommitIDs();
		g.initModules();
		System.out.println("Done");
	}
}
