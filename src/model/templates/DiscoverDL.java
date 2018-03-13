package model.templates;

import model.AttackTree;
import model.AttackTreeNode;
import model.Complexity;
import model.Countermeasure;
import model.Descriptions;
import model.Operation;
/**
 * DiscoverDLVuln(SystemPart) template
 * @author Wouter
 *
 */
public class DiscoverDL {
	
	private String component;
	private AttackTreeNode node;
	private AttackTree attackTree;
	
	public DiscoverDL(String component, AttackTreeNode node, AttackTree attackTree) {
		this.component = component;
		this.node = node;
		this.attackTree = attackTree;
	}

	public void discoverDL(){
		// root of this template has operation AND
		node.setOperation(Operation.AND);
		
		// vulnerability children
		AttackTreeNode discover = new AttackTreeNode("Discover zero-day data leakage vulnerability in component " + component);
		attackTree.addCountermeasureToNode(new Countermeasure("Disable unused ports & services",
				Descriptions.countermeasures.get("Disable unused ports & services"), Complexity.MEDIUM,2), discover);
		discover.setStealth(3);
		node.addChild(discover);
		attackTree.addNode(discover);
		
		AttackTreeNode exploit = new AttackTreeNode("Exploit zero-day data leakage vulnerability in component " + component);
		attackTree.addCountermeasureToNode(new Countermeasure("Install IDS/IPS",
				Descriptions.countermeasures.get("Install IDS/IPS"),Complexity.HIGH,1), exploit);
		attackTree.addCountermeasureToNode(new Countermeasure("Input validation: Path Traversal",
				Descriptions.countermeasures.get("Input validation: Path Traversal"),Complexity.MEDIUM,2), exploit);
		exploit.setStealth(2);
		node.addChild(exploit);
		attackTree.addNode(exploit);
	}
}
