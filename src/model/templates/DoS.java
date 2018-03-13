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
 *  DoS(SystemPart) template
 * @author Wouter
 *
 */
public class DoS {

	private IDPParser parser;
	private String systemPart;
	private AttackTreeNode node;
	private AttackTree attackTree;
	
	public DoS(IDPParser parser, String systemPart, AttackTreeNode node, AttackTree attackTree) {
		this.parser = parser;
		this.systemPart = systemPart;
		this.node = node;
		this.attackTree = attackTree;
	}

	public void doS(){
		// root of this template has operation OR
		node.setOperation(Operation.OR);
		node.setName("DoS(" + systemPart + ")");
		
		// find out in which component the module is located
		String component = "";
		for(Entry<String, String> entry : parser.getLocatedIn()){
			if(entry.getKey().equals(systemPart)) component = entry.getValue();
		}
		
		if (component.equals("")){
			component = systemPart;
		}
		
		// Children: Exploit vuln & Physical Access
		AttackTreeNode vuln = new AttackTreeNode("Exploit vuln in " + systemPart);
		node.addChild(vuln);
		attackTree.addNode(vuln);
		vuln.setOperation(Operation.AND);
		
		AttackTreeNode phys = new AttackTreeNode("Gain physical access to component " + component);
		attackTree.addCountermeasureToNode(new Countermeasure("Physical Isolation",
				Descriptions.countermeasures.get("Physical Isolation"),Complexity.LOW, 3), phys);
		attackTree.addCountermeasureToNode(new Countermeasure("Physical system hardening",
				Descriptions.countermeasures.get("Physical system hardening"),Complexity.LOW,2), phys);
		phys.setStealth(1);
		node.addChild(phys);
		attackTree.addNode(phys);
		
		// Children of Exploit vuln : Access & ExploitDoSvuln
		AttackTreeNode access = new AttackTreeNode("Access(" + component + ")");
		vuln.addChild(access);
		attackTree.addNode(access);
		Access accessTemp = new Access(parser, component, access, attackTree);
		accessTemp.access();
		
		AttackTreeNode exploit = new AttackTreeNode("ExploitDoSVuln(" + systemPart + ")");
		vuln.addChild(exploit);
		attackTree.addNode(exploit);
		ExploitDoS exploitTemp = new ExploitDoS(parser, systemPart, exploit, attackTree);
		exploitTemp.exploitDoS();
	}
}
