package view.dialogs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import main.ApplicationMain;
import model.AttackTree;
import model.AttackTreeNode;
import model.Attacker;
import model.Countermeasure;

/**
 * Utility class for saving functionality. The tree is serialized into a JSON
 * array in a text file (name and location are chosen by user)
 * 
 * @author Wouter
 *
 */
public class TreeSaver {
	private AttackTree attackTree;

	public TreeSaver(AttackTree attackTree) {
		this.attackTree = attackTree;
	}

	public void saveTree() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Tree");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt"));
		fileChooser.setInitialFileName("AttackTree.txt");
		File selectedFile = fileChooser.showSaveDialog(ApplicationMain.getPrimaryStage());
		if (selectedFile != null) {

			// make sure extension is set to .txt
			String oldFilename = selectedFile.toString();
			int indexExtension = oldFilename.indexOf(".");
			String filename;
			if (indexExtension > 0) {
				filename = oldFilename.substring(0, indexExtension) + ".txt";
			} else {
				filename = oldFilename + ".txt";
			}

			writeJsonToFile(new File(filename));
		}

	}

	/**
	 * Writes entire json-string to file
	 * @param file outputfile
	 */
	@SuppressWarnings("unchecked")
	private void writeJsonToFile(File file) {
		JSONObject attackerTreeCM = new JSONObject();
		JSONArray jsonAttackers = writeAttackersToJson();
		JSONArray jsonTree = writeTreeToJson();
		JSONArray jsonCountermeasures = writeCountermeasuresToJson();
		
		attackerTreeCM.put("attackers", jsonAttackers);
		attackerTreeCM.put("tree", jsonTree);
		attackerTreeCM.put("countermeasuremap", jsonCountermeasures);
		
		try (FileWriter fw = new FileWriter(file)) {

			fw.write(attackerTreeCM.toJSONString());
			fw.flush();

		} catch (IOException e) {
			AlertDialog.showErrorDialog("Saving Error",
					"File could not be saved. An unexpected error occurred: " + e.getMessage());
			e.printStackTrace();
		}
	}


	@SuppressWarnings("unchecked")
	private JSONArray writeAttackersToJson() {

		JSONArray jsonAttackers = new JSONArray();
		for (Attacker at : attackTree.getAttackersMap().values()) {
			JSONObject jsonAttacker = writeAttackerToJson(at);
			jsonAttackers.add(jsonAttacker);
		}
		return jsonAttackers;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject writeAttackerToJson(Attacker attacker){
		JSONObject jsonAttacker = new JSONObject();
		
		JSONObject capabilities = new JSONObject();
		jsonAttacker.put("name", attacker.getName());
		jsonAttacker.put("active", attacker.isActive());
		for(Entry<String, Integer> entry : attacker.getCapabilities().entrySet()){
			capabilities.put(entry.getKey(), entry.getValue());
		}
		jsonAttacker.put("capabilities", capabilities);
		jsonAttacker.put("access", new ArrayList<>(attacker.getAccess()));
		jsonAttacker.put("credentials", new ArrayList<>(attacker.getCredentials()));

		return jsonAttacker;
	}
	
	/**
	 * Create JSON String with a list of nodes
	 * 
	 * @return List of nodes in JSON format
	 */
	@SuppressWarnings("unchecked")
	private JSONArray writeTreeToJson() {
		// Write tree in JSON format
		JSONArray jsonTree = new JSONArray(); 
		for (AttackTreeNode atn : attackTree.getNodes().values()) {
			JSONObject jsonNode = writeNodeToJson(atn);
			jsonTree.add(jsonNode);
		}
		return jsonTree;
	}

	/** Create JSON String with representation of a node
	 * @param atn   AttackTreeNode that needs to be written in JSON format
	 * @return JSON String of the node
	 */
	@SuppressWarnings("unchecked") // Doesn't know type in advance
	private JSONObject writeNodeToJson(AttackTreeNode atn) {
		JSONObject jsonNode = new JSONObject();
		jsonNode.put("ID", atn.getIdentifier());
		jsonNode.put("depth", atn.getLevel());
		jsonNode.put("operation", atn.getOperation().toString());
		jsonNode.put("name", atn.getName());
		jsonNode.put("difficulty", atn.getDifficulty());
		jsonNode.put("difficultyEdit", atn.getDifficultyEdit());
		jsonNode.put("difficultyCM", atn.getDifficultyCM());
		jsonNode.put("stealth", atn.getStealth());
		jsonNode.put("stealthEdit", atn.getStealthEdit());
		jsonNode.put("parent", atn.getParent().getIdentifier());
		
		return jsonNode;
	}
	
	@SuppressWarnings("unchecked")
	private JSONArray writeCountermeasuresToJson() {
		// Write Node->Countermeasures in JSON format
		JSONArray jsonAtnCMMap = new JSONArray(); 
		for (Entry<String, List<Countermeasure>> entry : attackTree.getNodeCountermeasureMap().entrySet()) {
			// entry = atn.name -> List<CM>
			JSONObject atn = new JSONObject();
			atn.put("appliedto", entry.getKey());
			JSONArray jsonCMs = new JSONArray(); 
			for(Countermeasure cm : entry.getValue()){
				JSONObject c = new JSONObject();
				c.put("name", cm.getName());
				c.put("complexity", cm.getComplexity().toString());
				c.put("applied", cm.isApplied());
				c.put("difficultyIncrease", cm.getDifficultyIncrease());
				jsonCMs.add(c);
			}
			atn.put("countermeasures", jsonCMs);
			jsonAtnCMMap.add(atn);
		}
		return jsonAtnCMMap;
	}
	
}
