package view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import eu.hansolo.tilesfx.Section;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.Tile.TextSize;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.events.TileEventListener;
import eu.hansolo.tilesfx.tools.FlowGridPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import model.AttackTree;
import model.AttackTreeNode;
import model.Countermeasure;
import model.Descriptions;
import model.Heuristics;

public class DashboardRenderer {

	private AttackTree attackTree;
	
	private Tile difficultyDonutTile;
	private Tile stealthDonutTile;
	private Tile gaugeDiffTile;
	private Tile gaugeScoreTile;

	private ChartData       chartDiff1;
    private ChartData       chartDiff2;
    private ChartData       chartDiff3;
    private ChartData       chartDiff4;
    private ChartData       chartStealth1;
    private ChartData       chartStealth2;
    private ChartData       chartStealth3;
	private static final    double TILE_WIDTH  = 200;
    private static final    double TILE_HEIGHT = 200;
    
	public DashboardRenderer(AttackTree attackTree) {
		this.attackTree = attackTree;
	}
    
	//----------- TILES 
	
	/**
	 *  Render responsive tiles with dashboard information: difficulty distribution, total difficulty, 
	 *  security score and stealth distribution
	 * @return FlowGridPane (TilesFX type) with the tiles
	 */
	public FlowGridPane renderFlowGrid(){
		/*
		// 0) Attacker overview
		chartAttackerSpoof = new BarChartItem("Spoof Protocol",
				attackTree.getAttacker().getCapabilities().get("Spoof Protocol"), Tile.BLUE);
		chartAttackerExploit = new BarChartItem("Exploit Vulnerability",
				attackTree.getAttacker().getCapabilities().get("Exploit Vulnerability"), Tile.BLUE);
		chartAttackerDiscover = new BarChartItem("Discover Vulnerability",
				attackTree.getAttacker().getCapabilities().get("Discover Vulnerability"), Tile.BLUE);
		chartAttackerObtain = new BarChartItem("Obtain Credentials",
				attackTree.getAttacker().getCapabilities().get("Obtain Credentials"), Tile.BLUE);

		barChartTile = TileBuilder.create()
				.prefSize(TILE_WIDTH, TILE_HEIGHT)
				.skinType(SkinType.BAR_CHART)
				.title("Attacker")
				.textSize(TextSize.BIGGER)
				.maxValue(4)
				.barChartItems(chartAttackerSpoof, chartAttackerExploit, chartAttackerDiscover, chartAttackerObtain)
				.text(attackTree.getAttacker().getCredentials().size()+" credentials | "+attackTree.getAttacker().getAccess().size()+" accesses")
				.decimals(0).build();
		*/
		// 1) Donut Difficulty Distribution
		int [] amountPerDifficulty = attackTree.getAmountPerDifficulty();
		
		chartDiff1 = new ChartData(Descriptions.difficulties.get(1), amountPerDifficulty[0], Tile.RED);
		chartDiff2 = new ChartData(Descriptions.difficulties.get(2), amountPerDifficulty[1], Tile.ORANGE);
		chartDiff3 = new ChartData(Descriptions.difficulties.get(3), amountPerDifficulty[2], Tile.YELLOW);
		chartDiff4 = new ChartData(Descriptions.difficulties.get(4), amountPerDifficulty[3], Tile.GREEN);

		difficultyDonutTile = TileBuilder.create()
				.skinType(SkinType.DONUT_CHART)
				.prefSize(TILE_WIDTH+50, TILE_HEIGHT)
				.title("Node Difficulty Distribution")
				.textSize(TextSize.BIGGER)
				.chartData(chartDiff1, chartDiff2, chartDiff3, chartDiff4)
				.text(attackTree.getAmountAppliedCM() + " countermeasures")
				.build();
		
		difficultyDonutTile.addTileEventListener(new TileEventListener() {
			@Override
			public void onTileEvent(TileEvent arg0) {
				if (arg0 != null && TileEvent.EventType.SELECTED_CHART_DATA == arg0.getEventType()) {
					System.out.println("Donut, value: " + arg0.getData().getValue());
					difficultyDonutTile.setTooltipText(arg0.getData().getName());
				}
			}
		});
		
		// 2) Total score
		gaugeScoreTile = TileBuilder.create()
                .skinType(SkinType.GAUGE)
                .prefSize(TILE_WIDTH, TILE_HEIGHT)
                .title("Security Score")
                .textSize(TextSize.BIGGER)
                .minValue(0)
                .highlightSections(true)
                .decimals(1)
                .maxValue(3)
                .unit("")
                .build();
		gaugeScoreTile.setValue(attackTree.getSecurityScore());
		gaugeScoreTile.setSections(new Section(0,1,Tile.LIGHT_RED), 
				new Section(1,2,Tile.YELLOW),
				new Section(2,3,Tile.GREEN));
		gaugeScoreTile.setSectionsVisible(true);
		gaugeScoreTile.setMinMeasuredValueVisible(true);
		gaugeScoreTile.setGradientStops(new Stop(0.0, Tile.LIGHT_RED),
                               new Stop(0.33, Tile.LIGHT_RED),
                               new Stop(0.33,Tile.YELLOW),
                               new Stop(0.67, Tile.YELLOW),
                               new Stop(0.67, Tile.GREEN),
                               new Stop(1.0, Tile.GREEN));
		
		// 3) Total difficulty
		gaugeDiffTile = TileBuilder.create()
				.skinType(SkinType.GAUGE)
				.prefSize(TILE_WIDTH, TILE_HEIGHT)
				.title("Goal difficulty")
				.textSize(TextSize.BIGGER)
				.minValue(0)
				.decimals(0)
				.maxValue(4)
				.highlightSections(true)
				.unit("")
				.build();
		gaugeDiffTile.setValue(attackTree.getRoot().getTotalDifficulty());

		gaugeDiffTile.setSections(new Section(0, 1, Tile.LIGHT_RED), new Section(1, 2, Tile.ORANGE),
				new Section(2, 3, Tile.YELLOW), new Section(3, 4, Tile.GREEN));
		gaugeDiffTile.setSectionsVisible(true);
		gaugeDiffTile.setMinMeasuredValueVisible(false);
		gaugeDiffTile.setGradientStops(new Stop(0.0, Tile.LIGHT_RED), new Stop(0.25, Tile.LIGHT_RED),
				new Stop(0.25, Tile.ORANGE), new Stop(0.50, Tile.ORANGE), new Stop(0.50, Tile.YELLOW),
				new Stop(0.75, Tile.YELLOW), new Stop(0.75, Tile.GREEN), new Stop(1.0, Tile.GREEN));
		
	

		// 4) Donut Stealth Distribution
		int [] amountPerStealth = attackTree.getAmountPerStealth();
		
		chartStealth1 = new ChartData(Descriptions.stealths.get(1), amountPerStealth[0], Tile.GREEN);
		chartStealth2 = new ChartData(Descriptions.stealths.get(2), amountPerStealth[1], Tile.YELLOW);
		chartStealth3 = new ChartData(Descriptions.stealths.get(3), amountPerStealth[2], Tile.RED);

		stealthDonutTile = TileBuilder.create()
				.skinType(SkinType.DONUT_CHART)
				.prefSize(TILE_WIDTH+50, TILE_HEIGHT)
				.title("Node Stealth Distribution")
				.textSize(TextSize.BIGGER)
				.chartData(chartStealth1, chartStealth2, chartStealth3)
				.build();
		
		// 5) Number of countermeasures
		/*
		numberTile = TileBuilder.create()
                .skinType(SkinType.NUMBER)
                .prefSize(TILE_WIDTH, TILE_HEIGHT)
                .title("Countermeasures")
                .value(attackTree.getAmountAppliedCM())
                .unit("")
                .description("Countermeasures applied")
                .descriptionAlignment(Pos.TOP_LEFT)
                .decimals(0)
                .textVisible(true)
                .build();
		*/
		
		FlowGridPane flowGridPane = new FlowGridPane(5, 1,  difficultyDonutTile, gaugeScoreTile, gaugeDiffTile, stealthDonutTile);
		
		flowGridPane.setHgap(5);
		flowGridPane.setVgap(5);
		flowGridPane.setCenterShape(true);
		flowGridPane.setAlignment(Pos.TOP_CENTER);
		flowGridPane.setPadding(new Insets(5));
		//flowGridPane.setMaxSize(1000, 100);
		//flowGridPane.setPrefSize(800, 600);
		flowGridPane.setBackground(new Background(new BackgroundFill(Color.GREY, CornerRadii.EMPTY, Insets.EMPTY)));


		return flowGridPane;
	}

	//------------- BEST CM PANES 
	
	/**
	 *  Render panes with best possible countermeasures.
	 * @param emphasisChar: input of the radio buttons
	 * @return List of panes
	 */
	public List<AnchorPane> renderBestCMPanes(char emphasisChar) {
		System.out.println("called with "+emphasisChar);
		List<AnchorPane> panes = new ArrayList<>();
		attackTree.getBestCountermeasures(Heuristics.EASIEST, emphasisChar).forEach((entry) ->{
			//System.out.println("Best CM for ["+atnName+"] is "+cm.getName()+", diff inc: "+cm.getDifficultyIncrease()+", cmpx: "+cm.getComplexity());
			panes.add(createCMPane(entry.getKey(), entry.getValue()));
		});
		
		return panes;
	}
    
	/**
	 * Renders a Countermeasure pane. The strokeColor is related to the 
	 *  goal difficulty if the countermeasure would be applied. The CheckBox allows the user
	 *  to directly apply the countermeasure.
	 * @param diffAndAtnName e.g. "4#GainAccessToX#2.5", with 4 the goaldifficulty  if applied
	 * and 2.5 the security score of the tree if applied
	 * @param cm
	 * @return
	 */
	private AnchorPane createCMPane(String diffAndAtnName, Countermeasure cm){
		int width = 220;
		Label cmNameLabel = new Label(cm.getName());
		cmNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white");
		cmNameLabel.setWrapText(true);
		cmNameLabel.setMaxWidth(width-30);
		
		String totalGoalDiff = diffAndAtnName.split("#")[0];
		String atnName = diffAndAtnName.split("#")[1];
		String totalScore = diffAndAtnName.split("#")[2];
		
		Label atnLabel = new Label("Applied to: "+atnName);
		atnLabel.setStyle("-fx-text-fill: #dadada");
		atnLabel.setFont(new Font(9));
		atnLabel.setWrapText(true);
		atnLabel.setMaxWidth(width-20);
		
		Label cmDiffAndScoreLabel = new Label("Diff.: "+totalGoalDiff+" | Score: "+totalScore);
		cmDiffAndScoreLabel.setStyle("-fx-text-fill: #dadada");
		Label cmComplexityLabel = new Label("Compl.: "+cm.getComplexity());
		cmComplexityLabel.setStyle("-fx-text-fill: #dadada");
			
		Rectangle r = new Rectangle();
		r.setFill(Color.valueOf("#232323"));
		Color strokeColor = Color.BLACK;
		switch(totalGoalDiff){
		case "1": strokeColor = Color.valueOf("#ff2600"); break; // Red
		case "2": strokeColor = Color.valueOf("#ff8300");  break; // Orange
		case "3": strokeColor = Color.YELLOW; break;
		case "4": strokeColor = Color.valueOf("#6eff00"); break; // Green
		}
		r.setStroke(strokeColor);
		r.setStrokeType(StrokeType.INSIDE);
		r.setStrokeWidth(1.0);
		r.setArcHeight(15);
		r.setArcWidth(15);
		r.setHeight(80);
		r.setWidth(width);
		
		CheckBox applyCheckBox = new CheckBox("");
		applyCheckBox.setOnAction(e -> {
			boolean selected = applyCheckBox.isSelected();
			System.out.println("checbox selected: "+selected+" on cm: "+cm.getName());
			List<Countermeasure> temp =  attackTree.getNodeCountermeasureMap().get(atnName);
			temp.get(temp.indexOf(cm)).setApplied(selected);
			attackTree.recalculateDifficulty();
			attackTree.markEasiestPaths();
		});
		applyCheckBox.setStyle("-fx-opacity: 0.60");

		Tooltip tooltip = new Tooltip(cm.getDescription());
		tooltip.setWrapText(true);
		tooltip.setMaxWidth(200);
		
		AnchorPane node = new AnchorPane();
		node.setOnMouseEntered(e -> r.setStrokeWidth(2.0));
		node.setOnMouseExited(e -> r.setStrokeWidth(1.0)); 
		Tooltip.install(node, tooltip);
		
		AnchorPane.setTopAnchor(cmNameLabel, 5d);
		AnchorPane.setLeftAnchor(cmNameLabel, 10d);
		AnchorPane.setLeftAnchor(atnLabel, 10d);
		AnchorPane.setTopAnchor(atnLabel, 20d);
		AnchorPane.setLeftAnchor(cmDiffAndScoreLabel, 10d);
		AnchorPane.setBottomAnchor(cmDiffAndScoreLabel, 5d);
		AnchorPane.setRightAnchor(cmComplexityLabel, 10d);
		AnchorPane.setBottomAnchor(cmComplexityLabel, 5d);
		AnchorPane.setRightAnchor(applyCheckBox, 3d);
		AnchorPane.setTopAnchor(applyCheckBox, 5d);
		
		node.setMinHeight(80);
		node.minWidth(width);
		node.getChildren().addAll(r,cmNameLabel,atnLabel,cmDiffAndScoreLabel,cmComplexityLabel, applyCheckBox);
		
		return node;
	}

	//-------------- APPLIED CM PANES
	/**
	 *  Render panes with the applied countermeasures.
	 * @return List of panes
	 */
	public List<AnchorPane> renderAppliedCMPanes() {
		List<AnchorPane> panes = new ArrayList<>();
		
		attackTree.getNodeCountermeasureMap().forEach((atnName, cmList) -> {
			attackTree.getAppliedCountermeasures(atnName).forEach((countermeasure) -> {
				panes.add(createAppliedCMPane(atnName,countermeasure));
			});
		});
		
		return panes;
	}

	/**
	 *  Renders a Countermeasure pane. The CheckBox enables the user to disable 
	 *  this countermeasure
	 * @param atnName Name of the attack tree node it's applied to
	 * @param cm Countermeasure to create a pane for
	 * @return
	 */
	private AnchorPane createAppliedCMPane(String atnName, Countermeasure cm) {
		int width = 220;
		Label cmNameLabel = new Label(cm.getName());
		cmNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white");
		cmNameLabel.setWrapText(true);
		cmNameLabel.setMaxWidth(width-30);
		
		Label atnLabel = new Label("Applied to: "+atnName);
		atnLabel.setStyle("-fx-text-fill: #dadada");
		atnLabel.setFont(new Font(9));
		atnLabel.setWrapText(true);
		atnLabel.setMaxWidth(width-20);
		
		Label cmDiffLabel = new Label("Diff. incr.: "+cm.getDifficultyIncrease());
		cmDiffLabel.setStyle("-fx-text-fill: #dadada");
		Label cmComplexityLabel = new Label("Compl.: "+cm.getComplexity());
		cmComplexityLabel.setStyle("-fx-text-fill: #dadada");
		
		Rectangle r = new Rectangle();
		r.setFill(Color.valueOf("#232323"));
		r.setStroke(Color.DEEPSKYBLUE);
		r.setStrokeType(StrokeType.INSIDE);
		r.setArcHeight(15);
		r.setArcWidth(15);
		r.setHeight(80);
		r.setWidth(width);
		
		CheckBox appliedCheckBox = new CheckBox("");
		appliedCheckBox.setSelected(true);
		appliedCheckBox.setOnAction(e -> {
			boolean selected = appliedCheckBox.isSelected();
			System.out.println("checbox selected: "+selected+" on cm: "+cm.getName());
			List<Countermeasure> temp =  attackTree.getNodeCountermeasureMap().get(atnName);
			temp.get(temp.indexOf(cm)).setApplied(selected);
			attackTree.recalculateDifficulty();
			attackTree.markEasiestPaths();
		});
		appliedCheckBox.setStyle("-fx-opacity: 0.60");

		Tooltip tooltip = new Tooltip(cm.getDescription());
		tooltip.setWrapText(true);
		tooltip.setMaxWidth(200);
		
		AnchorPane node = new AnchorPane();
		node.setOnMouseEntered(e -> r.setStrokeWidth(2.0));
		node.setOnMouseExited(e -> r.setStrokeWidth(1.0)); 
		Tooltip.install(node, tooltip);
		
		AnchorPane.setTopAnchor(cmNameLabel, 5d);
		AnchorPane.setLeftAnchor(cmNameLabel, 10d);
		AnchorPane.setLeftAnchor(atnLabel, 10d);
		AnchorPane.setTopAnchor(atnLabel, 20d);
		AnchorPane.setLeftAnchor(cmDiffLabel, 10d);
		AnchorPane.setBottomAnchor(cmDiffLabel, 5d);
		AnchorPane.setRightAnchor(cmComplexityLabel, 10d);
		AnchorPane.setBottomAnchor(cmComplexityLabel, 5d);
		AnchorPane.setRightAnchor(appliedCheckBox, 3d);
		AnchorPane.setTopAnchor(appliedCheckBox, 5d);
		
		node.setMinHeight(80);
		node.minWidth(width);
		node.getChildren().addAll(r,cmNameLabel,atnLabel,cmDiffLabel,cmComplexityLabel, appliedCheckBox);
		
		return node;
	}
	
	//---------------  PATH TREE VIEW
	/**Renders tree view with the attack path*/
	public TreeItem<String> renderPathTree(){
		TreeItem<String> rootItem = new TreeItem<>(attackTree.getRoot().getName());
		rootItem.getChildren().addAll(getPathTreeItems(attackTree.getRoot()));
		
		return rootItem;
	}
	
	/**
	 *  Recursive method that returns a list with the TreeItem of the parameter, and of all the 
	 *  children that are part of the path.
	 * @param node
	 * @return
	 */
	private List<TreeItem<String>> getPathTreeItems(AttackTreeNode node){
		List<TreeItem<String>> answer = new ArrayList<>();
		TreeItem<String> treeItem;
		if (node.isParthOfPath(Heuristics.EASIEST)) {
			treeItem = new TreeItem<String>(node.getName()+" [diff: "+node.getTotalDifficulty()+"]");
			answer.add(treeItem);
			for (AttackTreeNode atn : node.getChildren()) {
				treeItem.getChildren().addAll(getPathTreeItems(atn));
			}
		}
		return answer;
	}

	//--------------- ATTACKER
	
	/**
	 *  Returns list of Strings with access of attacker, credentials and capabilities
	 * @return
	 */
	public List<String> renderAttackerLabels() {
		List<String> answer = new ArrayList<>();
		
		String accessString = Arrays.toString(attackTree.getAttacker().getAccess().toArray());
		accessString = accessString.substring(1, accessString.length()-1);
		answer.add(accessString);
		
		String credentialString = Arrays.toString(attackTree.getAttacker().getCredentials().toArray());
		credentialString = credentialString.substring(1, credentialString.length()-1);
		answer.add(credentialString);
		
		String capabilitiesString = "";
		for(Entry<String, Integer> entry : attackTree.getAttacker().getCapabilities().entrySet()){
			capabilitiesString += entry.getKey()+": "+Descriptions.capabilityLevels.get(entry.getValue())+"\n";
		}
		answer.add(capabilitiesString);
		
		return answer;
	}
	
}
