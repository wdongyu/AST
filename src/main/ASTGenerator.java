package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class ASTGenerator {
//	static public List<MyASTNode> astNodeList = new ArrayList<MyASTNode>();
	static public String astString;
	
	public ASTGenerator(File f) {
		// TODO Auto-generated constructor stub
		ParseFile(f);
	}

	public ASTGenerator(){		
	}

	// read file content into a string
	public String readFileToString(String filePath) throws IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));

		char[] buf = new char[10];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			// System.out.println(numRead);
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}

		reader.close();

		return fileData.toString();
	}

	// use ASTParse to parse string
	public void parse(String str) {		
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		// find the MethodDeclaration node, MethodNodeVisitor
//		MethodNodeVisitor methodNodeVisitor = new MethodNodeVisitor();
//		VariableNodeVisitor variableNodeVisitor = new VariableNodeVisitor();
		NodeVisitor nV = new NodeVisitor();
		cu.accept(nV);
		String var = "";
		for(ASTNode node : nV.nodeList){
			char type = (char)node.getNodeType();
			var += Character.toString(type);
			//System.out.print(node.getNodeType() + " ");
		}
		//System.out.println();
		astString = var;
		//System.out.println("TestAST");
	}

	// loop directory to get file list
	public void ParseFile(File f) {
		String filePath = f.getAbsolutePath();
		if (f.isFile()) {
			try {
				parse(readFileToString(filePath));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("Not a File!");
		}
	}
}
