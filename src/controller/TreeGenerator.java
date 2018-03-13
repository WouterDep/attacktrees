package controller;

import javafx.util.Pair;
import model.AttackTree;
import model.AttackerGoals;
import model.templates.DoS;
import model.templates.ModifyParameter;
import model.templates.ObtainAsset;

/**
 *  Calls the appropriate template according to the attack goal
 * @author Wouter
 *
 */
public class TreeGenerator {
	
	private AttackTree attackTree;
	private  Pair<AttackerGoals, String> attackerGoalPair;
	private IDPParser parser;
	
	public TreeGenerator(AttackTree attackTree, Pair<AttackerGoals, String> attackerGoalPair, IDPParser parser) {
		this.attackTree =  attackTree;
		this.attackerGoalPair = attackerGoalPair;
		this.parser = parser;
	}

	/**Call the root node template, according to the selected goal*/
	public void generateTree() {
		AttackerGoals attackGoal = attackerGoalPair.getKey();
		attackTree.initialize();
		switch (attackGoal) {
		case MODIFY_PARAMETER:
			//attackTree.modifyParameterTemplate(idpFile, attackerGoalPair.getValue(), attackTree.getRoot());
			ModifyParameter modifyTemp = new ModifyParameter(parser, attackerGoalPair.getValue(), attackTree.getRoot(), attackTree);
			modifyTemp.modifyParameterTemplate();
			break;
		case DENIAL_OF_SERVICE:
			//attackTree.DoSTemplate(idpFile, attackerGoalPair.getValue(), attackTree.getRoot());
			DoS dosTemp = new DoS(parser, attackerGoalPair.getValue(), attackTree.getRoot(), attackTree);
			dosTemp.doS();
			break;
		case OBTAIN_ASSET:
			//attackTree.obtainAssetTemplate(idpFile, attackerGoalPair.getValue(), attackTree.getRoot());
			ObtainAsset obtainTemp = new ObtainAsset(parser, attackerGoalPair.getValue(), attackTree.getRoot(), attackTree);
			obtainTemp.obtainAsset();
			break;
		}
		
	}

	
}
