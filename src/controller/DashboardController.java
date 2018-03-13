package controller;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import eu.hansolo.tilesfx.tools.FlowGridPane;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import main.ApplicationMain;
import model.AttackTree;
import model.Attacker;
import view.DashboardRenderer;
import view.dialogs.AttackerConfigurator;
import view.dialogs.AttackerListCell;

public class DashboardController implements Observer {

	@FXML private AnchorPane tilesPane;
	@FXML private FlowPane bestCMFlowPane;
	@FXML private ToggleGroup emphasis;
	@FXML private FlowPane appliedCMFlowPane;
	@FXML private TreeView<String> pathTreeView;
	@FXML private Label attackerPhysicalAccessLabel;
	@FXML private Label attackerCredentialsLabel;
	@FXML private Label attackerCapabilitiesLabel;
	@FXML private ListView<Attacker> dashboardCheckBoxListView;
	@FXML private Label attackerDetailsLabel;
	
	private AttackTree attackTree;
	private char emphasisChar = 'D';
	private int IDcounter=0;
	
    private DashboardRenderer renderer;
	
    /**Set the attack tree instance created in ApplicationMain to be used here*/
	public void setModel(AttackTree attackTree) {
		System.out.println("Set model dashboard");
		this.attackTree = attackTree;
		renderer = new DashboardRenderer(attackTree);
		attackTree.markEasiestPaths();
		renderDashboard();
	}
	
	/**Add an attacker to the set when the equally named button is clicked*/
	@FXML
	public void addAttacker(){
		while (attackTree.getAttackersMap().containsKey("Attacker " + IDcounter)) {
			IDcounter++;
		}
		Attacker temp = new Attacker("Attacker " + IDcounter++);
		attackTree.getAttackersMap().put(temp.getName(), temp);
		
		refresh();
	}

	/**Task that renders all visual components of the dashboard 
	 * in a separate thread.*/
	private void renderDashboard() {
		Task<Void> renderTask = new Task<Void>() {
			@Override
			public Void call() {

				FlowGridPane flow = renderer.renderFlowGrid();
				List<AnchorPane> bestCMPanes = renderer.renderBestCMPanes(emphasisChar);
				List<AnchorPane> appliedCMPanes = renderer.renderAppliedCMPanes();
				TreeItem<String> rootItem = renderer.renderPathTree();
				List<String> attackerLabels = renderer.renderAttackerLabels();
				//List<AttackerSelector.AttackerListCell> attackerCheckBoxes = renderer.renderAttackerCheckBoxes();
				
				Platform.runLater(() -> {
					// Tiles
					tilesPane.getChildren().clear();
					AnchorPane.setLeftAnchor(flow, 5d);
					AnchorPane.setRightAnchor(flow, 5d);
					AnchorPane.setTopAnchor(flow, 5d);
					AnchorPane.setBottomAnchor(flow, 5d);
					tilesPane.getChildren().add(flow);
					
					// Best FlowPane
					bestCMFlowPane.getChildren().clear();
					bestCMFlowPane.getChildren().addAll(bestCMPanes);
					
					// Applied FlowPane
					appliedCMFlowPane.getChildren().clear();
					appliedCMFlowPane.getChildren().addAll(appliedCMPanes);
					
					// TreeView
					pathTreeView.setRoot(rootItem);
					pathTreeView.setShowRoot(false);
					
					// Attacker
						// Global overview
					attackerPhysicalAccessLabel.setText(attackerLabels.get(0));
					attackerCredentialsLabel.setText(attackerLabels.get(1));
					attackerCapabilitiesLabel.setText(attackerLabels.get(2));
					
						// Listview with checkboxes
					ObservableList<Attacker> list = FXCollections.observableArrayList(attackTree.getAttackersMap().values());
					
							// Set cellfactory (for edit and delete button) and add the actions for each button
					dashboardCheckBoxListView.setCellFactory(param -> {
						AttackerListCell cell = new AttackerListCell();
								// Edit button
						cell.getEditButton().setOnAction(e -> {
							AttackerConfigurator atnCfg = new AttackerConfigurator(attackTree,
									attackTree.getAttackersMap().get(cell.getAttacker().getName()));
							atnCfg.showDialog();
							activateAttackers(cell.getListView().getItems());
							cell.getListView().refresh();
							refresh();
						});
								// Delete button
						cell.getDeleteButton().setOnAction(event -> {
							Attacker temp = cell.getAttacker();
							cell.getListView().getItems().remove(temp);
							attackTree.getAttackersMap().remove(temp.getName());
							refresh();
						});
								// CheckBox
						cell.getCheckBox().setOnAction((event) -> {
							cell.getAttacker().setActive(cell.getCheckBox().isSelected());
							refresh();
						});
						return cell;
					});
					dashboardCheckBoxListView.setItems(list);
					
				}); // update UI
				return null;

			}
		};
		Thread th = new Thread(renderTask);
		th.start();
		
	}
	
	/**Navigate to the Tool view with the attack tree*/
	@FXML
	public void showTool(){
		ApplicationMain.showTool();
	}
	
	/**Called whenever setChanged and notifyObserver is called on the attack tree
	 * Refreshes the dashboard if it is visible*/
	@Override
	public void update(Observable arg0, Object arg1) {
		if(!ApplicationMain.isToolShowing()) 	renderDashboard();
	}

	/**Add listeners to the emphasis radiobuttons and list of attackers*/
	@FXML
	public void initialize(){
		// Add listener on radiobuttons for emphasis
		emphasis.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
		      public void changed(ObservableValue<? extends Toggle> ov,
		          Toggle old_toggle, Toggle new_toggle) {
		    	  RadioButton selectedRadioButton = (RadioButton) emphasis.getSelectedToggle();
		    	  String toogleGroupValue = selectedRadioButton.getText();
		    	  System.out.println("Radio selected: "+toogleGroupValue);
		    	  if(toogleGroupValue.equals("Complexity to implement")) emphasisChar = 'C';
		    	  else emphasisChar = 'D';
		    	  bestCMFlowPane.getChildren().clear();
		    	  renderDashboard();
		      }
		    });
		// Add listener on listview with attackers
		dashboardCheckBoxListView.getSelectionModel().selectedItemProperty()
			.addListener((observable, wasSelected, isSelected) -> {
					if (isSelected != null) {
						String selected = isSelected.getName();
						System.out.println("Selected name: " + selected);
						Attacker selectedA = attackTree.getAttackersMap().get(selected);
						attackerDetailsLabel.setText(selectedA.toString());
					}
				});
	}
	
	/**Recalculates the easiest path and refreshes the dashboard*/
	private void refresh(){
		attackTree.recalculateDifficulty();
		attackTree.markEasiestPaths();
		renderDashboard();
	}
	
	/**Activates the attackers from the listview that are selected*/
	private void activateAttackers(List<Attacker> list) {
		for (Attacker at : list) {
			if (!attackTree.getAttackersMap().containsKey(at.getName())) {
				attackTree.getAttackersMap().put(at.getName(), at);
			}
			attackTree.getAttackersMap().get(at.getName()).setActive(at.isActive());
		}
	}
}