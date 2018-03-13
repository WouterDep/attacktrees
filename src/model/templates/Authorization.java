package model.templates;

import controller.IDPParser;
import model.AttackTree;
import model.AttackTreeNode;
import model.Complexity;
import model.Countermeasure;
import model.Descriptions;
import model.Operation;

/**
 * Authorization(Module) template
 * @author Wouter
 *
 */
public class Authorization {

	private IDPParser parser;
	private String parameter;
	private String module;
	private AttackTreeNode node;
	private AttackTree attackTree;
	
	public Authorization(IDPParser parser, String parameter, String module, AttackTreeNode node, AttackTree attackTree) {
		this.parser = parser;
		this.parameter = parameter;
		this.module = module;
		this.node = node;
		this.attackTree = attackTree;
	}
	
	public void authorization(){
		// root of this template has operation OR
				node.setOperation(Operation.OR);	
				attackTree.addCountermeasureToNode(new Countermeasure("Least privilege",
						Descriptions.countermeasures.get("Least privilege"), Complexity.MEDIUM,2), node);
				
				// Find out which credentials can be used to Modify the parameter from the given module
				// Authorization(Password, Module, Parameter, Operation)
				String credential = "";
				String[] authorizationTuples = parser.getAuthorization();
				for (String s : authorizationTuples){
					if (s.contains(module) && s.contains(parameter) && s.contains("Modify")){
						if (s.charAt(0) == ' '){
							s = s.substring(1, s.length());
						}
						String[] temp = s.split(",");
						credential = temp[0].substring(1, temp[0].length()-1);
					}
				}
				
				if (!credential.equals("")){
					AttackTreeNode possess = new AttackTreeNode("Possess credential " + credential);
					possess.setStealth(3);
					node.addChild(possess);
					attackTree.addNode(possess);
					
					AttackTreeNode obtain = new AttackTreeNode("ObtainCred(" + credential + ")");
					node.addChild(obtain);
					attackTree.addNode(obtain);
					ObtainCredential obtainTemp =  new ObtainCredential(parser, credential, obtain, attackTree);
					obtainTemp.obtainCredential();
					
					AttackTreeNode exploit = new AttackTreeNode("ExploitPEVuln(" + module + ")");
					node.addChild(exploit);
					attackTree.addNode(exploit);
					ExploitPE exploitTemp = new ExploitPE(parser, module, exploit, attackTree);
					exploitTemp.exploitPE();
				}
	}
	
	
}
