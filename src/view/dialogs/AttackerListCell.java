package view.dialogs;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import model.Attacker;

/**
 * Modification of JavaFX ListCell, to provide a checkbox and edit and delete buttons on each row
 * @author Wouter
 *
 */

public class AttackerListCell extends ListCell<Attacker> {
	
	private HBox hbox = new HBox();
	private CheckBox checkBox = new CheckBox();
	private Label name = new Label();
	private Button editBtn = new Button();
	private Button deleteBtn = new Button();
	private Region regionFill = new Region();
	private ImageView editImage = new ImageView(new Image("images/edit.png"));
	private ImageView deleteImage = new ImageView(new Image("images/delete.png"));
	private Attacker attacker;

	public AttackerListCell() {
		editImage.setFitWidth(15);
		editImage.setFitHeight(15);
		editBtn.setGraphic(editImage);
		deleteImage.setFitWidth(15);
		deleteImage.setFitHeight(15);
		deleteBtn.setGraphic(deleteImage);
		hbox.getChildren().addAll(checkBox, name, regionFill, editBtn, deleteBtn);
		HBox.setHgrow(regionFill, Priority.ALWAYS);
		hbox.setAlignment(Pos.BASELINE_LEFT);
	}

	@Override
	public void updateItem(Attacker at, boolean empty) {
		this.attacker = at;
		super.updateItem(at, empty);
		if (empty) {
			clearContent();
		} else {
			addContent();
		}
	}

	private void clearContent() {
		setText(null);
		setGraphic(null);
	}

	private void addContent() {
		setText(null);
		checkBox.setSelected(attacker.isActive());
		name.setText(attacker.getName());
		setGraphic(hbox);
	}
	
	// Getters
	public Button getEditButton(){return editBtn;}
	public Button getDeleteButton(){return deleteBtn;}
	public CheckBox getCheckBox(){return checkBox;}
	public Attacker getAttacker() {return attacker;}
	
}