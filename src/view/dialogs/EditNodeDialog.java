package view.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.ApplicationMain;
import model.AttackTreeNode;
import model.Descriptions;
import model.Operation;

/**
 *  Dialog to edit the fields of a node
 * @author Wouter
 *
 */
public class EditNodeDialog {

	private AttackTreeNode originalNode;
	private AttackTreeNode deepCopyNode;

	public EditNodeDialog(AttackTreeNode originalNode) {
		this.originalNode = originalNode;
		deepCopyNode = new AttackTreeNode(originalNode);
	}
	
	public void showDialog(){
		Stage dialog = new Stage();
		dialog.setTitle("Edit Node");
		// populate dialog with controls.

		Label nameLabel = new Label("Name:");
		TextField nameField = new TextField(deepCopyNode.getName());
		Label operationsLabel = new Label("Operation: ");
			// Difficulty
		Label difficultyLabel = new Label("Difficulty:");
		difficultyLabel.setFont(new Font(13));
		difficultyLabel.setPadding(new Insets(10,0,0,0));
		int initDiff = deepCopyNode.getDifficulty()+deepCopyNode.getDifficultyEdit();
		if(initDiff > 4) initDiff = 4;
		if(initDiff < 1) initDiff = 1;
		Label difficultyIncreaseDescription = new Label(Descriptions.difficulties.get(initDiff));
		difficultyIncreaseDescription.setPadding(new Insets(0,0,25,0));
		
		Slider difficultySlider = new Slider();
		difficultySlider.setMin(1);
		difficultySlider.setMax(4);
		
		difficultySlider.setValue(initDiff);
		difficultySlider.setShowTickLabels(true);
		difficultySlider.setShowTickMarks(true);
		difficultySlider.setMajorTickUnit(1);
		difficultySlider.setMinorTickCount(0);
		difficultySlider.setBlockIncrement(1);
		difficultySlider.snapToTicksProperty().set(true);
		difficultySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
		    //System.out.println("Slider Value Changed (newValue: " + newValue.intValue() + ", slidervalue: "+(int) difficultySlider.getValue());
		    difficultyIncreaseDescription.setText(Descriptions.difficulties.get(newValue.intValue()));
		    int delta = newValue.intValue() - deepCopyNode.getDifficulty();
		    deepCopyNode.setDifficultyEdit(delta); 
		});
		
			// Stealth
		Label stealthLabel = new Label("Stealth:");
		stealthLabel.setFont(new Font(13));
		stealthLabel.setPadding(new Insets(10,0,0,0));
		Label stealthDescription = new Label(Descriptions.stealths.get(deepCopyNode.getTotalStealth()));
		stealthDescription.setPadding(new Insets(0,0,25,0));
		
		Slider stealthSlider = new Slider();
		stealthSlider.setMin(1);
		stealthSlider.setMax(3);
		stealthSlider.setValue(deepCopyNode.getTotalStealth());
		stealthSlider.setShowTickLabels(true);
		stealthSlider.setShowTickMarks(true);
		stealthSlider.setMajorTickUnit(1);
		stealthSlider.setMinorTickCount(0);
		stealthSlider.setBlockIncrement(1);
		stealthSlider.snapToTicksProperty().set(true);
		stealthSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
		    stealthDescription.setText(Descriptions.stealths.get(newValue.intValue()));
		    int delta = newValue.intValue() - deepCopyNode.getStealth();
		    deepCopyNode.setStealthEdit(delta); 
		});

		
		//Operations
		ChoiceBox<Operation> operationChoiceBox = new ChoiceBox<>();
		operationChoiceBox.setMaxWidth(Double.MAX_VALUE);
		ArrayList<Operation> operations = new ArrayList<>(Arrays.asList(Operation.values()));
		operationChoiceBox.setItems(FXCollections.observableArrayList(operations));
		operationChoiceBox.setValue(deepCopyNode.getOperation());
		operationChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
				System.out.println("operation: " + newValue);
				deepCopyNode.setOperation(operations.get(newValue.intValue()));
			}
		});
		
		//Save button
		Button saveButton =  new Button("Save changes");
		saveButton.setDefaultButton(true);
		saveButton.setOnAction((event) -> {
			System.out.println("SAVE");
			deepCopyNode.setName(nameField.getText());
			alertIfOperationIsSetToLeaf();
			saveChanges();
			//hide dialog
			((Node)(event.getSource())).getScene().getWindow().hide();
		});
		
		//Cancel button
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction((event) ->
			((Node)(event.getSource())).getScene().getWindow().hide());
		cancelButton.setCancelButton(true);
		
		//add Controls to GridPane
		GridPane gridPane = new GridPane();
		gridPane.add(nameLabel, 0, 0,1,1);
		gridPane.add(nameField, 1, 0,3,1);
		gridPane.add(difficultyLabel, 0, 1);
		gridPane.add(difficultySlider, 0, 2,2,1);
		gridPane.add(difficultyIncreaseDescription, 2, 2);
		gridPane.add(stealthLabel, 0, 3);
		gridPane.add(stealthSlider, 0, 4,2,1);
		gridPane.add(stealthDescription, 2, 4);
		gridPane.add(operationsLabel, 0, 5);
		gridPane.add(operationChoiceBox, 1, 5,3,1);
		gridPane.add(saveButton, 0, 6);
		gridPane.add(cancelButton, 1, 6);
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setPadding(new Insets(10));
		
		Scene scene = new Scene(gridPane, 260, 300);
		dialog.setScene(scene);
		dialog.initOwner(ApplicationMain.getPrimaryStage());
		dialog.initModality(Modality.APPLICATION_MODAL); 
		dialog.showAndWait();
	}
	
	private void saveChanges(){
		originalNode.setDifficultyEdit(deepCopyNode.getDifficultyEdit());
		originalNode.setName(deepCopyNode.getName());
		originalNode.setOperation(deepCopyNode.getOperation());
		originalNode.setStealthEdit(deepCopyNode.getStealthEdit());
	}
	
	private void alertIfOperationIsSetToLeaf(){
		if(deepCopyNode.getOperation() == Operation.LEAF && originalNode.getOperation() != Operation.LEAF){
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Operation set to leaf");
			alert.setHeaderText("The operation of this node will be set to 'leaf'.");
			alert.setContentText("All children of this node will be deleted. Are you sure?");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
			    originalNode.getChildren().clear();
			}
		}
	}
	
}
