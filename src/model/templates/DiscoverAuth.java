package model.templates;

import model.AttackTree;
import model.AttackTreeNode;
import model.Complexity;
import model.Countermeasure;
import model.Descriptions;
import model.Operation;

/**
 *  DiscoverAuthVuln(SystemPart) template
 * @author Wouter
 *
 */
public class DiscoverAuth {

	private String module;
	private AttackTreeNode node;
	private AttackTree attackTree;
	
	public DiscoverAuth(String module, AttackTreeNode node, AttackTree attackTree) {
		this.module = module;
		this.node = node;
		this.attackTree = attackTree;
	}
	
	public void discoverAuth(){
		// root of this template has operation AND
		node.setOperation(Operation.AND);
		
		// vulnerability children
		AttackTreeNode discover = new AttackTreeNode("Discover zero-day compromised authentication vulnerability in module " + module);
		attackTree.addCountermeasureToNode(new Countermeasure("Disable unused ports & services",
				Descriptions.countermeasures.get("Disable unused ports & services"), Complexity.MEDIUM,2), discover);
		discover.setStealth(3);
		node.addChild(discover);
		attackTree.addNode(discover);
		
		AttackTreeNode exploit = new AttackTreeNode("Exploit zero-day compromised authentication vulnerability in module " + module);
		attackTree.addCountermeasureToNode(new Countermeasure("Install IDS/IPS",
				Descriptions.countermeasures.get("Install IDS/IPS"),Complexity.HIGH,1), exploit);
		exploit.setStealth(2);
		node.addChild(exploit);
		attackTree.addNode(exploit);
	}
}
