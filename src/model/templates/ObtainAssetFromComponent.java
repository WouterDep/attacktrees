package model.templates;

import controller.IDPParser;
import model.AttackTree;
import model.AttackTreeNode;
import model.Operation;

/**
 * Obtain(Asset, Component)
 * @author Wouter
 *
 */
public class ObtainAssetFromComponent {

	private IDPParser parser;
	private String component;
	private AttackTreeNode node;
	private AttackTree attackTree;
	
	public ObtainAssetFromComponent(IDPParser parser, String component, AttackTreeNode node,
			AttackTree attackTree) {
		this.parser = parser;
		this.component = component;
		this.node = node;
		this.attackTree = attackTree;
	}

	public void obtainAssetFromComponent(){
		node.setOperation(Operation.AND);
		
		// add access child
		AttackTreeNode access = new AttackTreeNode("Access(" + component + ")");
		node.addChild(access);
		attackTree.addNode(access);
		Access accesTemp = new Access(parser,component,access,attackTree);
		accesTemp.access();
		
		AttackTreeNode exploit = new AttackTreeNode("ExploitDLVuln(" + component + ")");
		node.addChild(exploit);
		attackTree.addNode(exploit);
		ExploitDL exploitTemp = new ExploitDL(parser,component,exploit,attackTree);
		exploitTemp.exploitDL();
		
	}
}
