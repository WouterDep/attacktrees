package view.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 *  Simplifies the generation of dialogs.
 * @author Wouter
 *
 */
public class AlertDialog {

	public static void showWarningDialog(String title, String msg){
		Alert warning = new Alert(AlertType.WARNING);
		showDialog(warning, title, msg);
	}
	
	public static void showErrorDialog(String title, String msg){
		Alert warning = new Alert(AlertType.ERROR);
		showDialog(warning, title, msg);
	}
	
	public static void showInformationDialog(String title, String msg){
		Alert warning = new Alert(AlertType.INFORMATION);
		showDialog(warning, title, msg);
	}
	
	private static void showDialog(Alert alert, String title, String msg){
		alert.setHeaderText(null);
		alert.setTitle(title);
		alert.setContentText(msg);
		alert.showAndWait();
	}
}
