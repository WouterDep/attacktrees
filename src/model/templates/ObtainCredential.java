package model.templates;

import java.util.Map.Entry;

import controller.IDPParser;
import model.AttackTree;
import model.AttackTreeNode;
import model.Complexity;
import model.Countermeasure;
import model.Descriptions;
import model.Operation;

/**
 *  Obtain(Credential) template
 * @author Wouter
 *
 */
public class ObtainCredential {

	private IDPParser parser;
	private String credential;
	private AttackTreeNode node;
	private AttackTree attackTree;
	
	public ObtainCredential(IDPParser parser, String credential, AttackTreeNode node, AttackTree attackTree) {
		this.parser = parser;
		this.credential = credential;
		this.node = node;
		this.attackTree = attackTree;
	}

	public void obtainCredential(){
	// root of this template has operation OR
			node.setOperation(Operation.OR);
			
			// find a user who owns this credential
			String user = "";
			for (Entry<String, String> entry : parser.getHasToken()) {
				// HasToken = {user, password}
				if (entry.getValue().equals(credential)) {
					user = entry.getKey();
				}
			}
			
			// add attacker leaf for one user u which owns the credential
			AttackTreeNode stealfromuser = new AttackTreeNode("Steal " + credential + " from user " + user);
			attackTree.addCountermeasureToNode(new Countermeasure("Install Anti-Virus",
					Descriptions.countermeasures.get("Install Anti-Virus"),Complexity.LOW, 2), stealfromuser);
			attackTree.addCountermeasureToNode(new Countermeasure("Strong Passwords",
					Descriptions.countermeasures.get("Strong Passwords"),Complexity.LOW, 1), stealfromuser);
			attackTree.addCountermeasureToNode(new Countermeasure("Change default passwords",
					Descriptions.countermeasures.get("Change default passwords"),Complexity.LOW, 1), stealfromuser);
			attackTree.addCountermeasureToNode(new Countermeasure("VPN for remote connection",
					Descriptions.countermeasures.get("VPN for remote connection"),Complexity.MEDIUM, 1), stealfromuser);
			attackTree.addCountermeasureToNode(new Countermeasure("Alert/delay on unsuccessful login",
					Descriptions.countermeasures.get("Alert/delay on unsuccessful login"),Complexity.LOW, 1), stealfromuser);
			attackTree.addCountermeasureToNode(new Countermeasure("Encrypted tunnel",
					Descriptions.countermeasures.get("Encrypted tunnel"),Complexity.MEDIUM, 2), stealfromuser);
			attackTree.addCountermeasureToNode(new Countermeasure("Disable LM hash",
					Descriptions.countermeasures.get("Disable LM hash"),Complexity.MEDIUM, 1), stealfromuser);
			stealfromuser.setStealth(2);
			node.addChild(stealfromuser);
			attackTree.addNode(stealfromuser);
			
			// node for obtain template
			AttackTreeNode obtain = new AttackTreeNode("Obtain(" + credential + ")");
			node.addChild(obtain);
			attackTree.addNode(obtain);
			ObtainAsset obtainAssetTemplate = new ObtainAsset(parser,credential,obtain,attackTree);
			obtainAssetTemplate.obtainAsset();
	}

}
