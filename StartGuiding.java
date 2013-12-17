package com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.GotoInstruction;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LOOKUPSWITCH;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.TABLESWITCH;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jdom2.input.SAXBuilder;

public class StartGuiding {
	
	/*private static String tomcatLogLoc = "/var/lib/tomcat6/logs/localhost_access_log.2013-12-16.txt";
	private static String wamAnalysisLoc = "/home/neeraj/bookstore/analysis/interfaces/bookstore-wamai-pda-interfaces.xml";
	private static String testSuiteLoc = "/home/neeraj/Project/ImproveCoverage/wgetTestSuite.sh";
	private static String augtestSuiteLoc = "/home/neeraj/Project/ImproveCoverage/augmentedTestSuite.sh";
	private static String baseURL = "http://localhost:8080/bookstore/";
	private static String coberturaJsp = "WriteCoberturaCoverage.jsp";
	private static String srcPath = "/home/neeraj/bookstore/src/org/apache/jsp/";
	private static String classPath = "/home/neeraj/bookstore/web/WEB-INF/classes/org/apache/jsp/";
	private static String coveragePath = "/home/neeraj/cobertura/project_report/coverage.xml";*/
	
	private static String tomcatLogLoc = "";
	private static String wamAnalysisLoc = "";
	private static String testSuiteLoc = "";
	private static String augtestSuiteLoc = "";
	private static String baseURL = "";
	private static String coberturaJsp = "";
	private static String srcPath = "";
	private static String classPath = "";
	private static String coveragePath = "";
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		tomcatLogLoc = args[0];
		wamAnalysisLoc = args[1];
		testSuiteLoc = args[2];
		augtestSuiteLoc = args[3];
		baseURL = args[4];
		coberturaJsp = args[5];
		srcPath = args[6];
		classPath = args[7];
		coveragePath = args[8];
		
		try {
			File file = new File(augtestSuiteLoc);
			if(file.exists())
				file.delete();
		    file.createNewFile();
			File oldFile = new File(testSuiteLoc);
			BufferedReader br = new BufferedReader(new FileReader(oldFile));
			PrintWriter writer = new PrintWriter(new FileWriter(file, true));
			String line = "";
			while((line = br.readLine()) != null){
				writer.println(line);
			}
			br.close();
			writer.println();
			writer.println("# NEW Testcases");
			writer.close();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		findUnusedVariablesWAM();
		findUnusedVariablesCFG();
		findUnclickedLinks();
		//findUnclickedButtons();
		
	}
	
	//private static void findUnclickedButtons(){
		
	//}
	
	private static void findUnclickedLinks(){
		List<String> urlsList = new ArrayList<String>();
		List<String> clickedLinksList = new ArrayList<String>();
		try {
			File accessLogs = new File(tomcatLogLoc);
			FileReader fr = new FileReader(accessLogs);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while((line = br.readLine()) != null){
				String regex = "[GET|POST]\\s\\/[^/]+\\/([^.]+\\.jsp)\\s+";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(line);
				if(matcher.find()){
					String jspName = matcher.group(1).trim();
					String finalURL = baseURL + jspName;
					if(!urlsList.contains(finalURL))
						urlsList.add(finalURL);
				}
				regex = "[GET|POST]\\s\\/[^/]+\\/([^.]+\\.jsp\\?[^HTTP]*)";
				pattern = Pattern.compile(regex);
				matcher = pattern.matcher(line);
				if(matcher.find()){
					String clickedLink = matcher.group(1).trim();
					String finalURL = baseURL + clickedLink;
					if(!clickedLinksList.contains(finalURL))
						clickedLinksList.add(finalURL);
				}
			}
			br.close();
			List<String> unclickedLinksList = new ArrayList<String>();
			for(int i=0; i<urlsList.size(); i++){
				String URL = urlsList.get(i);
				HttpClient client = HttpClientBuilder.create().build();
				HttpGet request = new HttpGet(URL);
				HttpResponse response = client.execute(request);
				BufferedReader br2 = new BufferedReader(new InputStreamReader(response.getEntity().getContent())); 
				StringBuffer result = new StringBuffer();
				String line2 = "";
				while ((line2 = br2.readLine()) != null) {
					result.append(line2);
				}
				br2.close();
				String regex = "<a\\shref=\"([^>]*)\"[^>]*>(.*?)<\\/a>";
				Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(result.toString());
				while(matcher.find()){
					String link = matcher.group(1).trim();
					String finalURL = baseURL + link;
					//if(link.contains("BookDetail.jsp?item_id=8")){
						//System.out.println();
					//}
					/*String linkDesc = matcher.group(2).trim();
					if(!clickedLinksList.contains(link) && !unclickedLinksList.contains(linkDesc)){
						if(!linkDesc.contains("<img ")){
							unclickedLinksList.add(linkDesc);
							System.out.println(linkDesc);
						}
					}*/
					if(!clickedLinksList.contains(finalURL) && !unclickedLinksList.contains(finalURL)){
						unclickedLinksList.add(finalURL);
					}
				}
				//System.out.println("---------------------");
			}
			augmentTestSuite(unclickedLinksList);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private static void findUnusedVariablesWAM(){
		Map<String, String> oldURLList = new HashMap<String, String>();
		try {
			File accessLogs = new File(tomcatLogLoc);
			FileReader fr = new FileReader(accessLogs);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while((line = br.readLine()) != null){
				String regex1 = "[GET|POST]\\s\\/[^/]+\\/([^.]+\\.jsp)\\s+";
				Pattern pattern1 = Pattern.compile(regex1, Pattern.CASE_INSENSITIVE);
				Matcher matcher1 = pattern1.matcher(line);
				if(matcher1.find()){
					String jspName = matcher1.group(1).trim();
					if(!oldURLList.containsKey(jspName) && !jspName.equals(coberturaJsp)){
						oldURLList.put(jspName, "");
						//System.out.println(jspName);
					}
				}
				else {
					String regex2 = "[GET|POST]\\s\\/[^/]+\\/([^.]+\\.jsp)\\?([^HTTP]*)";
					Pattern pattern2 = Pattern.compile(regex2, Pattern.CASE_INSENSITIVE);
					Matcher matcher2 = pattern2.matcher(line);
					if(matcher2.find()){
						String jspName = matcher2.group(1).trim();
						String queryStr = matcher2.group(2).trim();
						if(!oldURLList.containsKey(jspName)){
							oldURLList.put(jspName, queryStr);
							//System.out.println(jspName);
						}
					}
				}
			}
			br.close();
			formURLs(oldURLList,true);
			formURLs(oldURLList,false);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private static void formURLs(Map<String, String> oldURLList, boolean flag){
		List<String> newURLList = new ArrayList<String>();
		Map<String, Map<String, TypeValues>> WAM_Object = parseWAMAnalysis();
		
		Iterator<String> iter = oldURLList.keySet().iterator();
		while(iter.hasNext()){
			String jspName = iter.next();
			Map<String, TypeValues> paramMap = WAM_Object.get(jspName);
			String queryStr = oldURLList.get(jspName);
			//System.out.println(queryStr);
			if(queryStr.equals("")){
				Iterator<String> iterParam = paramMap.keySet().iterator();
				List<String> buildQueryStr = new ArrayList<String>();
				buildQueryStr.add(baseURL + jspName + "?");
				while(iterParam.hasNext()){
					String paramName = iterParam.next();
					TypeValues typeValObj = paramMap.get(paramName);
					String type = typeValObj.getType();
					String values = typeValObj.getValues();
					if(!type.equalsIgnoreCase("string")){
						sendToList(paramName + "=" + genValue(flag) + "&", buildQueryStr); 
					}
					else {
						values = values.substring(1,values.length()-1);
						if(values.isEmpty())
							sendToList(paramName + "=" + genValue(!flag) + "&", buildQueryStr);
						else {
							String[] valuesArr = values.split(",");
							buildQueryStr = augmentList(paramName, valuesArr, buildQueryStr);							
						}
					}
				}
				for(int i=0; i<buildQueryStr.size(); i++){
					newURLList.add(buildQueryStr.get(i));
				}
			}
			else {
				String[] pairs = queryStr.split("&");
				queryStr = baseURL + jspName + "?";
				/*for(int i=0; i<pairs.length; i++){
					if(pairs[i] != null && !pairs[i].isEmpty()){
						String[] pair = pairs[i].split("=");
						String key = pair[0];
						String val = "";
						if(pair.length == 1)
							val = genNumber();
						else
							val = pair[1];
						queryStr = queryStr + key + "=" + val + "&"; 
					}
				}*/
				Iterator<String> iterParam = paramMap.keySet().iterator();
				List<String> buildQueryStr = new ArrayList<String>();
				buildQueryStr.add(queryStr);
				while(iterParam.hasNext()){
					String paramName = iterParam.next().trim();
					if(!queryStr.contains(paramName)){
						TypeValues typeValObj = paramMap.get(paramName);
						String type = typeValObj.getType();
						String values = typeValObj.getValues();
						if(!type.equalsIgnoreCase("string")){
							sendToList(paramName + "=" + genValue(flag) + "&", buildQueryStr); 
						}
						else {
							values = values.substring(1,values.length()-1);
							if(values.isEmpty())
								sendToList(paramName + "=" + genValue(!flag) + "&", buildQueryStr);
							else { 
								String[] valuesArr = values.split(",");
								buildQueryStr = augmentList(paramName, valuesArr, buildQueryStr);
							}
						}
					}
				}
				for(int i=0; i<buildQueryStr.size(); i++){
					newURLList.add(buildQueryStr.get(i));
				}
			}
		}
		augmentTestSuite(newURLList);
	}
	
	private static String genValue(boolean flag){
		/*if(flag){
			int Max = 7;
			int Min = 0;
			int out = Min + (int)(Math.random() * ((Max - Min) + 1));
			return String.valueOf(out);
		}*/
		//else {
			String[] arr = new String[]
					{"John","^%$# @!~#","Bob","$%-=","text",
					"0", "&", "3", "2", "1", "5", "Rob", "%", "$",
					"abc@xyz.com", "  ", "null", "?", "/"};
			int Max = 18;
			int Min = 0;
			int out = Min + (int)(Math.random() * ((Max - Min) + 1));
			return arr[out];
		//}
	}
	
	private static void sendToList(String pair, List<String> list){
		for(int i=0; i<list.size(); i++){
			String currQueryStr = list.remove(i);
			currQueryStr += pair;
			list.add(currQueryStr);
		}
	}
	
	private static List<String> augmentList(String paramName, String[] valuesArr, List<String> list){
		//queryStr = queryStr + paramName + "=" + values.split(",")[1].trim() + "&";
		List<String> newList = new ArrayList<String>();
		for(int i=0; i<list.size(); i++){
			for(int j=0; j<valuesArr.length; j++){
				String queryStrUpd = list.get(i) + paramName + "=" + valuesArr[j].trim() + "&";
				newList.add(queryStrUpd);
			}
		}
		return newList;
	}
	
	private static void augmentTestSuite(List<String> urlList){
		try {
		    File file = new File(augtestSuiteLoc);
			PrintWriter writer = new PrintWriter(new FileWriter(file, true));
			for(int i=0; i<urlList.size(); i++){
				String item = urlList.get(i);
				writer.println("wget --load-cookies cookies_project.txt --keep-session-cookies " + item);
			}
			writer.close();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private static Map<String, Map<String, TypeValues>> parseWAMAnalysis(){
		 Map<String, Map<String, TypeValues>> WAM_Object = new HashMap<String, Map<String, TypeValues>>();
		 try {
			SAXBuilder builder = new SAXBuilder();
			File xmlFile = new File(wamAnalysisLoc);
			Document document = (Document) builder.build(xmlFile);
			Element rootNode = document.getRootElement();
			List<Element> componentlist = rootNode.getChildren("component");
			Iterator<Element> iterComponentList = componentlist.iterator();
			while(iterComponentList.hasNext()){
				Map<String, TypeValues> paramMap = new HashMap<String, TypeValues>();
				Element component = iterComponentList.next();
				String jspName = component.getAttribute("name").getValue();
				List<Element> interfaceList = component.getChildren("interface");
				Iterator<Element> iterInterfaceList = interfaceList.iterator();
				while(iterInterfaceList.hasNext()){
					Element interfaceWAM = iterInterfaceList.next();
					String servletName = interfaceWAM.getAttribute("target").getValue();
					List<Element> parameterList = interfaceWAM.getChildren("parameter");
					if(parameterList != null){
						Iterator<Element> iterParameterList = parameterList.iterator();
						while(iterParameterList.hasNext()){
							Element parameter = iterParameterList.next();
							String paramName = parameter.getAttribute("name").getValue();
							String paramType = parameter.getAttribute("type").getValue();
							String paramValues = parameter.getAttribute("values").getValue();
							TypeValues typeValObj = new TypeValues(paramType, paramValues);
							paramMap.put(paramName, typeValObj);
						}
					}
				}
				String[] parts = jspName.split("\\.");
				String jspNameFormatted = parts[parts.length-1].replace("_", ".");
				WAM_Object.put(jspNameFormatted, paramMap);
			}
			
		 } catch (Exception ex) {
			ex.printStackTrace();
		 }
		 return WAM_Object;
	}
	
	private static class TypeValues{
		String type;
		String values;
		public TypeValues(String type, String values) {
			super();
			this.values = values;
			this.type = type;
		}
		public String getValues() {
			return values;
		}
		public void setValues(String values) {
			this.values = values;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		
	}
	
	private static void findUnusedVariablesCFG(){
		//Parse coverage.xml to get Servlet-LineNo mappings for the initial testsuite execution.
		Map<String, List<Integer>> mappings = parseCoverageReport();
		//Iterate over mappings to get all Servlets covered.
		Set<String> keyset = mappings.keySet();
		Iterator<String> iter = keyset.iterator();
		List<String> urlList = new ArrayList<String>();
		while(iter.hasNext()){
			String servletJavaName = iter.next();
			String[] temp = servletJavaName.split("\\.");
			Map<Integer, String> servletSourceCode = parseJavaServlet(temp[temp.length-1] + ".java");
			String servletClassName = classPath + temp[temp.length-1] + ".class";
			String jspName = temp[temp.length-1] + ".jsp";
			Map<Method, Map<InstructionHandle, List<InstructionHandle>>> classCFGs
				= new CFG().generateCFG(servletClassName);
			Iterator<Method> methodsIter = classCFGs.keySet().iterator();
			List<Integer> potentialOPLineNos = new ArrayList<Integer>();
			while(methodsIter.hasNext()){
				Method m = methodsIter.next();
				//Replace every Instruction Node with corresponding Javasource LineNo Node. 
				if(m != null){
					Map<InstructionHandle, List<InstructionHandle>> methodCFG = classCFGs.get(m);
					InstructionHandle firstInstr =  getFirstElement(methodCFG);
					List<Integer> visitedList = new ArrayList<Integer>();
					getPotentialOutputLineNos(potentialOPLineNos, firstInstr, servletJavaName, m, methodCFG, mappings, visitedList);
				}
			}
			List<String> potentialOPLines =  getPotentialOutputLines(potentialOPLineNos, servletSourceCode);
			Map<String, String> all_variables = getAllVariables(servletSourceCode);
			printResult(urlList, jspName, potentialOPLines, all_variables);
		}
		augmentTestSuite(urlList);
	}
	
	private static void printResult(List<String> urlList, String jspName, List<String> potentialOPLines, Map<String, String> all_variables){
		try {
			List<String> actualVarList = new ArrayList<String>();
			Iterator<String> keys_iter = all_variables.keySet().iterator();
			while(keys_iter.hasNext()){
				String classVariable = keys_iter.next();
				for(int i=0; i<potentialOPLines.size(); i++){
					String line = potentialOPLines.get(i);
					if(line.contains(classVariable)){
						String actualVariable = all_variables.get(classVariable);
						if(!actualVarList.contains(actualVariable))
							actualVarList.add(actualVariable);
						break;
					}
				}
			}
			String url = baseURL + jspName + "?";
			for(int i=0; i<actualVarList.size(); i++){
				 url = url + actualVarList.get(i) + "=" + genValue(true);
			}
			urlList.add(url);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private static List<String> getPotentialOutputLines(
			List<Integer> potentialOPLineNos, 
			Map<Integer, String>servletSourceCode){
		List<String> potentialOPLines = new ArrayList<String>();
		for(int i=0; i<potentialOPLineNos.size(); i++){
			Integer lineNo = potentialOPLineNos.get(i);
			String line = servletSourceCode.get(lineNo);
			potentialOPLines.add(line);
		}
		return potentialOPLines;
	}
	
	private static Map<String, String> getAllVariables(Map<Integer, String> servletSourceCode){
		Map<String, String> variables = new HashMap<String, String>();
		Iterator<Integer> iter = servletSourceCode.keySet().iterator();
		while(iter.hasNext()){
			Integer i = iter.next();
			String line = servletSourceCode.get(i);
			if(line.contains("getParam(")){
				String regex = "([^=]+)=\\s*getParam([^<]*)\"([^<]*)\"([^<]*);";
				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(line);
				if(m.find()){
					String classVariable = m.group(1).trim();
					String actualVariable = m.group(3);
					variables.put(classVariable, actualVariable);
				}
			}
		}
		return variables;	
	}
	
	private static void getPotentialOutputLineNos(
			List<Integer> potentialOPLineNos, 
			InstructionHandle instr, 
			String servletJavaName,
			Method method,
			Map<InstructionHandle, List<InstructionHandle>> graph,
			Map<String, List<Integer>> mappings,
			List<Integer> visitedList){
		int instrPos = instr.getPosition();
		visitedList.add(instrPos);
		LineNumberTable lineNumberTable = method.getCode().getLineNumberTable();
		Integer srccode_lineNumber = lineNumberTable.getSourceLine(instrPos);
		if(isCovered(srccode_lineNumber, servletJavaName, mappings)){
			List<InstructionHandle> children = graph.get(instr);
			for(int i=0; i<children.size(); i++){
				InstructionHandle childInstr = children.get(i);
				if(!visitedList.contains(childInstr.getPosition())){
					getPotentialOutputLineNos(potentialOPLineNos, childInstr, servletJavaName, method, graph, mappings, visitedList);
				}
			}
		}
		else {
			potentialOPLineNos.add(srccode_lineNumber);
		}
	}
	
	private static boolean isCovered(Integer lineNumber, String servletJavaName, Map<String, List<Integer>> mappings){
		boolean flag = false;
		List<Integer> coveredLineNumbers = mappings.get(servletJavaName);
		if(coveredLineNumbers.contains(lineNumber)){
			flag = true;
		}
		return flag;
	}
	
	private static InstructionHandle getFirstElement(Map<InstructionHandle, List<InstructionHandle>> graph){
		InstructionHandle firstElement = null;
		Set<InstructionHandle> keyset = graph.keySet();
		Iterator<InstructionHandle> iter = keyset.iterator();
		int min = Integer.MAX_VALUE;
		while(iter.hasNext()){
			InstructionHandle element = iter.next();
			int pos = element.getPosition();
			if(pos < min){
				firstElement = element;
				min = pos;
				//break;
			}
		}
		return firstElement;
	}
	
	private static Map<Integer, String> parseJavaServlet(String fileName){
		Map<Integer, String> servletSourceCode = new HashMap<Integer,String>();
		try {
			File file = new File(srcPath + fileName);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			Integer lineNo = 1;
			while((line = br.readLine()) != null){
				servletSourceCode.put(lineNo, line);
				lineNo++;
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return servletSourceCode;
	}
	
	private static Map<String, List<Integer>> parseCoverageReport(){
		Map<String, List<Integer>> mappings = new HashMap<String, List<Integer>>();
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new File(coveragePath));
			Element rootNode = doc.getRootElement();
			Element packages = rootNode.getChild("packages");
			Element pck = packages.getChild("package");
			Element classes = pck.getChild("classes");
			List<Element> class_element_list = classes.getChildren("class");
			Iterator<Element> iter_class = class_element_list.iterator();
			while(iter_class.hasNext()){
				Element class_element = iter_class.next();
				String servlet_name = class_element.getAttribute("name").getValue();
				List<Integer> lineNumbersList = new ArrayList<Integer>();
				Element lines = class_element.getChild("lines");
				List<Element> line_list = lines.getChildren("line");
				Iterator<Element> iter_line = line_list.iterator();
				while(iter_line.hasNext()){
					Element line = iter_line.next();
					int hits = line.getAttribute("hits").getIntValue();
					if(hits != 0){
						boolean branchFlag = line.getAttribute("branch").getBooleanValue();
						if(branchFlag){
							String cond_cov = line.getAttribute("condition-coverage").getValue();
							if(cond_cov.contains("100%")){
								int number = line.getAttribute("number").getIntValue();
								lineNumbersList.add(number);
							}
						}
						else{
							int number = line.getAttribute("number").getIntValue();
							lineNumbersList.add(number);
						}
					}	
				}
				mappings.put(servlet_name, lineNumbersList);
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return mappings;
	}
	
	private static class CFG {
		
		private Map<InstructionHandle, List<InstructionHandle>> generate(InstructionList instrList) {
			Map<InstructionHandle, List<InstructionHandle>> graph = new HashMap<InstructionHandle, List<InstructionHandle>>();
			InstructionHandle[] instrHandle = instrList.getInstructionHandles();
			InstructionHandle from = null;
			InstructionHandle to = null;
			for(int i=1; i<instrHandle.length; i++){
				from = instrHandle[i-1];
				Instruction instr = instrHandle[i-1].getInstruction();
				if(instr instanceof BranchInstruction){
					BranchInstruction branchInstr = (BranchInstruction)instr;
					if(branchInstr instanceof IfInstruction){
						List<InstructionHandle> children = new ArrayList<InstructionHandle>();
						InstructionHandle target = ((IfInstruction)branchInstr).getTarget();
						to = target;
						children.add(to);
						to = instrHandle[i];
						children.add(to);
						graph.put(from, children);
					}
					else if(branchInstr instanceof GotoInstruction){
						List<InstructionHandle> children = new ArrayList<InstructionHandle>();
						InstructionHandle target = ((GotoInstruction)branchInstr).getTarget();
						to = target;
						children.add(to);
						graph.put(from, children);
					}
					else if(branchInstr instanceof LOOKUPSWITCH){
						List<InstructionHandle> children = new ArrayList<InstructionHandle>();
						InstructionHandle[] targets =  ((LOOKUPSWITCH) branchInstr).getTargets();
						for(int j=0; j<targets.length; j++){
							to = targets[j];
							children.add(to);
						}
						graph.put(from, children);
					}
					else if(branchInstr instanceof TABLESWITCH){
						List<InstructionHandle> children = new ArrayList<InstructionHandle>();
						InstructionHandle[] targets =  ((TABLESWITCH) branchInstr).getTargets();
						for(int j=0; j<targets.length; j++){
							to = targets[j];
							children.add(to);
						}
						graph.put(from, children);
					}
				}
				else {
					List<InstructionHandle> children = new ArrayList<InstructionHandle>();
					if(!(instr instanceof ReturnInstruction)){
						to = instrHandle[i];
					}
					children.add(to);
					graph.put(from, children);
				}
			}
			List<InstructionHandle> child_as_exit = new ArrayList<InstructionHandle>();
			graph.put(instrHandle[instrHandle.length-1], child_as_exit);
			return graph;
		}
		
		public Map<Method, Map<InstructionHandle, List<InstructionHandle>>> generateCFG(String inputClassFilename) {
			Map<Method, Map<InstructionHandle, List<InstructionHandle>>> cfgs_map = new HashMap<Method, Map<InstructionHandle, List<InstructionHandle>>>();
			JavaClass cls = null;
			try {
				cls = (new ClassParser( inputClassFilename )).parse();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit( 1 );
			}
			
			for ( Method m : cls.getMethods() ) {
				Code methodCode = m.getCode();
				byte[] methodByteArr = methodCode.getCode();
				InstructionList instrList = new InstructionList(methodByteArr);
				Map<InstructionHandle, List<InstructionHandle>> graph = generate(instrList);
				cfgs_map.put(m, graph);
			}
			return cfgs_map;
		}
		
	}

}
