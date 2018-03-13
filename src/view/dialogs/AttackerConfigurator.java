package view.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import main.ApplicationMain;
import model.AttackTree;
import model.AttackTreeNode;
import model.Attacker;
import model.Descriptions;

/**
 *  Shows dialog to configure Attacker (capabilities and passwords/access)
 * @author Wouter
 *
 */
public class AttackerConfigurator {
	
	private Attacker attacker;
	private HashMap<String, Integer> capabilities;
	private AttackTree attackTree;

	public AttackerConfigurator(AttackTree attackTree, Attacker attacker) {
		this.attacker = attacker;
		capabilities = new HashMap<>(attacker.getCapabilities()); //deep copy, in case changes are cancelled
		this.attackTree = attackTree;
	}

	public void showDialog() {
		Stage dialog = new Stage();
		dialog.setTitle("Configure Attacker");

		// Controls
			// Picture
		ImageView pictureImageView = new ImageView(new Image("images/hacker2.png"));
		pictureImageView.setFitHeight(50);
		pictureImageView.setFitWidth(50);
		pictureImageView.setDisable(true);
		GridPane.setHalignment(pictureImageView, HPos.CENTER);
			// Name
		Label nameLabel = new Label("Name");
		nameLabel.setFont(new Font(20));
		TextField nameTextField = new TextField(attacker.getName());
		nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
		    if(attackTree.getAttackersMap().containsKey(newValue)) {
		    	AlertDialog.showWarningDialog("Name already exists", "This name already exists. Please choose another name.");
		    	nameTextField.setText(oldValue);
		    }
		});
			// Access
		Label accessTitleLabel = new Label("Physical & Logical Access");
		accessTitleLabel.setPadding(new Insets(10,0,0,0));
		accessTitleLabel.setFont(new Font(20));
		Label accessLabel = new Label("Compontents to which the attacker has physical access to:");
		accessLabel.setWrapText(true);
		accessLabel.setMaxWidth(200d);
		Label credentialLabel = new Label("Credentials the attacker posseses:");
			// Capabilities
		Label capabilitiesLabel = new Label("Capabilities");
		capabilitiesLabel.setPadding(new Insets(10,0,0,0));
		capabilitiesLabel.setFont(new Font(20));
		Label capabilitiesLegendLabel = new Label(Arrays.toString(Descriptions.capabilityLevels.values().toArray()));
		Label spoofLabel = new Label("Spoofing a protocol");
		Label exploitLabel = new Label("Exploiting vulnerabilities");
		Label discoverLabel = new Label("Discovering vulnerabilities");
		Label obtainLabel = new Label("Obtaining credentials");

		// 1) List with CheckBoxes for Physical Access
		ListView<PhysicalAccess> accessListView = new ListView<>();
			// Converter
		StringConverter<PhysicalAccess> accessConverter = new StringConverter<PhysicalAccess>() {
			@Override
			public String toString(PhysicalAccess access) {
				return access.getAccess();
			}
			// not actually used by CheckBoxListCell
			@Override
			public PhysicalAccess fromString(String string) {
				return null;
			}
		};
			// CheckBoxes in every cell
		accessListView.setCellFactory(CheckBoxListCell.forListView(PhysicalAccess::hasAccessProperty, accessConverter));
			// Populate listView
		Set<String> accessSet = getAllAccesses();
		for(String s: accessSet){
			accessListView.getItems().add(new PhysicalAccess(s, attacker.getAccess().contains(s)));
		}
		accessListView.setPrefHeight(accessSet.size() * 24 + 2); // a row is 24px high and 2px margin
		accessListView.setPrefWidth(200d);
		
		
		// 2) List with CheckBoxes for Credentials (knowledge)
		ListView<Credential> credentialListView = new ListView<>();
		// Converter
		StringConverter<Credential> credentialConverter = new StringConverter<Credential>() {
			@Override
			public String toString(Credential credential) {
				return credential.getCredential();
			}

			// not actually used by CheckBoxListCell
			@Override
			public Credential fromString(String string) {
				return null;
			}
		};
		// CheckBoxes in every cell
		credentialListView
				.setCellFactory(CheckBoxListCell.forListView(Credential::hasCredentialProperty, credentialConverter));
		// Populate listView
		Set<String> credentialSet = getAllCredentials();
		for (String s : credentialSet) {
			credentialListView.getItems().add(new Credential(s, attacker.getCredentials().contains(s)));
		}
		credentialListView.setPrefHeight(credentialSet.size() * 24 + 2); // a row is 24px high and 2px margin
		credentialListView.setPrefWidth(200d);
		
		// 3) Sliders for capabilities 
			// Spoofing
		Slider spoofingSlider = new Slider();
		spoofingSlider = new Slider(1, 4, attacker.getCapabilities().get("Spoof Protocol"));
		spoofingSlider.setShowTickLabels(true);
		spoofingSlider.setShowTickMarks(false);
		spoofingSlider.setMajorTickUnit(1);
		spoofingSlider.setMinorTickCount(0);
		spoofingSlider.setBlockIncrement(1);
		spoofingSlider.snapToTicksProperty().set(true);
		spoofingSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
		    capabilities.replace("Spoof Protocol", newValue.intValue());
		});
			// Exploiting
		Slider exploitingSlider = new Slider();
		exploitingSlider = new Slider(1, 4, attacker.getCapabilities().get("Exploit Vulnerability"));
		exploitingSlider.setShowTickLabels(true);
		exploitingSlider.setShowTickMarks(false);
		exploitingSlider.setMajorTickUnit(1);
		exploitingSlider.setMinorTickCount(0);
		exploitingSlider.setBlockIncrement(1);
		exploitingSlider.snapToTicksProperty().set(true);
		exploitingSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			capabilities.replace("Exploit Vulnerability", newValue.intValue());
		});
			// Discovering
		Slider discoveringSlider = new Slider();
		discoveringSlider = new Slider(1, 4, attacker.getCapabilities().get("Discover Vulnerability"));
		discoveringSlider.setShowTickLabels(true);
		discoveringSlider.setShowTickMarks(false);
		discoveringSlider.setMajorTickUnit(1);
		discoveringSlider.setMinorTickCount(0);
		discoveringSlider.setBlockIncrement(1);
		discoveringSlider.snapToTicksProperty().set(true);
		discoveringSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			capabilities.replace("Discover Vulnerability", newValue.intValue());
		});
			// Obtaining
		Slider obtainingSlider = new Slider();
		obtainingSlider = new Slider(1, 4, attacker.getCapabilities().get("Obtain Credentials"));
		obtainingSlider.setShowTickLabels(true);
		obtainingSlider.setShowTickMarks(false);
		obtainingSlider.setMajorTickUnit(1);
		obtainingSlider.setMinorTickCount(0);
		obtainingSlider.setBlockIncrement(1);
		obtainingSlider.snapToTicksProperty().set(true);
		obtainingSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			capabilities.replace("Obtain Credentials", newValue.intValue());
		});
			
		// Save button
		Button okButton = new Button("Save ");
		okButton.setDefaultButton(true);
		okButton.setMaxWidth(Double.MAX_VALUE);
		okButton.setOnAction((event) -> {
			System.out.println("SAVE changes");
				// Add selected accesses to the set of the attacker
			if(!attacker.getName().equals(nameTextField.getText())){
				// If name changed, remove old mapping, insert the new mapping
				attackTree.getAttackersMap().remove(attacker.getName());
				attacker.setName(nameTextField.getText());
				attackTree.getAttackersMap().put(attacker.getName(), attacker);
			}
			attacker.setName(nameTextField.getText());
			System.out.println("attacker name: "+attacker.getName());
			
			attacker.setAccess(getSelectedAccesses(accessListView));
			attacker.setCredentials(getSelectedCredentials(credentialListView));
			attacker.setCapabilities(capabilities);
			// hide dialog
			((Node) (event.getSource())).getScene().getWindow().hide();
		});

		// Cancel button
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction((event) -> ((Node) (event.getSource())).getScene().getWindow().hide());
		cancelButton.setCancelButton(true);
		cancelButton.setMaxWidth(Double.MAX_VALUE);

		// add Controls to GridPane
		GridPane gridPane = new GridPane();
		int row = 0;
		gridPane.add(pictureImageView, 0, row,1,2);
		gridPane.add(nameLabel, 1, row++,1,1);
		gridPane.add(nameTextField, 1, row++);
		gridPane.add(accessTitleLabel, 0, row++,2,1);
		gridPane.add(accessLabel, 0, row);
		gridPane.add(accessListView, 1, row++,2,1);
		gridPane.add(credentialLabel, 0, row);
		gridPane.add(credentialListView, 1, row++,2,1);
		gridPane.add(capabilitiesLabel, 0, row++,2,1); 
		gridPane.add(capabilitiesLegendLabel, 0, row++,2,1);
		gridPane.add(spoofLabel, 0, row);
		gridPane.add(spoofingSlider, 1, row++);
		gridPane.add(exploitLabel, 0, row);
		gridPane.add(exploitingSlider, 1, row++);
		gridPane.add(discoverLabel, 0, row);
		gridPane.add(discoveringSlider, 1, row++);
		gridPane.add(obtainLabel, 0, row);
		gridPane.add(obtainingSlider, 1, row++);
		gridPane.add(okButton, 0, row);
		gridPane.add(cancelButton, 1, row);
		gridPane.setHgap(5);
		gridPane.setVgap(5);
		gridPane.setPadding(new Insets(10));

		Scene scene = new Scene(gridPane, gridPane.getMinWidth(), gridPane.getMinHeight());
		dialog.setScene(scene);
		dialog.initOwner(ApplicationMain.getPrimaryStage());
		dialog.initModality(Modality.WINDOW_MODAL);
		
		dialog.showAndWait();
		
		

	}
	
	private Set<String> getAllAccesses(){
		ArrayList<AttackTreeNode> leaves = attackTree.getLeaves();
		Set<String> accesses =  new HashSet<>();
		for (AttackTreeNode atn : leaves){
			if (atn.getName().contains("Gain physical access")){
				// parse the component out of the string and add to attacker access set
				String[] splits = atn.getName().split("component");
				String component = splits[1].substring(1, splits[1].length());
				accesses.add(component);
			}
		}
		return accesses;
	}
	
	private Set<String> getSelectedAccesses(ListView<PhysicalAccess> listView){
		Set<String> accessSet =  new HashSet<>();
		ObservableList<PhysicalAccess>selectedAccess = listView.getItems().filtered(PhysicalAccess::hasAccess);
		Iterator<PhysicalAccess> iterator =  selectedAccess.iterator();
		while(iterator.hasNext()){
			accessSet.add(iterator.next().getAccess());
		}
		return accessSet;
	}
	
	private Set<String> getAllCredentials(){
		ArrayList<AttackTreeNode> leaves = attackTree.getLeaves();
		Set<String> credentials =  new HashSet<>();
		
		for (AttackTreeNode atn : leaves){
			if (atn.getName().contains("Possess credential")){
				// parse the credential out of the string and check attacker credential set
				String[] splits = atn.getName().split("credential");
				String credential = splits[1].substring(1, splits[1].length());
				credentials.add(credential);
			}
		}
		return credentials;
	}
	
	private Set<String> getSelectedCredentials(ListView<Credential> listView){
		Set<String> credentialSet =  new HashSet<>();
		ObservableList<Credential>selectedCredentials = listView.getItems().filtered(Credential::hasCredential);
		Iterator<Credential> iterator =  selectedCredentials.iterator();
		while(iterator.hasNext()){
			credentialSet.add(iterator.next().getCredential());
		}
		return credentialSet;
	}
	
	// Data type <String, Boolean> that holds the component to which the attacker might have phys. access to.
	private static class PhysicalAccess {
		private StringProperty accessProperty = new SimpleStringProperty();
		private final BooleanProperty hasAccessProperty = new SimpleBooleanProperty();

		public PhysicalAccess(String access, boolean hasAccess) {
			setAccess(access);
			setHasAccess(hasAccess);
		}

		// Values
		public String getAccess() {	return accessProperty.get();}
		public void setAccess(String access) {this.accessProperty.set(access);}

		public boolean hasAccess() {return hasAccessProperty.get();}
		public void setHasAccess(boolean hasAccess) {this.hasAccessProperty.set(hasAccess);}

		// Properties
		@SuppressWarnings("unused")
		public StringProperty accessProperty() {return accessProperty;	}

		public BooleanProperty hasAccessProperty() {return hasAccessProperty;}
	}
	
	// Data type <String, Boolean> that holds the credentials that the attacker might have
	private static class Credential {
		private StringProperty credentialProperty = new SimpleStringProperty();
		private final BooleanProperty hasCredentialProperty = new SimpleBooleanProperty();

		public Credential(String credential, boolean hasCredential) {
			setCredential(credential);
			setHasCredential(hasCredential);
		}

		// Values
		public String getCredential() {	return credentialProperty.get();}
		public void setCredential(String credential) {this.credentialProperty.set(credential);}

		public boolean hasCredential() {return hasCredentialProperty.get();}
		public void setHasCredential(boolean hasCredential) {this.hasCredentialProperty.set(hasCredential);}

		// Properties
		@SuppressWarnings("unused")
		public StringProperty credentialProperty() {return credentialProperty;	}

		public BooleanProperty hasCredentialProperty() {return hasCredentialProperty;}
	}

}