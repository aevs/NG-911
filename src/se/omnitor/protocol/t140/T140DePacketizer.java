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

import java.io.UnsupportedEncodingException;
import java.util.Vector;
import se.omnitor.util.FifoBuffer;

/**
 * Unpacks byte arrays with T.140 data and converts them to T.140 events.
 * As T.140 has control codes for different actions, we transmit everything
 * with different events. <br>
 * <br>
 * This class starts a thread that reads data from a FifoBuffer. Then, it will
 * send unpacked T.140 data with T.140 events to a T140EventHandler. <br>
 * <br>
 * <u>How to use this:</u> <br>
 * Make an instance of this class, set FifoBuffer, set T140EventHandler and
 * finally execute start().
 *
 * @author Andreas Piirimets, Omnitor AB
 *
 * @todo Add redundancy.
 */
public class T140DePacketizer implements Runnable {

    private int redGen;
    private Thread thread;
    private FifoBuffer fifo;
    private T140EventHandler eventHandler;
    private boolean running;

    private int gathering;
    private String gString;


    /**
     * Initializes.
     *
     * @param redGen The number of redundant generations to use (T.140
     * redundancy). A value of zero disables redundancy.
     */
    public T140DePacketizer(int redGen) {

	this.redGen = redGen;
	fifo = null;
	eventHandler = null;
	running = false;

	gathering = -1;
	gString = "";

    }

    /**
     * Sets the buffer, where the depacketizer should poll for new data.
     *
     * @param fifo The buffer for incoming T.140 data
     */
    public void setInBuffer(FifoBuffer fifo) {
	this.fifo = fifo;
    }

    /**
     * Sets the event handler to send depacketized T.140 events to.
     *
     * @param eventHandler Event handler for outgoing T.140 events.
     */
    public void setEventHandler(T140EventHandler eventHandler) {
	this.eventHandler = eventHandler;
    }

    /**
     * Starts polling for incoming data.
     *
     */
    public void start() {
	if (fifo == null) {
	    return;
	}
	if (eventHandler == null) {
	    return;
	}

	running = true;

	thread = new Thread(this, "T140DePacketizer");
	thread.start();
    }

    /**
     * Stops polling.
     *
     */
    public void stop() {
	running = false;
	thread.interrupt();
    }

    /**
     * Runtime code for polling data and also for handling incoming data.
     *
     */
    public void run() {

	byte[] inData;
	String str;

	while (running) {

	    try {
		inData = fifo.getData();

		str = new String(inData, "UTF-8");
		//System.out.println("t140Depacketize: " + str);
		sendEvents(str);

	    }
	    catch (InterruptedException ie) {
	    }
	    catch (UnsupportedEncodingException uee) {
	    }

	}
    }

    /**
     * Parses the String and sends all events found to the event handler.
     *
     * @param data The String to parse, this should not be in UTF-8 format.
     */
    private void sendEvents(String data) {

	char[] cArray = data.toCharArray();

	Vector events = new Vector(0, 1);

	for (int cnt=0; cnt<cArray.length; cnt++) {

	    if (gathering != -1) {

		switch (gathering) {

		case T140Event.SGR:
		    if (cArray[cnt] != T140Constants.GRAPHIC_END) {
			gString += cArray[cnt];
		    }

		    if (cArray[cnt] == T140Constants.GRAPHIC_END ||
			gString.length() > 5) {

			// There is nothing in T.140 that specifies the
			// maximum length of the graphical code. Normally,
			// it consists of only one single character. The
			// maximum length check (max 5) above is done in
			// order to not "get stuck" in a graphical code string
			// gathering loop, in case of severe packet loss.

			eventHandler.newEvent(new T140Event(T140Event.SGR,
							    gString));
			gathering = -1;
			gString = "";
		    }
		    break;

		case T140Event.SOS_ST:
		    if (cArray[cnt] != T140Constants.ST) {
			gString += cArray[cnt];
		    }

		    if (cArray[cnt] == T140Constants.ST ||
			gString.length() > 255) {

			eventHandler.newEvent(new T140Event(T140Event.SOS_ST,
							    gString));
			gathering = -1;
			gString = "";
		    }
		    break;

		default:

		}

	    }
	    else {

		switch (cArray[cnt]) {
		case T140Constants.BELL:
		    eventHandler.newEvent(new T140Event(T140Event.BELL, null));
		    break;

		case T140Constants.ZERO_WIDTH_NO_BREAK_SPACE_CHAR:
		    // Ignore.
		    break;

		case T140Constants.BACKSPACE:
		    eventHandler.newEvent(new T140Event(T140Event.BS, null));
		    break;

		case T140Constants.LINE_SEPERATOR:
		case T140Constants.CR_LF:
		    eventHandler.newEvent(new T140Event(T140Event.NEW_LINE,
							null));
		    break;

  		    /* AP: Temporarily disabled
		case TextConstants.INTERRUPT2:
		    // How many bytes should we read?
		    eventHandler.newEvent(new T140Event(T140Event.INT, ""));
		    break;
		    */

		case T140Constants.SOS:
		    gathering = T140Event.SOS_ST;
		    gString = "";
		    break;

		case T140Constants.GRAPHIC_START:
		    gathering = T140Event.SGR;
		    gString = "";
		    break;

		default:
		    eventHandler.newEvent
			(new T140Event(T140Event.TEXT, ""+cArray[cnt]));
		}
	    }

	}


    }

}
