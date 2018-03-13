package view.dialogs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import main.ApplicationMain;
import model.AttackTree;
import model.Attacker;

/**
 * Utility class for the loading functionality of a saved tree. The user selects
 * the .txt file and the tree is regenerated (parsed JSON).
 * 
 * @author Wouter
 *
 */
public class TreeLoader {

	private AttackTree attackTree;

	public TreeLoader(AttackTree attackTree) {
		this.attackTree = attackTree;
	}

	public void loadTree() {

		System.out.println("TreeLoader:\t loadTree called");
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Tree File");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt"));
		File selectedFile = fileChooser.showOpenDialog(ApplicationMain.getPrimaryStage());
		if (selectedFile != null) {
			String jsonString = readInput(selectedFile.getAbsolutePath());
			attackTree.buildTreeFromJson(jsonString);
			readAttackersFromJson(jsonString);
		}
	}

	private String readInput(String path) {
		String content = null;
		try {
			content = new String(Files.readAllBytes(Paths.get(path)));
		} catch (IOException e) {
			AlertDialog.showErrorDialog("File error", "The selected file was invalid. Please try again.");
			e.printStackTrace();
		}
		return content;
	}

	@SuppressWarnings("unchecked")
	private void readAttackersFromJson(String json) {
		attackTree.getAttackersMap().clear();
		JSONParser parser = new JSONParser();

		JSONObject jsonInput;
		try {
			jsonInput = (JSONObject) parser.parse(json);
			JSONArray jsonAttackers = (JSONArray) jsonInput.get("attackers");
			Iterator<JSONObject> iterator = jsonAttackers.iterator();
			JSONObject tempJson;
			Attacker tempAt;

			while (iterator.hasNext()) {
				tempJson = iterator.next();
				tempAt = new Attacker();
				tempAt.initializeFromJsonObject(tempJson);
				attackTree.getAttackersMap().put(tempAt.getName(), tempAt);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}
}