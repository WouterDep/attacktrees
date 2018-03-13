package model.templates;

import model.AttackTree;
import model.AttackTreeNode;
import model.Complexity;
import model.Countermeasure;
import model.Descriptions;
import model.Operation;

/**
 * DiscoverDoS(SystemPart) template
 * @author Wouter
 *
 */
public class DiscoverDoS {


	private String systemPart;
	private AttackTreeNode node;
	private AttackTree attackTree;
	
	public DiscoverDoS(String systemPart, AttackTreeNode node, AttackTree attackTree) {
		this.systemPart = systemPart;
		this.node = node;
		this.attackTree = attackTree;
	}

	public void discoverDoS(){
		// root of this template has operation AND
		node.setOperation(Operation.AND);
		
		// vulnerability children
		AttackTreeNode discover = new AttackTreeNode("Discover zero-day DoS vulnerability in system part " + systemPart);
		attackTree.addCountermeasureToNode(new Countermeasure("Disable unused ports & services",
				Descriptions.countermeasures.get("Disable unused ports & services"), Complexity.MEDIUM,2), discover);
		attackTree.addCountermeasureToNode(new Countermeasure("Input validation: bounds checking",
				Descriptions.countermeasures.get("Input validation: bounds checking"),Complexity.MEDIUM,2), discover);
		discover.setStealth(2);
		node.addChild(discover);
		attackTree.addNode(discover);
		
		AttackTreeNode exploit = new AttackTreeNode("Exploit zero-day DoS vulnerability in system part " + systemPart);
		attackTree.addCountermeasureToNode(new Countermeasure("Install IDS/IPS",
				Descriptions.countermeasures.get("Install IDS/IPS"),Complexity.HIGH,1), exploit);
		exploit.setStealth(2);
		node.addChild(exploit);
		attackTree.addNode(exploit);
	}
}
