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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
/**
 * SignalPanel est une classe qui étend un JPanel pour la création
 * d'un panel specialise dans l'application Laboratoire ELE-680
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
public class SignalPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	private double stepx,stepy,maxVal; 
	int[] points = null;
	
	/** 
	 * La methode refreshData recalcule les sauts a utiliser pour l'affichage
	 * @param p		tableau de points (int)
	 * @since       1.00 		
	 */
	public boolean refreshData(int[] p){
		Dimension size = this.getSize();
		maxVal = 16384;
		if (p!=null){
			points = p;
			stepx = points.length / size.width;
			if (stepx < 1)
				stepx = 1;
			
			stepy = (maxVal) / (size.height-2);
		}
		repaint();
		return true;
	}
	
	/** 
	 * La methode paint affiche le signal de points
	 * @param g		graphics a utiliser
	 * @since       1.00 		
	 */
	public void paint ( Graphics g ){
		BufferedImage bim = new BufferedImage(this.getSize().width,this.getSize().height,BufferedImage.TRANSLUCENT);
		Graphics2D g2 = (Graphics2D) bim.getGraphics();
		Dimension size = this.getSize();
		g2.setPaint(Color.black);
		g2.fillRect(0, 0, size.width,size.height);
		g2.setPaint(Color.yellow);
		if (points!=null){
			for ( int i = 0 ; i < size.width && i < points.length ; i++ ){
				g2.fillRect(i,(size.height -2) - (int)(points[(int)(i*stepx)]/stepy), 1,1);
			}
		}
		g.drawImage(bim, 0, 0,this);
	}
	/** 
	 * La methode clear()  
	 * @since       1.00 		
	 **/
	public void clear() {
		points = null;
		repaint();
	}
}
