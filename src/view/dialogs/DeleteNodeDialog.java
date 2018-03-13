package view.dialogs;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import model.AttackTree;
import model.AttackTreeNode;

/**
 *  Warning message before actual removal of the selected node and its children
 * @author Wouter
 *
 */
public class DeleteNodeDialog {

	private AttackTreeNode deleteThisNode;
	private AttackTree attackTree;
	
	public DeleteNodeDialog(AttackTree attackTree, int id) {
		this.attackTree = attackTree;
		this.deleteThisNode = attackTree.getNodes().get(id);
	}
	
	public void showDialog(){
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Delete node");
		alert.setHeaderText("This node and all its children will be deleted..");
		alert.setContentText("Are you sure?");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK){
		    attackTree.deleteNode(deleteThisNode);
		} else {
		    // ... user chose CANCEL or closed the alert
		}
	}
	
	
}
