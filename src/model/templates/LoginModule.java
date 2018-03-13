package model.templates;

import java.util.Map.Entry;

import controller.IDPParser;
import model.AttackTree;
import model.AttackTreeNode;
import model.Operation;

/**
 *  Login(Module) template
 * @author Wouter
 *
 */
public class LoginModule {
	
	private IDPParser parser;
	private String module;
	private AttackTreeNode node;
	private AttackTree attackTree;
	
	public LoginModule(IDPParser parser, String module, AttackTreeNode node, AttackTree attackTree) {
		this.parser = parser;
		this.module = module;
		this.node = node;
		this.attackTree = attackTree;
	}
	
	public void loginModule(){
			// root of this template has operation OR
			node.setOperation(Operation.OR);
			
			// figure out credential required for authentication to module
			String credential = "";
			for (Entry<String, String> entry : parser.getAuthentication()) {
				// Authentication = {module, password}
				if (entry.getKey().equals(module)) {
					credential = entry.getValue();
				}
			}
			
			if (credential.equals("")){
				node.setDifficulty(1);
			} else {
//				// find out in which component the module is located
//				String component = "";
//				splits = idpFile.split("LocatedIn =");
//				splits2 = splits[1].split("}");
//				splits2[0] = splits2[0].substring(3, splits2[0].length()-1);
//				String[] LocatedInDuos = splits2[0].split(";");
//				
//				for (String s : LocatedInDuos){
//					if (s.charAt(0) == ' '){
//						s = s.substring(1, s.length());
//					}
//					String[] split = s.split("->");
//					if (split[0].substring(1, split[0].length()-1).equals(module)){
//						component = split[1].substring(1, split[1].length()-1);
//					}
//				}
				
				// add possess credential leaf
				AttackTreeNode ownsCredential = new AttackTreeNode("Possess credential " + credential);
				ownsCredential.setStealth(3);
				node.addChild(ownsCredential);
				attackTree.addNode(ownsCredential);
				
				// add obtains credential node
				AttackTreeNode obtainsCredential = new AttackTreeNode("ObtainCred(" + credential + ")");
				node.addChild(obtainsCredential);
				attackTree.addNode(obtainsCredential);
				ObtainCredential obtainCredentialTemplate = new ObtainCredential(parser,credential,obtainsCredential,attackTree);
				obtainCredentialTemplate.obtainCredential();
				
				// add exploit vulnerability node
				AttackTreeNode exploit = new AttackTreeNode("ExploitAuthVuln(" + module + ")");
				node.addChild(exploit);
				attackTree.addNode(exploit);
				ExploitAuth exploitTemp = new ExploitAuth(parser, module, exploit, attackTree);
				exploitTemp.exploitAuth();
			}
		}
	

	
}
