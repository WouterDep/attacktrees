package model;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.Set;

/**
 *  Model class for the attacker
 * @author Wouter
 *
 */
public class Attacker {

	protected String name; // Unique name
	protected HashMap<String,Integer> capabilities;
	protected Set<String> credentials;
	protected Set<String> access;
	protected boolean active;
	
	public Attacker(){
		name = "Default"+(new Date());
		initialize();
	}
	
	public Attacker(String name){
		this.name = name;
		initialize();
	}
	
	/**Sets default parameters*/
	public void initialize() {
		active = true;
		this.capabilities = new HashMap<String,Integer>();
		capabilities.put("Spoof Protocol", 1);
		capabilities.put("Exploit Vulnerability", 1);
		capabilities.put("Discover Vulnerability", 1);
		capabilities.put("Obtain Credentials", 1);
		this.credentials = new HashSet<String>();
		this.access = new HashSet<String>();
	}
	
	/**Copy constructor (called by AttackerSelector)*/
	public Attacker(Attacker original){
		this.name = original.name;
		this.active = original.active;
		//TODO complete if necessary
	} 
	
	/**Initializes attributes based on a jsonObject (which is parsed out of the text file)*/
	@SuppressWarnings("unchecked")
	public void initializeFromJsonObject(JSONObject jsonObject) {
		
		// Name & active
		name = (String) jsonObject.get("name");
		active = (boolean) jsonObject.get("active");
		
		// Access
		JSONArray jsonAccess =(JSONArray) jsonObject.get("access");
		Iterator<String> accessIterator = jsonAccess.iterator();
		while(accessIterator.hasNext()){
			String temp = accessIterator.next();
			access.add(temp);
		} //TODO set stealth to high if attacker has access!!
		
		// Credentials
		JSONArray jsonCredentials = (JSONArray) jsonObject.get("credentials");
		Iterator<String> credentialIterator = jsonCredentials.iterator();
		while (credentialIterator.hasNext()) {
			String temp =credentialIterator.next();
			credentials.add(temp);
		}
		
		// Capabilities
		JSONObject jsonCapabilities =(JSONObject) jsonObject.get("capabilities");
		for(Entry<String, Integer> entry : capabilities.entrySet()){
			capabilities.replace(entry.getKey(),((Long)  jsonCapabilities.get(entry.getKey())).intValue());
		}	
	}
	
	// Getters & Setters	
	public String getName() {return name;}
	public void setName(String name) {	this.name = name;	}

	public boolean isActive() {return active;	}
	public void setActive(boolean active) {this.active = active;}
	
	public HashMap<String,Integer> getCapabilities() {return capabilities;}
	public void setCapabilities(HashMap<String,Integer> capabilities) {this.capabilities = capabilities;}

	public Set<String> getCredentials() {return credentials;}
	public void setCredentials(Set<String> knowledge) {this.credentials = knowledge;}

	public Set<String> getAccess() {return access;}
	public void setAccess(Set<String> access) {this.access = access;}
	
	@Override
	public String toString() {
		String result ="";
		result += ">Capabilities:\n";
		for(Entry<String, Integer> entry : capabilities.entrySet()){
			result+=entry.getKey()+": "+entry.getValue()+"\n";
		}
		result += ">Credentials:\n";
		for(String s : credentials){ result+=s+"\n"; }
		result+=">Access:\n";
		for(String s : access){ result+=s+"\n"; }
		return result;
	}

}
