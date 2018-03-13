package view.dialogs;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.ApplicationMain;
import model.AttackTreeNode;
import model.Descriptions;

/**
 *  Pop-up dialog displaying details of the selected node
 * @author Wouter
 *
 */
public class AboutNodeDialog {

	private AttackTreeNode attackTreeNode;

	public AboutNodeDialog(AttackTreeNode attackTreeNode) {
		this.attackTreeNode = attackTreeNode;
	}

	public void showDialog() {
		Stage dialog = new Stage();
		dialog.setTitle("Details");

		// populate dialog with fields.
		Label nameLabel = new Label("Name:");
		Label difficultyLabel = new Label("Difficulty:");
		Label stealthLabel = new Label("Stealth:");
		Label childrenLabel = new Label("Number of Children: ");
		Label parentLabel = new Label("Parent:");
		Label levelLabel = new Label("Level: ");

		Label atnNameLabel = new Label(attackTreeNode.getName());
		Label atnDifficultyLabel = new Label(attackTreeNode.getTotalDifficulty()+": "+Descriptions.difficulties.get(attackTreeNode.getTotalDifficulty()));
		Label atnStealthLabel = new Label(attackTreeNode.getTotalStealth()+": "+Descriptions.stealths.get(attackTreeNode.getTotalStealth()));
		Label atnChildrenLabel = new Label("" + attackTreeNode.getChildren().size());
		Label atnParentLabel = new Label(attackTreeNode.getParent().getName());
		Label atnLevelLabel = new Label(""+attackTreeNode.getLevel());

		// OK button
		Button okButton = new Button("OK");
		okButton.setOnAction((event) -> ((Node) (event.getSource())).getScene().getWindow().hide());
		okButton.setDefaultButton(true);

		// add Controls to GridPane
		GridPane gridPane = new GridPane();
		gridPane.add(nameLabel, 0, 0);
		gridPane.add(atnNameLabel, 1, 0);
		gridPane.add(difficultyLabel, 0, 1);
		gridPane.add(atnDifficultyLabel, 1, 1);
		gridPane.add(stealthLabel, 0, 2);
		gridPane.add(atnStealthLabel, 1, 2);
		gridPane.add(childrenLabel, 0, 3);
		gridPane.add(atnChildrenLabel, 1, 3);
		gridPane.add(parentLabel, 0, 4);
		gridPane.add(atnParentLabel, 1, 4);
		gridPane.add(levelLabel, 0, 5);
		gridPane.add(atnLevelLabel, 1, 5);
		gridPane.add(okButton, 1,6 );
		gridPane.setHgap(5);
		gridPane.setVgap(5);
		gridPane.setPadding(new Insets(5));

		Scene scene = new Scene(gridPane, gridPane.getMinWidth(), gridPane.getMinHeight());
		dialog.setScene(scene);
		dialog.initOwner(ApplicationMain.getPrimaryStage());
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.showAndWait();
	}

}
