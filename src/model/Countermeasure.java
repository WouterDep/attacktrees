package model;

/**
 *  Model class for Countermeasure
 * @author Wouter
 *
 */
public class Countermeasure {

	private String name;
	private String description;
	private Complexity complexity;
	private int difficultyIncrease;  // +1 = little bit more difficult, +2 = significant impact, +3 = profound impact
	private boolean applied = false;
	
	public Countermeasure(String name, String description, Complexity complexity, int difficultyIncrease) {
		this.name = name;
		this.description = description;
		this.complexity = complexity;
		this.difficultyIncrease = difficultyIncrease;
	}
	
	// Copy constructor
	public Countermeasure(Countermeasure original){
		this.name = original.name;
		this.description = original.description;
		this.complexity = original.complexity;
		this.difficultyIncrease = original.difficultyIncrease;
		this.applied =original.applied;
	}

	public Countermeasure() {}
	
	@Override
	public String toString() {
		return " [" + name + ", " + complexity + ", " + difficultyIncrease + ", " + applied + "]";
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (applied ? 1231 : 1237);
		result = prime * result + difficultyIncrease;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Countermeasure other = (Countermeasure) obj;
		if (applied != other.applied)
			return false;
		if (difficultyIncrease != other.difficultyIncrease)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	//Getters & setters
	public String getName() {return name;}
	public void setName(String name) {	this.name = name;}
	
	public String getDescription() {return description;	}
	public void setDescription(String description) {	this.description = description;	}
	
	public Complexity getComplexity() {return complexity;	}
	public void setComplexity(Complexity complexity) {this.complexity = complexity;}

	public boolean isApplied() {	return applied;}
	public void setApplied(boolean applied) {	this.applied = applied;	}

	public int getDifficultyIncrease() {return difficultyIncrease;}
	public void setDifficultyIncrease(int difficultyIncrease) {	this.difficultyIncrease = difficultyIncrease;}

}
