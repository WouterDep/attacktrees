package view.dialogs;


import java.util.ArrayList;
import java.util.Collections;

import controller.IDPParser;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import main.ApplicationMain;
import model.AttackerGoals;
import model.Descriptions;

/**
 *  Shows dialog to choose attacker goal and specific target
 * @author Wouter
 *
 */
public class AttackerGoalChooser {
	
	private AttackerGoals attackerGoal;
	private String goalString;
	private Pair<AttackerGoals,String> returnPair;
	private IDPParser parser;
	private String target;
	private ArrayList<String> targets;


	public AttackerGoalChooser(IDPParser parser) {
		this.parser = parser;
	}

	public Pair<AttackerGoals, String> chooseAttackerGoal(){
		Stage dialog = new Stage();
		dialog.setTitle("Choose attacker goal");
		
		goalString = "";
		
		returnPair = new Pair<>(AttackerGoals.MODIFY_PARAMETER,"Parameter");
		
		// Controls
		Label goalLabel = new Label("Attacker goal:");
		Label targetLabel = new Label("Name target:");
		
		// ChoiceBox parameter/systempart/asset
		ChoiceBox<String> targetChoiceBox = new ChoiceBox<>();
		targetChoiceBox.setMaxWidth(Double.MAX_VALUE);
		targets = null;
		
		targetChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
				// heuristic = Heuristics.values()[newValue.intValue()];
				target = targetChoiceBox.getItems().get(newValue.intValue());
			}
		});
		
		// ChoiceBox Attacker Goal
		
		ChoiceBox<String> goalChoiceBox = new ChoiceBox<>();
		goalChoiceBox.setItems(FXCollections.observableArrayList(Descriptions.attackerGoals.values()));
		goalChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
				//heuristic = Heuristics.values()[newValue.intValue()];
				goalString = goalChoiceBox.getItems().get(newValue.intValue());
				switch (goalString) {
				case "Modify parameter": 							
					attackerGoal = AttackerGoals.MODIFY_PARAMETER;
					
					break;
				case "Denial-of-Service on systempart":	
					attackerGoal = AttackerGoals.DENIAL_OF_SERVICE;	
					break;
				case "Obtain an asset":								
					attackerGoal = AttackerGoals.OBTAIN_ASSET; 				
					break;
				}
				targets = getParameters(attackerGoal);
				targetChoiceBox.setItems(FXCollections.observableArrayList(targets));
			}
		});
		
		
		
		
		// Save button
		Button okButton = new Button("Save ");
		okButton.setDefaultButton(true);
		okButton.setOnAction((event) -> {
			returnPair = new Pair<>(attackerGoal, target);
			// hide dialog
			((Node) (event.getSource())).getScene().getWindow().hide();
		});

		// Cancel button
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction((event) -> ((Node) (event.getSource())).getScene().getWindow().hide());
		cancelButton.setCancelButton(true);

		// add Controls to GridPane
		GridPane gridPane = new GridPane();
		gridPane.add(goalLabel, 0, 0);
		gridPane.add(goalChoiceBox, 1, 0,2,1);
		gridPane.add(targetLabel, 0, 1);
		gridPane.add(targetChoiceBox, 1, 1,2,1);
		gridPane.add(okButton, 1, 2);
		gridPane.add(cancelButton, 2, 2);
		gridPane.setHgap(5);
		gridPane.setVgap(5);
		gridPane.setPadding(new Insets(10));

		
		Scene scene = new Scene(gridPane, gridPane.getMinWidth(), gridPane.getMinHeight());
		dialog.setScene(scene);
		dialog.initOwner(ApplicationMain.getPrimaryStage());
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.showAndWait();
		
		return returnPair;
	}

	public ArrayList<String> getParameters(AttackerGoals attackerGoal){
		ArrayList<String> parameters =  new ArrayList<>();
		switch(attackerGoal){
			case MODIFY_PARAMETER: 
				parser.getControl().forEach((entry) -> parameters.add(entry.getValue()));
				break;
			case DENIAL_OF_SERVICE: 
				Collections.addAll(parameters, parser.getSystemPart());
				break; 
			case OBTAIN_ASSET: 
				parser.getAssetStorage().forEach((entry) -> parameters.add(entry.getValue()));
				break;
			default: break;
		}
		
		return parameters;
	}
}