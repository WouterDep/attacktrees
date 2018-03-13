package model.templates;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import controller.IDPParser;
import model.AttackTree;
import model.AttackTreeNode;
import model.Complexity;
import model.Countermeasure;
import model.Descriptions;
import model.Operation;

/**
 *  Modify(Parameter) template
 * @author Wouter
 *
 */
public class ModifyParameter {

	//private String idpFile;
	private String parameter;
	private AttackTreeNode node;
	private AttackTree attackTree;
	private IDPParser parser;
	
	public ModifyParameter(IDPParser parser, String parameter, AttackTreeNode node, AttackTree attackTree) {
		this.parser = parser;
		this.parameter = parameter;
		this.node = node;
		this.attackTree = attackTree;
	}

	public void modifyParameterTemplate(){
		// root of this template has operation OR
		node.setOperation(Operation.OR);
			System.out.println("Modify("+parameter+"), node: "+node.getName());
		node.setName("Modify(" + parameter + ")");
			System.out.println("Modify("+parameter+"), node: "+node.getName()+" level: "+node.getLevel());
		attackTree.addCountermeasureToNode(new Countermeasure("Install IDS/IPS",
				Descriptions.countermeasures.get("Install IDS/IPS"),Complexity.HIGH,2), node);
		
		// identify all software modules that control parameter
		Set<String> modules = new HashSet<String>();
		
		for(Entry<String, String> entry : parser.getControl())  {
			// Control = {module, parameter}
			if(entry.getValue().equals(parameter)) modules.add(entry.getKey());
		}
	
		// identify all sensor components that measure parameter (if any)
		boolean nosensors = true;
		Set<String> sensors = new HashSet<String>();
		for(Entry<String, String> entry : parser.getMeasure())  {
			// Measure = {sensor, parameter}
			if(entry.getValue().equals(parameter)) {
				sensors.add(entry.getKey());
				nosensors = false;
			}
		}
		
		// identify the network which the sensor components are in
			//TODO what happens here??
		String sensor = "";
		if (!(sensors.isEmpty())){
			for (String s : sensors){
				sensor = s;
			}
		}
		
		// identify the network which the actuator components are in
		 //TODO what happens here??
		String actuator = "";
		if (!(modules.isEmpty())){
			for (String s : modules){
				actuator = s;
			}
		}
		
		// find component which 'actuator' is located in
		String component = "";
		
		for(Entry<String, String> entry : parser.getLocatedIn()) {
			// LocatedIn = {module, component}
			if(entry.getKey().equals(actuator)) component = entry.getValue();
		}
			// no component found that contains the actuator, so the component is the actuator then
		if (component.equals("")){component = actuator;	} 
		
		String network = "";
		if (nosensors){
			// find network of actuator
			for(Entry<String, String> entry : parser.getNetworkLocation()) {
				if(entry.getKey().equals(component)) network = entry.getValue();
			}
		} else {
			// find network of sensor
			for(Entry<String, String> entry : parser.getNetworkLocation()) {
				if(entry.getKey().equals(sensor)) network = entry.getValue();
			}
		}

		
		// Add subtrees for each sensor
		for (String s : sensors){
			AttackTreeNode atn = new AttackTreeNode("Gain physical access to component " + s);
			atn.setStealth(3);
			node.addChild(atn);
			attackTree.addNode(atn);
		}
		
		// Add subtrees for each module
		for (String s : modules){
			System.out.println("ModifyParameter: module s: "+s);
			AttackTreeNode atn = new AttackTreeNode("Modify(" + parameter + "," + s + ")");
			node.addChild(atn);
			attackTree.addNode(atn);
			ModifyParameterModule modifyParam = new ModifyParameterModule(parser,parameter,s,atn,attackTree);
			modifyParam.modifyParameterModule();
		}
		
		// Add subtree for writing own software
		AttackTreeNode usesMalware = new AttackTreeNode("Spoofing attack");
		node.addChild(usesMalware);
		attackTree.addNode(usesMalware);
		usesMalware.setOperation(Operation.AND);
		
		AttackTreeNode protocolAttack = new AttackTreeNode("Run spoofing software");
		attackTree.addCountermeasureToNode(new Countermeasure("Install IDS/IPS & Protocol Anomaly Detect.",
				Descriptions.countermeasures.get("Install IDS/IPS & Protocol Anomaly Detect."),Complexity.HIGH,2), protocolAttack);
		attackTree.addCountermeasureToNode(new Countermeasure("Static ARP table/Dynamic inspection",
				Descriptions.countermeasures.get("Static ARP table/Dynamic inspection"),Complexity.HIGH,1), protocolAttack);
		attackTree.addCountermeasureToNode(new Countermeasure("Encrypted tunnel",
				Descriptions.countermeasures.get("Encrypted tunnel"),Complexity.MEDIUM,2), protocolAttack);
		attackTree.addCountermeasureToNode(new Countermeasure("Checksums on msgs",
				Descriptions.countermeasures.get("Checksums on msgs"),Complexity.MEDIUM,1), protocolAttack);
		protocolAttack.setStealth(2);
		usesMalware.addChild(protocolAttack);
		attackTree.addNode(protocolAttack);
		
		AttackTreeNode networkAccessNode = new AttackTreeNode("NetworkAccess(" + network + ")");
		usesMalware.addChild(networkAccessNode);
		attackTree.addNode(networkAccessNode);
		NetworkAccess networkAccess = new NetworkAccess(parser,network,networkAccessNode,attackTree);
		networkAccess.networkAccess();
		
		//changed();
		//fireTreeChanged();
	}
	
}
