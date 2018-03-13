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
 *  Modify(Parameter,Module) template
 * @author Wouter
 *
 */
public class ModifyParameterModule {

	//private String idpFile;
	private String parameter;
	private String module;
	private AttackTreeNode node;
	private AttackTree attackTree;
	private IDPParser parser;
	
	public ModifyParameterModule(IDPParser parser, String parameter, String module,AttackTreeNode node, AttackTree attackTree) {
		this.parser = parser;
		this.parameter = parameter;
		this.module = module;
		this.node = node;
		this.attackTree = attackTree;
	}

	public void modifyParameterModule(){
		// root of this template has operation OR
		node.setOperation(Operation.OR);
		attackTree.addCountermeasureToNode(new Countermeasure("Application monitor",
				Descriptions.countermeasures.get("Application monitor"),Complexity.MEDIUM,2), node);
		// create the two helper nodes
		AttackTreeNode employee = new AttackTreeNode("Use " + module + " as a company employee");
		employee.setOperation(Operation.AND);		
		node.addChild(employee);
		attackTree.addNode(employee);
		
		AttackTreeNode abuse = new AttackTreeNode("Abuse vulnerabilities in " + module );
		abuse.setOperation(Operation.AND);		
		node.addChild(abuse);
		attackTree.addNode(abuse);
		
		// find out in which component the module is located 
		String component = "";
			// LocatedIn = {module, component}
		for(Entry<String, String> entry : parser.getLocatedIn()) {
			if(entry.getKey().equals(module)){
				component = entry.getValue();
			}
		}
		
		// add access node for component
		AttackTreeNode access = new AttackTreeNode("Access(" + component + ")");
		employee.addChild(access);
		attackTree.addNode(access);
		Access accessTemplate = new Access(parser, component, access, attackTree);
		accessTemplate.access();
	
		// add authenticate node for module
		AttackTreeNode authenticate = new AttackTreeNode("Authenticate(" + module + ")");
		employee.addChild(authenticate);
		attackTree.addNode(authenticate);
		Authenticate authenticateTemplate = new Authenticate(parser, module, authenticate, attackTree);
		authenticateTemplate.authenticate();
		
		// add authorization node for parameter and module
		AttackTreeNode authorization = new AttackTreeNode("Authorization(" + parameter + "," + module + ")");
		employee.addChild(authorization);
		attackTree.addNode(authorization);
		Authorization authorizationTemp = new Authorization(parser,parameter, module, authorization, attackTree);
		authorizationTemp.authorization();
		
		// add access node for component
		AttackTreeNode access2 = new AttackTreeNode("Access(" + component + ")");
		abuse.addChild(access2);
		attackTree.addNode(access2);
		Access accessTemp = new Access(parser,component,access2,attackTree);
		accessTemp.access();
		
		AttackTreeNode rce = new AttackTreeNode("ExploitRCEVuln(" + module + ")");
		abuse.addChild(rce);
		attackTree.addNode(rce);
		ExploitRCE exploitRCE = new ExploitRCE(parser, module, rce, attackTree);
		exploitRCE.exploitRCE();
	}
	
}
