package view.dialogs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import main.ApplicationMain;
import model.AttackTree;
import model.AttackTreeNode;
import model.Countermeasure;

/** Dialog with all possible and applied countermeasures for the selected node
 * */
public class CountermeasureDialog {

	private AttackTreeNode attackTreeNode;
	private AttackTree attackTree;

	public CountermeasureDialog(AttackTreeNode attackTreeNode, AttackTree attackTree) {
		this.attackTreeNode = attackTreeNode;
		this.attackTree = attackTree;
	}

	public void showDialog() {
		Stage dialog = new Stage();
		dialog.setTitle("Countermeasures");

		// populate dialog with fields.
		Label topLabel = new Label("Select the countermeasures to apply to this node:");
		Label descriptionLabel = new Label("Description");
		descriptionLabel.setFont(new Font(15));
		descriptionLabel.setPadding(new Insets(20,0,0,0));
		Label actualDescriptionLabel = new Label("No description available.");
		actualDescriptionLabel.setWrapText(true);
		actualDescriptionLabel.setAlignment(Pos.TOP_LEFT);
		actualDescriptionLabel.setMaxWidth(250d);
		actualDescriptionLabel.setPrefHeight(150d);
		Label complexityLabel = new Label("Complexity:");
		Label actualComplexityLabel = new Label("Unknown");
		Label difficultyIncreaseLabel = new Label("Difficulty Increase");
		difficultyIncreaseLabel.setFont(new Font(15));
		difficultyIncreaseLabel.setPadding(new Insets(20,0,0,0));
		Label difficultyIncreaseDescription = new Label("");
		difficultyIncreaseDescription.setWrapText(true);
		difficultyIncreaseDescription.setMaxWidth(250d);
		Slider slider = new Slider();
		slider.setMin(0);
		slider.setMax(3);
		slider.setValue(1);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setMajorTickUnit(1);
		slider.setMinorTickCount(0);
		slider.setBlockIncrement(1);
		slider.setDisable(true);
		slider.snapToTicksProperty().set(true);

		// Countermeasure list with CheckBoxes
		ListView<CountermeasurePair> countermeasureListView = new ListView<>();
		// Converter
		StringConverter<CountermeasurePair> countermeasureConverter = new StringConverter<CountermeasurePair>() {
			@Override
			public String toString(CountermeasurePair countermeasurePair) {
				return countermeasurePair.getCountermeasure();
			}

			// not actually used by CheckBoxListCell
			@Override
			public CountermeasurePair fromString(String string) {
				return null;
			}
		};
		// CheckBoxes in every cell
		countermeasureListView
				.setCellFactory(CheckBoxListCell
						.forListView(CountermeasurePair::hasCountermeasureProperty, countermeasureConverter));
		// Populate listView
			// Create Map<countermeasure.name, deepcopy(countermeasure)>, to be able to get a countermeasure by name
		Map<String, Countermeasure> countermeasureMap = new HashMap<>();
		//attackTreeNode.getCountermeasures().forEach((counter) -> countermeasureMap.put(counter.getName(), new Countermeasure(counter))); 
		attackTree.getNodeCountermeasureMap().get(attackTreeNode.getName()).forEach((counter) -> countermeasureMap.put(counter.getName(), new Countermeasure(counter)));
		for (Entry<String, Countermeasure> entry : countermeasureMap.entrySet()) {
			countermeasureListView.getItems().add(new CountermeasurePair(entry.getKey(), entry.getValue().isApplied()));
		}
			// Change description when row is selected (!= checkBox checked!!)
		countermeasureListView.getSelectionModel().selectedItemProperty()
			.addListener((observable, wasSelected, isSelected) -> {
				String selected = isSelected.getCountermeasure();
				Countermeasure selectedC = countermeasureMap.get(selected);
				actualDescriptionLabel.setText(selectedC.getDescription());
				actualComplexityLabel.setText(selectedC.getComplexity().toString());
				slider.setDisable(false);
				slider.setValue(selectedC.getDifficultyIncrease());
		});
		countermeasureListView.setPrefHeight(countermeasureMap.size() * 24 + 2); // a row is 24px high and 2px margin
		countermeasureListView.setPrefWidth(200d);
		
		// Slider
		String[] difficultyIncreases = {"No influence","Slightly more difficult","More difficult", "A lot more difficult"};
		slider.valueProperty().addListener((observable, oldValue, newValue) -> {
		    //System.out.println("Slider Value Changed (newValue: " + newValue.intValue() + ")");
		    difficultyIncreaseDescription.setText(difficultyIncreases[newValue.intValue()]);
		    String selectedC = countermeasureListView.getSelectionModel().getSelectedItem().getCountermeasure();
		    countermeasureMap.get(selectedC).setDifficultyIncrease((int) slider.getValue());
		});

		
		
		// Save button
		Button saveButton = new Button("Save");
		saveButton.setOnAction((event) -> {
				saveSelectedCountermeasures(countermeasureListView, countermeasureMap);
				// TreePrinter will apply countermeasures
				((Node) (event.getSource())).getScene().getWindow().hide();}
		);
		saveButton.setDefaultButton(true);
		saveButton.setMaxWidth(Double.MAX_VALUE);

		// add Controls to GridPane
		GridPane gridPane = new GridPane();
		gridPane.add(topLabel, 0, 0,2,1);
		gridPane.add(countermeasureListView, 0, 1,2,1);
		gridPane.add(descriptionLabel, 0, 2,2,1);
		gridPane.add(actualDescriptionLabel, 0, 3,2,1);
		gridPane.add(complexityLabel, 0, 4);
		gridPane.add(actualComplexityLabel, 1, 4);
		gridPane.add(difficultyIncreaseLabel, 0, 5);
		gridPane.add(difficultyIncreaseDescription, 0, 6);
		gridPane.add(slider, 0, 7);
		gridPane.add(saveButton, 0,8,2,1);
		gridPane.setHgap(5);
		gridPane.setVgap(5);
		gridPane.setPadding(new Insets(10));

		Scene scene = new Scene(gridPane, gridPane.getPrefWidth(), gridPane.getPrefHeight());
		dialog.setScene(scene);
		dialog.initOwner(ApplicationMain.getPrimaryStage());
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.showAndWait();
	}
	

	private void saveSelectedCountermeasures(ListView<CountermeasurePair> listView, Map<String, Countermeasure> map){
		// 1) apply changes to deepcopy map
		//reset to unapplied
		map.forEach((key, value) -> value.setApplied(false));
		//set on true if selected
		ObservableList<CountermeasurePair>selectedCountermeasures = listView.getItems().filtered(CountermeasurePair::hasCountermeasure);
		Iterator<CountermeasurePair> iterator =  selectedCountermeasures.iterator();
		while(iterator.hasNext()){
			map.get(iterator.next().getCountermeasure()).setApplied(true);
		}
		//2) save changes to originals
		attackTree.getNodeCountermeasureMap().get(attackTreeNode.getName()).forEach(countermeasure -> {
			countermeasure.setApplied(map.get(countermeasure.getName()).isApplied());
			countermeasure.setDifficultyIncrease(map.get(countermeasure.getName()).getDifficultyIncrease());
		});
		/*
		attackTreeNode.getCountermeasures().forEach(countermeasure -> {
			countermeasure.setApplied(map.get(countermeasure.getName()).isApplied());
			countermeasure.setDifficultyIncrease(map.get(countermeasure.getName()).getDifficultyIncrease());
		});*/
		
	}
	
	// Data type <String, Boolean> that holds the countermeasures that are possible
		private static class CountermeasurePair {
			private StringProperty countermeasureProperty = new SimpleStringProperty();
			private final BooleanProperty hasCountermeasureProperty = new SimpleBooleanProperty();

			public CountermeasurePair(String countermeasure, boolean hasCountermeasure) {
				setCountermeasure(countermeasure);
				setHasCountermeasure(hasCountermeasure);
			}

			// Values
			public String getCountermeasure() {	return countermeasureProperty.get();}
			public void setCountermeasure(String countermeasure) {this.countermeasureProperty.set(countermeasure);}

			public boolean hasCountermeasure() {return hasCountermeasureProperty.get();}
			public void setHasCountermeasure(boolean hasCountermeasure) {this.hasCountermeasureProperty.set(hasCountermeasure);}

			// Properties
			@SuppressWarnings("unused")
			public StringProperty countermeasureProperty() {return countermeasureProperty;	}

			public BooleanProperty hasCountermeasureProperty() {return hasCountermeasureProperty;}
		}
	
}
