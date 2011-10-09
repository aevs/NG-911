package edu.columbia.irt;
import java.text.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This Utils class contains a set of utility methods.
 * 
 * @author Wonsang Song (wonsang@cs.columbia.edu)
 */
public class Utils {
	
	private static final String DECIMAL_PATTERN = "###,###";
	private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";	
	
	/**
	 * Takes XML in String and aligns for better display.
	 * @param in_xml String
	 * @return String
	 */
    public static String reflowXML(String in_xml) {
        Vector<String>       parts = new Vector<String>();
        char[]       chars = removeSpaces(in_xml).toCharArray();
        int          index = 0;
        int          first = 0;
        String       part = null;
        while (index < chars.length) {
            // Check for start of tag
            if (chars[index] == '<') {
                // Did we have data before this tag?
                if (first < index) {
                    part = new String(chars,first,index-first);
                    part = part.trim();
                    // Save non-whitespace data
                    if (part.length() > 0) {
                        parts.addElement(part);
                    }
                }
                // Save the start of tag
                first = index;
            }
            // Check for end of tag
            if (chars[index] == '>') {
                // Save the tag
                part = new String(chars,first,index-first+1);
                parts.addElement(part);
                first = index+1;
            }
            // Check for end of line
            if ((chars[index] == '\n') || (chars[index] == '\r')) {
                // Was there data on this line?
                if (first < index) {
                    part = new String(chars,first,index-first);
                    part = part.trim();
                    // Save non-whitespace data
                    if (part.length() > 0) {
                        parts.addElement(part);
                    }
                }
                first = index+1;
            }
            index++;
        }
        // Reflow as XML
        StringBuffer buf = new StringBuffer();
        Object[] list = parts.toArray();
        int indent = 0;
        int pad = 0;
        index = 0;
        while (index < list.length) {
            part = (String) list[index];
            if (buf.length() == 0) {
                // Just add first tag (should be XML header)
                buf.append(part);
            } else {
                // All other parts need to start on a new line
                buf.append('\n');
                // If we're at an end tag then decrease indent
                if (part.startsWith("</")) {
                    indent--;
                }            
                // Add any indent
                for (pad = 0; pad < indent; pad++) {
                    buf.append("  ");
                }
                // Add the tag or data
                buf.append(part);
                // If this is a start tag then increase indent
                if (part.startsWith("<") &&
                    !part.startsWith("</") &&
                    !part.endsWith("/>")) {
                    indent++;
                    // Check for special <tag>data</tag> case
                    if ((index + 2) < list.length) {
                        part = (String) list[index+2];
                        if (part.startsWith("</")) {
                            part = (String) list[index+1];
                            if (!part.startsWith("<")) {
                                buf.append(part);
                                part = (String) list[index+2];
                                buf.append(part);
                                index = index + 2;
                                indent--;
                            }
                        }
                    }
                }
            }
            index++;
        }
        return buf.toString();
    }

    /**
     * Removes all spaces or tabs in the input string.
     * @param in String
     * @return String
     */
	public static String removeSpaces(String in) {
		String regex = "(\\p{Blank})+";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(in);
		return matcher.replaceAll(" ");
	}
	
	public static String trim(String in) {
		return in.trim();
	}
    
	/**
	 * Returns the current timestamp in GMT in yyyy-MM-ddThh:mm:ssZ format.
	 * @return String
	 */
    public static String getCurrentTimeStampGMT() {
    	SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
    	format.setTimeZone(TimeZone.getTimeZone("GMT"));
		Format formatter = format;
		return formatter.format(new Date());
    }
    public static String getCurrentTimeStamp() {
    	SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
    	//format.setTimeZone(TimeZone.getTimeZone("GMT"));
		Format formatter = format;
		return formatter.format(new Date());
    }
    /**
     * Converts miliseconds format of time to yyyy-MM-ddThh:mm:ssZ format.
     * @param in long: miliseonds since epoch.
     * @return String
     */
    public static String getTimeFormat(long in) {
    	SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
    	format.setTimeZone(TimeZone.getTimeZone("GMT"));
		Format formatter = format;
		return formatter.format(new Date(in));
    }
    
    /**
     * Converts Calendar format of time to yyyy-MM-ddThh:mm:ssZ format.
     * @param in Calendar
     * @return String
     */
    public static String getTimeFormat(Calendar in) {
    	SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
    	format.setTimeZone(TimeZone.getTimeZone("GMT"));
		Format formatter = format;
		return formatter.format(in.getTime());
    }

    /**
     * Converts Date format of time to yyyy-MM-ddThh:mm:ssZ format.
     * @param in Date
     * @return String
     */    
    public static String getTimeFormat(Date in) {
    	SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
    	format.setTimeZone(TimeZone.getTimeZone("GMT"));
		Format formatter = format;
		return formatter.format(in);
    }    
    
    /**
     * Converts long to string with commas.
     * @param in long
     * @return String
     */
    public static String decimalFormat(long in) {
        try {
        	DecimalFormat formatter = new DecimalFormat(DECIMAL_PATTERN);
            return formatter.format(in);
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Returns UUID based on key.
     * @param key String
     * @return String
     */
	public static String getUUID(String key) {
		UUID id = UUID.nameUUIDFromBytes(new String(key).getBytes());
		return id.toString();
	}
	
	/**
	 * Returns an unique string using UUID based on key.
	 * @param key String
	 * @return String
	 */
	public static String getUniqueId(String key) {
		UUID id = UUID.nameUUIDFromBytes(new String(key).getBytes());
		return id.toString().replace("-", "");
	}	
	
	/**
	 * Checks whether input string is a LoST URI.
	 * @param uri String
	 * @return String
	 */
	public static boolean isLostURI(String uri) {
		return (uri != null && uri.startsWith("lost:") ? true : false);
	}
	
	/**
	 * Return host part in LoST URI.
	 * @param uri String
	 * @return String
	 */
	public static String removeLostProto(String uri) {
		return uri.substring(5);
	}
	
	/**
	 * Makes XML safe string (without \ < > </)
	 * @param in String
	 * @return String
	 */
	public static String escapeXML(String in) {
		if (in != null) return in.replaceAll("\"|<|>|</", "");
		else return null;
	}
	
	public static void main(String[] args) throws Exception {
		long t1 = System.currentTimeMillis();
		Thread.sleep(1000);
		long t2 = System.currentTimeMillis();
		System.out.println(t1);
		System.out.println(t1);
		System.out.println(Math.abs(t1-t2));
		
		long t3 = System.nanoTime() / 1000;
		Thread.sleep(1000);
		long t4 = System.nanoTime() / 1000;
		System.out.println(t3);
		System.out.println(t4);
		System.out.println(Math.abs(t3-t4));		
		
	}
}
