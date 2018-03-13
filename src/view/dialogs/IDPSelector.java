package view.dialogs;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import main.ApplicationMain;
import view.dialogs.AlertDialog;

/**
 * Shows dialog with file chooser to select the input .idp file. 
 * @author Wouter
 *
 */
public class IDPSelector {

	public String selectIDP(){
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open FAST-CPS File");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("IDP Files", "*.idp"));
		File selectedFile = fileChooser.showOpenDialog(ApplicationMain.getPrimaryStage());
		String idpString = "";
		if (selectedFile != null) {
			idpString = readInput(selectedFile.getAbsolutePath());
		}
		return idpString;	
		
	}
	
	private static String readInput(String path){
		String content=null;
		try {
			content = new String(Files.readAllBytes(Paths.get(path)));
		} catch (IOException e) {
			AlertDialog.showErrorDialog("File error", "The selected file was invalid. Please try again.");
			e.printStackTrace();
		}
		return content;
	}
}