package io.github.sanctuuary.proteomics;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nl.uu.cs.ape.sat.APE;
import nl.uu.cs.ape.sat.utils.APEUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BioToolsAPI {
	
	/** Http-Client */
	public final static OkHttpClient client = new OkHttpClient();

	public static JSONArray readListOfTools(String filePath) throws IOException {
		
		File toolList = new File(filePath);
		JSONArray toolListJson = new JSONArray(FileUtils.readFileToString(toolList, "UTF-8"));
		/* Fetch tool annotations */
		JSONArray toolAnnotations = fetchToolListFromBioTools(toolListJson);
		return toolAnnotations;
	}
	
	public static JSONArray getToolsFromDomain(String domainName) throws IOException {
		JSONArray toolAnnotations = null;
		if(domainName != "") {
			toolAnnotations = fetchToolsFromURI("https://bio.tools/api/t?domain=" + domainName + "&format=json");
		} else {
			toolAnnotations = fetchToolsFromURI("https://bio.tools/api/t?format=json");
		}
		return toolAnnotations;
	}
	
	/**
	 * Send Get request to get tool annotations Saves JSONArray with all the tool
	 * annotations (in tool list)
	 * 
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	private static JSONArray fetchToolListFromBioTools(JSONArray toolListJson) throws JSONException, IOException {
		JSONArray bioToolAnnotations = new JSONArray();
		for (int i = 0; i < toolListJson.length(); i++) {
			String currTool = toolListJson.getString(i);
			Request request = new Request.Builder().url("https://bio.tools/api/" + currTool + "?format=json").build();
			try (Response response = client.newCall(request).execute()) {
				if (!response.isSuccessful())
					throw new IOException("Unexpected code when trying to fetch" + response);
				// Get response body
				JSONObject responseJson = new JSONObject(response.body().string());
				bioToolAnnotations.put(i, responseJson);
			}
		}
		System.out.println("Tools fetched.");
		return bioToolAnnotations;
	}
	
	/**
	 * Send Get request to get tool annotations Saves JSONArray with all the tool
	 * annotations (in tool list)
	 * 
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	private static JSONArray fetchToolsFromURI(String url) throws JSONException, IOException {
		JSONArray bioToolAnnotations = new JSONArray();
		String next = "";
		int i = 1;
		while(next != null) {
			System.out.print("\n" + (i++) + ") ");
			Request request = new Request.Builder().url(url + "&format=json" + next.replace('?', '&')).build();
			try (Response response = client.newCall(request).execute()) {
				if (!response.isSuccessful())
					throw new IOException("Unexpected code when trying to fetch" + response);
				// Get response body
				JSONObject responseJson = new JSONObject(response.body().string());
				JSONArray toolListJson = responseJson.getJSONArray("list");
				for (int j = 0; j < toolListJson.length(); j++) {
					JSONObject tool = toolListJson.getJSONObject(j);
					bioToolAnnotations.put(tool);
					System.out.print(".");
				}
				try {
					next = responseJson.getString("next");
				} catch (JSONException e) {
					next = null;
				}
			}
			
		}
		System.out.println("Tools fetched.");
		return bioToolAnnotations;
	}
	
	/**
	 * Method converts tools annotated using 'bio.tools' standard (see <a href=
	 * "https://biotools.readthedocs.io/en/latest/api_usage_guide.html">bio.tools
	 * API</a>), into standard supported by the APE library.
	 * <p>
	 * In practice, the method takes a {@link JSONArray} as an argument, where each
	 * {@link JSONObject} in the array represents a tool annotated using 'bio.tools'
	 * standard, and returns a {@link JSONObject} that represents tool annotations
	 * that can be used by the APE library.
	 *
	 * @param bioToolsAnnotation A {@link JSONArray} object, that contains list of
	 *                           annotated tools ({@link JSONObject}s) according the
	 *                           bio.tools specification (see <a href=
	 *                           "https://biotools.readthedocs.io/en/latest/api_usage_guide.html">bio.tools
	 *                           API</a>)
	 * @return {@link JSONObject} that represents the tool annotation supported by
	 *         the APE library.
	 * @throws JSONException the json exception
	 */
	public static JSONObject convertBioTools2ApeAnnotation(JSONArray bioToolsAnnotation) throws JSONException {
		Set<String> notAcceptedTools = new HashSet<String>();
		Set<String> toolsMissingDimension = new HashSet<String>();
		int notAcceptedOperations = 0;
		
		JSONArray apeToolsAnnotations = new JSONArray();
		for (int i = 0; i < bioToolsAnnotation.length(); i++) {

			JSONObject bioJsonTool = bioToolsAnnotation.getJSONObject(i);
			List<JSONObject> functions = APEUtils.getListFromJson(bioJsonTool, "function", JSONObject.class);
			int functionNo = 1;
			functionloop:
			for (JSONObject function : functions) {
				JSONObject apeJsonTool = new JSONObject();
				apeJsonTool.put("label", bioJsonTool.getString("name"));
				if(functions.size() > 1) {
					apeJsonTool.put("id", bioJsonTool.getString("biotoolsID") + "_op" + (functionNo++));
				} else {
					apeJsonTool.put("id", bioJsonTool.getString("biotoolsID"));
				}
				
				JSONArray apeTaxonomyTerms = new JSONArray();

				JSONArray operations = function.getJSONArray("operation");
				for (int j = 0; j < operations.length(); j++) {
					JSONObject bioOperation = operations.getJSONObject(j);
					apeTaxonomyTerms.put(bioOperation.get("uri"));
				}
				apeJsonTool.put("taxonomyOperations", apeTaxonomyTerms);
//			reading inputs
				JSONArray apeInputs = new JSONArray();
				JSONArray bioInputs = function.getJSONArray("input");
//			for each input
				for (int j = 0; j < bioInputs.length(); j++) {
					JSONObject bioInput = bioInputs.getJSONObject(j);
					JSONObject apeInput = new JSONObject();
					JSONArray apeInputTypes = new JSONArray();
					JSONArray apeInputFormats = new JSONArray();
//				add all data types
					for (JSONObject bioType : APEUtils.getListFromJson(bioInput, "data", JSONObject.class)) {
						apeInputTypes.put(bioType.getString("uri"));
					}
					if(apeInputTypes.length() == 0) {
						notAcceptedTools.add(bioJsonTool.getString("biotoolsID"));
						toolsMissingDimension.add(bioJsonTool.getString("biotoolsID"));
						notAcceptedOperations++;
						continue functionloop;
					}
					apeInput.put("data_0006", apeInputTypes);
//				add all data formats (or just the first one)
					for (JSONObject bioType : APEUtils.getListFromJson(bioInput, "format", JSONObject.class)) {
						apeInputFormats.put(bioType.getString("uri"));
					}
					if(apeInputFormats.length() == 0) {
						notAcceptedTools.add(bioJsonTool.getString("biotoolsID"));
						toolsMissingDimension.add(bioJsonTool.getString("biotoolsID"));
						notAcceptedOperations++;
						continue functionloop;
					}
					apeInput.put("format_1915", apeInputFormats);

					apeInputs.put(apeInput);
				}
				apeJsonTool.put("inputs", apeInputs);

//			reading outputs
				JSONArray apeOutputs = new JSONArray();
				JSONArray bioOutputs = function.getJSONArray("output");
//			for each output
				for (int j = 0; j < bioOutputs.length(); j++) {

					JSONObject bioOutput = bioOutputs.getJSONObject(j);
					JSONObject apeOutput = new JSONObject();
					JSONArray apeOutputTypes = new JSONArray();
					JSONArray apeOutputFormats = new JSONArray();
//				add all data types
					for (JSONObject bioType : APEUtils.getListFromJson(bioOutput, "data", JSONObject.class)) {
						apeOutputTypes.put(bioType.getString("uri"));
					}
					if(apeOutputTypes.length() == 0) {
						notAcceptedTools.add(bioJsonTool.getString("biotoolsID"));
						toolsMissingDimension.add(bioJsonTool.getString("biotoolsID"));
						notAcceptedOperations++;
						continue functionloop;
					}
					apeOutput.put("data_0006", apeOutputTypes);
//				add all data formats
					for (JSONObject bioType : APEUtils.getListFromJson(bioOutput, "format", JSONObject.class)) {
						apeOutputFormats.put(bioType.getString("uri"));
					}
					if(apeOutputFormats.length() == 0) {
						notAcceptedTools.add(bioJsonTool.getString("biotoolsID"));
						toolsMissingDimension.add(bioJsonTool.getString("biotoolsID"));
						notAcceptedOperations++;
						continue functionloop;
					}
					apeOutput.put("format_1915", apeOutputFormats);

					apeOutputs.put(apeOutput);
				}
				apeJsonTool.put("outputs", apeOutputs);
				
				// if the tool has outputs add it to the tool annotation
				if(bioOutputs.length() > 0) {
					apeToolsAnnotations.put(apeJsonTool);
				} else {
					notAcceptedTools.add(bioJsonTool.getString("biotoolsID"));
					notAcceptedOperations++;
				}
			}
		}
		System.out.println("Provided bio.tools: " + bioToolsAnnotation.length());
		System.out.println("Errored bio.tools: " + notAcceptedTools.size());
		System.out.println("bio.tools missing a data dimension: " + notAcceptedTools.size());
		System.out.println("Errored bio.tools operations: " + notAcceptedOperations);
		System.out.println("Created APE annotations: " + apeToolsAnnotations.length());
		
		return new JSONObject().put("functions", apeToolsAnnotations);
	}
	
}
