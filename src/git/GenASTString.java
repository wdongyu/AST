package git;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;
import main.ASTGenerator;

import javafx.util.*;
import util.SimFeatureUtil;
//class moduleSim{
//	String moduleName;
//	ArrayList<Double> sim = new ArrayList<Double>();
//}
class Release implements Comparable{
	String commitID;
	int index;
	public int compareTo(Object o){
		if(index < ((Release)o).index)
			return -1;
		else if(index > ((Release)o).index)
			return 1;
		else
			return 0;
	}
}
public class GenASTString {
	//javaFile为commitID到相关Java模块的映射
	HashMap<String,List<JavaModule>> modules = new HashMap<String,List<JavaModule>>();
	public List<String> commitIDs = new ArrayList<String>();
	HashMap<String[], List<ModuleSim>> similarity =new HashMap<String[], List<ModuleSim>>();
	
	HashMap<String, List<Double>> moduleSim = new HashMap<String, List<Double>>();
	HashMap<String, int[]> moduleSimCount = new HashMap<String, int[]>();
	
	ArrayList<String>moduleNames = new ArrayList<String>();
	ArrayList<String> releaseModuleName;
	public PrintWriter release;
//	public List<String> functionCommitIDs = new ArrayList<String>();
//	public ArrayList<String>bugFixIDs[] = new ArrayList[10];
	
	public HashMap<Release,List<JavaModule>> functionCommitIDs = new HashMap<Release,List<JavaModule>>();
	public HashMap<Release,List<JavaModule>> bugFixIDs[] = new HashMap[10];
	
	
	public void genModuleSim(){
		for(String s : moduleNames){
			ArrayList<Double> sims = new ArrayList<Double>();
			count.println("MODULE : " + s);
			for(String[] pair : similarity.keySet())//similarity的keySet不是连续的
				for(ModuleSim ms : similarity.get(pair))
					if(ms.moduleName.equals(s)){
						sims.add(ms.sim);
						count.println(pair[0] + "-" + pair[1] + " : " + ms.sim);
						break;
					}
			moduleSim.put(s, sims);
		}
	}
	
	public void genModuleSimCount(){
		int[] res = new int[22];
		for(int i = 0;i < res.length; i++)
			res[i] = 0;
		for(String s : moduleSim.keySet()){
			List<Double> ms = moduleSim.get(s);
			for(Double d : ms){
				if(d.doubleValue() == 0.0)
					res[20]++;
				else if(d.doubleValue() == 1.0)
					res[21]++;
				else{
					int index = (int)Math.ceil((double)d * 20) - 1;
					res[index]++;
				}
			}
			moduleSimCount.put(s, res);
			for(int i = 0;i < res.length;i++){
				double low = i * 0.05;
				double high = (i + 1) * 0.05;
				count.println("Module(" + s + ")" + low + "~" + high + " " + res[i]);
			}
			count.println();
			res = new int[22];
			for(int i = 0;i < res.length; i++)
				res[i] = 0;
		}
	}
	
	public int versions = 0;
	int IDnums = 10;
	public PrintWriter writer;
	public PrintWriter count;
	//生成所有的commitID
	public void genBugFixAndFunctionIDs(){
		BufferedReader br = null;
		try{
			Runtime rt = Runtime.getRuntime();
			File dir = new File("D:\\commons-dbutils");
			Process p = rt.exec("cmd /c git tag",null,dir);
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			ArrayList<String> tags = new ArrayList<String>();
			ArrayList<String> commitIDs = new ArrayList<String>();
			ArrayList<Integer> spilt = new ArrayList<Integer>(); 
			while((line = br.readLine()) != null){
				tags.add(line);
				commitIDs.add(genCommitID(line));
				System.out.println(line);
			}
			
			gitResetToCommitID(commitIDs.get(0));
			String rootDir = "D:\\commons-dbutils\\src\\java";
			File root = new File(rootDir);
			releaseModuleName = getAllModuleName(root);
			
			for(int i = 0;i < tags.size() - 1;i++){
				if(!isSameVersion(tags.get(i), tags.get(i + 1)))
					spilt.add(i);
			}
			spilt.add(tags.size() - 1);//最后一个版本在循环中未加进去
			
			int index = 0;
			for(Integer i : spilt){
				String commitID = commitIDs.get(i);
				
				gitResetToCommitID(commitID);
				ArrayList<JavaModule> ms = genModuleList(releaseModuleName);
				Release r = new Release();
				r.commitID = commitID;
				r.index = index;
				functionCommitIDs.put(r, ms);
				index++;
			}
			
			index = 0;
			int versionNum = 0;
			HashMap<Release,List<JavaModule>> sameVersion = new HashMap<Release,List<JavaModule>>();
			//ArrayList<String> sameVersion = new ArrayList<String>();
			for(int i = 0;i < tags.size();i++){
				String commitID = commitIDs.get(i);
				
				gitResetToCommitID(commitID);
				ArrayList<JavaModule> ms = genModuleList(releaseModuleName);
				
				Release r = new Release();
				r.commitID = commitID;
				r.index = index;		
				sameVersion.put(r, ms);
				
				index++;
				if(spilt.contains(i)){
					//sameVersion.add(commitIDs.get(i));
					bugFixIDs[versionNum] = sameVersion;
					sameVersion = new HashMap<Release,List<JavaModule>>();
					versionNum++;
					index=0;
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean isSameVersion(String tag1, String tag2){
		if(tag1.contains("RC"))
			tag1 = tag1.substring(0, tag1.indexOf("RC") - 1);
		if(tag2.contains("RC"))
			tag2 = tag2.substring(0, tag2.indexOf("RC") - 1);
		return tag1.equals(tag2);
	}
	
	public String genCommitID(String tagName){
		BufferedReader br = null;
		String line = null;
		try{
			Runtime rt = Runtime.getRuntime();
			File dir = new File("D:\\commons-dbutils");
			Process p = rt.exec("cmd /c git show " + tagName,null,dir);
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			while((line = br.readLine()) != null){
				if(line.startsWith("commit ")){
					String[] temp = line.split(" ");
					System.out.println(line);
					line = temp[1];
					break;
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return line;
	}
	
	public void genCommitIDs(){
		BufferedReader br = null;
		try{
			Runtime rt = Runtime.getRuntime();
			File dir = new File("D:\\commons-dbutils");
			Process p = rt.exec("cmd /c git log --pretty=oneline",null,dir);
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while((line = br.readLine()) != null){
//				if(commitIDs.size() >= IDnums)
//					break;
				String[] temp = line.split(" ");
				commitIDs.add(temp[0]);
				//System.out.println(line);
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//回退到某一版本
	public void gitResetToCommitID(String commitID){
		BufferedReader br = null;
		try{
			Runtime rt = Runtime.getRuntime();
			File dir = new File("D:\\commons-dbutils");
			Process p = rt.exec("cmd /c git reset --hard " + commitID,null,dir);
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while((line = br.readLine()) != null){
//				String[] temp = line.split(" ");
//				logs.add(temp[0]);
//				System.out.println(line);
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	//ModuleName为同一个文件夹下所有java文件所在文件夹的绝对路径
	public ArrayList<String> getAllModuleName(File rootDir){
		Queue<File> q = new LinkedList<File>();
		ArrayList<String> moduleNames = new ArrayList<String>();
		q.offer(rootDir);
		while(!q.isEmpty()){
			File dir = q.poll();
			if(dir.isDirectory()){
				for(File f : dir.listFiles())
					q.offer(f);
			}
			else{
				String path = dir.getParentFile().getPath();
				if(!moduleNames.contains(path))
					moduleNames.add(path);
			}
		}
		return moduleNames;
	}
	
	public JavaModule getJavaModule(String commitID, String moduleName){
		JavaModule res = null;
		for(JavaModule jM : modules.get(commitID))
			if(jM.moduleName.equals(moduleName))
				return jM;
		return res;
	}
	
	public List<JavaModule> getJavaModuleList(String commitID){
		ArrayList<JavaModule> res = null;
		for(Release rs : functionCommitIDs.keySet()){
			if(rs.commitID.equals(commitID))
				return functionCommitIDs.get(rs);
		}
		return res;
	}
	
	public List<JavaModule> getJavaModuleList(int index, String commitID){
		ArrayList<JavaModule> res = null;
		for(Release rs : bugFixIDs[index].keySet()){
			if(rs.commitID.equals(commitID))
				return bugFixIDs[index].get(rs);
		}
		return res;
	}
	
	public JavaModule getReleaseModule(String commitID, String moduleName){
		JavaModule res = null;
		for(JavaModule jM : getJavaModuleList(commitID))
			if(jM.moduleName.equals(moduleName))
				return jM;
		return res;
	}
	public JavaModule getBugFixModule(int index, String commitID, String moduleName){
		JavaModule res = null;
		for(JavaModule jM : getJavaModuleList(index, commitID))
			if(jM.moduleName.equals(moduleName))
				return jM;
		return res;
	}
	public double calSim(String commitID1, String commitID2, String moduleName){
		JavaModule m0 = getJavaModule(commitID1, moduleName);
		JavaModule m1 = getJavaModule(commitID2, moduleName);
		return util.SimFeatureUtil.sim(m0.moduleASTString, m1.moduleASTString);
	}
	
	
	public void calVersionSim(){
		Set<Release> set = functionCommitIDs.keySet();
		Release[] rs =set.toArray(new Release[set.size()]);
		Arrays.sort(rs);
		for(int i = 0;i < functionCommitIDs.size() - 1;i++){
//			Release rs1 = rs[i];
//			rs[i + 1];
			String commitID1 = rs[i].commitID;
			String commitID2 = rs[i + 1].commitID;
			for(String moduleName : releaseModuleName){
				JavaModule m0 = getReleaseModule(commitID1, moduleName);
				JavaModule m1 = getReleaseModule(commitID2, moduleName);
				double sim = sim(m0.moduleASTString, m1.moduleASTString);
//				double sim = calSim(pair[0], pair[1], moduleName);
				release.println("Release : " + commitID1 + " VS " + commitID2 + " ModuleName : " + moduleName + " : " + sim);
			}
		}
	}
	public void calBugFixSim(){
		for(int i = 0; i < bugFixIDs.length; i++){
			Set<Release> set = bugFixIDs[i].keySet();
			Release[] rs =set.toArray(new Release[set.size()]);
			Arrays.sort(rs);
			for(int j = 0;j < bugFixIDs[i].size() - 1;j++){
				String commitID1 = rs[j].commitID;
				String commitID2 = rs[j + 1].commitID;
				for(String moduleName : releaseModuleName){
					JavaModule m0 = getBugFixModule(i, commitID1, moduleName);
					JavaModule m1 = getBugFixModule(i, commitID2, moduleName);
					double sim = sim(m0.moduleASTString, m1.moduleASTString);
//					double sim = calSim(pair[0], pair[1], moduleName);
					release.println("BugFix " + i + ": " + commitID1 + " VS " + commitID2 + " ModuleName : " + moduleName + " : " + sim);
				}
			}
		}
	}
	//public ArrayList<String>
	public void calSim(){
		ArrayList<String> dbutils = new ArrayList<String>();
		ArrayList<String> dbutilsHandlers = new ArrayList<String>();
		ArrayList<String> dbutilsWrappers = new ArrayList<String>();
		ArrayList<String> dbutilsHandlersCol = new ArrayList<String>();
		ArrayList<String> dbutilsHandlersPro = new ArrayList<String>();
		for(int i = 0;i < commitIDs.size() - 1;i++){
			String[] pair = new String[]{commitIDs.get(i), commitIDs.get(i + 1)};
			List<ModuleSim> m = new ArrayList<ModuleSim>();
			similarity.put(pair, m);
			System.out.println(i + " : " + pair[0] + "-" + pair[1]);
//			ArrayList<moduleSim> moduleSims = new ArrayList<moduleSim>();

			for(String moduleName : moduleNames){
//				ArrayList<Double> sim = moduleSims.
				JavaModule m0 = getJavaModule(pair[0], moduleName);
				JavaModule m1 = getJavaModule(pair[1], moduleName);
				double sim = sim(m0.moduleASTString, m1.moduleASTString);
//				double sim = calSim(pair[0], pair[1], moduleName);
				if(moduleName.endsWith("dbutils"))
					dbutils.add(pair[0] + " VS " + pair[1] + " : " + sim);
				else if(moduleName.endsWith("handlers"))
					dbutilsHandlers.add(pair[0] + " VS " + pair[1] + " : " + sim);
				else if(moduleName.endsWith("wrappers"))
					dbutilsWrappers.add(pair[0] + " VS " + pair[1] + " : " + sim);
				else if(moduleName.endsWith("columns"))
					dbutilsHandlersCol.add(pair[0] + " VS " + pair[1] + " : " + sim);
				else
					dbutilsHandlersPro.add(pair[0] + " VS " + pair[1] + " : " + sim);
//				writer.println(pair[0] + " VS " + pair[1] + " ModuleName : " + moduleName + " : " + sim);
				m.add(new ModuleSim(moduleName, sim));
			}
		}
		
		for(String s : dbutils)
			writer.println(s);
		writer.println();
		writer.println();
		for(String s : dbutilsHandlers)
			writer.println(s);
		writer.println();
		writer.println();
		for(String s : dbutilsWrappers)
			writer.println(s);
		writer.println();
		writer.println();
		for(String s : dbutilsHandlersCol)
			writer.println(s);
		writer.println();
		writer.println();
		for(String s : dbutilsHandlersPro)
			writer.println(s);
	}
	
	public double sim(String str1, String str2) {
		try {
			int dis = SimFeatureUtil.ld(str1, str2);
			double ld = (double)dis;
			writer.println("ld : " + ld + ", str1.length = " + str1.length() + ", str2.length = " + str2.length());
			if(Math.max(str1.length(), str2.length()) == 0)
				return 1;
			else 
				return (1-ld/(double)Math.max(str1.length(), str2.length()));
		} catch (Exception e) {
			return 0.1;
		}
	}
	public void printSim(){
		for(String[] pair : similarity.keySet()){
			List<ModuleSim> mS = similarity.get(pair);
			for(ModuleSim m : mS)
				System.out.println(pair[0] + " VS " + pair[1] + " ModuleName : " + m.moduleName + " : " + m.sim);
		}
	}
	public void initModules(){
		genCommitIDs();
		String fileDir = "D:\\commons-dbutils\\src\\main\\java";
		File dir = new File(fileDir);
		moduleNames = getAllModuleName(dir);
		int i = 0;
		for(String commitID : commitIDs){
			gitResetToCommitID(commitID);
			addModule(commitID,modules);
			System.out.println("initModules " + i + " : " + commitID);
			i++;
		}
		System.out.println("Modules init successfully");
	}
	
	public ArrayList<JavaModule> genModuleList(ArrayList<String> moduleNames){
		ArrayList<JavaModule> javaModules = new ArrayList<JavaModule>();
		ASTGenerator ast = new ASTGenerator();
		for(String s : moduleNames){

			JavaModule javaModule = new JavaModule();
			javaModule.moduleName = s;
			File module = new File(s);//module一定为目录
			//System.out.println(module);
			if(module.listFiles() == null)
				javaModule.moduleASTString = "";
			else 
				for(File f : module.listFiles()){
					if(f.isFile() && f.getName().endsWith(".java")){
						FileInfo info = new FileInfo();
						info.fileName = f.getName().substring(0, f.getName().indexOf(".java"));
						ast.ParseFile(f);
						info.astString =  ASTGenerator.astString;
						javaModule.moduleASTString += info.fileName + info.astString;
						
						javaModule.info.add(info);
					}
				}
			javaModules.add(javaModule);
		}
		return javaModules;
	}
	public void addModule(String commitID, HashMap<String,List<JavaModule>> modules){
		ArrayList<JavaModule> javaModules = genModuleList(moduleNames);
		modules.put(commitID, javaModules);
	}
	
}
