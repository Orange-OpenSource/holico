/**
 * Holico : Proposition d'implementation du HomeBus Holico
 *
 * Module name: com.francetelecom.rd.holico-tools.holico-dashboard-pc
 * Version:     0.4-SNAPSHOT
 *
 * Copyright (C) 2013 Orange
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Orange nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * 	http://opensource.org/licenses/BSD-3-Clause
 */
package com.francetelecom.rd.dashboard.pc;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Panel;

import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.SystemColor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

import javax.swing.border.EtchedBorder;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.francetelecom.rd.holico.logs.Logger;
import com.francetelecom.rd.holico.logs.LoggerFactory;
import com.francetelecom.rd.hlc.Condition;
import com.francetelecom.rd.hlc.HlcConnector;
import com.francetelecom.rd.hlc.HomeBusException;
import com.francetelecom.rd.hlc.Node;
import com.francetelecom.rd.hlc.NodeService;
import com.francetelecom.rd.hlc.Rule;
import com.francetelecom.rd.hlc.RuleDefinitionsListener;
import com.francetelecom.rd.hlc.impl.HomeBusFactory;
import com.francetelecom.rd.hlc.impl.Tools;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.ScrollPaneConstants;

public class DashboardPC extends JFrame implements RuleDefinitionsListener {

	// GUI
	private JPanel contentPane;
	private JPanel ruleTabContent;
	private JPanel rulesContent;
	private JPanel nodeTabContent;
	private JScrollPane scrollRuleList;

	// nodes
	private static Node myNode;
	private static int sdsId;
	private static int defaultSdsId = 30;
	private HomeBusFactory busFactory;
	private HlcConnector busConnector;
	private HashMap<String, JPanel> myRulePanelMap;

	private static final Logger logger = LoggerFactory.getLogger(DashboardPC.class.getName());

	// statics constants
	private static final int ID_type_Node = 0;
	private static final int ID_type_Device = 1;
	private static final int ID_type_Rule = 2;
	private static final int ID_type_Service = 3;
	private static int rulePanel_width = 250;
	private static int rulePanel_height = 90;
	private static int interCellSpace = 10;
	private static int scrollArea_maxWidth = 550;
	private static int scrollArea_maxHeight = 390;

	public static final int TYPE_PARAM   = 0;
	public static final int TYPE_INT     = 1;
	public static final int TYPE_BOOL    = 2;
	public static final int TYPE_STRING  = 3;
	public static final int TYPE_GEN_DIR = 4;
	public static final int TYPE_SPE_DIR = 5;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		// retrieve the sds node id from launch argument 
		if (args.length != 0){
			if (args[0] != null){
				sdsId = Integer.parseInt(args[0]);
				if (sdsId < 1 || sdsId > 126){
					logger.error("Sds ID not valid; must be an int between 1 and 125. " +
							"Application will exit");
					System.exit(0);
				}
			}
		}
		else {
			//logger.error("Sds ID missing from launch command! Application will exit");
			//System.exit(0);
			sdsId = defaultSdsId;
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() { 
				try {
					logger.info("Application close, will unpublish node!");
					myNode.unPublishFromHomeBus();
				} catch (HomeBusException e) {
					logger.error("Application close, could not unpublish node : " + e.getMessage());
					e.printStackTrace();
				}
			}
		});

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					DashboardPC frame = new DashboardPC();

					frame.busFactory = new HomeBusFactory(sdsId);
					try {
						// temporary solution
						// fake solution
						// must remove this sleep after sds will no longer need it
						Thread.sleep(1000);
					} catch (Exception e){
						logger.error("Error while sleep before node publish! \n" + e.getMessage());
					}

					frame.initNode();					
					frame.initDashboardWithRules();

					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public DashboardPC() {

		myRulePanelMap = new HashMap<String, JPanel>();

		setResizable(false);
		setTitle("Home Life Context");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 583, 478);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		ruleTabContent = new JPanel();
		ruleTabContent.setBackground(SystemColor.control);
		ruleTabContent.setBorder(new EmptyBorder(5, 5, 5, 5));
		ruleTabContent.setLayout(null);

		nodeTabContent = new JPanel();
		nodeTabContent.setBackground(SystemColor.control);
		nodeTabContent.setBorder(new EmptyBorder(5, 5, 5, 5));
		nodeTabContent.setLayout(null);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(10, 11, 557, 422);
		tabbedPane.add("Rules", ruleTabContent);
		tabbedPane.setEnabledAt(0, true);

		rulesContent = new JPanel();
		int initialWidth = 2*rulePanel_width + interCellSpace;
		int initialHeight = rulePanel_height;
		rulesContent.setBounds(10, 11, initialWidth, initialHeight);
		//ruleTabContent.add(rulesContent);
		rulesContent.setLayout(new GridLayout(0, 2, interCellSpace, interCellSpace));

		scrollRuleList = new JScrollPane (rulesContent);
		scrollRuleList.setBounds(1, 1, scrollArea_maxWidth, initialHeight);
		ruleTabContent.add(scrollRuleList);

		tabbedPane.add("Nodes", nodeTabContent);
		tabbedPane.setEnabledAt(1, true);
		contentPane.add(tabbedPane);

		/*
		// small rule panel

		JPanel panelRule = new JPanel();
		rulesContent.add(panelRule);
		panelRule.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelRule.setLayout(null);

		JPanel rulePanelServicePhoto = new JPanel();
		rulePanelServicePhoto.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		rulePanelServicePhoto.setBounds(10, 16, 27, 27);
		panelRule.add(rulePanelServicePhoto);

		JLabel ruleLblServiceName = new JLabel("Service friendly name");
		ruleLblServiceName.setFont(new Font("Arial", Font.BOLD, 15));
		ruleLblServiceName.setForeground(new Color(100, 149, 237));
		ruleLblServiceName.setBounds(47, 11, 212, 18);
		panelRule.add(ruleLblServiceName);

		JLabel ruleLblIf = new JLabel("IF");
		ruleLblIf.setForeground(Color.GRAY);
		ruleLblIf.setFont(new Font("Arial", Font.BOLD, 30));
		ruleLblIf.setBounds(10, 49, 27, 35);
		panelRule.add(ruleLblIf);

		JLabel ruleLblConditionParam = new JLabel("Condition parameter");
		ruleLblConditionParam.setFont(new Font("Arial", Font.BOLD, 13));
		ruleLblConditionParam.setForeground(Color.GRAY);
		ruleLblConditionParam.setBounds(47, 49, 192, 35);
		panelRule.add(ruleLblConditionParam);

		JLabel ruleLblOnDevice = new JLabel("on device name");
		ruleLblOnDevice.setForeground(Color.GRAY);
		ruleLblOnDevice.setFont(new Font("Arial", Font.PLAIN, 11));
		ruleLblOnDevice.setBounds(47, 29, 212, 14);
		panelRule.add(ruleLblOnDevice);

		// small add rule panel

		JPanel panelAddRule = new JPanel();
		rulesContent.add(panelAddRule);
		panelAddRule.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelAddRule.setLayout(null);

		JLabel addRuleLbl = new JLabel("Configure new rule");
		addRuleLbl.setHorizontalAlignment(SwingConstants.CENTER);
		addRuleLbl.setForeground(new Color(169, 169, 169));
		addRuleLbl.setFont(new Font("Arial", Font.BOLD, 13));
		addRuleLbl.setBounds(10, 25, 229, 27);
		panelAddRule.add(addRuleLbl);

		JButton addRuleBtn = new JButton("+");
		addRuleBtn.setForeground(new Color(128, 128, 128));
		addRuleBtn.setBounds(100, 57, 41, 23);
		panelAddRule.add(addRuleBtn);
		 */
	}

	private void initNode(){
		try {
			// create node
			//myNode = busFactory.createNode(getNodeId(), getDeviceId(), nodeName);
			myNode = busFactory.createNode(getIdForType(ID_type_Node), 
					getIdForType(ID_type_Device), ("dashboardPC-" + String.valueOf(sdsId)));
			myNode.setManufacturer("Orange");
			myNode.setVersion("1.0");

			//logger.info("Node is created");
			// declare the resource to publish on
			// publish node
			myNode.publishOnHomeBus();
			//logger.info("Node is published");

			busConnector = myNode.getHlcConnector();

		} catch (Exception e){
			System.out.println("error while publishing node : " + e.getMessage() + "\n");
			e.printStackTrace();
		}
	}

	private void initDashboardWithRules(){
		// get existing rules and display them
		try {
			Rule[] ruleList = busConnector.getAllRules();
			for (int i = 0; i < ruleList.length; i++){
				if (!ruleList[i].isPrivate())
					addRulePanelToDashboard(ruleList[i].getId());
			}
		} catch (HomeBusException e) {
			logger.error("Could not retrieve bus rules : " + e.getMessage());
			e.printStackTrace();
		}

		// small add rule panel init
		JPanel panelAddRule = new JPanel();
		panelAddRule.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelAddRule.setLayout(null);

		JLabel addRuleLbl = new JLabel("Configure new rule");
		addRuleLbl.setHorizontalAlignment(SwingConstants.CENTER);
		addRuleLbl.setForeground(new Color(169, 169, 169));
		addRuleLbl.setFont(new Font("Arial", Font.BOLD, 13));
		addRuleLbl.setBounds(10, 25, 229, 27);
		panelAddRule.add(addRuleLbl);

		JButton addRuleBtn = new JButton("+");
		addRuleBtn.setForeground(new Color(128, 128, 128));
		addRuleBtn.setBounds(100, 57, 41, 23);
		panelAddRule.add(addRuleBtn);

		rulesContent.add(panelAddRule);
		myRulePanelMap.put("0", panelAddRule);
		updateRuleListDisplay();

		// add panel elements : photo, service friendly name, IF label, condition parameter
		JPanel panelRule1 = new JPanel();
		panelRule1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelRule1.setLayout(null);

		JPanel rulePanelServicePhoto1 = new JPanel();
		rulePanelServicePhoto1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		rulePanelServicePhoto1.setBounds(10, 16, 27, 27);
		panelRule1.add(rulePanelServicePhoto1);

		String serviceName1 = "Visio TV";
		JLabel ruleLblServiceName1 = new JLabel(serviceName1);
		ruleLblServiceName1.setFont(new Font("Arial", Font.BOLD, 15));
		ruleLblServiceName1.setForeground(new Color(100, 149, 237));
		ruleLblServiceName1.setBounds(47, 11, 212, 18);
		panelRule1.add(ruleLblServiceName1);

		String serviceDeviceOwner1 = "Set-top Box";
		JLabel ruleLblOnDevice1 = new JLabel("on "  + serviceDeviceOwner1);
		ruleLblOnDevice1.setForeground(Color.GRAY);
		ruleLblOnDevice1.setFont(new Font("Arial", Font.PLAIN, 11));
		ruleLblOnDevice1.setBounds(47, 29, 212, 14);
		panelRule1.add(ruleLblOnDevice1);

		JLabel ruleLblIf1 = new JLabel("IF");
		ruleLblIf1.setForeground(Color.GRAY);
		ruleLblIf1.setFont(new Font("Arial", Font.BOLD, 30));
		ruleLblIf1.setBounds(10, 49, 27, 35);
		panelRule1.add(ruleLblIf1);

		// condition 
		String condition1 = "IncomingVoIP" + 
				" = " + "true";
		JLabel ruleLblConditionParam1 = new JLabel(condition1);
		ruleLblConditionParam1.setFont(new Font("Arial", Font.BOLD, 13));
		ruleLblConditionParam1.setForeground(Color.GRAY);
		ruleLblConditionParam1.setBounds(47, 49, 192, 35);
		panelRule1.add(ruleLblConditionParam1);

		myRulePanelMap.put("1", panelRule1);
		rulesContent.add(panelRule1);

		// add panel elements : photo, service friendly name, IF label, condition parameter
		JPanel panelRule2 = new JPanel();
		panelRule2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelRule2.setLayout(null);

		JPanel rulePanelServicePhoto2 = new JPanel();
		rulePanelServicePhoto2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		rulePanelServicePhoto2.setBounds(10, 16, 27, 27);
		panelRule2.add(rulePanelServicePhoto2);

		String serviceName2 = "Wi-Fi Off";
		JLabel ruleLblServiceName2 = new JLabel(serviceName2);
		ruleLblServiceName2.setFont(new Font("Arial", Font.BOLD, 15));
		ruleLblServiceName2.setForeground(new Color(100, 149, 237));
		ruleLblServiceName2.setBounds(47, 11, 212, 18);
		panelRule2.add(ruleLblServiceName2);

		String serviceDeviceOwner2 = "Livebox";
		JLabel ruleLblOnDevice2 = new JLabel("on "  + serviceDeviceOwner2);
		ruleLblOnDevice2.setForeground(Color.GRAY);
		ruleLblOnDevice2.setFont(new Font("Arial", Font.PLAIN, 11));
		ruleLblOnDevice2.setBounds(47, 29, 212, 14);
		panelRule2.add(ruleLblOnDevice2);

		JLabel ruleLblIf2 = new JLabel("IF");
		ruleLblIf2.setForeground(Color.GRAY);
		ruleLblIf2.setFont(new Font("Arial", Font.BOLD, 30));
		ruleLblIf2.setBounds(10, 49, 27, 35);
		panelRule2.add(ruleLblIf2);

		// condition 
		String condition2 = "Absence" + 
				" = " + "true";
		JLabel ruleLblConditionParam2 = new JLabel(condition2);
		ruleLblConditionParam2.setFont(new Font("Arial", Font.BOLD, 13));
		ruleLblConditionParam2.setForeground(Color.GRAY);
		ruleLblConditionParam2.setBounds(47, 49, 192, 35);
		panelRule2.add(ruleLblConditionParam2);

		myRulePanelMap.put("2", panelRule2);
		rulesContent.add(panelRule2);

		updateRuleListDisplay();

		busConnector.addRuleDefinitionsListener(this);
	}

	private void addRulePanelToDashboard(String ruleId){

		if (myRulePanelMap.containsKey(ruleId)){
			// remove it from display as it will be redisplayed
			rulesContent.remove(myRulePanelMap.remove(ruleId));
		}

		Rule newRule;
		try {
			newRule = busConnector.getRule(ruleId);
		} catch (Exception e) {
			logger.error("Could not get new rule (" + ruleId 
					+ ") from bus connector! \n" + e.getMessage()
					+ "Will not display new rule");
			e.printStackTrace();
			return;
		}

		// add panel elements : photo, service friendly name, IF label, condition parameter

		JPanel panelRule = new JPanel();
		panelRule.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelRule.setLayout(null);

		JPanel rulePanelServicePhoto = new JPanel();
		rulePanelServicePhoto.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		rulePanelServicePhoto.setBounds(10, 16, 27, 27);
		panelRule.add(rulePanelServicePhoto);

		String serviceName = "";
		String serviceDeviceOwner = "";
		try {
			String serviceId = newRule.getServiceReference();
			NodeService ns = busConnector.getNodeService(serviceId);
			serviceName = ns.getName();
			serviceDeviceOwner = busConnector.getServiceOwner(serviceId).getName();			
		} catch (Exception e) {
			logger.error("Error when getting new rule's service name! \n" + e.getMessage());
			e.printStackTrace();
		}

		JLabel ruleLblServiceName = new JLabel(serviceName);
		ruleLblServiceName.setFont(new Font("Arial", Font.BOLD, 15));
		ruleLblServiceName.setForeground(new Color(100, 149, 237));
		ruleLblServiceName.setBounds(47, 11, 212, 18);
		panelRule.add(ruleLblServiceName);

		JLabel ruleLblOnDevice = new JLabel("on "  + serviceDeviceOwner);
		ruleLblOnDevice.setForeground(Color.GRAY);
		ruleLblOnDevice.setFont(new Font("Arial", Font.PLAIN, 11));
		ruleLblOnDevice.setBounds(47, 29, 212, 14);
		panelRule.add(ruleLblOnDevice);

		JLabel ruleLblIf = new JLabel("IF");
		ruleLblIf.setForeground(Color.GRAY);
		ruleLblIf.setFont(new Font("Arial", Font.BOLD, 30));
		ruleLblIf.setBounds(10, 49, 27, 35);
		panelRule.add(ruleLblIf);

		// condition 
		Condition c = newRule.getCondition();
		// condition resource
		String[] resource = c.getResourcePath().split("\\.");
		// condition operator
		String operator = "";
		switch (c.getOperator()){
		case Condition.OPERATOR_DIFF : {operator = "!=";break;}
		case Condition.OPERATOR_EQUAL : {operator = "=";break;}
		case Condition.OPERATOR_INF : {operator = "<";break;}
		case Condition.OPERATOR_INFEQUAL : {operator = "<=";break;}
		case Condition.OPERATOR_SUP : {operator = ">";break;}
		case Condition.OPERATOR_SUPEQUAL : {operator = ">=";break;}
		default : {logger.error("Unknown condition operator " + c.getOperator());break;}
		}
		// condition target value
		// FIXME the target value type is an int, that corresponds to static 
		// parameters from SDS that we dont have in Hlc
		// must find a way to make the types constants be available in Hlc
		// temporary solution : declared here
		String targetValue = "";
		switch (c.getTargetValueType()){
		case TYPE_BOOL : {targetValue = String.valueOf(c.getTargetBooleanValue()); break;} 
		case TYPE_INT : {targetValue = String.valueOf(c.getTargetIntValue()); break;}
		case TYPE_STRING : {targetValue = String.valueOf(c.getTargetStringValue()); break;}
		default: {logger.warn("Attention target value type " + c.getTargetValueType() + " not treated!"); break;}
		}

		String condition = resource[(resource.length-1)] + 
				" " + operator + " " + targetValue;
		JLabel ruleLblConditionParam = new JLabel(condition);
		ruleLblConditionParam.setFont(new Font("Arial", Font.BOLD, 13));
		ruleLblConditionParam.setForeground(Color.GRAY);
		ruleLblConditionParam.setBounds(47, 49, 192, 35);
		panelRule.add(ruleLblConditionParam);

		myRulePanelMap.put(ruleId, panelRule);
		rulesContent.add(panelRule);

		updateRuleListDisplay();

	}

	private void removeRulePanelFromDashboard(String ruleId){
		rulesContent.remove(myRulePanelMap.get(ruleId));
		myRulePanelMap.remove(ruleId);
		updateRuleListDisplay();
	}

	private void modifyRulePanelInDashboard(String ruleId){
		addRulePanelToDashboard(ruleId);
	}

	private void updateRuleListDisplay(){

		int nbRows = (int)(myRulePanelMap.size()+1)/2;
		int newHeight = 0;
		int newWidth = 0;

		if (nbRows == 0){
			newHeight = rulePanel_height;
			newWidth = 2*rulePanel_width + interCellSpace;
			rulesContent.setBounds(10, 10, newWidth, newHeight);
			rulesContent.setPreferredSize(new Dimension(newWidth, newHeight));
		}
		else{
			newWidth = 2*rulePanel_width + interCellSpace;
			newHeight = (nbRows-1)*interCellSpace + nbRows*rulePanel_height;
			rulesContent.setBounds(10, 10, newWidth, newHeight);
			rulesContent.setPreferredSize(new Dimension(newWidth, newHeight));
		}

		if (scrollArea_maxHeight < newHeight){
			scrollRuleList.setBounds(1, 1, scrollArea_maxWidth, scrollArea_maxHeight);
		}
		else {
			scrollRuleList.setBounds(1, 1, scrollArea_maxWidth, newHeight + 5);
		}

		rulesContent.revalidate();
		rulesContent.repaint();
	}

	private String getIdForType(int idType){
		String id = "";

		String configFilePath = "configDasboardPC.json";

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
				logger.error("Could not initilize configuration file config.json, \nERROR : " + e.getMessage());
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
			logger.error("ERROR : Bad id type " + idType);
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
			logger.error("Could not retrieve deviceId!\n" + e.getMessage());
		}
		return id;
	}

	@Override
	public void onRuleAdded(String ruleId) {
		try {
			Rule newRule = busConnector.getRule(ruleId);
			if (!newRule.isPrivate())
				addRulePanelToDashboard(ruleId);
		} catch (Exception e) {
			logger.error("Error when new rule added. Could not retrieve rule " +
					"from bus connector! \n" + e.getMessage());
			e.printStackTrace();
		}		
	}

	@Override
	public void onRuleChanged(String ruleId) {
		try {
			// TODO check rule's isPrivate parameter
			// if private the rule must be removed from the list
			// must check if rule already existent and then removal
			modifyRulePanelInDashboard(ruleId);
		} catch (Exception e) {
			logger.error("Error when rule modified. Could not retrieve rule " +
					"from bus connector! \n" + e.getMessage());
			e.printStackTrace();
		}	
	}

	@Override
	public void onRuleRemoved(String ruleId) {
		removeRulePanelFromDashboard(ruleId);
	}
}
