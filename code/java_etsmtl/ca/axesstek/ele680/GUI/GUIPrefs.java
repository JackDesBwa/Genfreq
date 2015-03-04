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
/**
 * GUIPrefs est une classe qui regroupe les dimensions et l’information générale de l’application principale.
 * 
 * @author      Marc Juneau   
 * 				&nbsp;&nbsp<a href="mailto:marcjuneau@axesstek.com">marcjuneau@axesstek.com</a>
 * @version     1.00 ,&nbsp 27 aout 2009
 * @since       1.00
 */
public class GUIPrefs {
	/** Largeur de la fenetre en pixel */
	public static final int FRAME_W = 450;
	/** Hauteur de la fenetre en pixel */
	public static final int FRAME_H = 430;
	/** Version courante */
	public static final long 		VERSION 			= 101L;
	/** Dimension du frame */
	public static final Dimension 	FRAME_SIZE    		= new Dimension(FRAME_W,FRAME_H);
	/** Dimension du panel connexion */
	public static final Dimension   CONNECT_PANE_SIZE = new Dimension(FRAME_W-40,40);
	/** Dimension du panel d'index */
	public static final Dimension   INDEX_PANE_SIZE = new Dimension(FRAME_W-100,FRAME_H-130);
	/** Couleur de fond pour la console */
	public static final Color 		CONSOLE_BACK = 		Color.black;
	/** Couleur du text pour la console */
	public static final Color 		CONSOLE_FORE = 		Color.green;
	/**  */
	public static final Dimension SIDE_BUFFER = new Dimension(100,470);
	/**  */
	public static final Dimension BOT_BUFFER = new Dimension(FRAME_W-100,100);
	

}
