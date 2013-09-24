package com.francetelecom.rd.holicotestapp;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataListener;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.UIManager;
import javax.swing.SwingConstants;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.francetelecom.rd.holico.logs.Logger;
import com.francetelecom.rd.holico.logs.LoggerFactory;
import com.francetelecom.rd.hlc.Condition;
import com.francetelecom.rd.hlc.HlcConnector;
import com.francetelecom.rd.hlc.HomeBusException;
import com.francetelecom.rd.hlc.Node;
import com.francetelecom.rd.hlc.NodeDiscoveryListener;
import com.francetelecom.rd.hlc.NodeInfo;
import com.francetelecom.rd.hlc.NodeService;
import com.francetelecom.rd.hlc.NodeServiceCallback;
import com.francetelecom.rd.hlc.Resource;
import com.francetelecom.rd.hlc.ResourcePublication;
import com.francetelecom.rd.hlc.Rule;
import com.francetelecom.rd.hlc.RuleDefinitionsListener;
import com.francetelecom.rd.hlc.impl.HomeBusFactory;
import com.francetelecom.rd.hlc.impl.Tools;

import java.awt.Font;
import java.awt.EventQueue;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.JComboBox;

public class HomeGui extends JFrame implements NodeDiscoveryListener, ActionListener, RuleDefinitionsListener{

	private static final long serialVersionUID = 1L;

	private static final int ID_type_Node = 0;
	private static final int ID_type_Device = 1;
	private static final int ID_type_Rule = 2;
	private static final int ID_type_Service = 3;

	private static final String Operator_equal = "==";
	private static final String Operator_not_equal = "!=";
	private static final String Operator_less = "<";
	private static final String Operator_greater = ">";
	private static final String Operator_less_or_equal = "<=";
	private static final String Operator_greater_or_equal = ">=";

	// for gui
	private JPanel contentPane;
	private JTextField textSdsId;
	private JTextField textNodeName;
	private JTextField textManufacturer;
	private JTextField textVersion;
	private JTextField textPublishRes;
	private JTextField textResNewVal;
	private JButton btnPublish;
	private JButton btnUnpublish;
	private JButton btnSetResNewVal;
	private JButton btnAddUpdateRule;
	private JButton btnRemoveRule;
	private JLabel lblNodeStatus;
	private JList jlistNodes;
	private JTextArea textAreaNodeDetail;
	private JComboBox comboConditionOperator;

	public static Boolean ruleAdded = false;
	public final static String ruleAddedTxt = "Rule added";
	public final static String ruleRemovedTxt = "Rule removed";
	public final static String ruleUpdatedTxt = "Rule updated";
	public static Boolean isNodePublished = false;
	public final static String nodePublishTxt = "Node published";
	public final static String nodeUnpublishTxt = "Node unpublished";
	private CustomNodeListModel nodeListModel;

	// for home bus
	private HomeBusFactory busFactory;
	private HlcConnector busConnector;
	// myNode must be static because we have a shutdownhook that will unpublish 
	// the node if the application is closed brutally with ctrl+c 
	public static Node myNode = null;
	private HashMap<String, NodeInfo> listNodes;
	private LinkedList<CustomListElement> nodeListElements;
	private static final String resourceToPublishPath = "Heat.Temperature";
	private static final String resourceToPublishName = "temperature";
	private static final String ruleName = "ResourceValueChange";
	private static final String nodeServiceName = "PublishParameterValueChange";
	//private static String configFilePath = "C:\\Agora\\trunk\\Software\\Demo\\HoLiCoTestApp\\src\\test\\resource\\config.json";
	private static String configFilePath = "config.json";
	private NodeServiceCallback serviceCallback;
	private DefaultListModel resourceValueListModel;

	private static final Logger logger = LoggerFactory.getLogger(HomeGui.class.getName());
	private JLabel lblNodeDetails;
	private JTextField textRuleName;
	private JTextField textConditioResource;
	private JTextField textConditionTargetVal;
	private JList listResourceCurrentValue;
	private JLabel lblRuleStatus;
	private JLabel lblRuleservice;
	private JTextField textServiceCalled;
	private JLabel lblServiceName;
	private JTextField textServiceId;
	private JTextField textServiceName;

	/**
	 * Launch the application
	 */
	public static void main(String[] args) {

		// check for the configuration file path, potentially given as argument 
		if (args.length != 0){
			if (args[0] != null){
				configFilePath = args[0];
			}
		}

		// shutdown hook : in case application's process is terminated
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() { 
				if (isNodePublished){
					// app is abusively closed/interrupted => unpublish node if published on home bus
					try {
						logger.info("Process interrupted, will unpublish node");
						myNode.unPublishFromHomeBus();
					} catch (HomeBusException e) {
						e.printStackTrace();
						logger.error("Could not unpublish node from bus before application interrupetd!\n" + e.getMessage());
					}
				}
			}
		});

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					final HomeGui frame = new HomeGui();

					frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
					// override the close window event
					// from http://stackoverflow.com/questions/9093448/do-something-when-the-close-button-is-clicked-on-a-jframe
					frame.addWindowListener(new java.awt.event.WindowAdapter() {
						@Override
						public void windowClosing(java.awt.event.WindowEvent windowEvent) {
							// TODO when exit, save datamodel state in sds.data
							// but this should normally be done on hlc level
							int exitAnswer = JOptionPane.showConfirmDialog(frame, 
									"Application will be closed!", "Exit", 
									JOptionPane.OK_CANCEL_OPTION,
									JOptionPane.INFORMATION_MESSAGE);
							if ( exitAnswer == JOptionPane.OK_OPTION){
								if (isNodePublished){
									// if node published => unpublish it
									try {
										logger.info("Application closed, will unpublish node");
										frame.myNode.unPublishFromHomeBus();
										isNodePublished = false;
									} catch (HomeBusException e) {
										e.printStackTrace();
										logger.error("Could not unpublish node from bus before application exit!\n" + e.getMessage());
									}
								}
								System.exit(0);
							}
						}
					});		

					isNodePublished = false;
					frame.lblNodeStatus.setText(nodeUnpublishTxt);

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
	public HomeGui() {

		listNodes = new HashMap<String,NodeInfo>();
		nodeListElements = new LinkedList<HomeGui.CustomListElement>();

		resourceValueListModel = new DefaultListModel();

		serviceCallback = new NodeServiceCallback() {

			@Override
			public void onServiceActivated(Object parameter) {
				logger.info("Service PublishValueChanged activated");
				final String temp;
				try {
					temp = busConnector.getHomeLifeContextRoot().
							getChildResourceForPath("HomeLifeContext." 
							+ textPublishRes.getText().toString().trim()).
							getValue().toString();
					
					SwingUtilities.invokeLater(new Runnable(){
						public void run(){
							resourceValueListModel.addElement(temp);
						}
					});
										
					displayResourceListElements();
				} catch (Exception e) {
					logger.error("Error reading parameter " + "HomeLifeContext." 
							+ textPublishRes.getText().toString() 
							+ "\nError : " + e.getMessage());
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


		setResizable(false);
		setTitle("HoLiCo Test App");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 540, 821); 
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		// ------- init node panel ----------------------------------------
		JPanel panelInit = new JPanel();
		panelInit.setBackground(UIManager.getColor("Button.shadow"));
		panelInit.setBounds(10, 11, 512, 198);
		contentPane.add(panelInit);
		panelInit.setLayout(null);

		JLabel lblSdsId = new JLabel("SDS ID (between 1 and 127)*");
		lblSdsId.setBounds(10, 11, 206, 14);
		panelInit.add(lblSdsId);

		textSdsId = new JTextField();
		textSdsId.setBounds(215, 11, 287, 20);
		panelInit.add(textSdsId);
		textSdsId.setColumns(10);

		JLabel lblNodeName = new JLabel("Node name*");
		lblNodeName.setBounds(10, 42, 206, 14);
		panelInit.add(lblNodeName);

		textNodeName = new JTextField();
		textNodeName.setText("AgoraNode");
		textNodeName.setBounds(215, 42, 287, 20);
		panelInit.add(textNodeName);
		textNodeName.setColumns(10);

		JLabel lblManufacturer = new JLabel("Manufacturer*");
		lblManufacturer.setBounds(10, 73, 102, 14);
		panelInit.add(lblManufacturer);

		textManufacturer = new JTextField();
		textManufacturer.setText("Orange");
		textManufacturer.setBounds(107, 70, 137, 20);
		panelInit.add(textManufacturer);
		textManufacturer.setColumns(10);

		JLabel lblVersion = new JLabel("Version*");
		lblVersion.setBounds(284, 73, 71, 14);
		panelInit.add(lblVersion);

		textVersion = new JTextField();
		textVersion.setText("1.0");
		textVersion.setBounds(365, 73, 137, 20);
		panelInit.add(textVersion);
		textVersion.setColumns(10);

		btnPublish = new JButton("Publish");
		btnPublish.addActionListener(this);
		btnPublish.setBounds(215, 166, 137, 23);
		panelInit.add(btnPublish);

		btnUnpublish = new JButton("Unpublish");
		btnUnpublish.addActionListener(this);
		btnUnpublish.setBounds(365, 166, 137, 23);
		panelInit.add(btnUnpublish);

		JLabel lblPublishRes = new JLabel("Publish resource*");
		lblPublishRes.setBounds(10, 104, 129, 14);
		panelInit.add(lblPublishRes);

		textPublishRes = new JTextField();
		textPublishRes.setEditable(false);
		textPublishRes.setText(resourceToPublishPath);
		textPublishRes.setBounds(215, 101, 287, 20);
		panelInit.add(textPublishRes);
		textPublishRes.setColumns(10);

		lblNodeStatus = new JLabel("Node status");
		lblNodeStatus.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblNodeStatus.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNodeStatus.setBounds(13, 170, 192, 14);
		panelInit.add(lblNodeStatus);
		
		JLabel lblPublishResType = new JLabel("int");
		lblPublishResType.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPublishResType.setBounds(177, 104, 32, 14);
		panelInit.add(lblPublishResType);
		
		lblServiceName = new JLabel("Service");
		lblServiceName.setFont(new Font("Arial", Font.PLAIN, 8));
		lblServiceName.setBounds(10, 135, 35, 14);
		panelInit.add(lblServiceName);
		
		textServiceId = new JTextField();
		textServiceId.setEditable(false);
		textServiceId.setBounds(236, 132, 266, 20);
		panelInit.add(textServiceId);
		textServiceId.setColumns(10);
		
		textServiceName = new JTextField();
		textServiceName.setEditable(false);
		textServiceName.setText(nodeServiceName);
		textServiceName.setBounds(46, 132, 180, 20);
		panelInit.add(textServiceName);
		textServiceName.setColumns(10);

		// ------- rule panel --------------------------------------------
		JPanel panelRule = new JPanel();
		panelRule.setBackground(UIManager.getColor("Button.shadow"));
		panelRule.setBounds(10, 220, 512, 161);
		contentPane.add(panelRule);
		panelRule.setLayout(null);

		JLabel lblRuleName = new JLabel("Rule name");
		lblRuleName.setBounds(10, 11, 185, 14);
		panelRule.add(lblRuleName);

		textRuleName = new JTextField();
		textRuleName.setText(ruleName);
		textRuleName.setEditable(false);
		textRuleName.setBounds(218, 8, 284, 20);
		panelRule.add(textRuleName);
		textRuleName.setColumns(10);

		JLabel lblCondition = new JLabel("Condition : Resource Operator TargetValue (eg. Temperature == 0)");
		lblCondition.setBounds(10, 39, 492, 14);
		panelRule.add(lblCondition);

		textConditioResource = new JTextField();
		textConditioResource.setEditable(false);
		textConditioResource.setText(resourceToPublishPath);
		textConditioResource.setBounds(10, 61, 196, 20);
		panelRule.add(textConditioResource);
		textConditioResource.setColumns(10);

		textConditionTargetVal = new JTextField();
		textConditionTargetVal.setText("3000");
		textConditionTargetVal.setBounds(365, 61, 137, 20);
		panelRule.add(textConditionTargetVal);
		textConditionTargetVal.setColumns(10);

		// must not change the order of these operators
		// i use this order so that the element index 
		//is the same as the int value of the static condition.operator_types
		String[] operatorList = {Operator_equal,Operator_greater,Operator_less,Operator_greater_or_equal,Operator_less_or_equal,Operator_not_equal};
		comboConditionOperator = new JComboBox(operatorList);
		comboConditionOperator.setSelectedIndex(2);
		comboConditionOperator.setBounds(218, 61, 137, 20);
		panelRule.add(comboConditionOperator);

		btnAddUpdateRule = new JButton("Add / Update");
		btnAddUpdateRule.setBounds(218, 127, 137, 23);
		btnAddUpdateRule.addActionListener(this);
		panelRule.add(btnAddUpdateRule);

		btnRemoveRule = new JButton("Remove");
		btnRemoveRule.setBounds(365, 127, 137, 23);
		btnRemoveRule.addActionListener(this);
		panelRule.add(btnRemoveRule);

		lblRuleStatus = new JLabel("Rule status");
		lblRuleStatus.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblRuleStatus.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRuleStatus.setBounds(10, 131, 195, 14);
		lblRuleStatus.setText(ruleRemovedTxt);
		panelRule.add(lblRuleStatus);

		lblRuleservice = new JLabel("Service called");
		lblRuleservice.setBounds(10, 95, 161, 14);
		panelRule.add(lblRuleservice);

		textServiceCalled = new JTextField();
		textServiceCalled.setEditable(false);
		textServiceCalled.setText(nodeServiceName);
		textServiceCalled.setBounds(218, 92, 284, 20);
		panelRule.add(textServiceCalled);
		textServiceCalled.setColumns(10);

		// --------- nodes panel ------------------------------------------
		JLabel lblNodes = new JLabel("Node list");
		lblNodes.setBounds(10, 534, 147, 14);
		contentPane.add(lblNodes);


		lblNodeDetails = new JLabel("Node detail");
		lblNodeDetails.setBounds(194, 534, 103, 14);
		contentPane.add(lblNodeDetails);

		textAreaNodeDetail = new JTextArea();
		textAreaNodeDetail.setWrapStyleWord(true);
		textAreaNodeDetail.setLineWrap(true);
		textAreaNodeDetail.setEditable(false);
		//textAreaNodeDetail.setBounds(194, 559, 327, 219);
		//contentPane.add(textAreaNodeDetail);

		JScrollPane scrollNodeDetal = new JScrollPane (textAreaNodeDetail);
		scrollNodeDetal.setBounds(194, 559, 327, 219);
		contentPane.add(scrollNodeDetal);

		// nodeListModel contains the list of nodes
		nodeListModel = new CustomNodeListModel(nodeListElements);
		//jlistNodes = new JList(nodeListModel);
		jlistNodes = new JList();
		jlistNodes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//jlistNodes.setBounds(9, 559, 175, 219);
		//contentPane.add(jlistNodes);
		
		JScrollPane scrollListNodes= new JScrollPane (jlistNodes);
		scrollListNodes.setBounds(9, 559, 175, 219);
		contentPane.add(scrollListNodes);
		
		// listener that displays the selected node's details   
		jlistNodes.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent arg0) {}
			@Override
			public void mousePressed(MouseEvent arg0) {}
			@Override
			public void mouseExited(MouseEvent arg0) {}
			@Override
			public void mouseEntered(MouseEvent arg0) {}
			@Override
			public void mouseClicked(MouseEvent arg0) {
				CustomListElement c = (CustomListElement)jlistNodes.getSelectedValue();
				NodeInfo nodeInfo = listNodes.get(c.getNodeId());
				String nodeDetail = "Name : " + c.getNodeName() 
						+ "\nID : " + c.getNodeId() 
						+ "\nDevice ID : " + nodeInfo.getDeviceId()
						+ "\nAvailability : " + nodeInfo.getAvailability()
						+ "\nKeepAlive : " + nodeInfo.getKeepAlive()
						+ "\nManufacturer : " + nodeInfo.getManufacturer()
						+ "\nVersion : " + nodeInfo.getVersion();
				ResourcePublication[] rp = nodeInfo.getResourcePublications();
				nodeDetail += "\nResource publications : " + rp.length;
				for (int i = 0; i < rp.length; i++){
					nodeDetail += "\n---resource " + (i+1)
							+ "\n------name : " + rp[i].getResourcePath()
							+ "\n------ID : " + rp[i].getId()
							+ "\n------type : " + rp[i].getType();
				}
				NodeService[] ns = nodeInfo.getNodeServices();
				nodeDetail += "\nNode services : " + ns.length;
				for (int i = 0; i < ns.length; i++){
					nodeDetail += "\n---service " + (i+1)
							+ "\n------name : " + ns[i].getName()
							+ "\n------ID : " + ns[i].getNodeServiceId()
							+ "\n------parameter name : " + ns[i].getParameterName()
							+ "\n------parameter type : " + ns[i].getParameterType();
				}
				textAreaNodeDetail.setText(nodeDetail);
				//logger.info(nodeDetail);
			}
		});
		/*jlistNodes.addListSelectionListener(new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent event) {
			//if (!event.getValueIsAdjusting()) {
				//textAreaNodeDetail.setText(jlistNodes.getSelectedValue().toString());
				CustomListElement c = (CustomListElement)jlistNodes.getSelectedValue();
				NodeInfo nodeInfo = listNodes.get(c.getNodeId());
				String nodeDetail = "Name : " + c.getNodeName() 
									+ "\nID : " + c.getNodeId() 
									+ "\nDevice ID : " + nodeInfo.getDeviceId()
									+ "\nAvailability : " + nodeInfo.getAvailability()
									+ "\nKeepAlive : " + nodeInfo.getKeepAlive()
									+ "\nManufacturer : " + nodeInfo.getManufacturer()
									+ "\nVersion : " + nodeInfo.getVersion();
				ResourcePublication[] rp = nodeInfo.getResourcePublications();
				nodeDetail += "\nResource publications : " + rp.length;
				for (int i = 0; i < rp.length; i++){
					nodeDetail += "\n resource " + (i+1)
									+ "\n   name : " + rp[i].getResourcePath()
									+ "\n   ID : " + rp[i].getId()
									+ "\n   type : " + rp[i].getType();
				}
				NodeService[] ns = nodeInfo.getNodeServices();
				nodeDetail += "\nNode services : " + ns.length;
				for (int i = 0; i < ns.length; i++){
					nodeDetail += "\n service " + (i+1)
							        + "\n   name : " + ns[i].getName()
									+ "\n   ID : " + ns[i].getNodeServiceId()
									+ "\n   parameter name : " + ns[i].getParameterName()
									+ "\n   parameter type : " + ns[i].getParameterType();
				}
				textAreaNodeDetail.setText(nodeDetail);
				logger.info(nodeDetail);
			}
		//}
	});*/
		
		// ------ resource panel ------------------------------------------
		JPanel panelResource = new JPanel();
		panelResource.setBackground(UIManager.getColor("Button.shadow"));
		panelResource.setBounds(10, 392, 512, 131);
		contentPane.add(panelResource);
		panelResource.setLayout(null);

		btnSetResNewVal = new JButton("Set");
		btnSetResNewVal.setBounds(434, 35, 56, 23);
		btnSetResNewVal.addActionListener(this);
		panelResource.add(btnSetResNewVal);

		textResNewVal = new JTextField();
		textResNewVal.setBounds(243, 36, 171, 20);
		panelResource.add(textResNewVal);
		textResNewVal.setColumns(10);

		JLabel lblResNewVal = new JLabel("Resource new value");
		lblResNewVal.setBounds(248, 11, 227, 14);
		panelResource.add(lblResNewVal);

		JLabel lblResCurrentVal = new JLabel("Resource current value");
		lblResCurrentVal.setBounds(10, 11, 208, 14);
		panelResource.add(lblResCurrentVal);
		lblResCurrentVal.setBackground(UIManager.getColor("Button.disabledShadow"));

		listResourceCurrentValue = new JList(resourceValueListModel);
		listResourceCurrentValue.setVisibleRowCount(3);
		listResourceCurrentValue.setLayoutOrientation(JList.VERTICAL_WRAP);
		listResourceCurrentValue.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//listResourceCurrentValue.setBounds(10, 38, 216, 80);
		//panelResource.add(listResourceCurrentValue);
		
		JScrollPane scrollListResource = new JScrollPane (listResourceCurrentValue);
		scrollListResource.setBounds(10, 38, 216, 80);
		panelResource.add(scrollListResource);

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// BUTTON PUBLISH
		if (arg0.getSource().equals(btnPublish)){
			// if node already published, do not republish
			if (isNodePublished){
				JOptionPane.showMessageDialog(null, "Node already published!");
				return;
			}
			// check that parameters are correctly filled in

			// parameter sdsId
			if (textSdsId.getText().isEmpty()){
				JOptionPane.showMessageDialog(null, "SDS ID name not valid! \nPlease fill in the SDS ID.");
				return;
			}
			int sdsId = Integer.parseInt(textSdsId.getText().trim());
			if (sdsId < 1 || sdsId > 127){
				JOptionPane.showMessageDialog(null, "SDS ID not valid! \nPlease choose a SDS ID between 1 and 127.");
				return;
			}
			// parameter node name
			String nodeName = textNodeName.getText().trim();
			if (nodeName.isEmpty()){
				JOptionPane.showMessageDialog(null, "Node name not valid! \nPlease fill in the node's name.");
				return;
			}
			// parameter manufacturer
			String manufacturer = textManufacturer.getText().trim();
			if (manufacturer.isEmpty()){
				JOptionPane.showMessageDialog(null, "Manufacturer not valid! \nPlease fill in the manufacturer.");
				return;
			}
			// parameter version
			String version = textVersion.getText().trim();
			if (version.isEmpty()){
				JOptionPane.showMessageDialog(null, "Version not valid! \nPlease fill in the version.");
				return;
			}
			// parameter resource to publish on
			String resourcePath = textPublishRes.getText().trim();
			if (resourcePath.isEmpty()){
				JOptionPane.showMessageDialog(null, "Resource not valid! \nPlease fill in the resource path.");
				return;
			}
			// if here, all parameters are valid
			// go ahead, create node and publish it
			try {
				// get HomeBusFactory object which can publish the Agora node on the home bus
				// no, because if we change the node's name, the now name is not taken into consideration
				// create node only one time
				// in the case where the node is published, unpublished, 
				// and published again, we must not create it again
				// (at the second publication)
				busFactory = new HomeBusFactory(sdsId);
				
				// create node
				//myNode = busFactory.createNode(getNodeId(), getDeviceId(), nodeName);
				myNode = busFactory.createNode(getIdForType(ID_type_Node), 
						getIdForType(ID_type_Device), nodeName);
				myNode.setManufacturer(manufacturer);
				myNode.setVersion(version);
				// declare service for the value change of the publish parameter
				myNode.addNodeService(getIdForType(ID_type_Service), nodeServiceName, false, serviceCallback);
				logger.info("Node is created");
				// declare the resource to publish on
				myNode.addResourcePublication(resourceToPublishName, resourcePath, Resource.TYPE_VALUE_INT);

				// publish node
				myNode.publishOnHomeBus();
				logger.info("Node is published");
				
				busConnector = myNode.getHlcConnector();
				textServiceId.setText(getIdForType(ID_type_Service));
				isNodePublished = true;
				lblNodeStatus.setText(nodePublishTxt);
				// show the bus tree in the text box
				//displayDirectory("", busConnector.getHomeLifeContextRoot());
				//JOptionPane.showMessageDialog(null, "Node published!");
			} catch (Exception e){
				e.printStackTrace();
				logger.error(e.getMessage());
				isNodePublished = false;
				lblNodeStatus.setText(nodeUnpublishTxt);
				JOptionPane.showMessageDialog(null, "Error while publishing the node!\n" + e.getMessage());
			}

			if (isNodePublished) {
				// listen for node discovery
				busConnector.addNodeDiscoveryListener(this); 
				// listen for rule discovery
				busConnector.addRuleDefinitionsListener(this);				
				
				try {
					NodeInfo[] existingNodes = busConnector.getAllNodes(true);
					for(int i = 0; i < existingNodes.length; i++){
						listNodes.put(existingNodes[i].getNodeId(), existingNodes[i]);
						nodeListElements.add(new CustomListElement(existingNodes[i].getNodeId(), existingNodes[i].getAvailability(), existingNodes[i].getName()));
					}
					displayNodeListElements();					
				} catch (HomeBusException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		// BUTTON UNPUBLISH
		else if (arg0.getSource().equals(btnUnpublish)){
			// if node is not published cannot unpublish it
			if (!isNodePublished){
				JOptionPane.showMessageDialog(null, "Node is not published!");
				return;
			}
			// must unpublish node
			try {
				//myNode.removesFromHomeBus();
				myNode.unPublishFromHomeBus();
				// if all goes well
				isNodePublished = false;
				lblNodeStatus.setText(nodeUnpublishTxt);
				//JOptionPane.showMessageDialog(null, "Node unpublished!");

				listNodes.clear();
				nodeListElements.clear();
				displayNodeListElements();
				textAreaNodeDetail.setText("");

			} catch (HomeBusException e) {
				logger.error("Could not unpublish the node! \n" + e.getMessage());
				e.printStackTrace();
				// node unpublishion failed, so node is still published
				isNodePublished = true;
				lblNodeStatus.setText(nodePublishTxt);
				JOptionPane.showMessageDialog(null, "Could not unpublish node! Node still published.");
			}
		}
		// BUTTON SET RESOURCE VALUE
		else if (arg0.getSource().equals(btnSetResNewVal)){
			// if node is not published, the resource value cannot be published
			if (!isNodePublished){
				JOptionPane.showMessageDialog(null, "Node is not published!\nCannot set resource value.");
				return;
			}
			// check that new value is valid, should be int (temperature)
			String resourceNewValue = textResNewVal.getText().trim();
			if (resourceNewValue.isEmpty()){
				JOptionPane.showMessageDialog(null, "Resource new value not valid! \nPlease fill in the resource's new value.");
				return;
			}
			int tempNewVal = Integer.parseInt(resourceNewValue);			
			// if here, the value is valid
			try {
				myNode.publishOnResource(textPublishRes.getText().trim(), new Integer(tempNewVal));
				// if all goes well
				//textResCurrentVal.setText(resourceNewValue);
				//resourceValueListModel.addElement(resourceNewValue);
				//JOptionPane.showMessageDialog(null, "Resource set!");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Could not publish value for resource! \n" + e.getMessage());
			}
		}
		// BUTTON ADD/UPDATE RULE
		else if (arg0.getSource().equals(btnAddUpdateRule)){
			// check to see if the node is published
			// FIXME : an unpublished node can add a rule? what if the service is not existent?
			if (!isNodePublished){
				JOptionPane.showMessageDialog(null, "Node is not published!\nCannot add or update rule.");
				return;
			}
			// verify that fields are correctly filled in
			String ruleName = textRuleName.getText().trim();
			if (ruleName.isEmpty()){
				JOptionPane.showMessageDialog(null, "Rule name not valid! \nPlease fill in the rule name.");
				return;
			}
			String resourceName = textConditioResource.getText().trim();
			if (resourceName.isEmpty()){
				JOptionPane.showMessageDialog(null, "Condition resource name not valid! \nPlease fill in the condition resource name.");
				return;
			}
			int operator  = comboConditionOperator.getSelectedIndex();
			if ((operator < 0) || (operator > 5)){
				JOptionPane.showMessageDialog(null, "Condition operator not valid! \nPlease choose a condition operator.");
				return;
			}
			if (textConditionTargetVal.getText().isEmpty()){
				JOptionPane.showMessageDialog(null, "Condition target value not valid! \nPlease fill in the condition target value.");
				return;
			}
			int targetValue = Integer.parseInt(textConditionTargetVal.getText().trim());
			String serviceCalled = textServiceCalled.getText().trim();
			if (serviceCalled.isEmpty()){
				JOptionPane.showMessageDialog(null, "Service called name not valid! \nPlease fill in the service called name.");
				return;
			}
			// if here all parameters are valid
			// create condition
			try {
				Condition c1 = busFactory.createCondition(operator,
						new Integer(targetValue), 
						myNode.getResourcePublications()[0].getResourcePath());
				Rule r1 = busFactory.createRule(getIdForType(ID_type_Rule),
						ruleName, c1, 
						getIdForType(ID_type_Service), 
						new String("0"), false, myNode.getNodeId());
				logger.info("Condition and rule created");
				if (!ruleAdded){
					busConnector.addRule(r1);
					logger.info("Rule added successfully");
					ruleAdded = true;
					lblRuleStatus.setText(ruleAddedTxt);
				}
				else {
					// rules already added
					busConnector.updateRule(r1);
					logger.info("Rule updated");
					lblRuleStatus.setText(ruleUpdatedTxt);
				}
			} catch (Exception e) {
				logger.error("Error while adding/updating/creating rule condition. Cannot get the resource publication path : " + e.getMessage());
				e.printStackTrace();
				
			}
		}
		// BUTTON REMOVE RULE
		else if (arg0.getSource().equals(btnRemoveRule)){
			if (!ruleAdded){
				JOptionPane.showMessageDialog(null, "Rule not added!\nCannot remove unexistent rule.");
				return;
			}
			try {
				busConnector.removeRule(getIdForType(ID_type_Rule));
				logger.info("Rule removed");
				lblRuleStatus.setText(ruleRemovedTxt);
				ruleAdded = false;
			} catch (Exception e) {
				logger.error("Failed to remove rule. Error : " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	// ------------------- node listener callbacks -----------------------------------
	
	@Override
	public void onNodeArrival(String nodeId) {
		// could be a node from the list that has become available again, or a node that has just been created
		logger.info("callback node arrived : " + nodeId);
		NodeInfo arrivedNode = null;
		try {
			arrivedNode = busConnector.getNode(nodeId);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Could not read node from nodeId = " + nodeId);
		}
		if (!(listNodes.containsKey(nodeId))) {
			//logger.info("will add new arrived node to the list node");
			// arrived node is a new one
			// add it to the list of existent nodes
			listNodes.put(nodeId, arrivedNode);
			// add it to the list with custom elements to display in the node list
			nodeListElements.add(new CustomListElement(nodeId, arrivedNode.getAvailability(), arrivedNode.getName()));
		}
		else {
			// arrived node is already in the list -> update nodeInfo
			listNodes.remove(nodeId);
			listNodes.put(nodeId, arrivedNode);
			// means that he is available again -> update list element
			for (int i=0; i < nodeListElements.size(); i++){
				if (nodeListElements.get(i).getNodeId().equals(nodeId)){
					nodeListElements.get(i).setAvailability(arrivedNode.getAvailability());
					nodeListElements.get(i).setNodeName(arrivedNode.getName());
				}
			}
		}
		displayNodeListElements();
	}

	@Override
	public void onNodeRemoval(String nodeId) {
		logger.info("calback node removed : " + nodeId);
		listNodes.remove(nodeId);
		for (int i=0; i < nodeListElements.size(); i++){
			if (nodeListElements.get(i).getNodeId().equals(nodeId)){
				nodeListElements.remove(i);
			}
		}
		displayNodeListElements();
	}

	@Override
	public void onNodeModification(String nodeId) {
		logger.info("callback node modified : " + nodeId);
		NodeInfo modifiedNode = null;
		try {
			modifiedNode = busConnector.getNode(nodeId);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Could not read node from nodeId = " + nodeId);
		}
		
		logger.info("name " + modifiedNode.getName() 
				+ ", availability " + modifiedNode.getAvailability()
				+ ", keepalive " + modifiedNode.getKeepAlive());
		
		listNodes.remove(nodeId); // remove old nodeinfo
		listNodes.put(nodeId, modifiedNode); // add new node info
		// update the list elements
		for (int i=0; i < nodeListElements.size(); i++){
			if (nodeListElements.get(i).getNodeId().equals(nodeId)){
				nodeListElements.get(i).setAvailability(modifiedNode.getAvailability());
				nodeListElements.get(i).setNodeName(modifiedNode.getName());
			}
		}		
		displayNodeListElements();
	}

	@Override
	public void onNodeUnavailable(String nodeId) {
		logger.info("callback node unavailable : " + nodeId);
		// node has modified its availability, is no longer published on the home bus
		// normally its just the availability that is modified, but i just use the same treatment as for onNodeModification
		NodeInfo modifiedNode = null;
		try {
			modifiedNode = busConnector.getNode(nodeId);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Could not read node from nodeId = " + nodeId);
		}
		listNodes.remove(nodeId); // remove old nodeinfo
		listNodes.put(nodeId, modifiedNode); // add new node info
		// update the list elements
		for (int i=0; i < nodeListElements.size(); i++){
			if (nodeListElements.get(i).getNodeId().equals(nodeId)){
				nodeListElements.get(i).setAvailability(modifiedNode.getAvailability());
				nodeListElements.get(i).setNodeName(modifiedNode.getName());
			}
		}
		displayNodeListElements();
	}

	// ------------------- rule listener callbacks -----------------------------------
	
	@Override
	public void onRuleAdded(String ruleId) {
		logger.info("callback rule added : " + ruleId);		
	}

	@Override
	public void onRuleChanged(String ruleId) {
		logger.info("callback rule modified : " + ruleId);	
	}

	@Override
	public void onRuleRemoved(String ruleId) {
		logger.info("callback rule removed : " + ruleId);	
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

	// ------------------- display in GUI lists -----------------------------------
	
	private void displayNodeListElements(){
		//jlistNodes.setModel(new CustomNodeListModel(new LinkedList<HomeGui.CustomListElement>()));
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				CustomNodeListModel l = new CustomNodeListModel(nodeListElements);
				jlistNodes.setModel(l);
			}
		});
		
	}
	
	private void displayResourceListElements(){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				int lastIndex = listResourceCurrentValue.getModel().getSize()-1;
				if (lastIndex >= 0){
					listResourceCurrentValue.ensureIndexIsVisible(lastIndex);
				}
			}
		});
	}

	public class CustomNodeListModel implements ListModel{

		private LinkedList<CustomListElement> listElements;

		public CustomNodeListModel(LinkedList<CustomListElement> nodeList){
			listElements = nodeList;
		}

		@Override
		public Object getElementAt(int index) {
			return listElements.get(index);
		}

		@Override
		public int getSize() {
			return listElements.size();
		}

		@Override
		public void addListDataListener(ListDataListener arg0) { }

		@Override
		public void removeListDataListener(ListDataListener arg0) {	}

	}

	/**
	 * @author hjpl6323
	 * represents an element printed in the list of existent nodes
	 */
	public class CustomListElement{
		private String nodeId;
		private Integer availability; 
		private String nodeName;

		public CustomListElement(String nodeId, Integer availability, String nodeName){
			this.nodeId = nodeId;
			this.nodeName = nodeName;
			this.availability = availability;
		}

		public String getNodeId() {
			return nodeId;
		}

		public void setNodeId(String nodeId) {
			this.nodeId = nodeId;
		}

		public Integer getAvailability() {
			return availability;
		}

		public void setAvailability(Integer availability) {
			this.availability = availability;
		}

		public String getNodeName() {
			return nodeName;
		}

		public void setNodeName(String nodeName) {
			this.nodeName = nodeName;
		}

		// string used for print in JList
		@Override
		public String toString(){
			String line = (availability==1)?"o":"x";
			return line + "  " + nodeName;
		}

	}
}



