package model;

import java.util.ArrayList;
import java.util.List;

/**
 *  Model class for a Node
 * @author Wouter
 *
 */
public class AttackTreeNode {

	protected int identifier;
	protected String name = "";
	protected int difficulty = 0; 		//based on attacker 
	protected int difficultyCM = 0; 	// added difficulty by countermeasures
	protected int difficultyEdit = 0; 	// changed with Edit Node (avoids erasure when recalculated)
	protected int stealth = 0; 			// each node has default value
	protected int stealthEdit = 0; 	// changed with Edit Node (avoids erasure when recalculated)
	
	private Operation operation = Operation.LEAF;
	protected int level;
	protected boolean easy = false;
	protected boolean stealthy = false;
	
	protected List<AttackTreeNode> children = new ArrayList<>();
	protected AttackTreeNode parent = null;
	
	protected int x1;
	protected int x2;
	protected int y1;
	protected int y2;
	private boolean visible = true;
	private boolean childrenVisible = true;
	

	public AttackTreeNode(String name){
		this.name = name;
		this.difficulty = 0;
	}
	
	public AttackTreeNode(String name, int difficulty){
		this.name = name;
		this.difficulty = difficulty;
	}
	
	public AttackTreeNode(String name, int difficulty, Operation operation){
		this.name = name;
		this.difficulty = difficulty;
		this.operation = operation;
	}
	
	//copy constructor
	public AttackTreeNode(AttackTreeNode original) {
		this.name = original.getName();
		this.difficulty = original.getDifficulty();
		this.operation = original.getOperation();
		this.level = original.getLevel();
		this.children = new ArrayList<>();
		/*for(AttackTreeNode child : original.getChildren()) {
			AttackTreeNode temp = new AttackTreeNode(child);
			children.add(temp);
			//temp.setParent(this);
			//System.out.println("Parent of "+temp.name+" is "+this.name);
			
		}*/
		//this.children = new ArrayList<>(original.getChildren());
		//this.parent = new AttackTreeNode(original.parent); //no recursion!!
		this.identifier = original.getIdentifier();
		this.x1 = original.getX1();
		this.x2 = original.getX2();
		this.y1 = original.getY1();
		this.y2 = original.getY2();
		this.easy = original.isEasy();
		this.difficultyCM = original.difficultyCM;
		this.difficultyEdit = original.difficultyEdit;
		this.stealth = original.stealth;
		this.stealthEdit = original.stealthEdit;
		this.stealthy = original.stealthy;
	}

	/**Adds an Attack Tree Node to the node*/
	public void addChild(AttackTreeNode child){
		children.add(child);
		child.setParent(this);
		child.setLevel(this.getLevel()+1);
		//System.out.println("Added child: "+child.printNode());
	}
	
	//---------- VISIBILITY ----------------
	
	/**Collapse children if they are visible, or expand them if otherwise*/
	public void toggleVisibilityChildren() {
		//System.out.println("toggle visibility children of "+name);
		if(childrenVisible) collapseChildren();
		else expandChildren();
	}	
	
	/**Set children (and their subtrees) to visible*/
	private void expandChildren(){
		childrenVisible = true;
		for(AttackTreeNode n :children){
			n.setVisible(true);
			n.expandChildren();
		}
	}
	
	/**Set children (and their subtrees) to invisible*/
	private void collapseChildren(){
		childrenVisible = false;
		for(AttackTreeNode n :children){
			n.setVisible(false);
			n.collapseChildren();
		}
	}
	
	/**Collapse all children that are not part of the current attack path*/
	public void hideUnmarkedNodes(Heuristics heuristic){
		switch (heuristic){
			case EASIEST: 			if(!easy) collapseChildren(); break;
			case STEALTHIEST: if(!stealthy) collapseChildren();	break;
			case BOTH: 					if(!easy && !stealthy) collapseChildren(); break;
		}
		
		for(AttackTreeNode atn : children){
			atn.hideUnmarkedNodes(heuristic);
		}
	}
	
	//---------------- DIFFICULTY ---------------
	
	/**Get sum of difficulty caused by attacker, countermeasures and possible adjustment */
	public int getTotalDifficulty(){
		int temp = difficulty + difficultyCM + difficultyEdit;
		if(temp <= 1) temp = 1;
		if(temp > 4) temp = 4;
		return temp;
	}
	
	/**Recursive method that calculates total difficulty based on children*/
	protected void calculateDifficulty() {
		this.setEasy(false);
		for (AttackTreeNode atn : this.getChildren()) {
			atn.setEasy(false);
		}
		
		// System.out.println(this.printNode());
		switch (this.getOperation()) {

		case OR: // OR-relation
			// the difficulty of the parent (this) is the minimum difficulty of
			// its children (only easiest child matters)
			int minDifficulty = 999;
			// iterate over children and set this difficulty to minimum of
			// children
			for (AttackTreeNode atn : this.getChildren()) {
				if (this.getIdentifier() != atn.getIdentifier()) { 
					// root is its own parent
					atn.calculateDifficulty();
					if (atn.getTotalDifficulty() < minDifficulty) {
						minDifficulty = atn.getTotalDifficulty();
					}
				}
			}
			this.setDifficulty(minDifficulty); // min diff of child + its countermeasures
			
			break;
		case AND: // AND-relation
					// the difficulty of the parent (this) is the maximum of all
					// its children (all must be executed)
			int maxDifficulty = 0;
			for (AttackTreeNode atn : this.getChildren()) {
				if (this.getIdentifier() != atn.getIdentifier()) {
					atn.calculateDifficulty();
					if (atn.getTotalDifficulty() > maxDifficulty) {
						maxDifficulty = atn.getTotalDifficulty();
					}
				}
			}
			this.setDifficulty(maxDifficulty); //max diff of children (incl.  their countermeasures)
			break;

		default: 	break;
		}

	}
	
	/**Recursive method that marks easiest path*/
	protected void markEasiestPath() {

		this.setEasy(true);
		// boolean flag = false; // use flag if only one of the children with
		// same difficulty may be displayed

		if (operation == Operation.OR) { // OR relation
			// find easiest child and go further from it
			int minimumDifficulty = 6;
			// search minimum difficulty amongst children
			for (AttackTreeNode atn : this.getChildren()) {
				if (identifier != atn.getIdentifier()) { // root is his own child
					if (atn.getTotalDifficulty() < minimumDifficulty) {
						minimumDifficulty = atn.getTotalDifficulty();
					}
				}
			}
			// set flag on the node with this minimum difficulty and call this
			// method recursively on its children
			for (AttackTreeNode atn : this.getChildren()) {
				if (identifier != atn.getIdentifier()) {
					if (atn.getTotalDifficulty() == minimumDifficulty) { // && !flag
																			
						atn.markEasiestPath();
						// flag = true;
					}
				}
			}
		} else if (operation == Operation.AND) { // AND relation
			// go further from all this children
			for (AttackTreeNode atn : this.getChildren()) {
				if (this.getIdentifier() != atn.getIdentifier()) {
					atn.markEasiestPath();
				}
			}
		}

	}
	
	//------------------ STEALTH ----------------------
	
	/**Get total of stealth and a possible adjustment*/
	public int getTotalStealth(){
		int temp = stealth + stealthEdit;
		if(temp < 1) temp =1;
		if(temp > 3) temp = 3;
		return temp;
	}
	
	/**Recursive method that calculates stealth based on children*/
	protected void calculateStealth(){
		this.setStealthy(false);
		for (AttackTreeNode atn : this.getChildren()) {
			atn.setStealthy(false);
		}
		// System.out.println(this.printNode());
		switch (this.getOperation()) {

		case OR: // OR-relation
			// the stealth of the parent (this) is the MAXIMUM stealth of
			// its children (only easiest child matters)
			int maximumStealth = 0; 
			// iterate over children and set this stealth to maximum of children
			for (AttackTreeNode atn : this.getChildren()) {
				if (this.getIdentifier() != atn.getIdentifier()) { // root is its own parent
					atn.calculateStealth();
					if (atn.getTotalStealth() > maximumStealth) {
						maximumStealth = atn.getTotalStealth();
					}
				} 
			}
			this.setStealth(maximumStealth); // MAX stealth of child

			break;
		case AND: // AND-relation
					// the Stealth of the parent (this) is the MINIMUM of all
					// its children (all must be executed)
			int minStealth = 999;
			for (AttackTreeNode atn : this.getChildren()) {
				if (this.getIdentifier() != atn.getIdentifier()) {
					atn.calculateStealth();
					if (atn.getTotalStealth() < minStealth) {
						minStealth = atn.getTotalStealth();
					}
				}
			}
			this.setStealth(minStealth); //MIN Stealth of children

			break;

		default: 	break;
		}
	}
	
	/**Recursive method that marks stealthiest path*/
	protected void markStealthiestPath() {

		this.setStealthy(true);
		// boolean flag = false; // use flag if only one of the children with
		// same Stealth may be displayed

		if (operation == Operation.OR) { // OR relation
			// find stealthiest child and go further from it
			int maxStealth = 0;
			// search maximum Stealth amongst children
			for (AttackTreeNode atn : this.getChildren()) {
				if (identifier != atn.getIdentifier()) { // root is his own child
					if (atn.getTotalStealth() > maxStealth) {
						maxStealth = atn.getTotalStealth();
					}
				}
			}
			// call this method recursively on the children of this max stealth node
			for (AttackTreeNode atn : this.getChildren()) {
				if (identifier != atn.getIdentifier()) {
					if (atn.getTotalStealth() == maxStealth) { // && !flag
																			
						atn.markStealthiestPath();
						// flag = true;
					}
				}
			}
		} else if (operation == Operation.AND) { // AND relation
			// go further from all this children
			for (AttackTreeNode atn : this.getChildren()) {
				if (this.getIdentifier() != atn.getIdentifier()) {
					atn.markStealthiestPath();
				}
			}
		}

	}
	
	/**Check if this node is part of a path with a provided heuristic*/
	public boolean isParthOfPath(Heuristics heuristic){
		switch(heuristic){
		case EASIEST: return easy; 
		case STEALTHIEST: return stealthy; 
		case BOTH: return easy && stealthy; 
		default: return false;
		}
	}
	
	//Getters & Setters, printNode, addListener
	public int getX1() {return x1;	}
	public void setX1(int x1) {	this.x1 = x1;}

	public int getX2() {return x2;	}
	public void setX2(int x2) {	this.x2 = x2;}
	
	public int getY1() {	return y1;}
	public void setY1(int y1) {this.y1 = y1;	}

	public int getY2() {return y2;	}
	public void setY2(int y2) {this.y2 = y2;	}

	public boolean isEasy() {return easy;}
	public void setEasy(boolean easy) {	this.easy = easy;}

	public int getLevel() {	return level;}
	public void setLevel(int level) {	this.level = level;}
	
	public int getIdentifier() {	return identifier;}
	public void setIdentifier(int identifier) {	this.identifier = identifier;	}
	
	public String getName() {	return name;	}
	public void setName(String name) {	this.name = name;}

	public int getDifficulty() {	return difficulty;	}
	public void setDifficulty(int difficulty) {	this.difficulty = difficulty;}

	public Operation getOperation() {	return operation;}
	public void setOperation(Operation operation) {	this.operation = operation;}

	public List<AttackTreeNode> getChildren() {return children;}
	public void setChildren(List<AttackTreeNode> children) {		this.children = children;	}

	public AttackTreeNode getParent() {	return parent;	}
	public void setParent(AttackTreeNode parent) {	this.parent = parent;	}

	public boolean isVisible() {		return visible;	}
	public void setVisible(boolean visible) {	this.visible = visible;	}
	
	public boolean areChildrenVisible() {	return childrenVisible;	}
	public void setChildrenVisible(boolean childrenVisible) {this.childrenVisible = childrenVisible;	}

	public int getDifficultyCM() {return difficultyCM;}
	public void setDifficultyCM(int difficultyDelta) {this.difficultyCM = difficultyDelta;}

	public int getDifficultyEdit() {return difficultyEdit;}
	public void setDifficultyEdit(int difficyltyEdit) {this.difficultyEdit = difficyltyEdit;}
	
	public int getStealth() {return stealth;}
	public void setStealth(int stealth) {this.stealth = stealth;}

	public int getStealthEdit() {	return stealthEdit;	}
	public void setStealthEdit(int stealthEdit) {this.stealthEdit = stealthEdit;}

	public boolean isStealthy() {return stealthy;}
	public void setStealthy(boolean stealthy) {	this.stealthy = stealthy;	}

	public String printNode(){
		
		String output = "";
		output += "node.name = " + this.getName() + '\n';
		output += "node.difficulty = " + this.getDifficulty() + '\n';
		output += "node.diffCM = " + this.difficultyCM + '\n';
		output += "node.totalDiff = " + this.getTotalDifficulty() + '\n';
		output += "node.operation = " + this.getOperation() + '\n';
		output += "node.level = " + this.getLevel() + '\n';
		output += "node.identifier = " + this.getIdentifier() + '\n';

		if(this.identifier!=0) output += "node.parent = " + this.getParent().getIdentifier() + '\n';
		return output;
	}

	
}
