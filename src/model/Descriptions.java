package model;

import java.util.HashMap;

/**
 * 	Class that provides description for the parameters
 * @author Wouter
 *
 */
public class Descriptions {

	public static HashMap<Integer, String> difficulties = new HashMap<>();
	public static HashMap<Integer, String> stealths = new HashMap<>();
	public static HashMap<Integer, String> capabilityLevels = new HashMap<>();
	public static HashMap<AttackerGoals, String> attackerGoals = new HashMap<>();
	public static HashMap<String, String> countermeasures = new HashMap<>();
	
	static {
		// initialize descriptions of difficulty
		difficulties.put(0, "Unknown");
		difficulties.put(1, "Trivial");
		difficulties.put(2, "Normal");
		difficulties.put(3, "Difficult");
		difficulties.put(4, "Unlikely");
		
		// initialize descriptions of stealth
		stealths.put(0, "Unknown");
		stealths.put(1, "Publicly");
		stealths.put(2, "Detectable");
		stealths.put(3, "Stealthy");
	
		// initialize descriptions of capability level
		capabilityLevels.put(1, "1 - No prior expertise");
		capabilityLevels.put(2, "2 - Little expertise");
		capabilityLevels.put(3, "3 - Moderate expertise");
		capabilityLevels.put(4, "4 - Expert");
		
		// initialize attacker goals
		attackerGoals.put(AttackerGoals.MODIFY_PARAMETER,"Modify parameter");
		attackerGoals.put(AttackerGoals.DENIAL_OF_SERVICE,"Denial-of-Service on systempart");
		attackerGoals.put(AttackerGoals.OBTAIN_ASSET,"Obtain an asset");
		
		// initialize countermeasure descriptions
		countermeasures.put("Physical Isolation",
				"Strong physical isolation (fence, heavy doors) and strict authentication (doors with electronic loc), surveillance (guards, CCTV)");
		countermeasures.put("Physical system hardening",
				"Obstruct unused ports in order to make physical connection impossible.");
		countermeasures.put("Data diode",
				"A data diode physically restricts data flow to only one direction. This provides a physical segmentation with good performance and few maintenance.");
		countermeasures.put("Change firewall policy",
				"Restrict access from this component to the network if unnecessary.");
		countermeasures.put("Avoid IP-address ranges",
				"Avoid ranges of IP-addresses in the firewall rules. An unused but allowed IP-address can be used to sneak through the firewall");
		countermeasures.put("Least privilege",
				"Apply the Principle of Least Privilege: Grant an employee only the minimum privileges required to perform his job.");
		countermeasures.put("Disable unused ports & services",
				"Unused ports and services are a common attack vector. An unused service can contain vulnerabilities, unused ports can have unintended access "
						+ "to other components in the network. Disable both if unnecessary.");
		countermeasures.put("Install IDS/IPS",
				"Intrusion Detection and Prevention systems inspect the network traffic for malware or exploits. IDS alerts suspicious packets, IPS can also drop them, preventing further harm. "
						+ "Some available IDS/IPS devices have anomaly detection functionality. Using statistical analysis of common behaviour, "
						+ "unusual events are detected, might mark unknown threats.");
		countermeasures.put("Install IDS/IPS & Protocol Anomaly Detect.",
				"Intrusion Detection and Prevention systems inspect the network traffic for malware or exploits. IDS alerts suspicious packets, IPS can also drop them, preventing further harm. "
						+ "Monitor network traffic for changing MAC addresses to prevent MitM attacks. Choose for a device with Protocol Anomaly detection.");
		countermeasures.put("Input validation: Path Traversal", "Prevent Path Traversal by defining "
				+ "whitelist of acceptable inputs, reject if not valid or transform. Use prepared statements to avoid SQL injection.");
		countermeasures.put("Input validation: bounds checking", "Make the application resilient for "
				+ "abnormal values: very large, negative, zero, floating point, ... This can often cause a crash of the system if it can't handle valid values.");
		countermeasures.put("Input validation: code injection",
				"Prevent Code Injection by creating statements statically, "
						+ "whitelist of acceptable inputs, reject if not valid or transform. Use prepared statements to avoid SQL injection.");
		countermeasures.put("Input validation: buffer length",
				"Perform a code review of applications that handle network traffic. "
						+ "Check for buffer overflow vulnerabilities by implementing input validation. Inspect the lenght of the input and buffer (esp. for C/C++).");
		countermeasures.put("Restrict outbound traffic",
				"An attacker needs feedback during his attack. If outbound traffic is restricted appropriately, the attacker cannot establish an outbound connection for feedback, "
						+ "and is therefore limited to blind attacks.");
		countermeasures.put("Install updates/patches", "Install software updates. Vulnerable elements "
				+ "will be protected to newly discovered vulnerabilities.");
		countermeasures.put("Static ARP table/Dynamic inspection", "Use hard-coded ARP tables for static IP addresses "
				+ "or dynamic ARP inspection for dynamic IP addresses to prevent MitM attacks.");
		countermeasures.put("Encrypted tunnel",
				"Exchange data within the ICS through an encrypted tunnel. Avoid FTP, telnet and rlogin. Any communication  protocol can be 'tunneled' "
						+ "through SSH. Use HTTPS instead of HTTP.");
		countermeasures.put("Checksums on msgs",
				"Add checksums to the messages (or even better: encrypt/hash messages using secret key)");
		countermeasures.put("Application monitor",
				"Install an application monitor that evaluates the content of the commands and messages using Deep Packet Inspection (DPI). Any suspicious commands are reported.");
		countermeasures.put("Port Security",
				"Check MAC address of connected devices and refuse access if unknown device. Disable unsecured/unused ports. "
						+ "Disable USB ports to prevent malware injection.");
		countermeasures.put("Install Anti-Virus",
				"Prevent installation of malware (Trojan Horse, keylogger, ...) on the workstation of an employee.");
		countermeasures.put("Strong Passwords",
				"Use strong passwords with an expiration time. (See recommendations specified in ISATR99.00.02-2004).");
		countermeasures.put("Change default passwords",
				"A lot of default passwords are published in the vendors documentation.");
		countermeasures.put("VPN for remote connection",
				"Use Virtual Private Network for remote connection to the enterprise. With an unencrypted channel, the credentials could be sniffed.");
		countermeasures.put("Alert/delay on unsuccessful login", "When a series of unsuccesful logins exceeds a certain limit, generate an alert and/or delay the next login.");
		countermeasures.put("Disable LM hash", "Disable LM hashes (on all Windows versions before Vista and Server 2008), because these hashes can be cracked really fast. Prevent automatic "
				+ "generation of LM hash by using a password of at least 15 characters.");
		
	}
}
