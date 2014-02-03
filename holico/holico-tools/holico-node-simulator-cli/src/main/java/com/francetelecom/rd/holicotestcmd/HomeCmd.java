package com.francetelecom.rd.holicotestcmd;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.francetelecom.rd.hlc.Condition;
import com.francetelecom.rd.hlc.HlcConnector;
import com.francetelecom.rd.hlc.HomeBusException;
import com.francetelecom.rd.hlc.Node;
import com.francetelecom.rd.hlc.NodeDiscoveryListener;
import com.francetelecom.rd.hlc.NodeInfo;
import com.francetelecom.rd.hlc.NodeServiceCallback;
import com.francetelecom.rd.hlc.Resource;
import com.francetelecom.rd.hlc.Rule;
import com.francetelecom.rd.hlc.RuleDefinitionsListener;
import com.francetelecom.rd.hlc.impl.HomeBusFactory;
import com.francetelecom.rd.hlc.impl.Tools;

import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;


public class HomeCmd implements NodeDiscoveryListener, RuleDefinitionsListener{

	private static final int ID_type_Node = 0;
	private static final int ID_type_Device = 1;
	private static final int ID_type_Rule = 2;
	private static final int ID_type_Service = 3;

	public static Boolean ruleAdded = false;
	public final static String ruleAddedTxt = "Rule added";
	public final static String ruleRemovedTxt = "Rule removed";
	public final static String ruleUpdatedTxt = "Rule updated";
	public static Boolean isNodePublished = false;
	public final static String nodePublishTxt = "Node published";
	public final static String nodeUnpublishTxt = "Node unpublished";

	// for home bus
	private HomeBusFactory busFactory;
	private HlcConnector busConnector;
	// myNode must be static because we have a shutdownhook that will unpublish 
	// the node if the application is closed brutally with ctrl+c 
	public static Node myNode = null;
	private Hashtable<String, NodeInfo> listNodes;
	private static final String resourceToPublishPath = "Heat.Temperature";
	private static final String resourceToPublishName = "temperature";
	private static final String ruleName = "ResourceValueChange";
	private static final String nodeServiceName = "PublishParameterValueChange";
	//private static String configFilePath = "C:\\Agora\\trunk\\Software\\Demo\\HoLiCoTestApp\\src\\test\\resource\\config.json";
	private static String configFilePath = "config.json";
	private NodeServiceCallback serviceCallback;


	// commands and options
	private static String cmdSetResourceValue = "set";
	private static String cmdSetResourceValue_opt = "intValue";
	private static String cmdRule = "rule";
	private static String cmdRule_opt_add = "add";
	private static String cmdRule_opt_remove = "remove";
	private static String cmdExit = "exit";
	
	/**
	 * Launch the application
	 */
	public static void main(String[] args) {

		Scanner c = new Scanner(System.in);
        
		System.out.println("Usage : java -jar <holico-node-simulator-cli.jar>");
		
		System.out.println("possible commands :");
		System.out.println("\t\t set intValue // set intValue on resource " + resourceToPublishPath);
		System.out.println("\t\t rule add // add rule for resource value change detection ");
		System.out.println("\t\t rule remove // remove rule for resource value change detection");
		System.out.println("\t\t exit");
		
		HomeCmd tool = new HomeCmd();
		tool.initNode();
		
		boolean exit = false;
		
		while (!exit){
			System.out.println("enter command!");
			String commandLine = c.nextLine();
			if (commandLine == null){
				continue;
			}
						
			try {
				String command = "";
				String option = "";
				if (commandLine.contains(" ")){
					String[] params = commandLine.split(" ");
					command = params[0];
					option = params[1];
					//System.out.println("1. received command : " + command);
				}
				else {
					command = commandLine.trim();
					//System.out.println("2. received command : " + command);
					if(command.equals(cmdExit)){
						exit = true;
						tool.unpublishNode();
						System.out.println("bye");
					}
				}
				//System.out.println("received command : " + command);
				
				if (command.equals(cmdSetResourceValue)){
					tool.setResourceVal(Integer.parseInt(option));
				}
				else if (command.equals(cmdRule)){
					tool.rule(option);
				}
			}
			catch (Exception e){
				continue;
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.exit(0);
	}

	private void initNode()
	{	
		serviceCallback = new NodeServiceCallback() {

			@Override
			public void onServiceActivated(Object parameter) {
				//logger.info("Service PublishValueChanged activated");
				final String temp;
				try {
					temp = busConnector.getHomeLifeContextRoot().
							getChildResourceForPath("HomeLifeContext." 
							+ resourceToPublishPath).
							getValue().toString();
					System.out.println("resource value changed to : " + temp);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// parameter not needed
			@Override
			public int getParameterType() {
				return Resource.TYPE_VALUE_INT;
			}

			// parameter not needed
			@Override
			public String getParameterName() {
				return "temp";
			}
		};
		
		try {
			// get HomeBusFactory object which can publish the Agora node on the home bus
			// no, because if we change the node's name, the now name is not taken into consideration
			// create node only one time
			// in the case where the node is published, unpublished, 
			// and published again, we must not create it again
			// (at the second publication)
		   String nodeId = getIdForType(ID_type_Node);
			busFactory = new HomeBusFactory(nodeId);

			// create node
			//myNode = busFactory.createNode(getNodeId(), getDeviceId(), nodeName);
			myNode = busFactory.createNode(nodeId, getIdForType(ID_type_Device), ("agoraNode-" + nodeId.substring(0, 4)));
			myNode.setManufacturer("Orange");
			myNode.setVersion("1.0");
			// declare service for the value change of the publish parameter
			myNode.addNodeService(getIdForType(ID_type_Service), nodeServiceName, false, serviceCallback);
			//logger.info("Node is created");
			// declare the resource to publish on
			myNode.addResourcePublication(resourceToPublishName, resourceToPublishPath, Resource.TYPE_VALUE_INT);

			// publish node
			myNode.publishOnHomeBus();
			//logger.info("Node is published");
			
			busConnector = myNode.getHlcConnector();
			isNodePublished = true;
		} catch (Exception e){
			//logger.error(e.getMessage());
			isNodePublished = false;
			System.out.println("error while publishing node : " + e.getMessage());
			e.printStackTrace();
		}

		if (isNodePublished) {
			// listen for node discovery
			busConnector.addNodeDiscoveryListener(this); 
			// listen for rule discovery
			busConnector.addRuleDefinitionsListener(this);				
			
			try {
				NodeInfo[] existingNodes = busConnector.getAllNodes(true);
				for(int i = 0; i < existingNodes.length; i++){
					//String line = existingNodes[i].getAvailability()==1 ? "o " : "x ";
					//line += existingNodes[i].getName() + ", " + existingNodes[i].getNodeId();
					//System.out.println(line);
					listNodes.put(existingNodes[i].getNodeId(), existingNodes[i]);		
				}				
			} catch (HomeBusException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			displayNodes();
		}
		
	}
	
	private void unpublishNode(){
		try {
			//myNode.removesFromHomeBus();
			myNode.unPublishFromHomeBus();
			// if all goes well
			isNodePublished = false;
			listNodes.clear();
		} catch (HomeBusException e) {
			//logger.error("Could not unpublish the node! \n" + e.getMessage());
			e.printStackTrace();
			// node unpublishion failed, so node is still published
			isNodePublished = true;
		}
	}
	
	private void displayNodes(){
		//System.out.println("will list nodes, nb of nodes:" + listNodes.size());
		Iterator<NodeInfo> it = listNodes.values().iterator();
		while (it.hasNext()){
			String line = "";
			NodeInfo n = it.next();
			try {
				if (n.getAvailability() != null){
					line = n.getAvailability()==1 ? "\to " : "\tx ";
				}
				else{
					line = "\tnull ";
				}

				if (n.getName() != null){
					line += n.getName() + ", " + n.getNodeId();
				}
				else{
					line += "null , " + n.getNodeId();
				}
			}
			catch (Exception e){

			}
			System.out.println(line);
		}
	}

	private void setResourceVal(int val){
		try {
			myNode.publishOnResource(resourceToPublishPath, val);
		} catch (Exception e) {
			System.out.println("error when setting resource value : " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void rule(String option){
		
		if (option.equals(cmdRule_opt_add)){
			// add rule
			try {
				Condition c1 = busFactory.createCondition(4,
						new Integer(30000), 
						myNode.getResourcePublications()[0].getResourcePath());
				Rule r1 = busFactory.createRule(getIdForType(ID_type_Rule),
						ruleName, c1, 
						getIdForType(ID_type_Service), 
						new String("0"), false, myNode.getNodeId());
				//logger.info("Condition and rule created");
				if (!ruleAdded){
					busConnector.addRule(r1);
					//logger.info("Rule added successfully");
					ruleAdded = true;
				}
				else {
					// rules already added
					busConnector.updateRule(r1);
				}
			} catch (Exception e) {
				System.out.println("Error while adding/updating/creating rule condition. Cannot get the resource publication path : " + e.getMessage());
				e.printStackTrace();
				
			}
		}
		else if (option.equals(cmdRule_opt_remove)){
			// remove rule
			if (!ruleAdded){
				return;
			}
			try {
				busConnector.removeRule(getIdForType(ID_type_Rule));
				ruleAdded = false;
			} catch (Exception e) {
				System.out.println("Failed to remove rule. Error : " + e.getMessage());
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Create the frame.
	 */
	public HomeCmd() {

		listNodes = new Hashtable<String,NodeInfo>();
		
	}
	
	// ------------------- node listener callbacks -----------------------------------
	
	@Override
	public void onNodeArrival(String nodeId) {
		// could be a node from the list that has become available again, or a node that has just been created
		// logger.info("callback node arrived : " + nodeId);
		System.out.println("node arrived");
		NodeInfo arrivedNode = null;
		try {
			arrivedNode = busConnector.getNode(nodeId);
		} catch (Exception e) {
			//logger.error("Could not read node from nodeId = " + nodeId);
			System.out.println("Could not read node from nodeId = " + nodeId);
			e.printStackTrace();
		}
		if (!(listNodes.containsKey(nodeId))) {
			//logger.info("will add new arrived node to the list node");
			// arrived node is a new one
			// add it to the list of existent nodes
			listNodes.put(nodeId, arrivedNode);
			// add it to the list with custom elements to display in the node list
			//nodeListElements.add(new CustomListElement(nodeId, arrivedNode.getAvailability(), arrivedNode.getName()));
		}
		else {
			// arrived node is already in the list -> update nodeInfo
			listNodes.remove(nodeId);
			listNodes.put(nodeId, arrivedNode);
			// means that he is available again -> update list element
		}
		displayNodes();
	}

	@Override
	public void onNodeRemoval(String nodeId) {
		//logger.info("calback node removed : " + nodeId);
		listNodes.remove(nodeId);
		displayNodes();
	}

	@Override
	public void onNodeModification(String nodeId) {
		//logger.info("callback node modified : " + nodeId);
		NodeInfo modifiedNode = null;
		try {
			modifiedNode = busConnector.getNode(nodeId);
		} catch (Exception e) {
			System.out.println("Could not read node from nodeId = " + nodeId);
			e.printStackTrace();
		}
		
		listNodes.remove(nodeId); // remove old nodeinfo
		listNodes.put(nodeId, modifiedNode); // add new node info
		displayNodes();
	}

	@Override
	public void onNodeUnavailable(String nodeId) {
		//logger.info("callback node unavailable : " + nodeId);
		// node has modified its availability, is no longer published on the home bus
		// normally its just the availability that is modified, but i just use the same treatment as for onNodeModification
		NodeInfo modifiedNode = null;
		try {
			modifiedNode = busConnector.getNode(nodeId);
		} catch (Exception e) {
			System.out.println("Could not read node from nodeId = " + nodeId);
			e.printStackTrace();
		}
		listNodes.remove(nodeId); // remove old nodeinfo
		listNodes.put(nodeId, modifiedNode); // add new node info
		displayNodes();
	}

	// ------------------- rule listener callbacks -----------------------------------
	
	@Override
	public void onRuleAdded(String ruleId) {
		//logger.info("callback rule added : " + ruleId);		
	}

	@Override
	public void onRuleChanged(String ruleId) {
		//logger.info("callback rule modified : " + ruleId);	
	}

	@Override
	public void onRuleRemoved(String ruleId) {
		//logger.info("callback rule removed : " + ruleId);	
	}
	
	// ------------------- get id -----------------------------------
		
	private String getIdForType(int idType){
		String id = "";
	
		if (!(new File(configFilePath).isFile())){
			// configuration file does not exist 
			// => create default one that contains "{}"
			try{
				// create file 
				FileWriter fstream = new FileWriter(configFilePath);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write("{}");
				// close the output stream
				out.close();
			} catch (Exception e){
				//logger.error("Could not initilize configuration file config.json, \nERROR : " + e.getMessage());
				return id;
			}
		}
		String idTypeName = "";
		switch (idType){
		case ID_type_Node : {idTypeName = "nodeId"; break;}
		case ID_type_Device : {idTypeName = "deviceId"; break;}
		case ID_type_Rule : {idTypeName = "ruleId"; break;}
		case ID_type_Service : {idTypeName = "serviceId"; break;}
		default : {// type of id invalid
			//logger.error("ERROR : Bad id type " + idType);
			return id;}
		}
	
		// if not existent, generate it and write it to the configuration file
		try {
			// parse JSON format file and retrieve parameters
			JSONParser parser = new JSONParser();
			// entire JSON object
			JSONObject configObj = (JSONObject)parser.parse((new FileReader(configFilePath)));
			// nodeId and deviceId are generated using hlc-connector-impl Tools.generateId
			if (!configObj.containsKey(idTypeName)){
				// not existent in config file => generate id and write it to file
				id = Tools.generateId();
				configObj.put(idTypeName, new String(id));
			}
			else{
				// existent (already generated a previous time)
				id = configObj.get(idTypeName).toString();
			}
			// re-write configObj to the config file
			// in case the nodeId was generated
			// so just added to the object
			// http://www.roseindia.net/tutorial/java/core/files/fileoverwrite.html
			// 30/04/2013
			File configFile = new File(configFilePath);
			FileOutputStream fos = new FileOutputStream(configFile, false);
			fos.write((configObj.toJSONString()).getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
			//logger.error("Could not retrieve deviceId!\n" + e.getMessage());
		}
		return id;
	}

}



