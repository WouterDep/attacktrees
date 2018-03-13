package main;

import java.io.IOException;
import java.util.Optional;

import controller.ApplicationController;
import controller.DashboardController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import model.AttackTree;
import model.Attacker;
import view.dialogs.AlertDialog;
/**
 *  Main class that launches the GUI.
 * @author Wouter
 *
 */
public class ApplicationMain extends Application {

	private static AttackTree attackTree;
	private static Attacker attacker;
	private static Stage primaryStage;
	private static boolean toolShowing;
	
	@Override
	public void start(Stage primaryStage) {
		attackTree = new AttackTree();
		attacker =  new Attacker();
		attackTree.setAttacker(attacker);
		attacker.setName("Global");
		attacker.setActive(true);
		Attacker dummy = new Attacker("Default Attacker");
		attackTree.getAttackersMap().put(dummy.getName(),dummy);
		ApplicationMain.primaryStage = primaryStage;
		toolShowing = true;
		showTool();
	}

	/**Show the tool view, with lay-out in view/Application.fxml*/
	public static void showTool(){
		//Load lay-out (.fxml file)
    	FXMLLoader loader= new FXMLLoader();
    	loader.setLocation(ApplicationMain.class.getResource("/view/Application.fxml"));
    	
    	//Create the root scene
    	Parent root =null;
		try {
			root = loader.load();
		} catch (IOException e) {
			AlertDialog.showErrorDialog("Startup error", "An unexpected error occurred: \n"+e.getMessage());
			e.printStackTrace();
		}
    	Scene scene = new Scene(root);
    	toolShowing = true;
    	//get controller from root pane
    	ApplicationController controller = loader.getController();
    	controller.setModel(attackTree);
    	
    	//add observer (the controller) to the model (which is observable)
    	attackTree.addObserver(controller);
    	
    	//prepare primaryStage
    	primaryStage.setTitle("Attack Tree Tool");
    	primaryStage.setScene(scene);
    	primaryStage.getIcons().add(new Image(ApplicationMain.class.getResourceAsStream("/images/icon.png")));
    	primaryStage.setMinHeight(600);
    	primaryStage.setMinWidth(1200);
    	primaryStage.setOnCloseRequest(e -> {
        	e.consume();
        	shutdown();
        });
        primaryStage.show();
	}
	
	/**Show the tool view, with lay-out in view/Dashboard.fxml*/
	public static void showDashboard(){
		//Load lay-out (.fxml file)
    	FXMLLoader loader= new FXMLLoader();
    	loader.setLocation(ApplicationMain.class.getResource("/view/Dashboard.fxml"));
    	
    	//Create the root scene
    	Parent root =null;
		try {
			root = loader.load();
		} catch (IOException e) {
			AlertDialog.showErrorDialog("Startup error", "An unexpected error occurred: \n"+e.getMessage());
			e.printStackTrace();
		}
    	Scene scene = new Scene(root);
    	toolShowing = false;
    	//get controller from root pane
    	DashboardController controller = loader.getController();
    	controller.setModel(attackTree);
    	
    	//add observer (the controller) to the model (which is observable)
    	attackTree.addObserver(controller);
    	
    	//prepare primaryStage
    	primaryStage.setTitle("Attack Tree Dashboard");
    	primaryStage.setScene(scene);
    	primaryStage.getIcons().add(new Image(ApplicationMain.class.getResourceAsStream("/images/icon.png")));
    	primaryStage.setMinHeight(750);
    	primaryStage.setMinWidth(950);
    	primaryStage.setWidth(950);
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	/**Show dialog that asks if the user wants to shutdown the application*/
	private static void shutdown() {
		//Show dialog "Are you sure?"
		Alert confirmation = new Alert(AlertType.CONFIRMATION);
    	confirmation.setTitle("Closing application...");
    	confirmation.setHeaderText(null);
    	confirmation.setContentText("Are you sure you want to shut down the application?");
    	Optional<ButtonType> result= confirmation.showAndWait();
    	if(result.get() == ButtonType.OK){
    		System.exit(0);
    	}
    	else {confirmation.close();}
	}
	
	public static Stage getPrimaryStage(){
		return primaryStage;
	}
	public static boolean isToolShowing(){
		return toolShowing;
	}
}
