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
package ca.axesstek.ele680.usbComm;

// Inclusion des lib pour la communication
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jd2xx.JD2XX;
import jd2xx.JD2XX.DeviceInfo;
import jd2xx.JD2XX.ProgramData;
/**
 * FtdiComm est une classe qui permet la communication avec un IC FTDI via l'utilisation de la librairie JD2XX
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
public class FtdiComm {
	
	private JD2XX jd;
	private Object[] devs;
	private ProgramData pd;
	/** 
	 * Constructeur
	 * @since       1.01
	 **/
	public FtdiComm(){
		// Creation d'un objet JD2XX. Le jd2xx.dll doit être dans le path!
		try{
			jd = new JD2XX();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	/** 
	 * La methode sendTrame envoie un tableau de byte vers le FTDI 
	 * @param 	b		Tableau de byte
	 * @return True lorsque l'envoie a reussit
	 * @since   1.01 		
	 **/
	public boolean sendTrame(byte[] b){
		try {
			jd.write(b);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	/** 
	 * La methode sendByte envoie un byte vers le FTDI 
	 * @param 	b		Tableau de byte
	 * @return True lorsque l'envoie a reussit
	 * @since   1.01 		
	 **/
	public boolean sendByte(byte b){
		try {
			jd.write(b);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	/** 
	 * La methode getState retourne l'etat du device
	 * @return tableau de int
	 * @since   1.01 		
	 **/
	public int[] getState(){
		try {
			return jd.getStatus();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	/** 
	 * La methode setSerialComm etabli la vitesse de communication.
	 * Presentement fixe a 115200. Devrait etre un param.
	 * @since   1.01 		
	 **/
	public void setSerialComm(){
		try {
			jd.setBaudRate(115200);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** 
	 * La methode writeConfig ecrit la configuration du FTDI
	 * @since   1.01 		
	 **/
	public void writeConfig(String desc, String serial, int maxI){
		if (pd!=null){
			if (maxI <=500 && maxI > 0)
				pd.maxPower = maxI;
			pd.description = desc;
			pd.serialNumber = serial;
			pd.cbus0 = 2;
			pd.cbus1 = 3;
			try {
				jd.eeProgram(pd);
				jd.resetDevice();
			} catch (IOException e) {
				e.printStackTrace();
			}
			readConfig();
		}
	}
	

	/** 
	 * La methode readConfig
	 * @since   1.01 		
	 **/
	public void readConfig() {
		try {
			pd = jd.eeRead();
			
			System.out.println(pd.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("erreur PD");
		}
	
	}
	/** 
	 * La methode getDevInfo
	 * @since   1.01 		
	 **/
	public DeviceInfo getDevInfo(){
		try {
			
			return jd.getDeviceInfo();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/** 
	 * La methode getDevDescription
	 * @since   1.01 		
	 **/
	public String getDevDescription(){
		if (pd!=null){
			return pd.description;
		}
		else	
			return "";
 	}

	/** 
	 * La methode getDevSerial
	 * @since   1.01 		
	 **/
	public String getDevSerial(){
		if (pd!=null){
			return pd.serialNumber;
		}
		else	
			return "";
 	}

	/** 
	 * La methode getDevMaxPwr
	 * @since   1.01 		
	 **/
	public String getDevMaxPwr(){
		if (pd!=null){
			return Integer.toString(pd.maxPower);
		}
		else	
			return "";
 	}

	/** 
	 * La methode getDevUSBVer
	 * @since   1.01 		
	 **/
	public String getDevUSBVer(){
		if (pd!=null){
			String s = Integer.toHexString(pd.usbVersion);
			return (s.charAt(0) + "." + s.charAt(1) + s.charAt(2));
		}
		else	
			return "";
 	}

	/** 
	 * La methode connectDev
	 * @since   1.01 		
	 **/
	public boolean connectDev(String devName){
		if (jd != null){
			try {
				jd.openBySerialNumber(devName);
				setSerialComm();
				jd.setEventNotification(JD2XX.EVENT_RXCHAR | JD2XX.EVENT_MODEM_STATUS, 0);
				jd.purge(JD2XX.PURGE_RX);
				jd.purge(JD2XX.PURGE_TX);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	/** 
	 * La methode disconect()
	 * @since   1.01 		
	 **/
	public boolean disconect(){
		if (jd != null){
			try {
				jd.close();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	
	/** 
	 * La methode listDevices()
	 * @since   1.01 		
	 **/
	public List<String> listDevices() {
		List<String> liste = new ArrayList<String>();
		jd.rescan();
		if (jd.createDeviceInfoList()>0){
			try {
				devs = jd.listDevicesBySerialNumber();
				for (int i=0; i<devs.length; ++i){
					liste.add(devs[i].toString());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return liste;
	}
}
