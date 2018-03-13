package model.templates;

import model.AttackTree;
import model.AttackTreeNode;
import model.Complexity;
import model.Countermeasure;
import model.Descriptions;
import model.Operation;

/**
 * DiscoverRemoteCodeExecutionVulnerability(Module) template
 * @author Wouter
 *
 */
public class DiscoverRCE {

	private String module;
	private AttackTreeNode node;
	private AttackTree attackTree;

	public DiscoverRCE(String module, AttackTreeNode node, AttackTree attackTree) {
		this.module = module;
		this.node = node;
		this.attackTree = attackTree;
	}

	public void discoverRCE() {								
		// root of this template has operation AND
		node.setOperation(Operation.AND);

		// vulnerability children
		AttackTreeNode discover = new AttackTreeNode("Discover zero-day remote code execution vulnerability in module " + module);
		attackTree.addCountermeasureToNode(new Countermeasure("Disable unused ports & services",
				Descriptions.countermeasures.get("Disable unused ports & services"), Complexity.MEDIUM,2), discover);
		discover.setStealth(3);
		node.addChild(discover);
		attackTree.addNode(discover);

		AttackTreeNode exploit = new AttackTreeNode(	"Exploit zero-day remote code execution vulnerability in module " + module);
		attackTree.addCountermeasureToNode(new Countermeasure("Install IDS/IPS",
				Descriptions.countermeasures.get("Install IDS/IPS"),Complexity.HIGH,1), exploit);
		attackTree.addCountermeasureToNode(new Countermeasure("Input validation: code injection",
				Descriptions.countermeasures.get("Input validation: code injection"),Complexity.MEDIUM,2), exploit);
		attackTree.addCountermeasureToNode(new Countermeasure("Input validation: buffer length",
				Descriptions.countermeasures.get("Input validation: buffer length"),Complexity.MEDIUM,1), exploit);
		exploit.setStealth(2);
		node.addChild(exploit);
		attackTree.addNode(exploit);
	}
}
