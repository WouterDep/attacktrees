package controller;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.text.Text;
import javafx.util.Pair;
import main.ApplicationMain;
import model.AttackTree;
import model.AttackerGoals;
import model.Descriptions;
import model.Heuristics;
import view.TreePrinter;
import view.dialogs.AttackerGoalChooser;
import view.dialogs.AttackerSelector;
import view.dialogs.IDPSelector;
import view.dialogs.TreeLoader;
import view.dialogs.TreeSaver;

/**
 *  Controller of the JavaFX view (application.fxml) 
 * @author Wouter
 *
 */
public class ApplicationController implements Observer{

	private AttackTree attackTree;
	private TreePrinter treePrinter;
	private Heuristics heuristic;

	private IDPParser idpParser;
	private Pair<AttackerGoals,String> attackerGoalPair;

	@FXML private ScrollPane scrollPane; 
	@FXML private ChoiceBox<Heuristics> choiceBoxHeuristic;
	@FXML private RadioMenuItem radioWide;
	@FXML private RadioMenuItem radioNormal;
	@FXML private RadioMenuItem radioSmall;
	@FXML private RadioMenuItem radioBlue;
	@FXML private RadioMenuItem radioRed;
	@FXML private Button findPathBtn;
	@FXML private Label goalLabel;
	@FXML private Button attackerGoalButton;
	@FXML private Button configureAttackerBtn;
	@FXML private TreeView<Text> countermeasureTreeView;
		
	/**Set the attack tree instance created in ApplicationMain to be used here*/
	public void setModel(AttackTree attackTree) {
		this.attackTree = attackTree;
		drawTree();
	}

	/**Read an attack tree (saved as text file) and reconstruct it*/
	@FXML
	public void openSavedTree(){
		System.out.println("AppController:\t openSavedTree");
		TreeLoader treeLoader = new TreeLoader(attackTree);
		treeLoader.loadTree();
		goalLabel.setText(attackTree.getRoot().getName());
	}
	
	/**Save the attack tree and its attackers to a text file*/
	@FXML
	public void saveTree(){
		System.out.println("AppController:\t saveTree");
		TreeSaver treeSaver = new TreeSaver(attackTree);
		treeSaver.saveTree();
	}
	
	/**Save a screenshot of the attack tree to a .png image*/
	@FXML
	public void exportAsPNG(){
		treePrinter.exportAsPNG();
	}
	
	/**Open an IDP file*/
	@FXML
	public void selectIDP(){
		IDPSelector idpSelector = new IDPSelector();
		String idpFile = idpSelector.selectIDP();
		if (idpFile != "") {
			idpParser = new IDPParser(idpFile);
			idpParser.parseIDP();

			radioWide.setDisable(false);
			radioNormal.setDisable(false);
			radioSmall.setDisable(false);
			choiceBoxHeuristic.setDisable(false);
			findPathBtn.setDisable(false);
			attackerGoalButton.setDisable(false);
			configureAttackerBtn.setDisable(false);
			refreshTreeView();
		}
		
	}
	
	/**Select attacker goal and generate corresponding tree*/
	@FXML
	public void chooseAttackerGoal(){
		AttackerGoalChooser goalChooser =  new AttackerGoalChooser(idpParser);
		attackerGoalPair = goalChooser.chooseAttackerGoal();
		if (attackerGoalPair != null) {
			goalLabel.setText(Descriptions.attackerGoals
					.get(attackerGoalPair.getKey()) + " ("	+ attackerGoalPair.getValue() + ")");
			System.out.println("pair: " + attackerGoalPair.getKey() + ", " + attackerGoalPair.getValue());

			TreeGenerator treeGenerator = new TreeGenerator(attackTree, attackerGoalPair, idpParser);
			treeGenerator.generateTree();
			drawTree();
			refreshTreeView();
		}
	}
	
	/**Open a dialog in which the attacker(s) can be modelled and redraw the tree*/
	@FXML
	public void configureAttacker(){
		AttackerSelector attackerSelector = new AttackerSelector(attackTree);
		attackerSelector.showDialog();
		refreshTree();
	}
	
	/**(Re-)calculate the parameters (difficulty, ...) and redraw the tree */
	private void refreshTree(){
		attackTree.recalculateDifficulty();
		attackTree.recalculateStealth();
		drawTree();
	}
	
	/**Mark the path corresponding to the selected heuristic*/
	@FXML
	public void findPath(){

		switch(heuristic){
			case EASIEST:  attackTree.markEasiestPaths(); break;
			case STEALTHIEST:  attackTree.markStealthiesPaths(); break;
			case BOTH: 	attackTree.markEasiestPaths();
									attackTree.markStealthiesPaths(); break;
			default:break;
		}
		drawTree();
	}
	
	/**Draw the JavaFX nodes using the TreePrinter instance*/
	private void drawTree() {
		//System.out.println("AppController:\t drawTree");
		if (treePrinter == null) 	{
			treePrinter = new TreePrinter(attackTree, scrollPane); 
			radioWide.setDisable(false);
			radioNormal.setDisable(false);
			radioSmall.setDisable(false);
			choiceBoxHeuristic.setDisable(false);
			findPathBtn.setDisable(false);
			configureAttackerBtn.setDisable(false);
			
			}
		
		treePrinter.drawTree();
	}
	
	/**Refresh the treeview with the applied countermeasures*/
	private void refreshTreeView() {
		
		
		ArrayList<TreeItem<Text>> cmTreeItems = new ArrayList<>();
		attackTree.getNodeCountermeasureMap().forEach((atnName, cmList) -> {
			attackTree.getAppliedCountermeasures(atnName).forEach((countermeasure) -> {

				TreeItem<Text> tempC = new TreeItem<>(new Text(countermeasure.getName()));
				tempC.getChildren().add(new TreeItem<Text>(new Text("Applied to: " + atnName)));
				tempC.getChildren()
						.add(new TreeItem<Text>(new Text("Description: " + countermeasure.getDescription())));
				tempC.getChildren().add(new TreeItem<Text>(new Text("Complexity: " + countermeasure.getComplexity())));
				cmTreeItems.add(tempC);
			});
		});
		
		attackTree.setAmountAppliedCM(cmTreeItems.size());
		
		TreeItem<Text> rootItem = new TreeItem<>(new Text("Countermeasures"));
		rootItem.getChildren().addAll(cmTreeItems);
		countermeasureTreeView.setRoot(rootItem);
		countermeasureTreeView.setShowRoot(true);
		
			// custom cellfactory for text wrap & tooltip
		countermeasureTreeView.setCellFactory(item -> {
			final Tooltip tooltip = new Tooltip();
			tooltip.setWrapText(true);
			tooltip.setMaxWidth(200);
            TreeCell<Text> treeCell = new TreeCell<Text>() {
                @Override
                protected void updateItem(Text item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !empty){
                        setText(item.getText());
                        tooltip.setText(item.getText());
                    	setTooltip(tooltip);
                    } else {
                        setText("");
                        setTooltip(null);
                        }
                }
            };
            treeCell.prefWidthProperty().bind(countermeasureTreeView.widthProperty().subtract(2.0));
            treeCell.setMaxHeight(50);
            
            treeCell.setStyle("-fx-wrap-text: true;");
            return treeCell;
        });
		
	}
	
	/**Change the width of the tree nodes and redraw*/
	@FXML
	public void changeWidth(){
		if (radioWide.isSelected()) {
			treePrinter.setWidth(200);
		} else if (radioNormal.isSelected()) {
			treePrinter.setWidth(150);
		} else if (radioSmall.isSelected()) {
			treePrinter.setWidth(100);
		}
		treePrinter.drawTree();
	}
	
	/**Collapse all nodes that aren't part of the current attack path*/
	@FXML
	public void collapseGreyNodes(){
		attackTree.getRoot().hideUnmarkedNodes(heuristic);
		treePrinter.drawTree();
	}
	
	/**Switch to the dashboard view*/
	@FXML
	public void showDashboard(){
		ApplicationMain.showDashboard();
	}
	
	/**Initialize the heuristics ChoiceBox and disable unavailable functions*/
	@FXML
	public void initialize() {
		System.out.println("Initializing tool...");
		heuristic = Heuristics.EASIEST; 
		
		// initialize choiceBox Heuristic
		choiceBoxHeuristic.setItems(FXCollections.observableArrayList(Heuristics.values()));
		//choiceBoxHeuristic.setValue(Heuristics.EASIEST);
		choiceBoxHeuristic.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
				System.out.println(Heuristics.values()[newValue.intValue()]);
				heuristic = Heuristics.values()[newValue.intValue()];
				if(treePrinter != null) treePrinter.setHeuristic(heuristic);
			}

		});
		choiceBoxHeuristic.setTooltip(new Tooltip("Select heuristic for analysis"));
		
		//disable all controls that can't be used yet
		boolean treePrinterNullOrIDPFile = (treePrinter == null || !idpParser.hasIDPFile());
		
		radioWide.setDisable(treePrinterNullOrIDPFile);
		radioNormal.setDisable(treePrinterNullOrIDPFile);
		radioSmall.setDisable(treePrinterNullOrIDPFile);
		choiceBoxHeuristic.setDisable(treePrinterNullOrIDPFile);
		findPathBtn.setDisable(treePrinterNullOrIDPFile);
		attackerGoalButton.setDisable(treePrinterNullOrIDPFile);
		configureAttackerBtn.setDisable(treePrinterNullOrIDPFile);
	}
	
	/**Called when the model's (attack tree) setChanged & notifyObservers is called*/
	@Override
	public void update(Observable arg0, Object arg1) {
		refreshTreeView();
		drawTree();
		
	}
}