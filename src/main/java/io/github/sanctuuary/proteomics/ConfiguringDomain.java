package io.github.sanctuuary.proteomics;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import nl.uu.cs.ape.sat.utils.APEUtils;

public class ConfiguringDomain {

	
	public static void setupDomain() throws IOException {
		getLimitedToolSet(Benchmarking.TOOLS_DIR + "toolList.json");
//		System.out.println();
		setupToolSetFromExistingDomain("proteomics", "Extended");
		System.out.println();
		setupToolSetFromExistingDomain("", "FullBioTools");
	}
	
	private static void getLimitedToolSet(String listFilePath) throws IOException {
		String toolType = "Original";
		
		// Fetch the Limited (predefined) set of tool
		JSONArray bioToolsRAW = BioToolsAPI.readListOfTools(listFilePath);
		
		APEUtils.write2file(bioToolsRAW.toString(), new File(Benchmarking.TOOLS_DIR + Benchmarking.TOOLS_PREFIX + toolType + "RAW.json"), false);
		
		JSONObject apeToolAnnotation = BioToolsAPI.convertBioTools2ApeAnnotation(bioToolsRAW);
		APEUtils.write2file(apeToolAnnotation.toString(), new File(Benchmarking.TOOLS_DIR + Benchmarking.TOOLS_PREFIX + toolType + ".json"), false);
	}
	
	
	
	private static void getToolSetFromDomain(String domainName, String toolType) throws IOException {

		// Fetch the Extended set of tool
		JSONArray bioToolsRAW = BioToolsAPI.getToolsFromDomain(domainName);
		
		APEUtils.write2file(bioToolsRAW.toString(), new File(Benchmarking.TOOLS_DIR + Benchmarking.TOOLS_PREFIX + toolType + "RAW.json"), false);
		
		JSONObject apeToolAnnotation = BioToolsAPI.convertBioTools2ApeAnnotation(bioToolsRAW);
		APEUtils.write2file(apeToolAnnotation.toString(), new File(Benchmarking.TOOLS_DIR + Benchmarking.TOOLS_PREFIX + toolType + ".json"), false);
	}
	
	private static void setupToolSetFromExistingDomain(String domainName, String toolType) throws IOException {

		JSONArray bioToolsRAW = APEUtils.readFileToJSONArray(new File(Benchmarking.TOOLS_DIR + Benchmarking.TOOLS_PREFIX + toolType + "RAW.json"));
		
		
		JSONObject apeToolAnnotation = BioToolsAPI.convertBioTools2ApeAnnotation(bioToolsRAW);
		APEUtils.write2file(apeToolAnnotation.toString(), new File(Benchmarking.TOOLS_DIR + Benchmarking.TOOLS_PREFIX + toolType + ".json"), false);
	}
	
}
