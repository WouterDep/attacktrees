package controller;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Map.Entry;

/**
 *  Parse the input .idp file and save the contents in the respective attributes for later use.
 * @author Wouter
 *
 */
public class IDPParser {

	private String idpFile;
	
	private String[] component;
	private ArrayList<Entry<String, String>> assetStorage;			// component, parameter/password
	private ArrayList<Entry<String, String>> locatedIn; 					// module, component
	private ArrayList<Entry<String, String>> networkLocation; 		// component, network
	private ArrayList<Entry<String, String>> remoteAccess;			// component, component
	private ArrayList<Entry<String, String>> modulePrerequisite;	// module, prerequisite
	private ArrayList<Entry<String, String>> control; 					// module, parameter
	private ArrayList<Entry<String, String>> authentication; 			// module, password
	private ArrayList<Entry<String, String>> measure;					// sensor, parameter
	private ArrayList<Entry<String, String>> hasToken;					// user, password
	private String[] hasWrongPrivilegesVuln;									// component with WPvuln
	private String[] hasDataLeakageVuln;										// component with DLvuln
	private String[] hasAuthBrokenVuln;											// component with ABvuln
	private String[] hasCodeExecutionVuln;									// component with CEvuln
	private String[] hasDoSVuln;														// component with DoSvuln
	private String[] authorization; 													// password, module, parameter, operation
	private String[] systemPart; 														// components & modules
	
	public IDPParser(String idpFile){
		this.idpFile = idpFile;
	}
	
	/**Store all relevant predicates in memory*/
	public void parseIDP(){
		// Read
		assetStorage = splitDuo(splitSemicolon( "AssetStorage"),",");
		component = stripArrayQuotes(splitSemicolon( "Component"));
		locatedIn = splitDuo(splitSemicolon( "LocatedIn"),"->");
		networkLocation = splitDuo(splitSemicolon( "NetworkLocation"),"->");
		remoteAccess = splitDuo(splitSemicolon( "RemoteAccess"),",");
		modulePrerequisite = splitDuo(splitSemicolon( "ModulePrerequisite"),",");
		control = splitDuo(splitSemicolon( "Control"),",");
		authentication = splitDuo(splitSemicolon( "Authentication"),",");
		measure = splitDuo(splitSemicolon( "Measure"),",");
		hasToken = splitDuo(splitSemicolon( "HasToken"),",");
		hasWrongPrivilegesVuln = stripArrayQuotes(splitSemicolon( "HasWrongPrivilegesVuln"));
		hasDataLeakageVuln = stripArrayQuotes(splitSemicolon( "HasDataLeakageVuln"));
		hasAuthBrokenVuln = stripArrayQuotes(splitSemicolon( "HasAuthBrokenVuln"));
		hasCodeExecutionVuln = stripArrayQuotes(splitSemicolon( "HasCodeExecutionVuln"));
		hasDoSVuln = stripArrayQuotes(splitSemicolon( "HasDoSVuln"));
		authorization = splitSemicolon("Authorization");
		systemPart = stripArrayQuotes(splitSemicolon("SystemPart"));
		
		// Print
		//modulePrerequisite.forEach((entry) -> {System.out.println(entry.getKey()+"&"+entry.getValue());});

	}
	
	/**
	 * Retrieve the values from a given predicate, separated by a semicolon
	 * @param element Predicate whose values are requested
	 * @return array with values of element predicate
	 */
	private String[] splitSemicolon(String element){
		String[] splitFile = idpFile.split(element+ " =");
		String[] splitElement = splitFile[1].split("}");
		splitElement[0] = splitElement[0].substring(3,splitElement[0].length()-1);
		String[] splitSemicolon = splitElement[0].split("; ");
		if(splitSemicolon[0].length()==0) return null; //no elements {  }
		return splitSemicolon;	
	}
	
	/**
	 *  Split a String array of duos and add to arrayList with pairs.
	 * @param duos String array with "left"[delimiter]"right"
	 * @param delimiter ";" or "->" etc.
	 * @return arrayList of SimpleEntry (left,right)
	 */
	private ArrayList<Entry<String, String>> splitDuo(String[] duos, String delimiter){
		if(duos == null) return null;
		ArrayList<Entry<String, String>> tempList = new ArrayList<>();
		for (int i=0;i<duos.length; i++){
			String duo = duos[i];
			if (duo.charAt(0) == ' '){ 
				duo = duo.substring(1, duo.length()); //strip space in front
			}
			String[] split = duo.split(delimiter);
			SimpleEntry<String,String> entry;
			split[0] = stripQuotes(split[0]);
			split[1] = stripQuotes(split[1]);
			entry = new SimpleEntry<String, String>(split[0], split[1]);
			tempList.add(entry);
		}
		
		return tempList;
		
	}
	
	/**Strip the quotes of all elements in the provided array*/
	private String[] stripArrayQuotes(String [] input){
		if(input == null) return null;
		for(int i=0; i<input.length; i++){
			input[i] = stripQuotes(input[i]);
		}
		return input;
	}
	
	/**Strip the beginning and end quote of a string*/
	public String stripQuotes(String item){
		return item.substring(1,item.length()-1);
	}

	//Getters
	public String[] getComponent() {return component;}
	public ArrayList<Entry<String, String>> getAssetStorage() {	return assetStorage;}
	public ArrayList<Entry<String, String>> getLocatedIn() {return locatedIn;}
	public ArrayList<Entry<String, String>> getNetworkLocation() {return networkLocation;}
	public ArrayList<Entry<String, String>> getRemoteAccess() {return remoteAccess;}
	public ArrayList<Entry<String, String>> getModulePrerequisite() {return modulePrerequisite;}
	public ArrayList<Entry<String, String>> getControl() {return control;}
	public ArrayList<Entry<String, String>> getAuthentication() {return authentication;}
	public ArrayList<Entry<String, String>> getMeasure() {return measure;	}
	public ArrayList<Entry<String, String>> getHasToken() {return hasToken;}
	public String[] getHasWrongPrivilegesVuln() {return hasWrongPrivilegesVuln;}
	public String[] getHasDataLeakageVuln() {return hasDataLeakageVuln;}
	public String[] getHasAuthBrokenVuln() {return hasAuthBrokenVuln;}
	public String[] getHasCodeExecutionVuln() {return hasCodeExecutionVuln;	}
	public String[] getAuthorization() {	return authorization;	}
	public String[] getHasDoSVuln() {	return hasDoSVuln; }
	public String[] getSystemPart() { return systemPart;	}
	public boolean hasIDPFile() { return idpFile != null;}
}
