/*
 * Copyright (c) 2008 - 2009 Axesstek, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Axesstek nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.axesstek.ele680.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import ca.axesstek.ele680.usbComm.FtdiComm;
/**
 * MainFrame est une classe qui étend un JFrame pour la création de l’interface graphique de l’application Laboratoire ELE680.
 * 
 * @author      Marc Juneau   
 * 				&nbsp;&nbsp<a href="mailto:marcjuneau@axesstek.com">marcjuneau@axesstek.com</a>
 * @since       1.00
 * @version     1.01, &nbsp 10 octobre 2009
 * <ul>
 * <li> Version 1.00   : Premiere version
 * <li> Version 1.01   : Retrait du code non publique
 * </ul>
 * 
 */
public class MainFrame extends JFrame implements ListSelectionListener, ChangeListener, KeyListener, MouseListener{
	private JFileChooser fileChooser;
	private File openFile;
	private String[] points;
	private Locale locale;
	private SignalPanel sigPanel;
	private ResourceBundle resLang;
	private JButton bConnect,bScan,bDisconnect,bStart,bStop,bReset,bSetSpeed,bSetLvl;
	private JButton bOpen,bLoad,bLoad2;
	private JList devList; 
	private JPanel pCtrl,pConnect,pSignal,pUSB,pConsole;
	private FtdiComm ftdicomm;
	private List<String> listDevSerial;
	private DefaultListModel listModel;
	private JTabbedPane pIndex;
	private JTextArea console;
	private Boolean connected;
	private JTextField tUSBDesc,tUSBSer,tUSBMaxPWR,tUSBVer,tSigSpeed,tSigAtt;
	private JLabel tUSBRxSize,tUSBTxSize,tNbPoints;
	private JPanel infoPane;
	private JButton bUpFTDI;
	private int[] signal;
	private JScrollPane listScrollPane;
	private boolean loaded;
	
	/** Protocole de communication : Octet de debut de communication  */
	public static final Byte START_BYTE = 0x42;
	/** Protocole de communication : Commande START  */
	public static final Byte START_CMD = 0x00;
	/** Protocole de communication : Commande STOP  */
	public static final Byte STOP_CMD = 0x01;
	/** Protocole de communication : Commande RESET  */
	public static final Byte RESET_CMD = 0x02;
	/** Protocole de communication : Commande SPEED  */
	public static final Byte SPEED_CMD = 0x03;
	/** Protocole de communication : Commande ATTENUATION  */
	public static final Byte ATTENUATION_CMD = 0x04;
	/** Protocole de communication : Commande LOAD  */
	public static final Byte LOAD_CMD = 0x05;
	
	private static final long serialVersionUID = 1L;

	/** 
	 * Constructeur
	 * @param lang		Langue pour l'interface 		
	 * @throws HeadlessException 
	 * @since       1.00
	 **/
	public MainFrame(String lang) throws HeadlessException {
		super();
		initialize(lang);
	}

	/** 
	 * La methode Initialize fait l'initialisation des composants de l'interface usager. 
	 * @param lang		Langue pour l'interface
	 * @since       1.00 		
	 **/
	private void initialize(String lang) {
		URL url = MainFrame.class.getResource("img/appicon.png");
		if (url != null) {
		    ImageIcon icon = new ImageIcon(url);
			setIconImage(icon.getImage()); 
		}
		connected = false;
		locale = new Locale(lang,"");
		resLang = ResourceBundle.getBundle("properties.lang",locale);
		 /* Use an appropriate Look and Feel */
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        /* Start runtime OpenGL*/
		Runtime runtime = Runtime.getRuntime();
	    try {
	    	runtime.exec("java -Dsun.java2d.opengl=true");
		} catch (IOException e) {
			e.printStackTrace();
		}
		ftdicomm = new FtdiComm();
		createConsolePane();
		createConnectPane();
		createTabbedPane();
		addPanelsAndButtons();
		setSizeAndPos();
		// Close operation
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		  {
		   public void windowClosing(WindowEvent e)
		     { 
			   if (connected) {
				   disconnect();// to be polite :)
				   System.out.println("Auto disconnect");
			   }
			   System.out.println("Exit");
		       System.exit(0);  
		     }
		  
		public void windowActivated(WindowEvent e)
		     { 		       
			   getContentPane().requestFocus();	
		     }
		  });
       
	}
	/** 
	 * La methode disconnect() termine la communication avec le FTDI
	 * @since       1.00
	 **/
	private void disconnect() {
		connected = false;
		ftdicomm.disconect();
	}
	/** 
	 * La methode createConsolePane() cree la console text pour l'information a afficher
	 * @since       1.00
	 **/
	private void createConsolePane() {
		pConsole = new JPanel();
		pConsole.setLayout(new BorderLayout());
		console = new JTextArea();
		console.setBackground(GUIPrefs.CONSOLE_BACK);
		console.setForeground(GUIPrefs.CONSOLE_FORE);
		JScrollPane jsp = new JScrollPane(console);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jsp.setAutoscrolls(true);
		println(" ========== ELE680 interface USB ========== ");
		JPanel pL = new JPanel();
		JPanel pR = new JPanel();
		pL.setPreferredSize(new Dimension(10,110));
		pR.setPreferredSize(new Dimension(10,110));
		pConsole.add(jsp,BorderLayout.CENTER);
		pConsole.add(pL,BorderLayout.WEST);
		pConsole.add(pR,BorderLayout.EAST);
		JLabel emailmarc = new JLabel("(2009) Marc Juneau    marcjuneau@axesstek.com",JLabel.CENTER);
		emailmarc.addMouseListener(this);
		pConsole.add(emailmarc,BorderLayout.SOUTH);
		
	}
	/** 
	 * La methode println() affiche une String sur la console.
	 * @param string String a afficher
	 * @since       1.00
	 **/
	private void println(String string) {
		console.append(string);
		console.append("\n");
		console.paint(console.getGraphics());
	}
	/** 
	 * La methode addPanelsAndButtons() ajoute les boutons et les panels dans l'interface
	 * @since       1.00
	 **/
	private void addPanelsAndButtons() {
		JPanel pL = new JPanel();
		JPanel pR = new JPanel();
		pL.setPreferredSize(new Dimension(5,470));
		pR.setPreferredSize(new Dimension(5,470));
		setLayout( new BorderLayout(5,5));
		add(pConnect,BorderLayout.NORTH);
		add(pIndex,BorderLayout.CENTER);
		add(pConsole,BorderLayout.SOUTH);
		add(pL,BorderLayout.WEST);
		add(pR,BorderLayout.EAST);
		refreshButtons();
	}
	/** 
	 * La methode setSizeAndPos() positionne et defini la dimension du Frame
	 * @since       1.00
	 **/
	private void setSizeAndPos() {
		setTitle(resLang.getString("titre"));
		setMinimumSize(GUIPrefs.FRAME_SIZE);
		setResizable(false);
	}

	/** 
	 * La methode createConnectPane() cree le panel Connexion.
	 * @since       1.00
	 **/
	private void createConnectPane() {
		bConnect = new JButton(new bConnectAction());
		bScan = new JButton(new bScanAction());
		bDisconnect = new JButton(new bDisconnectAction());
		pConnect = new JPanel();
		pConnect.setLayout(new FlowLayout(FlowLayout.LEFT));
		pConnect.setPreferredSize(GUIPrefs.CONNECT_PANE_SIZE);
		pConnect.add(bConnect);
		pConnect.add(createList());
		pConnect.add(bScan);
		pConnect.add(bDisconnect);
		
	}
	
	/** 
	 * La methode createList() cree la liste des modules detectes
	 * @since       1.00
	 **/
	private JScrollPane createList() {
		listModel = new DefaultListModel();
		scanUSBPort();
      //Create the list and put it in a scroll pane.
        devList = new JList(listModel);
		if (listModel.size() ==0)
			devList.setPreferredSize(new Dimension(120,16));
		else
			devList.setPreferredSize(new Dimension(120,listModel.size()*16));
        devList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        devList.setSelectedIndex(0);
        devList.addListSelectionListener(this);
        devList.setVisibleRowCount(1);
        listScrollPane = new JScrollPane(devList);
        listScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
		return listScrollPane;
	}
	/** 
	 * La methode createTabbedPane() cree le panel avec l'index
	 * @since       1.00
	 **/
	private void createTabbedPane() {
		createCtrlPane();
		createSignalPane();
		createUSBPane();
		pIndex = new JTabbedPane();
		pIndex.setSize(GUIPrefs.INDEX_PANE_SIZE);
		pIndex.addTab(resLang.getString("pCtrl"),pCtrl);
		pIndex.addTab(resLang.getString("pSignal"),pSignal);
		pIndex.addTab(resLang.getString("pUSB"),pUSB);
		pIndex.addChangeListener(this);
	}
	
	/** 
	 * La methode createUSBPane() cree le panel de configuration du FTDI
	 * @since       1.00
	 **/
	private void createUSBPane() {
		bUpFTDI = new JButton(new bUpFTDIAction());
		bUpFTDI.setPreferredSize(new Dimension(100, 30));
		pUSB = new JPanel();
		pUSB.setLayout(new BorderLayout());
		infoPane = new JPanel();
		tUSBDesc = new JTextField("");
		tUSBSer = new JTextField("");
		tUSBMaxPWR = new JTextField("");
		tUSBVer = new JTextField("");
		tUSBDesc.setEditable(true);
		tUSBSer.setEditable(true);
		tUSBMaxPWR.setEditable(true);
		tUSBMaxPWR.addKeyListener(this);
		tUSBVer.setEditable(false);
		infoPane.setLayout(new SpringLayout());
		infoPane.add(new JLabel(resLang.getString("lDesc"),JLabel.TRAILING));
		infoPane.add(tUSBDesc);
		infoPane.add(new JLabel(resLang.getString("lSerial"),JLabel.TRAILING));
		infoPane.add(tUSBSer);
		infoPane.add(new JLabel(resLang.getString("lMaxPwr"),JLabel.TRAILING));
		infoPane.add(tUSBMaxPWR);
		infoPane.add(new JLabel(resLang.getString("lUSBVer"),JLabel.TRAILING));
		infoPane.add(tUSBVer);
		infoPane.setPreferredSize(new Dimension(400,140));
		infoPane.add(new JLabel(""));
		infoPane.add(bUpFTDI);
		
		SpringUtilities.makeCompactGrid(infoPane,
                5, 2,
                10, 10, //init x,y
                10, 5);//xpad, ypad
		pUSB.add(infoPane,BorderLayout.NORTH);
		getFTDIData();
	}

	/** 
	 * La methode createUSBPane() cree le panel de configuration du FTDI
	 * @since       1.00
	 */
	private void createSignalPane() {
		JPanel pl = new JPanel();
		JPanel pr = new JPanel();
		JPanel pbot = new JPanel();
		pl.setSize(GUIPrefs.SIDE_BUFFER);
		pr.setSize(GUIPrefs.SIDE_BUFFER);
		pbot.setSize(GUIPrefs.BOT_BUFFER);
		pSignal = new JPanel();
		pSignal.setLayout(new BorderLayout());
		JPanel toppanel = new JPanel();
		toppanel.setLayout(new SpringLayout());
		bOpen = new JButton(new bOpenAction());
		bLoad = new JButton(new bLoadAction());
		bLoad.setEnabled(false);
		tNbPoints = new JLabel("n/a");
		toppanel.add(bOpen);
		toppanel.add(bLoad);
		toppanel.add(new JLabel(resLang.getString("lNbPoints")));
		toppanel.add(tNbPoints);
		sigPanel = new SignalPanel();
		pSignal.add(sigPanel,BorderLayout.CENTER);
		pSignal.add(pl,BorderLayout.WEST);
		pSignal.add(pr,BorderLayout.EAST);
		pSignal.add(pbot,BorderLayout.SOUTH);
		pSignal.add(toppanel,BorderLayout.NORTH);
		SpringUtilities.makeCompactGrid(toppanel,
                2, 2,
                10, 10, //init x,y
                10, 5);//xpad, ypad
	}
	/** 
	 * La methode createCtrlPane() cree le panel de controle
	 * @since       1.00
	 */
	private void createCtrlPane() {
		pCtrl = new JPanel();
		pCtrl.setLayout(new BorderLayout());
		JPanel toppanel = new JPanel();
		toppanel.setLayout(new SpringLayout());
		bStart = new JButton(new bStartAction());
		bStop = new JButton(new bStopAction());
		bReset = new JButton(new bResetAction());
		bSetSpeed = new JButton(new bSetSpeedAction());
		bSetLvl = new JButton(new bSetLvlAction());
		bLoad2 = new JButton(new bLoadAction());
		tSigSpeed = new JTextField("0");
		tSigSpeed.setEditable(true);
		tSigSpeed.setMaximumSize(new Dimension(75, 20));
		tSigSpeed.setPreferredSize(new Dimension(75, 20));
		tSigSpeed.addKeyListener(this);
		tSigAtt = new JTextField("0");
		tSigAtt.setEditable(true);
		tSigAtt.setMaximumSize(new Dimension(75, 20));
		tSigAtt.setPreferredSize(new Dimension(75, 20));
		tSigAtt.addKeyListener(this);
		tUSBRxSize= new JLabel();
		tUSBRxSize.setMaximumSize(new Dimension(50,20));
		tUSBTxSize= new JLabel();
		tUSBTxSize.setMaximumSize(new Dimension(50,20));
		toppanel.add(new JLabel(""));
		toppanel.add(bStart);
		toppanel.add(new JLabel(""));
		toppanel.add(new JLabel(""));
		toppanel.add(new JLabel(""));
		toppanel.add(bStop);
		toppanel.add(new JLabel(""));
		toppanel.add(new JLabel(""));
		toppanel.add(new JLabel(""));
		toppanel.add(bReset);
		toppanel.add(new JLabel(""));
		toppanel.add(new JLabel(""));
		toppanel.add(new JLabel(""));
		toppanel.add(bSetSpeed);
		toppanel.add(tSigSpeed);
		toppanel.add(new JLabel("0 à 65535"));
		toppanel.add(new JLabel(""));
		toppanel.add(bSetLvl);
		toppanel.add(tSigAtt);
		toppanel.add(new JLabel("0 à 15"));
		toppanel.add(new JLabel(""));
		toppanel.add(bLoad2);
		toppanel.add(new JLabel(""));
		toppanel.add(new JLabel(""));
		SpringUtilities.makeCompactGrid(toppanel,
                6, 4,
                10, 10, //init x,y
                10, 5);//xpad, ypad
		pCtrl.add(toppanel,BorderLayout.NORTH);
	}
	
	/** 
	 * La methode scanUSBPort() interroge la dll FTDI pour obtenir la liste des 
	 * devices presentement detectes sur le port USB
	 * @since       1.00
	 */
	private void scanUSBPort() {
		listDevSerial = ftdicomm.listDevices();
		listModel.removeAllElements();
		if (listDevSerial.size()==0)
			listModel.addElement(resLang.getString("mNoDevice"));
		for (String s : listDevSerial){
			listModel.addElement(s);
			println("Device detected >> " + s);
		}
		if (devList!=null)
		{
			if (listModel.size() ==0)
				devList.setPreferredSize(new Dimension(120,16));
			else
				devList.setPreferredSize(new Dimension(120,listModel.size()*16));
		}
	}

	/** 
	 * La Classe bConnectAction() 
	 * @since       1.00
	 */
	protected class bConnectAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public bConnectAction() {			
			putValue(Action.NAME, resLang.getString("bConnect"));
			putValue(Action.SHORT_DESCRIPTION, resLang.getString("sd.bConnect"));
		}
		public void actionPerformed(ActionEvent e) {
			println("Try to connect...");
			if (listDevSerial.size()>0){
				int sel = devList.getSelectedIndex();
				if (sel >=0){
					if (ftdicomm.connectDev(listDevSerial.get(sel))){
						println("Connected to "+listDevSerial.get(0));
						connected = true;
						refreshButtons();
					}
					else{
						println("! Not connected !");
					}
				}
			}
		}
	}

	/** 
	 * La Classe bDisconnectAction() 
	 * @since       1.00
	 */
	protected class bDisconnectAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public bDisconnectAction() {			
			putValue(Action.NAME, resLang.getString("bDisconnect"));
			putValue(Action.SHORT_DESCRIPTION, resLang.getString("sd.bDisconnect"));
		}

		public void actionPerformed(ActionEvent e) {
			System.out.println("Disconnect");
			
			if (listDevSerial.size()>0){
				if (ftdicomm.disconect()){
					System.out.println(" Disconnected ok ");
					connected = false;
					refreshButtons();
				}
				else{
					System.out.println("Fail to disconnect");
				}
			}
		}
	}
	/** 
	 * La Classe bScanAction() 
	 * @since       1.00
	 */
	protected class bScanAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		
		public bScanAction() {			
			putValue(Action.NAME, resLang.getString("bScan"));
			putValue(Action.SHORT_DESCRIPTION, resLang.getString("sd.bScan"));
		}

		public void actionPerformed(ActionEvent e) {
			scanUSBPort();
		}
	}

	
	/** 
	 * La Classe bStartAction() 
	 * @since       1.00
	 */
	
	protected class bStartAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public bStartAction() {			
			putValue(Action.NAME, resLang.getString("bStart"));
			putValue(Action.SHORT_DESCRIPTION, resLang.getString("sd.bStart"));
		}
		public void actionPerformed(ActionEvent e) {
				if(!ftdicomm.sendByte(START_BYTE)){showErrorCommMessage();}
				if(!ftdicomm.sendByte(START_CMD)){showErrorCommMessage();}
		}
	}
	/** 
	 * La Classe bStopAction() 
	 * @since       1.00
	 */
	protected class bStopAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public bStopAction() {			
			putValue(Action.NAME, resLang.getString("bStop"));
			putValue(Action.SHORT_DESCRIPTION, resLang.getString("sd.bStop"));
		}
		public void actionPerformed(ActionEvent e) {
			if(!ftdicomm.sendByte(START_BYTE)){showErrorCommMessage();}
			if(!ftdicomm.sendByte(STOP_CMD)){showErrorCommMessage();}

		}
	}
	/** 
	 * La Classe bOpenAction() 
	 * @since       1.00
	 */
	protected class bOpenAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		
		
		public bOpenAction() {			
			putValue(Action.NAME, resLang.getString("bOpen"));
			putValue(Action.SHORT_DESCRIPTION, resLang.getString("sd.bOpen"));
		}
		public void actionPerformed(ActionEvent e) {
			String curDir;
			if (openFile == null)
				curDir = new File(new File("t.tmp").getAbsolutePath()).getParentFile().getAbsolutePath();
			else
				curDir = new File(openFile.getAbsolutePath()).getParentFile().getAbsolutePath();
			fileChooser = new JFileChooser(curDir);
			fileChooser.setLocale(locale);
			fileChooser.setFileFilter(new FileFilter() {
				public boolean accept(File file) {
					if (file.isDirectory()) {
						return true;
					} else {
						String name = file.getName();
						int lastIndex = name.lastIndexOf('.');
						if (lastIndex > 0 && lastIndex < name.length() - 1)
							return name.substring(lastIndex + 1).toLowerCase().equals("sig");
						else
							return false;
					}
				}
				public String getDescription() {
					return resLang.getString("file.type");
				}
			});

			int result = fileChooser.showOpenDialog(MainFrame.this);

			if (result == JFileChooser.APPROVE_OPTION) {
				openFile = fileChooser.getSelectedFile();
				importData();
			}
		}
	}
	/** 
	 * La Classe bResetAction() 
	 * @since       1.00
	 */
	protected class bResetAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public bResetAction() {			
			putValue(Action.NAME, resLang.getString("bReset"));
			putValue(Action.SHORT_DESCRIPTION, resLang.getString("sd.bReset"));
		}
		public void actionPerformed(ActionEvent e) {
			if(!ftdicomm.sendByte(START_BYTE)){showErrorCommMessage();}
			if(!ftdicomm.sendByte(RESET_CMD)){showErrorCommMessage();}
		}
	}
	/** 
	 * La Classe bSetSpeedAction() 
	 * @since       1.00
	 */
	protected class bSetSpeedAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public bSetSpeedAction() {			
			putValue(Action.NAME, resLang.getString("bSetSpeed"));
			putValue(Action.SHORT_DESCRIPTION, resLang.getString("sd.bSetSpeed"));
		}
		public void actionPerformed(ActionEvent e) {
			if(!ftdicomm.sendByte(START_BYTE)){showErrorCommMessage();}
			if(!ftdicomm.sendByte(SPEED_CMD)){showErrorCommMessage();}
			byte high = 0;
			byte low = 0;
			int	sample;
			try {
				sample = Integer.parseInt(tSigSpeed.getText());
			}catch (NumberFormatException ne){
				sample = 0 ;
			}
			high = (byte)((sample & 0x00FF00)>>8);
			low  = (byte)((sample & 0x0000FF));
			if(!ftdicomm.sendByte(high)){showErrorCommMessage();}
			if(!ftdicomm.sendByte(low)){showErrorCommMessage();}
		}
	}
	/** 
	 * La Classe bSetLvlAction() 
	 * @since       1.00
	 */
	protected class bSetLvlAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public bSetLvlAction() {			
			putValue(Action.NAME, resLang.getString("bSetLvl"));
			putValue(Action.SHORT_DESCRIPTION, resLang.getString("sd.bSetLvl"));
		}
		public void actionPerformed(ActionEvent e) {
			if(!ftdicomm.sendByte(START_BYTE)){showErrorCommMessage();}
			if(!ftdicomm.sendByte(ATTENUATION_CMD)){showErrorCommMessage();}
			byte low = 0;
			int	sample;
			try {
				sample = Integer.parseInt(tSigAtt.getText());
			}catch (NumberFormatException ne){
				sample = 0 ;
			}
			low  = (byte)((sample & 0x0000FF));
			if(!ftdicomm.sendByte(low)){showErrorCommMessage();}
		}
	}
	/** 
	 * La Classe bLoadAction() 
	 * @since       1.00
	 */
	protected class bLoadAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public bLoadAction() {			
			putValue(Action.NAME, resLang.getString("bLoad"));
			putValue(Action.SHORT_DESCRIPTION, resLang.getString("sd.bLoad"));
		}
		public void actionPerformed(ActionEvent e) {
			if(signal.length==65536){
				final int BUFFER_SIZE = 2 + 64;
				int cnt = 0;
				int sample = 0;
				byte high = 0;
				byte low = 0;
				//byte[] trameBuff = new byte[BUFFER_SIZE];
				byte[] trameBuff = new byte[BUFFER_SIZE];
				pCtrl.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				pConnect.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				while(cnt<signal.length){
					trameBuff[0] = START_BYTE;
					trameBuff[1] = LOAD_CMD;
					
					for ( int i = 0 ; i < 63 ; i+=2){
						sample = signal[cnt];
						
						if (sample > 0x3FFF)
						{
							high = (byte)0x3f;
							low  = (byte)0xff;
					
						}
						else{
							high = (byte)((sample & 0x3F00)>>8);
							low  = (byte)((sample & 0x00FF));
						}
						trameBuff[2+i] =high;
						trameBuff[3+i] =low;
						cnt++;
					}
					if(!ftdicomm.sendTrame(trameBuff)){showErrorCommMessage();}
				}
				pConnect.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				pCtrl.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}

		}
	}
	/** 
	 * La Classe bUpFTDIAction() 
	 * @since       1.00
	 */
	protected class bUpFTDIAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		
		public bUpFTDIAction() {			
			putValue(Action.NAME, resLang.getString("bUpFTDI"));
			putValue(Action.SHORT_DESCRIPTION, resLang.getString("sd.bUpFTDI"));
		}

		public void actionPerformed(ActionEvent e) {
			updateFTDIConfig();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		refreshButtons();
		
	}

	private boolean importData(){
		boolean extractSuccess = true;
		String line ;
		points = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(openFile),"UTF-8"));
			try {
				line = in.readLine() ;
				if (line!=null){
					
					if (line.contains(";")){
						points = line.split(";");
						refreshData();
					}
					else{
						extractSuccess = false;
						JOptionPane.showMessageDialog(this, resLang.getString("mErrorFileType"),resLang.getString("mErrorFileTypeTitle"),JOptionPane.ERROR_MESSAGE);
						println("mErrorFileTypeTitle");
					}
				}
			} catch (IOException e) {
				extractSuccess = false;
				e.printStackTrace();
			}
			
		} catch (UnsupportedEncodingException e) {
			extractSuccess = false;
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			extractSuccess = false;
			e.printStackTrace();
		}

		
		
		return extractSuccess;

	}

	public void showErrorCommMessage() {
		JOptionPane.showMessageDialog(this, resLang.getString("mErrorComm"),resLang.getString("mErrorCommTitle"),JOptionPane.ERROR_MESSAGE);
		
	}
	
	public void refreshData(){
		if (points != null){
			tNbPoints.setText(Integer.toString(points.length));
			if (points.length != 65536){
				tNbPoints.setForeground(Color.red);
				tNbPoints.setText(tNbPoints.getText() +"  " +resLang.getString("mErrorNbPoints"));
				sigPanel.clear();
				bLoad.setEnabled(false);
			}
			else{
				boolean update = true;
				tNbPoints.setForeground(Color.black);
				signal = new int[points.length];
				for (int i = 0 ; i < points.length ; i ++){
					try{
						signal[i] = Integer.parseInt(points[i]);
					}catch (NumberFormatException e){
						update = false;
						JOptionPane.showMessageDialog(this, resLang.getString("mBadNumberFormat"),resLang.getString("mBadNumberFormatTitle"),JOptionPane.ERROR_MESSAGE);
					}
				}
				if (update){
					if (!sigPanel.refreshData(signal)){
						JOptionPane.showMessageDialog(this, resLang.getString("mBadNumberFormat"),resLang.getString("mBadNumberFormatTitle"),JOptionPane.ERROR_MESSAGE);
					}
					bLoad.setEnabled(connected);
					bLoad2.setEnabled(connected);
					
				}
				else{
					bLoad.setEnabled(false);
					bLoad2.setEnabled(false);
				}
				loaded = update;
			
			}
			
		}
	}

	public void refreshButtons() {
		int sel = devList.getSelectedIndex();
		if (sel >=0){
			bConnect.setEnabled(!connected);
		}
		else
			bConnect.setEnabled(false);
		bScan.setEnabled(!connected);
		bDisconnect.setEnabled(connected);
		bUpFTDI.setEnabled(connected);
		getFTDIData();
		bStart.setEnabled(connected);
		bStop.setEnabled(connected);
		bReset.setEnabled(connected);
		bSetSpeed.setEnabled(connected);
		bSetLvl.setEnabled(connected);

	
		bLoad.setEnabled(connected && loaded);
		bLoad2.setEnabled(connected && loaded);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(pIndex)){
			if (pIndex.getSelectedIndex()==2){
				getFTDIData();
			}
		}
		
	}
	private void updateFTDIConfig() {
		if (connected){
			int maxI = -1;
			try{
				maxI = Integer.parseInt((tUSBMaxPWR.getText()));
			}
			catch (NumberFormatException e){
				JOptionPane.showMessageDialog(this, resLang.getString("mBadNumber"),resLang.getString("mBadNumberTitle"),JOptionPane.ERROR_MESSAGE);
			}
			
			if (maxI > 0 && maxI <= 500){
				ftdicomm.writeConfig(tUSBDesc.getText(), tUSBSer.getText(), maxI);
			}
			else{
				JOptionPane.showMessageDialog(this, resLang.getString("mBadLimit"),resLang.getString("mBadLimitTitle"),JOptionPane.ERROR_MESSAGE);
			}
			getFTDIData();
			
			
		}
		
	}

	private void getFTDIData() {
		if (connected){
			ftdicomm.readConfig();
			tUSBDesc.setText(ftdicomm.getDevDescription());
			tUSBSer.setText(ftdicomm.getDevSerial());
			tUSBMaxPWR.setText(ftdicomm.getDevMaxPwr());
			tUSBVer.setText(ftdicomm.getDevUSBVer());
			
		}else{
			
		}
		
	}
	@Override
	public void keyPressed(KeyEvent arg0) {}
	@Override
	public void keyReleased(KeyEvent arg0) {}

	@Override
	public void keyTyped(KeyEvent ke) {
		if (ke.getSource().equals(tUSBMaxPWR)){
			char c = ke.getKeyChar();
			int tmpi;
			if (Character.isDigit(c)){
				String tmp = tUSBMaxPWR.getText() + Character.toString(c);
				try{
					tmpi = Integer.parseInt(tmp);
					if (tmpi >0 && tmpi <=500){
						
					}
					else
						ke.consume();
						
				}catch(NumberFormatException e){}
				
			}
			else if ((int)c != 8 && (int)c != 127 ){
				JOptionPane.showMessageDialog(this, resLang.getString("mBadNumber"),resLang.getString("mBadNumberTitle"),JOptionPane.ERROR_MESSAGE);	
				ke.consume();
			}
			
			
		}
		else if (ke.getSource().equals(tSigSpeed)){
			char c = ke.getKeyChar();
			int tmpi;
			if (Character.isDigit(c)){
				String tmp = tSigSpeed.getText() + Character.toString(c);
				try{
					tmpi = Integer.parseInt(tmp);
					if (tmpi >=65536){
						ke.consume();//tSigSpeed.setText(Integer.toString(tmpi));
					}
				}catch(NumberFormatException e){}
				
			}
			else if ((int)c != 8 && (int)c != 127 ){
				JOptionPane.showMessageDialog(this, resLang.getString("mBadNumber"),resLang.getString("mBadNumberTitle"),JOptionPane.ERROR_MESSAGE);	
				ke.consume();
			}
		}
		else if (ke.getSource().equals(tSigAtt)){
			char c = ke.getKeyChar();
			int tmpi;
			if (Character.isDigit(c)){
				String tmp = tSigAtt.getText() + Character.toString(c);
				try{
					tmpi = Integer.parseInt(tmp);
					if (tmpi > 15){
						ke.consume();
					}
				}catch(NumberFormatException e){}
				
			}
			else if ((int)c != 8 && (int)c != 127){
				JOptionPane.showMessageDialog(this, resLang.getString("mBadNumber"),resLang.getString("mBadNumberTitle"),JOptionPane.ERROR_MESSAGE);	
				ke.consume();
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		pConsole.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		pConsole.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	public static void showURL(String url) {
        try {
            String[] cmd = new String[2];
            cmd[0] = DEFAULT_CMD_LINE;
            cmd[1] = "" + url + "";
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
//oh oh
        }
    }
	public static String DEFAULT_CMD_LINE = System.getProperty("os.name" ).startsWith("Windows" ) ? "explorer " : "mozilla";
	@Override
	public void mousePressed(MouseEvent arg0) {
		showURL("mailto:marcjuneau@axesstek.com");
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}