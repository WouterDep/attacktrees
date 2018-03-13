package view.dialogs;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.ApplicationMain;
import model.AttackTree;
import model.Attacker;

/** Dialog to select active attackers, and delete or add attackers
 * @author Wouter
 *
 */
public class AttackerSelector {

	private AttackTree attackTree;
	private int IDcounter = 0;
	private Label descriptionTextLabel;

	public AttackerSelector(AttackTree attackTree) {
		this.attackTree = attackTree;
	}

	public void showDialog() {
		Stage dialog = new Stage();
		dialog.setTitle("Attacker selection");

		// populate dialog with fields.
		Font titleFont = new Font(20);
		Label attackerTitle = new Label("Attackers");
		attackerTitle.setFont(titleFont);
		Label topLabel = new Label("Select the attackers that cooperate:");

		// Description panel
		Label descriptionLabel = new Label("Description");
		descriptionLabel.setFont(Font.font(15));
		// descriptionLabel.setPadding(new Insets(10, 0, 0, 0));
		descriptionTextLabel = new Label("Click on an attacker to see\n detailed description.\n\n\n\n\n\n");
		// descriptionTextLabel.setWrapText(true);
		descriptionTextLabel.setMaxWidth(Double.MAX_VALUE);
		descriptionTextLabel.setMinWidth(150);
		descriptionTextLabel.setMaxHeight(Double.MAX_VALUE);
		descriptionTextLabel.setAlignment(Pos.TOP_LEFT);
		ScrollPane descriptionScrollPane = new ScrollPane();
		descriptionScrollPane.setMinWidth(200);
		descriptionScrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		descriptionScrollPane.setContent(descriptionTextLabel);

		GridPane.setVgrow(descriptionTextLabel, Priority.ALWAYS);
		GridPane.setHgrow(descriptionScrollPane, Priority.ALWAYS);
		GridPane.setValignment(descriptionTextLabel, VPos.TOP);

		// List view
		ObservableList<Attacker> list = FXCollections.observableArrayList(attackTree.getAttackersMap().values());
		ListView<Attacker> lv = new ListView<>(list);
			// Set cellfactory (for edit and delete button) and add the actions for each button
		lv.setCellFactory(param -> {
			AttackerListCell cell = new AttackerListCell();
					// Edit button
			cell.getEditButton().setOnAction(e -> {
				AttackerConfigurator atnCfg = new AttackerConfigurator(attackTree,
						attackTree.getAttackersMap().get(cell.getAttacker().getName()));
				atnCfg.showDialog();
				// refresh listview and description after edit
				activateAttackers(cell.getListView().getItems());
				cell.getListView().refresh();
				descriptionTextLabel.setText(cell.getAttacker().toString());
			});
					// Delete button
			cell.getDeleteButton().setOnAction(event -> {
				Attacker temp = cell.getAttacker();
				cell.getListView().getItems().remove(temp);
				attackTree.getAttackersMap().remove(temp.getName());
			});
					// CheckBox
			cell.getCheckBox().setOnAction((event) -> {
				cell.getAttacker().setActive(cell.getCheckBox().isSelected());
			});
			return cell;
		});
		
		lv.setPrefHeight(100);
		GridPane.setVgrow(lv, Priority.ALWAYS);
		IDcounter = list.size() + 1;
		// Change description when row is selected (!= checkBox checked!!)
		lv.getSelectionModel().selectedItemProperty().addListener((observable, wasSelected, isSelected) -> {
			String selected = isSelected.getName();
			System.out.println("Selected name: " + selected);
			Attacker selectedA = attackTree.getAttackersMap().get(selected);
			descriptionTextLabel.setText(selectedA.toString());
		});

		// Add button
		Button addBtn = new Button("+ Add...");
		addBtn.setOnAction(e -> {
			while (attackTree.getAttackersMap().containsKey("Attacker " + IDcounter)) {
				IDcounter++;
			}
			Attacker temp = new Attacker("Attacker " + IDcounter++);
			list.add(temp);
			activateAttackers(list);
		});

		// Save button
		Button saveButton = new Button("OK");
		saveButton.setOnAction((event) -> {
			// applyAttackers(attackerListView);
			activateAttackers(list);
			// TreePrinter will apply countermeasures
			((Node) (event.getSource())).getScene().getWindow().hide();
		});
		saveButton.setDefaultButton(true);
		saveButton.setMaxWidth(Double.MAX_VALUE);
		GridPane.setVgrow(saveButton, Priority.ALWAYS);

		// add Controls to GridPane
		GridPane gridPane = new GridPane();
		gridPane.add(attackerTitle, 0, 0);
		gridPane.add(topLabel, 0, 1);
		gridPane.add(lv, 0, 2, 1, 2);
		gridPane.add(descriptionLabel, 1, 2);
		gridPane.add(descriptionScrollPane, 1, 3);
		gridPane.add(addBtn, 0, 4);
		gridPane.add(saveButton, 0, 5, 2, 1);
		gridPane.setPadding(new Insets(10, 10, 10, 10));
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setAlignment(Pos.TOP_LEFT);
		gridPane.setMinHeight(350);
		gridPane.setMinWidth(400);
		Scene scene = new Scene(gridPane);
		dialog.setMinHeight(350);
		dialog.setMinWidth(400);
		dialog.setScene(scene);
		dialog.initOwner(ApplicationMain.getPrimaryStage());
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.showAndWait();
	}

	private void activateAttackers(List<Attacker> list) {
		for (Attacker at : list) {
			if (!attackTree.getAttackersMap().containsKey(at.getName())) {
				attackTree.getAttackersMap().put(at.getName(), at);
			}
			attackTree.getAttackersMap().get(at.getName()).setActive(at.isActive());
		}
	}

}