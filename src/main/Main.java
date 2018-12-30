package main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import git.GenASTString;

public class Main {
	public static boolean FLAG = true;
	public static void main(String[] args) throws IOException {
		GenASTString gas = new GenASTString();
		gas.gitResetToCommitID("c66efa940fd7b2bfa4d2a9b9a65f50bd7a5a2659");
		if(FLAG){
			File log = new File("result.txt");
			File count = new File("count.txt");
			gas.writer = new PrintWriter(log);
			gas.count = new PrintWriter(count);
			
			gas.initModules();
			gas.calSim();
				
			gas.genModuleSim();
			gas.genModuleSimCount();
			gas.writer.close();
			gas.count.close();
		}	
		else{
			File release = new File("release.txt");
			gas.release = new PrintWriter(release);
			gas.genBugFixAndFunctionIDs();
			gas.calVersionSim();
			gas.calBugFixSim();
			gas.release.close();
		}
//		double d = 1.2412124124141241241241;
//		System.out.println((int)d);
		System.out.println("Program exit successfully");
	}

}
