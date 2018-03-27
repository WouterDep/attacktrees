# Attack Tree Generation and Evaluation for CPS
JavaFX tool for Attack Tree generation and evaluation for CPS systems. First a system model is moduled in *SysML*, which is then parsed to the *IDP* format using the *FAST-CPS* framework. The output of this framework, i.e. the system model and vulnerabilities, forms the input of this tool. An example input file is provided on this repository: *inputfile.idp*.
## Documentation
### JavaFX tool
The documentation of the source code can be found [here](https://wouterdep.github.io/attacktrees/api-docs/ "JavaFX Tool JavaDocs").
### Countermeasures
The documentation of all implemented countermeasures (description and mapping to attack tree nodes) can be found [here](https://wouterdep.github.io/attacktrees/cm-docs/ "Countermeasure Documentation").
### Templates
The used templates can be found [here](https://wouterdep.github.io/attacktrees/template-docs/ "Template Documentation")
## Runnable tool (.jar)
The runnable .jar, an IDP-inputfile and a demo attack tree can be downloaded [here](https://drive.google.com/file/d/1SX90KAGA6yTwASNY24ps0fIBtPGB540K/view?usp=sharing "Runnable .jar").
### Usage
#### Attack Tree Generation
Once the application is booted, an inputfile is selected. Click on 'Select IDP file' and browse to the desired file (f.i. 'inputfile.idp' which can be found together with the .jar). Subsequently, an attacker goal must be chosen, which can be accomplished by clicking 'Set attacker goal'. In this dialog, an attacker goal and parameter can be picked. After confirmation, the attack tree should be generated automatically. Collapse or expand a subtree by clicking on the arrow on the bottom of a node.
#### Attack Tree Assessment
![alt text](/docs/images/tool.PNG "Tool for Attack Tree Assessment")

To assess the attack tree, an attacker (or multiple) must be modelled. To this end, click on 'Configure Attackers' to edit or add attackers. After confirmation, the difficulties are calculated and the attack path can be found by selecting 'easiest path' and clicking on 'Find path'. Collapse all subtrees that aren't part of the attack path by clicking 'Settings > Collapse grey nodes'.
#### Dashboard
![alt text](/docs/images/dashboard.PNG "Dashboard for Attack Tree Assessment")

To see an overview of some security metrics, click on 'Dashboard'. The top section consists of tiles with security indicators. A lower view shows an intelligent list of countermeasure suggestions. Click on a checkbox to activate the corresponding countermeasure. The effect on the overall security should be visible immediately. Another tab enlists the attack path, and a third tab is dedicated to the attackers. Click on the corresponding checkboxes to activate multiple attackers, in order to simulate collaborations.
#### Other functionality
The attack tree and modelled attackers and countermeasures can be saved in a text file with JSON format, by selecting "Save tree as..." from the File menu. This instance can be opened later for further usage, by selecting "Open saved tree" from the file menu. The attack tree can be saved as .PNG by clicking 'File > Export as PNG image'.
