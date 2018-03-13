package model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Observable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import view.dialogs.AlertDialog;

/**
 *  Model class for attacktree
 * @author Wouter
 *
 */
public class AttackTree extends Observable{

	protected Map<Integer,AttackTreeNode> nodes = new HashMap<>();
	protected AttackTreeNode root;
	protected int amount;
	private int depth;
	protected Attacker attacker;
	protected Map<String,Attacker> attackersMap = new HashMap<>();
	private int amountAppliedCM;
	private Map<String, List<Countermeasure>> nodeCountermeasureMap = new HashMap<>(); //Map<atn.name, List<CM> : every node with same name refers to this CM-list
	
	
	public AttackTree(){
		AttackTreeNode root = new AttackTreeNode("Root");
		root.setParent(root);
		nodes.put(0, root);
		this.root = root;
		root.setLevel(1);
		root.setIdentifier(0);
		amount = 1;
	}
	
	/** Resets tree attributes. Called when a tree is read from a file, or when another goal is selected*/
	public void initialize(){
		AttackTreeNode root = new AttackTreeNode("Root");
		root.setParent(root);
		nodes.clear();
		nodes.put(0, root);
		this.root = root;
		root.setLevel(1);
		root.setIdentifier(0);
		amount = 1;
	}
	
	
	/**  Copyconstructor (deep copy). Used for countermeasure simulation.
	 * @param original Original Attack Tree */
	public AttackTree(AttackTree original) {
		this.nodes = new HashMap<>();
		// Deepcopy of nodes (all attr except parent & children)
		for (Entry<Integer, AttackTreeNode> entry : original.nodes.entrySet())
			this.nodes.put(entry.getKey(), new AttackTreeNode(entry.getValue()));
		
		// Set parents & children
		for (Entry<Integer, AttackTreeNode> entry : this.nodes.entrySet()) {
			if (entry.getKey() != 0) {
				// Set parent
				entry.getValue().setParent(nodes.get(original.nodes.get(entry.getKey()).parent.identifier));
			} 
			// Set children
			for (AttackTreeNode origChild : original.nodes.get(entry.getKey()).children) {
				entry.getValue().children.add(nodes.get(origChild.identifier));

			}
		}
		
		this.root = nodes.get(0);
		this.root.setParent(root);
		this.amount = original.getAmount();
		this.depth = original.getDepth();
		original.mergeActiveAttackers(); // Deepcopy has no deepcopy of attackers, only the global
		this.attacker = original.getAttacker();
		this.nodeCountermeasureMap = new HashMap<>();
		for(Entry<String, List<Countermeasure>> entry : original.nodeCountermeasureMap.entrySet()) {
			List<Countermeasure> tempCM = new ArrayList<>();
			for(Countermeasure cm : entry.getValue()) tempCM.add(new Countermeasure(cm));
			this.nodeCountermeasureMap.put(entry.getKey(), tempCM);
		}
	}

	//--------------------------------------------------
	//				TREE MANIPULATIONS
	//--------------------------------------------------
	
	/**Add node to tree (used when tree is read from file)*/
	protected void addNode(int id, AttackTreeNode node){
		node.setIdentifier(id);
		nodes.put(id, node);
		amount++;
		recalculateDepth();
		//changed();
	}
	
	/**Add node to tree (used when templates are executed)*/
	public void addNode(AttackTreeNode node){
		node.setIdentifier(amount);
		nodes.put(amount, node);
		amount++;
		recalculateDepth();
		//changed(); 
	}
	
	/** Delete a node and all its children
	 * @param node Attack Tree Node to be deleted */
	public void deleteNode(AttackTreeNode node){
		nodes.remove(node);		//delete itself from the global map of nodes
		if (!(node.getName() == node.getParent().getName())){
			node.getParent().getChildren().remove(node);  //delete itself from the list of children
		}
			
		amount--;
		
		deleteChildren(node);
		
		recalculateDepth();
		changed();
	}
	
	/**Delete the children of a node (but not the node itself)
	 * @param atn Attack Tree node to delete its children from */
	public void deleteChildren(AttackTreeNode atn) {
		// Delete the children (and all their children), but not the node itself
		//you  can't iterate of a list that is changing in length
		List<AttackTreeNode> childrenToDelete = new ArrayList<>(atn.getChildren());
		
		if (!(childrenToDelete.isEmpty())){
			for (AttackTreeNode atnChild : childrenToDelete){
				this.deleteNode(atnChild); //call recursively
			}
		}
	}
	
	/**Calculate the total depth of the tree*/
	private void recalculateDepth(){
		//iterate over all nodes to check the deepest
		depth=1;
		for(AttackTreeNode n : this.getNodes().values()){
			if(n.getLevel()>depth) depth =n.getLevel();
		}
	}
	
	/** Collect all leaf nodes
	 * @return List of leaf nodes*/
	public ArrayList<AttackTreeNode> getLeaves(){
		// gather all leaves in a list
				ArrayList<AttackTreeNode> leaves = new ArrayList<>();
				
				for(AttackTreeNode atn : this.getNodes().values()){
					if (atn.getChildren().isEmpty()){
						leaves.add(atn);
					}
				}
			return leaves;
	}
	
	//-------------------------------------------------------------
	//				DIFFICULTY CALCULATION		
	//--------------------------------------------------------------
	
	/**Recalculate the difficulty parameter of the attack tree.
	 * Successively calls mergeActiveAttackers, node.setDifficultyCM (for each node), 
	 * addDifficultyToLeaves and root.calculateDifficulty*/
	public void recalculateDifficulty(){
		mergeActiveAttackers();
		
		//Set difficulty delta caused by applied CM's on all nodes.
		nodes.values().forEach(atn -> {
			atn.setDifficultyCM(getTotalDifficultyIncrease(atn.getName()));
			});
		addDifficultyToLeaves();
		root.calculateDifficulty();
		nodes.get(root.getIdentifier()).setDifficulty(root.getTotalDifficulty()); //TODO why?
		changed();
	}
	
	/**Calculates the union of all active attackers. The 'merged' attacker is
	 * the attacker attribute of the attack tree.*/
	private void mergeActiveAttackers() {
		System.out.println("MERGE");
		if(attackersMap.size() > 0)attacker.initialize(); // reset the global attacker if there are others
		attackersMap.values().forEach(at -> {
			if(at.isActive()){
				attacker.getAccess().addAll(at.getAccess());
				attacker.getCredentials().addAll(at.getCredentials());
				attacker.getCapabilities().forEach((skill,level) -> {
					attacker.getCapabilities().replace(skill, Math.max(level, at.getCapabilities().get(skill)));
				});
			}
		});
		System.out.println("Merged attacker: "+attacker);
	}
	
	
	/**Set the 'difficulty'-field of the leaves based on the (merged) attacker*/
	private void addDifficultyToLeaves(){
		// gather all leaves
		ArrayList<AttackTreeNode> leaves = getLeaves();
		
		// assign difficulty
		for (AttackTreeNode atn : leaves){
			if (atn.getName().contains("Gain physical access")){
				// parse the component out of the string and check attacker access set
				String[] splits = atn.getName().split("component");
				String component = splits[1].substring(1, splits[1].length());
				if (this.getAttacker().getAccess().contains(component)){
					atn.setDifficulty(1);
				} else{
					atn.setDifficulty(4);
				}
			}
			if (atn.getName().contains("Possess credential")){
				// parse the credential out of the string and check attacker knowledge set
				String[] splits = atn.getName().split("credential");
				String credential = splits[1].substring(1, splits[1].length());
				if (this.getAttacker().getCredentials().contains(credential)){
					atn.setDifficulty(1);
				} else{
					atn.setDifficulty(4);
				}
			}
			if (atn.getName().contains("Firewall")){
				atn.setDifficulty(4);
			}
			else{
				// capabilities
				if (atn.getName().contains("Run spoofing software")){
					atn.setDifficulty(5-(attacker.getCapabilities().get("Spoof Protocol")));
				}
				if (atn.getName().contains("Exploit")){
					atn.setDifficulty(5-(attacker.getCapabilities().get("Exploit Vulnerability")));
				}
				if (atn.getName().contains("Discover")){
					atn.setDifficulty(5-(attacker.getCapabilities().get("Discover Vulnerability")));
				}
				if (atn.getName().contains("Steal")){
					atn.setDifficulty(5-(attacker.getCapabilities().get("Obtain Credentials")));
				}				
			}
		}

		
		changed();
	}
	
	//-------------------------------------------------
	//			STEALTH CALCULATION
	//-------------------------------------------------
	/**Calls calculateStealth on the root.*/
	public void recalculateStealth(){
		root.calculateStealth();
	}
	
	//---------------------------------------
	//				MARK PATHS
	//---------------------------------------
	/**Calls markEasiestPath on the root*/
	public void markEasiestPaths(){
		//reset current paths
		for(Entry<Integer, AttackTreeNode> entry : this.getNodes().entrySet()){
			entry.getValue().setEasy(false);
		}
		
		this.getRoot().markEasiestPath();
		
		changed();
	}
	
	/**Calls markStealthiestPath on the root*/
	public void markStealthiesPaths(){
		//reset current paths
		for(Entry<Integer, AttackTreeNode> entry : nodes.entrySet()){
			entry.getValue().setStealthy(false);
		}
		
		this.getRoot().markStealthiestPath();
		
		changed();
	}
	
	//-----------------------------------------------------
	//				COUNTERMEASURES
	//-----------------------------------------------------
	
	/**Sum difficulty increases of all applied CM's for node 'atnName'*/
	private int getTotalDifficultyIncrease(String atnName){
		int total =0;
		if (nodeCountermeasureMap.containsKey(atnName)) {
			for (Countermeasure c : getAppliedCountermeasures(atnName)) {
				total += c.getDifficultyIncrease();
			}
		}
		return total;
	}
	
	/**Collect the applied countermeasures for the node with name 'atnName'*/
	public List<Countermeasure> getAppliedCountermeasures(String atnName){
		return nodeCountermeasureMap.get(atnName)
							.stream()
							.filter(countermeasure -> countermeasure.isApplied())
							.collect(Collectors.toList());
		
	}
	
	/**Add a mapping of a node with a countermeasure. Used by the templates*/
	public void addCountermeasureToNode(Countermeasure c, AttackTreeNode atn){
		if(nodeCountermeasureMap.containsKey(atn.getName())){
			// The node is already in the map
			if(!nodeCountermeasureMap.get(atn.getName()).contains(c)){
				// The countermeasure was not in the list yet
				nodeCountermeasureMap.get(atn.getName()).add(c);
			}
		} else {
			// Add node and its first countermeasure to the map
			List<Countermeasure> temp = new ArrayList<>();
			temp.add(c);
			nodeCountermeasureMap.put(atn.getName(), temp);
		}
	}

	
	//---------------------------------------
	//				DASHBOARD 
	//---------------------------------------
	
	/**  Calculate distribution of difficulty of all nodes
	 * @return array with amount of difficulty 1 on index 0, etc.
	 */
	public int[] getAmountPerDifficulty() {
		int [] amountPerDifficulty = new int[4];
		for(AttackTreeNode atn: nodes.values()){
			switch(atn.getTotalDifficulty()){
				case 1: amountPerDifficulty[0]++; break;
				case 2: amountPerDifficulty[1]++;break;
				case 3: amountPerDifficulty[2]++; break;
				case 4: amountPerDifficulty[3]++; break;
			}
		}
		return amountPerDifficulty;
	}
	
	/** Calculate distribution of stealth of all nodes
	 * @return array with amount of stealth 1 on index 0, etc.
	 */
	public int[] getAmountPerStealth() {
		int [] amountPerStealth = new int[3];
		for(AttackTreeNode atn: nodes.values()){
			switch(atn.getTotalStealth()){
				case 1: amountPerStealth[0]++; break;
				case 2: amountPerStealth[1]++;break;
				case 3: amountPerStealth[2]++; break;
			}
		}
		return amountPerStealth;
	}
	
	
	/** Calculate weighted score: "Sum[ (difficulty-1)*(amountWithThisDifficulty) ] / totalNumberOfNodes" */
	public double getSecurityScore(){
		int [] amountPerDifficulty = getAmountPerDifficulty();
		int weightedSum = 0;
		
		for(int diff  = 0; diff < amountPerDifficulty.length; diff++){
			weightedSum += diff*amountPerDifficulty[diff];
		}
		double answer = weightedSum / (double) nodes.size();
		// round to one decimal
		return Math.round(answer*10.0)/10.0;
	}
	
	/**
	 *  Returns a sorted list with the best (not applied) countermeasure for every node in the path. The list is sorted
	 *  on security score (weighted difficulty average). If 'D' is used as  emphasis, the countermeasure with the 
	 *  highest difficulty increase of each node is added to a list, which is first sorted on security score and then by decreasing 
	 *  difficulty-increase. Likewise for 'C' (complexity).
	 * @param heuristic
	 * @param X 'D' or 'C', meaning emphasis on Difficulty or Complexity while comparing
	 * @return Map("totalDifficulty#AttackTreeNode.name#securityScore", Countermeasure)
	 */
	public List<Entry<String, Countermeasure>> getBestCountermeasures(Heuristics heuristic, char X){
		/* The nodes on the path need further countermeasures.
		 * 	 If the path-node has the same difficulty as its parent, it means its one of its weakest children. 
		 *   If this node has countermeasures, select the least complex with the highest 
		 *   difficulty increase (or other way around, depending on emphasis)
		 */
		
		// 1) Find best countermeasure for each node of the path with emphasis on X.
		Map<String, Countermeasure> bestNodeCMMap = new HashMap<>();
		
		for(AttackTreeNode atn : nodes.values()){
			if(atn.isParthOfPath(heuristic) 
					&& atn.getTotalDifficulty()<=atn.getParent().getTotalDifficulty() 
					&&	nodeCountermeasureMap.containsKey(atn.getName())) {
				
				try {
					bestNodeCMMap.put(atn.getName(), getBestExtraCM(atn.getName(), X));
				} catch (Exception e) {
					// "No unapplied cm's left"-exception
					System.out.println(e.getMessage());
				}
			}
		}
		
		
		// 2) Calculate Attacker Goal difficulty if countermeasure would be applied.
		AttackTree deepCopyTree = new AttackTree(this);

		Map<String,Countermeasure> unsortedAnswer =  new HashMap<>();
		Countermeasure cm; int rootDifficulty; double scoreDeep;
		for(Entry<String, Countermeasure> entry : bestNodeCMMap.entrySet()){
			cm = deepCopyTree.nodeCountermeasureMap
					.get(entry.getKey())
					.get(deepCopyTree.nodeCountermeasureMap
							.get(entry.getKey())
							.indexOf(entry.getValue())
					);
			// Calculate total difficulty by temporarily applying countermeasure
			cm.setApplied(true);
			deepCopyTree.recalculateDifficulty();
			rootDifficulty = deepCopyTree.getRoot().getTotalDifficulty();
			scoreDeep = deepCopyTree.getSecurityScore();
			
			cm.setApplied(false);
			unsortedAnswer.put(rootDifficulty+"#"+entry.getKey()+"#"+scoreDeep, cm); // e.g. ["4#obtainCredentialX#2.5", countermeasure]
		}
		
		// 3) Sort the list (according to emphasis)
		List<Entry<String, Countermeasure>> sortedAnswer = new LinkedList<>(unsortedAnswer.entrySet());
		
			// Create appropriate comparator
		Comparator<Entry<String, Countermeasure>> comp = null;
		
		
			// First compare by total score
		Comparator<Entry<String, Countermeasure>> scoreComp = 
				Comparator.comparing(Entry<String, Countermeasure>::getKey,	(s1, s2) -> {
							return s2.split("#")[2].compareTo(s1.split("#")[2]);
						});
		
			// Then compare by emphasis
		Comparator<Entry<String, Countermeasure>> diffComp = 
				Comparator.comparing(Entry<String, Countermeasure>::getKey,	(s1, s2) -> {
					return s2.split("#")[0].compareTo(s1.split("#")[0]);
				});
		
		Comparator<Entry<String, Countermeasure>> complexityComp = 
				Comparator.comparing(Entry<String, Countermeasure>::getValue, (cm1, cm2) -> {
					return cm1.getComplexity().compareTo(cm2.getComplexity());
				});
		
		switch(X){
		case 'D':
			comp = scoreComp.thenComparing(diffComp).thenComparing(complexityComp);
			break;
		case 'C':
			comp = scoreComp.thenComparing(complexityComp).thenComparing(diffComp);
			break;
		}
		
			// Sort
		sortedAnswer.sort(comp);
		
		return sortedAnswer;
	}
	
	
	/**Get the best not applied countermeasure, with highest difficulty increase and lowest complexity
	 *  for node with name 'atnName'. The emphasis X defines whether the algorithm should give precedence
	 *  to highest difficulty increase or lowest complexity. 
	 * @param X: 'D' or 'C', meaning emphasis on Difficulty or Complexity while comparing
	 * @return
	 * @throws Exception 
	 */
	private Countermeasure getBestExtraCM(String atnName, char X) throws Exception{
		List<Countermeasure> unapplied = nodeCountermeasureMap.get(atnName).stream()
																			.filter(countermeasure -> !countermeasure.isApplied())
																			.collect(Collectors.toList());
		if(unapplied.isEmpty()) throw new Exception("No unapplied countermeasures left");
		switch(X){
		case 'D': 
			unapplied.sort(Comparator
				    .comparing(Countermeasure::getDifficultyIncrease).reversed()
				    .thenComparing(Countermeasure::getComplexity));
			break;
		case 'C': 
			unapplied.sort(Comparator
				    .comparing(Countermeasure::getComplexity)
				    .thenComparing(Comparator.comparing(Countermeasure::getDifficultyIncrease).reversed()));
			break;
		}
		return unapplied.get(0);
	}
	
	
	//----------------------------------
	//				JSON
	//-----------------------------------
	
	/**Parse JSON string and build the corresponding attack tree and countermeasures*/
	@SuppressWarnings("unchecked")
	public void buildTreeFromJson(String s) {
		initialize();
		//	1) add all nodes to the map omitting parental edges for now
		// Root node must be skipped, since it has been added in the constructor
		JSONParser parser = new JSONParser();
		try {
			JSONObject jsonInput = (JSONObject) parser.parse(s);
			//JSONArray jsonTree =(JSONArray) parser.parse(s);
			JSONArray jsonTree = (JSONArray) jsonInput.get("tree");
			Iterator<JSONObject> iterator = jsonTree.iterator();
			JSONObject tempJson;
			AttackTreeNode tempAtn;
			
			while (iterator.hasNext()) {
				tempJson = iterator.next();
				// fill all attributes except parent
				String name = (String) tempJson.get("name");
				int level = ((Long) tempJson.get("depth")).intValue();
				if (level == 1) { // set root node
					root.setName(name);
					tempAtn = root;
				} else {
					tempAtn = new AttackTreeNode(name);
				}
				tempAtn.setDifficulty(((Long) tempJson.get("difficulty")).intValue());
				tempAtn.setDifficultyCM(((Long) tempJson.get("difficultyCM")).intValue());
				tempAtn.setDifficultyEdit(((Long) tempJson.get("difficultyEdit")).intValue());
				tempAtn.setStealth(((Long) tempJson.get("stealth")).intValue());
				tempAtn.setStealthEdit(((Long) tempJson.get("stealthEdit")).intValue());
				tempAtn.setLevel(((Long) tempJson.get("depth")).intValue());
				int id = ((Long) tempJson.get("ID")).intValue();
				tempAtn.setIdentifier(id);
				tempAtn.setOperation(Operation.valueOf((String) tempJson.get("operation")));
				
				this.addNode(id, tempAtn);
			}

			// 2) Link children to parent and set rootnode as root
			iterator = jsonTree.iterator();
			AttackTreeNode tempParent;
			while (iterator.hasNext()) {
				tempJson = iterator.next();

				int id = ((Long) tempJson.get("ID")).intValue();
				tempAtn = this.nodes.get(id); // the node that the iterator is pointing to

				// find parent, set parent, add itself as child of the parent if the node is not root
				int parentID = ((Long) tempJson.get("parent")).intValue();
				if (id != parentID) {
					tempParent = this.nodes.get(parentID);
					tempAtn.setParent(tempParent); // get parent from nodesmap and set
					tempParent.addChild(tempAtn);
				} else { // root node points to itself, can't add itself as its own child
					this.setRoot(tempAtn);
				}
			}
			
			// 3) Countermeasures
			JSONArray jsonCMMap = (JSONArray) jsonInput.get("countermeasuremap");
			Iterator<JSONObject> mapIterator = jsonCMMap.iterator();
			JSONObject jsonEntry;
			while(mapIterator.hasNext()){
				jsonEntry = mapIterator.next();
				String atnName = (String) jsonEntry.get("appliedto");
				
				List<Countermeasure> cmList = new ArrayList<>();
				JSONArray jsonCMs = (JSONArray) jsonEntry.get("countermeasures");
				Iterator<JSONObject> cmIterator = jsonCMs.iterator();
				JSONObject jsonCM;
				while(cmIterator.hasNext()){
					jsonCM = cmIterator.next();
					Countermeasure cm = new Countermeasure();
					cm.setName((String) jsonCM.get("name"));
					cm.setDescription(Descriptions.countermeasures.get(cm.getName()));
					cm.setApplied((boolean) jsonCM.get("applied"));
					cm.setComplexity(Complexity.valueOf((String) jsonCM.get("complexity")));
					cm.setDifficultyIncrease(((Long) jsonCM.get("difficultyIncrease")).intValue());
					cmList.add(cm);
				}
				nodeCountermeasureMap.put(atnName, cmList);
			}
			
		} catch (ParseException e) {
			
			e.printStackTrace();
		} catch (Exception e){
			AlertDialog.showErrorDialog("Tree Build Error", "The tree could not be build. Error: "+e.getMessage());
			e.printStackTrace();
		}
		recalculateDepth();
		changed();
	}
	
	
	public void changed(){
		setChanged();
		notifyObservers();
	}
	
	// Getters and setters
	public int getAmount() {return amount;}
	public void setAmount(int amount) {	this.amount = amount;	}
	
	public Attacker getAttacker() {return attacker;}
	public void setAttacker(Attacker attacker) {this.attacker = attacker;}

	public Map<String, Attacker> getAttackersMap() {return attackersMap;}
	public void setAttackersMap(Map<String, Attacker> attackersMap) {	this.attackersMap = attackersMap;}

	public Map<Integer,AttackTreeNode> getNodes() {return nodes;}
	public void setNodes(HashMap<Integer,AttackTreeNode> nodes) {this.nodes = nodes;}

	public AttackTreeNode getRoot() {return root;}
	public void setRoot(AttackTreeNode root) {this.root = root;}

	public int getDepth() {	return depth;	}
	
	public AttackTreeNode getNode(int id){return nodes.get(id);}
	
	public int getAmountAppliedCM() {return amountAppliedCM;	}
	public void setAmountAppliedCM(int amountAppliedCM) {	this.amountAppliedCM = amountAppliedCM;	}

	public Map<String, List<Countermeasure>> getNodeCountermeasureMap() {
		return nodeCountermeasureMap;
	}

	public void printTree() {

		String output = "";

		for (AttackTreeNode atn : this.getNodes().values()) {
			output += '\n';
			output += atn.printNode();
		}
		System.out.println("-------TREE-------");
		System.out.println(output);
		System.out.println("---------------");
		

	}

}
