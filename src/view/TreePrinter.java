package view;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import main.ApplicationMain;
import model.AttackTree;
import model.AttackTreeNode;
import model.Heuristics;
import model.Operation;
import view.dialogs.AboutNodeDialog;
import view.dialogs.AlertDialog;
import view.dialogs.CountermeasureDialog;
import view.dialogs.DeleteNodeDialog;
import view.dialogs.EditNodeDialog;

/**
 * Class that draws the tree on the canvas using JavaFX
 * 
 * @author Wouter
 *
 */
public class TreePrinter {

	private AttackTree attackTree;
	private Pane pane;
	private ScrollPane scrollPane;
	private int mostRightX;
	private int width;
	private Color easyColor = Color.valueOf("#003c96");
	private Color stealthyColor = Color.valueOf("#ce0000");
	private Color bothColor = Color.valueOf("#aa00af");
	private Heuristics heuristic;
	private int [] maxHeightPerLevel;
	private int canvasHeight;
	private Image defenseImage;
	private Image collapseImage;
	private Image expandImage;
	private ArrayList<Node> lines;
	private HashMap<Integer,AnchorPane> nodesMap; //Map(ID, AnchorPane)
	
	private boolean debug =  false;

	public TreePrinter(AttackTree attackTreeOriginal, ScrollPane scrollPane) {
		this.attackTree = attackTreeOriginal; //shallow copy (for actually Edit and Delete nodes)
		//this.attackTree = new AttackTree(attackTreeOriginal); //deepcopy (for "hiding (i.e. deleting)" children
		this.scrollPane = scrollPane;
		width = 150;
		
	}

	/**
	 *  Draws the tree on the Pane. The method recursiveDrawLeft() fills the nodesMap 
	 *  by going through the tree from left to right (recursively) and creating the nodes (and their appropriate X-coordinates).
	 *  The graphical representation is created in createNode(). Subsequently the height is optimized and then everything 
	 *  is drawn on the pane
	 */
	public void drawTree() {
		
		mostRightX = 0;
		canvasHeight =0;
		//pane.requestLayout();
		scrollPane.setContent(null);
		pane = new Pane();
		scrollPane.setContent(pane);
		
		// Global images
		collapseImage = new Image("images/collapse.png");
		expandImage = new Image("images/expand.png");
		defenseImage = new Image("images/defense.png");
		
		if(debug) System.out.println("Depth:  "+attackTree.getDepth());
		maxHeightPerLevel = new int[attackTree.getDepth()];
		for(int i =0; i<maxHeightPerLevel.length;i++) maxHeightPerLevel[i] = 0;
		nodesMap = new HashMap<>();
		lines = new ArrayList<>();
		
		// call draw method
		recursiveDrawLeft(attackTree.getRoot()); // start with root
		// optimalize height of each level
		optimalizeHeight();
		// draw lines using the recalculated heights
		drawLines();
		// add nodes to actual pane
		pane.getChildren().addAll(lines);
		pane.getChildren().addAll(nodesMap.values());
		pane.setMinWidth(mostRightX);
		pane.setMinHeight(canvasHeight);
	}

	/**
	 * 	Recursive method that draws the nodes from left to right. Every next node is placed
	 *  to the right of the imaginary vertical line of the right border of the previous node.
	 * @param atn Node to be drawn
	 */
	private void recursiveDrawLeft(AttackTreeNode atn) {
		if(debug) System.out.println("recursiveDraw: " + atn.getName());

		for (AttackTreeNode n : atn.getChildren()) {
			if (n.isVisible()) {
				recursiveDrawLeft(n);

				// Draw leftmost leaf to the right of the rightmost node on this level
				if (n.getOperation() == Operation.LEAF || !n.areChildrenVisible()) { 

					AnchorPane nodePane = createNodePane(n);
					// calculate and set x-coordinates
					int level = n.getLevel();
					int x1 = mostRightX;
					int x2 = x1 + width + 10;
					n.setX1(x1);
					n.setX2(x2);

					if (debug)
						System.out.println("DRAW CHILD " + n.getName() + ", level:" + level + ", x1:" + x1 + ", x2:" + x2);

					// adjust the mostRight array
					mostRightX = x2;
					if (debug)	System.out.println("\t mostrightx: " + mostRightX);

					// draw on the correct coordinates
					nodePane.setTranslateX(x1);
					int y1 = (level - 1) * 100 + 10;
					nodePane.setTranslateY(y1);
					n.setY1(y1);
					// nodes.add(nodePane);
					nodesMap.put(n.getIdentifier(), nodePane);
				}
			}
		}

		// Draw parent (all children of atn were drawn)
		if (atn.getOperation() != Operation.LEAF && atn.isVisible() && atn.areChildrenVisible() && !atn.getChildren().isEmpty()) {
			// it must be a parent, not a leaf (if for-loop is skipped when
			// called on a leaf, this check is necessary)
			drawParent(atn);
	
		}
	}

	/**
	 * 	Draw parent by finding the leftmost and rightmost child and placing
	 * 	itself on the middle above them.
	 * @param parent The node that is a parent
	 */
	private void drawParent(AttackTreeNode parent){
		AnchorPane nodePane = createNodePane(parent);
		int level = parent.getLevel();
		int x1 = parent.getParent().getX1();
		int x2 = parent.getParent().getX2();
		int y1 = (level - 1) * 100 + 10;
		int y2 = (int) (y1 + nodePane.getMinHeight());
		
		// find leftmost and rightmost child if they are visible
		if (parent.areChildrenVisible()) {
			int x1LeftmostChild = Integer.MAX_VALUE;
			int x2RightmostChild = 0;
			for (AttackTreeNode n : parent.getChildren()) {
				if (n.isVisible()) {
					if (n.getX1() < x1LeftmostChild)
						x1LeftmostChild = n.getX1();
					if (n.getX2() > x2RightmostChild)
						x2RightmostChild = n.getX2();
				}
			}

			// calculate temporary coordinates (y will change)
			x1 = (x1LeftmostChild + x2RightmostChild) / 2 - (width + 10) / 2;
			x2 = x1 + width + 10;
		}
		
		//set coordinates 
		parent.setX1(x1);
		parent.setX2(x2);
		parent.setY1(y1);
		parent.setY2(y2);
		
		// draw on correct coordinates (= translate from origin in top-left corner)
		nodePane.setTranslateX(x1);
		nodePane.setTranslateY(y1);
		
		if(debug) System.out.println("DRAW PARENT " + parent.getName() + " level:" + level+", x1:"+x1+", x2:"+x2+", y1:"+y1);
		nodesMap.put(parent.getIdentifier(), nodePane);
	}
	
	/**Reduce height of all levels, based on the largest node on each level*/
	private void optimalizeHeight(){
		if(debug) System.out.println("OPTIMALIZE HEIGHT");
		if(debug ) for(int i = 0;i<maxHeightPerLevel.length; i++) System.out.print(maxHeightPerLevel[i]+", ");
		
		for(int l = 2; l<= attackTree.getDepth();l++) {
			//level i
			if(debug) System.out.println("\t level "+l);
			for(Entry<Integer,AnchorPane> entry : nodesMap.entrySet()){
				AttackTreeNode tempNode = attackTree.getNodes().get(entry.getKey());
				if(tempNode.getLevel() == l && tempNode.isVisible()) {
					if(debug) System.out.println("\t \t "+tempNode.getName()+" is on this level");
					if(debug) System.out.println("\t \t Parents y1: "+tempNode.getParent().getY1()+" maxHeight parent: "+maxHeightPerLevel[l-2]);
					//new y1 = old y1 line of the parent + highest uncle + margin 
					int newy1 = tempNode.getParent().getY1()+maxHeightPerLevel[l-2]+30;
					int newy2 = (int) (newy1+entry.getValue().getMinHeight());
					tempNode.setY1(newy1);
					tempNode.setY2(newy2);
					entry.getValue().setTranslateY(newy1);
					if(newy2 > canvasHeight) canvasHeight = newy2 + 10;
				}
			}
		}
		
	}
	
	/**  Draw lines (after height was optimalized)*/
	private void drawLines(){
		for (AttackTreeNode node : attackTree.getNodes().values()) {
			if (!node.getChildren().isEmpty() && node.isVisible() && node.areChildrenVisible()) { // it's a parent
				// draw lines from parent to children
				if (debug)
					System.out.println("DRAW LINE from " + node.getName() + " to:");
				for (AttackTreeNode child : node.getChildren()) {
					if (debug)
						System.out.println("\t " + child.getName());
					Line line = new Line();
					
					if (child.isEasy() && (heuristic == Heuristics.EASIEST || heuristic == Heuristics.BOTH)) {
						if (child.isStealthy() && heuristic == Heuristics.BOTH) line.setStroke(bothColor);		
						 else line.setStroke(easyColor);	
					} else {
						if (child.isStealthy() && (heuristic == Heuristics.STEALTHIEST || heuristic == Heuristics.BOTH)){
							line.setStroke(stealthyColor);
						} else line.setStroke(Color.DARKGREY);
					}
					line.setStartX((node.getX1() + node.getX2()) / 2);
					line.setStartY(node.getY2());
					line.setEndX((child.getX1() + child.getX2()) / 2);
					line.setEndY(child.getY1());
					lines.add(line);

				}
			}
		}
	}
	
	/**
	 *  Creates the visual representation of the AttackTreeNode. It consists of an AnchorPane
	 *  with labels for name, operation and parameters. A rectangle surrounds these labels.
	 * @param atn node for which a visual representation must be generated
	 * @return anchorpane
	 */
	private AnchorPane createNodePane(AttackTreeNode atn) {
		if(debug) System.out.println("Create Node for "+ atn.getName()+", level: "+atn.getLevel());
		String nodeString = atn.getName();
		int textWrapWidth = width-10;

		// 1) Label with name/description of node
		Label nodeLabel = new Label(nodeString);
		nodeLabel.setWrapText(true);
		nodeLabel.setMaxWidth(textWrapWidth);

		// 2) Rectangle around everything
		Rectangle r = new Rectangle();
		r.setFill(Color.WHITE);
		r.setStrokeType(StrokeType.OUTSIDE);
		Tooltip tt = new Tooltip("Not part of path");
		if(atn.isEasy() && (heuristic == Heuristics.EASIEST || heuristic == Heuristics.BOTH)){
			if(atn.isStealthy() && heuristic == Heuristics.BOTH) {r.setStroke(bothColor);tt.setText("Both");}
			 else {r.setStroke(easyColor);tt.setText("Easy");}
		} else {
			if(atn.isStealthy() && (heuristic == Heuristics.STEALTHIEST || heuristic == Heuristics.BOTH)) {
				r.setStroke(stealthyColor);tt.setText("Stealthy");
				}
			 else r.setStroke(Color.DARKGREY);
		}
		
		r.setX(5);
		r.setY(0);
		r.setArcHeight(10);
		r.setArcWidth(10);
		r.setWidth(textWrapWidth + 10);

			// calculate the height of the rectangle
		Text theText = new Text(nodeLabel.getText());
		theText.setFont(nodeLabel.getFont());
		theText.setWrappingWidth(textWrapWidth);
		double height = theText.getBoundsInLocal().getHeight();
		r.setHeight(height + 25);

		// 4) Label with parameters: difficulty & stealth
		Label parametersLabel = new Label();
		String parametersString = "" + atn.getTotalDifficulty()+" "+atn.getTotalStealth();
		parametersLabel.setText(parametersString);
		parametersLabel.setTextFill(Color.DIMGREY);

		// 5) Label with operation: AND/OR/Leaf
		Label operationLabel = new Label();
		String operationString = "TEST";

		switch (atn.getOperation()) {
		case OR: operationString = "OR"; break;
		case AND: operationString = "AND"; break;
		default: 	operationString = ""; r.getStrokeDashArray().addAll(5d,5d); break;
		}
		operationLabel.setText(operationString);
		operationLabel.setTextFill(Color.MIDNIGHTBLUE);
		
		// 6) Button expand/collapse
		
		Button expandCollapseButton = new Button();
		expandCollapseButton.setStyle(
                "-fx-min-width: 15px; " +
                "-fx-min-height: 15px; " +
                "-fx-max-width: 15px; " +
                "-fx-max-height: 15px;"
        );
		
			// image collapse/expand & tooltip
		
		ImageView imageView =  new ImageView();
		imageView.setFitWidth(12);
		imageView.setFitHeight(12);
		Tooltip tooltip = new Tooltip();
		if (atn.areChildrenVisible()) {
			imageView.setImage(collapseImage);
			tooltip.setText("Collapse children");
		} else {
			imageView.setImage(expandImage);
			tooltip.setText("Expand children");
		}
		expandCollapseButton.setGraphic(imageView);
        expandCollapseButton.setTooltip(tooltip);
        
        	// action
        expandCollapseButton.setOnAction((event)-> {
        		// toggle visibility of the children of this node
  				atn.toggleVisibilityChildren();
  				drawTree();
        });
        if(atn.getChildren().isEmpty() || atn == attackTree.getRoot()) expandCollapseButton.setVisible(false);    
        
        // defense icon
        
        imageView =  new ImageView(defenseImage);
        imageView.setFitWidth(12);
		imageView.setFitHeight(12);
        Button defenseButton = new Button();
        defenseButton.setStyle(
                "-fx-min-width: 15px; " +
                "-fx-min-height: 15px; " +
                "-fx-max-width: 15px; " +
                "-fx-max-height: 15px;"+
               " -fx-background-color: transparent;"
        );
        defenseButton.setGraphic(imageView);
        defenseButton.setOnAction((event) -> {
        	CountermeasureDialog countermeasureDialog = new CountermeasureDialog(atn, attackTree);
        	countermeasureDialog.showDialog();
        	attackTree.recalculateDifficulty();
        	drawTree();
        });
        if(!attackTree.getNodeCountermeasureMap().containsKey(atn.getName())) defenseButton.setDisable(true);
        
		// Create anchorpane with calculated height
		AnchorPane node = new AnchorPane();
		node.setMinHeight(height + 25);
		node.setOnMouseEntered(e -> r.setStrokeWidth(2.0));
		node.setOnMouseExited(e -> r.setStrokeWidth(1.0)); 
		Tooltip.install(node, tt);
		// Align all shapes to the appropriate edges
		AnchorPane.setBottomAnchor(parametersLabel, 2d);
		AnchorPane.setLeftAnchor(parametersLabel, 10d);
		AnchorPane.setBottomAnchor(defenseButton, 5d);
		AnchorPane.setLeftAnchor(defenseButton, 25d);
		AnchorPane.setTopAnchor(nodeLabel, 2d);
		AnchorPane.setLeftAnchor(nodeLabel, 10d);
		AnchorPane.setBottomAnchor(operationLabel, 2d);
		AnchorPane.setLeftAnchor(operationLabel, 2d);
		AnchorPane.setRightAnchor(operationLabel, 2d);
		AnchorPane.setBottomAnchor(expandCollapseButton, 5d);
		AnchorPane.setRightAnchor(expandCollapseButton, 5d);
		operationLabel.setAlignment(Pos.BASELINE_CENTER);
		// Add shapes to the pane
		node.getChildren().addAll(r, nodeLabel, parametersLabel, operationLabel,expandCollapseButton, defenseButton);
		
		// Add ContextMenu
		addContextMenuToNode(node, atn);
		
		// Check largest height on this level
		int nodeHeight = (int) node.getMinHeight();
		int maxHeight = maxHeightPerLevel[atn.getLevel()-1];
		if(nodeHeight > maxHeight) maxHeightPerLevel[atn.getLevel()-1] = nodeHeight;
	
		return node;
	}

	/** Creates a snapshot of the current visualization of the tree. A FileChooser shows 
	 *  up to let the user decide where to save the image. The extension is .PNG. */
	public void exportAsPNG() {
		
		//Create a high resolution snapshot of entire pane, not just the viewport.
		//Therefore the size of the pane is multiplied by pixelScale and the snapParameters are transformed.
		double pixelScale = 2.0;
		WritableImage writableImage = new WritableImage((int)Math.round(pixelScale*pane.getWidth()), (int)Math.round(pixelScale*pane.getHeight()));
	    SnapshotParameters snapParameters = new SnapshotParameters();
	    snapParameters.setTransform(Transform.scale(pixelScale, pixelScale));
		try {
			pane.snapshot(snapParameters, writableImage);
			
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save PNG snapshot");
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			fileChooser.getExtensionFilters().addAll(new ExtensionFilter("PNG File", "*.png"));
			fileChooser.setInitialFileName("AttackTree.png");
			File selectedFile = fileChooser.showSaveDialog(ApplicationMain.getPrimaryStage());
			if (selectedFile != null) {
				ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", selectedFile);
			}
		} catch (IOException e) {
			AlertDialog.showErrorDialog("Export error", "Tree could not be exported. " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			AlertDialog.showErrorDialog("Snapshot too large", "The tree is to large to be snapshotted. Please collapse nodes.");
		} 
	}

	/**
	 *  Adds a ContextMenu if the user right clicks on a node with the options Edit and Delete
	 * @param node Visual representation of the node
	 * @param atn Back-end AttackTreeNode
	 */
	private void addContextMenuToNode(AnchorPane node, AttackTreeNode atn){
		ContextMenu contextMenu = new ContextMenu();
		 
		// Edit
        MenuItem itemEdit = new MenuItem("Edit Node...");
        itemEdit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	//show dialog that edits the original node (not the deep copy)
            	EditNodeDialog editDialog = new EditNodeDialog(atn);
            	editDialog.showDialog();
            	attackTree.recalculateDifficulty();
            	attackTree.recalculateStealth();  //TODO stealth delta! avoid erasure in parent nodes
            	drawTree();
            }
        });
        
        // Delete
        MenuItem itemDelete = new MenuItem("Delete Node...");
        itemDelete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	DeleteNodeDialog deleteDialog = new DeleteNodeDialog(attackTree, atn.getIdentifier()); 
            	deleteDialog.showDialog();
            	drawTree();
            }
        });
 
        // Separator
        SeparatorMenuItem separator = new SeparatorMenuItem();
        
        // About this node
        MenuItem itemAbout = new MenuItem("See details...");
        itemAbout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	AboutNodeDialog aboutDialog = new AboutNodeDialog(atn);
            	aboutDialog.showDialog();
            }
        });
        
        // Countermeasures
        MenuItem itemCountermeasures = new MenuItem("Select countermeasures...");
        itemCountermeasures.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	CountermeasureDialog countermeasureDialog = new CountermeasureDialog(atn, attackTree);
            	countermeasureDialog.showDialog();
            	attackTree.recalculateDifficulty();
            }
        });
        if(!attackTree.getNodeCountermeasureMap().containsKey(atn.getName())) itemCountermeasures.setDisable(true);
        
        contextMenu.getItems()
        	.addAll(itemEdit, itemDelete,separator,itemAbout,itemCountermeasures);
 
        // Show ContextMenu on right click
        node.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                contextMenu.show(node, event.getScreenX(), event.getScreenY());
            }
        });
	}
	

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeuristic(Heuristics h){
		this.heuristic = h;
	}
	
}