/* 
 * T140handler for NG9-1-1 project
 * 
 * ? 2007 copyright by the Trustees of Columbia University in the City of New York. 
 * 
 * Modified by Wonsang Song (wonsang@cs.columbia.edu)
 * Modified Date: 12/16/2007
 * 
 * "Parts of this program were based on reference designs developed by Omnitor AB 
 * and the Trace Center, University of Wisconsin-Madison under funding from the 
 * National Institute on Disability and Rehabilitation Research US Dept of 
 * Education and the European Commission."
 * 
 * This code was modified from the original distributed by Trace/Omnitor 
 * in order to integrated with SIPc.
 */

/*
 * T.140 Presentation Library
 *
 * Copyright (C) 2004 Board of Regents of the University of Wisconsin System
 * (Univ. of Wisconsin-Madison, Trace R&D Center)
 * Copyright (C) 2004 Omnitor AB
 *
 * This software was developed with support from the National Institute on
 * Disability and Rehabilitation Research, US Dept of Education under Grant
 * # H133E990006 and H133E040014
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Please send a copy of any improved versions of the library to:
 * Gunnar Hellstrom, Omnitor AB, Renathvagen 2, SE 121 37 Johanneshov, SWEDEN
 * Gregg Vanderheiden, Trace Center, U of Wisconsin, Madison, Wi 53706
 *
 */

package se.omnitor.protocol.t140;
import java.io.*;
public class T140Panel implements T140EventHandler {
	private StringBuffer send_buffer;
	private StringBuffer receive_buffer;
	private String call_id;
	private Writer out;
	private Writer log_out;
    private String lineSeparator = System.getProperty("line.separator");
    private T140EventHandler outHandler;

    /**
     * Makes data transfer objects for output (synchObject)
     * and input (playerSynchObject).
     * It also makes a thread that will update the GUI.
     *
     */
    public T140Panel(T140EventHandler outHandler, String call_id, Writer out, Writer log_out) {
        this.outHandler = outHandler;
        this.call_id = call_id;
        this.out = out;
        this.log_out = log_out;
        this.send_buffer = new StringBuffer();
        this.receive_buffer = new StringBuffer();
    }

    /**
     * Handles incoming T.140 events.
     *
     * @param event The event
     */
    public void newEvent(T140Event event) {
	    switch (event.getType()) {
	    case T140Event.TEXT:
			processText((String)event.getData());
			break;

	    case T140Event.BELL:
			break;

	    case T140Event.BS:
	    	processBS();
	    	break;

	    case T140Event.NEW_LINE:
	    case T140Event.CR_LF:
	    	processNewLine();
	    	break;

	    default:
                // Ignore the rest
	    }
    }

    private void processNewLine() {
    	try {
	    	System.out.println("Receiving [10]: LF");
	    	//out.write(call_id+" LF\n");
	    	out.write("\n");
	    	out.flush();
	    	
	    	// If newline is received, write all in the receive_buffer and flush it.
	    	try {
	    		if (log_out != null) { 
	    			StringBuffer log_line = new StringBuffer();
	    			log_line.append("[caller][");
	    			log_line.append(edu.columbia.irt.Utils.getCurrentTimeStamp());
	    			log_line.append("]: ");
	    			log_line.append(receive_buffer.toString());
	    			log_line.append("\n");
	    			log_out.write(log_line.toString());
	    			log_out.flush();
	    		}
	    	} catch (Exception e) {
	    		System.out.println("log write error: " + e.getMessage());
	    		// do nothing
	    	}
	    	receive_buffer.setLength(0);
	    	
    	} catch (Exception e) {
     	}  
    }

    private void processText(String inText) {
    	try {
	    	System.out.println("Receiving [" + (int)inText.charAt(0) + "]: " + inText);
	    	//out.write(call_id+" "+inText+"\n");
	    	out.write(inText);
	    	out.flush();
	    	
	    	// If a character is received, put it in receive_buffer
	    	receive_buffer.append(inText);
    	} catch (Exception e) {
    		System.out.println("processText error [" + inText + "]: " + e.getMessage());
    	}
    }

    private void processBS() {
    	try {
	    	System.out.println("Receiving [8]: BS");
	    	//out.write(call_id+" BS\n");
	    	out.write(0x08);
	    	out.flush();
	    	
	    	// If BS is received, delete the latest received in receive_buffer
	    	if (receive_buffer.length() > 0) {
	    		receive_buffer.deleteCharAt(receive_buffer.length() - 1);
	    	}
    	} catch (Exception e) {
     	}    	
    }
    /**
     * Handles typed keys.
     *
     */
    public void keyTyped(char input) {

        char[] inChar = new char[1];
        inChar[0] = input;

        if ((inChar[0] == T140Constants.CARRIAGE_RETURN) ||
            (inChar[0] == T140Constants.LINE_FEED) || 
            (inChar[0] == lineSeparator.charAt(0))) {
        	inChar[0] =  T140Constants.LINE_SEPERATOR;
        }
        
		if (inChar[0] != 3) {
			
			// If BS is entered, delete the latest character in send_buffer
			if (inChar[0] == T140Constants.BACKSPACE) {		    	
		    	if (send_buffer.length() > 0) {
		    		send_buffer.deleteCharAt(send_buffer.length() - 1);
		    	}				
			}
			// If newline is entered, write all in send_buffer in the log and flush it!
			else if (inChar[0] == T140Constants.LINE_SEPERATOR) {
		    	try {
		    		if (log_out != null) { 
		    			StringBuffer log_line = new StringBuffer();
		    			log_line.append("[calltaker][");
		    			log_line.append(edu.columbia.irt.Utils.getCurrentTimeStamp());
		    			log_line.append("]: ");
		    			log_line.append(send_buffer.toString());
		    			log_line.append("\n");
		    			log_out.write(log_line.toString());
		    			log_out.flush();
		    		}
		    	} catch (Exception e) {
		    		System.out.println("log write error: " + e.getMessage());
		    		// do nothing
		    	}
		    	send_buffer.setLength(0);
				
			} 
			// if a normal char, put it in send_buffer
			else {
				send_buffer.append(inChar[0]);		
			}
			
			// send entered char using T140Event
		    String theText = new String(inChar);
		    if (theText.length() > 0) {
		    	System.out.println("Sending [" + (int)inChar[0] + "]: " + theText);
		    	outHandler.newEvent(new T140Event(T140Event.TEXT, theText));
		    }
		}
    }
}

