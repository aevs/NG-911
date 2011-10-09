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
import se.omnitor.util.FifoBuffer;

/**
 * This is a packetizer for T.140 data. Sent T.140 events to this
 * T140EventHandler and the events will be converted to byte arrays of data,
 * which will be put in a FifoBuffer. <br>
 * <br>
 * <u>How to use this:</u> <br>
 * Create a FifoBuffer for outgoing data and start sending T.140 events to
 * this class via the newEvent(..) function!
 *
 * @author Andreas Piirimets, Omnitor AB
 *
 * @todo Add redundancy.
 */
public class T140Packetizer implements T140EventHandler {

    private int redGen;
    private Thread thread;
    private FifoBuffer fifo;

    /**
     * Initializes.
     *
     * @param redGen The number of redundant generations (for T.140
     * redundancy), a value of zero disables redundancy.
     */
    public T140Packetizer(int redGen) {
	this.redGen = redGen;
	fifo = null;

    }

    /**
     * Sets the FifoBuffer to append new data to
     *
     * @param fifo The FifoBuffer to use
     */
    public void setOutBuffer(FifoBuffer fifo) {
	this.fifo = fifo;
    }

    /**
     * Processes an incoming T.140 event
     *
     * @param event The incoming event
     */
    public void newEvent(T140Event event) {

	String str = "";

	switch (event.getType()) {

	case T140Event.TEXT:
	    str += (String)event.getData();
	    break;

	case T140Event.BELL:
	    str += T140Constants.BELL;
	    break;

	case T140Event.BS:
	    str += T140Constants.BACKSPACE;
	    break;

	case T140Event.NEW_LINE:
	case T140Event.CR_LF:
	    str += T140Constants.LINE_SEPERATOR;
	    break;

	case T140Event.INT:
	    str += T140Constants.INTERRUPT2 + (String)event.getData();
	    break;

	case T140Event.SGR:
	    str += T140Constants.GRAPHIC_START + (String)event.getData();
	    break;

	case T140Event.SOS_ST:
	    str +=
		T140Constants.SOS + (String)event.getData() + T140Constants.ST;

	default:
	}

	if (str.length() > 0) {
	    try {
		fifo.setData(str.getBytes("UTF-8"));
	    }
	    catch (UnsupportedEncodingException uee) {

	    }
	}

    }

}





