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
 * Access(Component) template
 * 
 * @author Wouter
 *
 */
public class Access {

	// private String idpFile;
	private String component;
	private AttackTreeNode node;
	private AttackTree attackTree;
	private IDPParser parser;

	public Access(IDPParser parser, String component, AttackTreeNode node, AttackTree attackTree) {
		this.parser = parser;
		this.component = component;
		this.node = node;
		this.attackTree = attackTree;
	}

	public void access() {
		// root of this template has operation OR
		node.setOperation(Operation.OR);

		// find out which network n the component is part of
		String network = "";
		for (Entry<String, String> entry : parser.getNetworkLocation()) {
			// NetworkLocation = {component, network}
			if (entry.getKey().equals(component)) {
				network = entry.getValue();
			}
		}

		// add physical access node
		AttackTreeNode physical = new AttackTreeNode("Gain physical access to component " + component);
		attackTree.addCountermeasureToNode(new Countermeasure("Physical Isolation",
						Descriptions.countermeasures.get("Physical Isolation"),Complexity.LOW, 3), physical);
		attackTree.addCountermeasureToNode(new Countermeasure("Physical system hardening",
				Descriptions.countermeasures.get("Physical system hardening"),Complexity.LOW,2), physical);
		physical.setStealth(1);
		node.addChild(physical);
		attackTree.addNode(physical);

		// add remote access node
		AttackTreeNode remote = new AttackTreeNode("Gain remote access to component " + component);
		node.addChild(remote);
		attackTree.addNode(remote);
		remote.setOperation(Operation.OR);

		// network access node
		AttackTreeNode networkAccess = new AttackTreeNode("NetworkAccess(" + network + ")");
		remote.addChild(networkAccess);
		attackTree.addNode(networkAccess);
		NetworkAccess networkAccessTemplate = new NetworkAccess(parser, network, networkAccess, attackTree);
		networkAccessTemplate.networkAccess();

		// find networks n' such that exists a RemoteConnection(c',c) with c in n and c' in n'
			// systempart c is in network n, systempart c' is in network n'. 
			// RemoteAccess between  c and c' therefore requires 
			// RemoteConnection(c',c) & NetworkLocation(c,n) & NetworkLocation(c',n')
		// achieved in logic with something along these lines:
		// ! c[SystemPart] c'[SystemPart] n[Network] n'[Network] :
		// RemoteAccess(c',c) <- RemoteConnection(c',c) & NetworkLocation(c,n) &
		// NetworkLocation(c',n').
		// now extract all networks n' and place them in set NDASHNETWORKS

		// 1) Find components c' with remote access to c
		Set<String> cDashComponents = new HashSet<String>();

		for (Entry<String, String> entry : parser.getRemoteAccess()) {
			// RemoteAccess = { c', c}
			if (entry.getValue().equals(component)) {
				cDashComponents.add(entry.getKey());
			}
		}

		// 2) Find the corresponding network n' for every component c' in
		// cdashcomponents
		for (String cdash : cDashComponents) {
			// find network n' containing component cdash
			String ndashNetwork = "";
			for (Entry<String, String> entry : parser.getNetworkLocation()) {
				// NetworkLocation = {component, network}
				if (entry.getKey().equals(cdash)) {
					ndashNetwork = entry.getValue();
					// n' must be another network than n
					if (!ndashNetwork.equals(network)) {

						AttackTreeNode ndash = new AttackTreeNode(
								"Access " + component + " from " + cdash + " in network " + ndashNetwork);
						
						remote.addChild(ndash);
						attackTree.addNode(ndash);
						attackTree.addCountermeasureToNode(new Countermeasure("Data diode",Descriptions.countermeasures.get("Data diode"),Complexity.LOW,4), ndash);
						ndash.setOperation(Operation.AND);
						ndash.setStealth(2);
						// network access node
						AttackTreeNode networkAccess2 = new AttackTreeNode("NetworkAccess(" + ndashNetwork + ")");
						ndash.addChild(networkAccess2);
						attackTree.addNode(networkAccess2);
						NetworkAccess networkAccessTemplate2 = new NetworkAccess(parser, network, networkAccess2,
								attackTree);
						networkAccessTemplate2.networkAccess();

						// firewall node
						AttackTreeNode firewall = new AttackTreeNode(
								"Firewall(" + ndashNetwork + "," + network + "," + cdash + ")");
						attackTree.addCountermeasureToNode(new Countermeasure("Change firewall policy",
								Descriptions.countermeasures.get("Change firewall policy"), Complexity.MEDIUM,2), firewall);
						attackTree.addCountermeasureToNode(new Countermeasure("Avoid IP-address ranges",
								Descriptions.countermeasures.get("Avoid IP-address ranges"),Complexity.MEDIUM,1), firewall);
						firewall.setStealth(2);
						ndash.addChild(firewall);
						attackTree.addNode(firewall);
					}
				}
			}
		}
	}
}
