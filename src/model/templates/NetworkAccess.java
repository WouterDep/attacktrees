package model.templates;

import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import controller.IDPParser;
import model.AttackTree;
import model.AttackTreeNode;
import model.Complexity;
import model.Countermeasure;
import model.Descriptions;
import model.Operation;

/**
 *  NetworkAccess(Network) template
 * @author Wouter
 *
 */
public class NetworkAccess {

	private String network;
	private AttackTreeNode node;
	private AttackTree attackTree;
	private IDPParser parser;

	public NetworkAccess(IDPParser parser, String network, AttackTreeNode networkAccessNode, AttackTree attackTree) {
		this.parser= parser;
		this.network = network;
		this.node = networkAccessNode;
		this.attackTree = attackTree;
	}

	public void networkAccess() {
		// root of this template has operation OR
		node.setOperation(Operation.OR);

		// identify the components c that are part of this network
		Set<String> networkComponents = new HashSet<String>();
		for (Entry<String, String> entry : parser.getNetworkLocation()) {
			// NetworkLocation = {component, network}
			if (entry.getValue().equals(network)) {
				networkComponents.add(entry.getKey());
			}
		}
		
		for (String s : networkComponents) {

			// add physical access node
			AttackTreeNode physicalAccess = new AttackTreeNode("Gain physical access to component " + s);
			attackTree.addCountermeasureToNode(new Countermeasure("Port Security",
					Descriptions.countermeasures.get("Port Security"),Complexity.LOW, 2), physicalAccess);
			attackTree.addCountermeasureToNode(new Countermeasure("Physical Isolation",
					Descriptions.countermeasures.get("Physical Isolation"),Complexity.LOW, 3), physicalAccess);
			physicalAccess.setStealth(1);
			node.addChild(physicalAccess);
			attackTree.addNode(physicalAccess);
		}
	}
}