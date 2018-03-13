package model.templates;

import java.util.Map.Entry;

import controller.IDPParser;
import model.AttackTree;
import model.AttackTreeNode;
import model.Operation;

/**
 * Authenticate(Module) template
 * 
 * @author Wouter
 *
 */
public class Authenticate {

	private String module;
	private AttackTreeNode node;
	private AttackTree attackTree;
	private IDPParser parser;

	public Authenticate(IDPParser parser, String module, AttackTreeNode node, AttackTree attackTree) {
		this.parser = parser;
		this.module = module;
		this.node = node;
		this.attackTree = attackTree;
	}

	public void authenticate() {
		// root of this template has operation AND
		node.setOperation(Operation.AND);

		// add module login node
		AttackTreeNode moduleLogin = new AttackTreeNode("LogIn(" + module + ")");
		moduleLogin.setStealth(2);
		node.addChild(moduleLogin);
		attackTree.addNode(moduleLogin);
		LoginModule loginModuleTemplate = new LoginModule(parser,module,moduleLogin,attackTree);
		loginModuleTemplate.loginModule();

		// find out in which component the module is located
		String component = "";
			// LocatedIn = {module, component}
		for (Entry<String, String> entry : parser.getLocatedIn()) {
			if (entry.getKey().equals(module)) {
				component = entry.getValue();
			}
		}

		String prereq = "";
		prereq = findModulePrerequisite(module);

		while(prereq!= null && !prereq.equals(component)){
			prereq = findModulePrerequisite(module);
			
			// add authenticate node for module prereq
			if (prereq != null) {
				AttackTreeNode authenticate = new AttackTreeNode("Authenticate(" + prereq + ")");
				node.addChild(authenticate);
				attackTree.addNode(authenticate);
				Authenticate authenticateTemplate = new Authenticate(parser, prereq, authenticate, attackTree);
				authenticateTemplate.authenticate();
			}
			module = prereq;
		}
			
	}

	private String findModulePrerequisite(String module){
		String prereq = "";
		
		// find out the prerequisites
		for (Entry<String, String> entry : parser.getModulePrerequisite()) {
			// ModulePrerequisite = {module, prerequisite}
			if (entry.getKey().equals(module)) {
				prereq = entry.getValue();
			}
		}
		boolean prereqIsComponent = false;
		int i = 0;
		while(!prereqIsComponent && i < parser.getComponent().length){
			if(parser.getComponent()[i].equals(prereq)) prereqIsComponent  = true;
			i++;
		}
		if(prereqIsComponent) return null;
		return prereq;
		
	}
}
