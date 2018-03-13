package model.templates;

import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import controller.IDPParser;
import model.AttackTree;
import model.AttackTreeNode;
import model.Operation;

/**
 *  Obtain(Asset) template
 * @author Wouter
 *
 */
public class ObtainAsset {

	private IDPParser parser;
	private String asset;
	private AttackTreeNode node;
	private AttackTree attackTree;
	
	public ObtainAsset(IDPParser parser, String asset, AttackTreeNode node, AttackTree attackTree) {
		this.parser = parser;
		this.asset = asset;
		this.node = node;
		this.attackTree = attackTree;
	}

	public void obtainAsset(){
		// root of this template has operation OR
		node.setOperation(Operation.OR);
		node.setName("Obtain(" + asset + ")");
		
		// check in which components asset d is stored
		Set<String> components = new HashSet<String>();
		for (Entry<String, String> entry : parser.getAssetStorage()) {
			// AssetStorage = {component, asset}
			if (entry.getValue().equals(asset)) {
				components.add(entry.getKey());
			}
		}
		
		for (String s : components){
			AttackTreeNode atn = new AttackTreeNode("Obtain(" + asset + "," + s + ")");
			node.addChild(atn);
			attackTree.addNode(atn);
			ObtainAssetFromComponent temp = new ObtainAssetFromComponent(parser,s,atn,attackTree);
			temp.obtainAssetFromComponent();
		}
	}
}
