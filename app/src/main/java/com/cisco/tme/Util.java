package com.cisco.tme;

/**
 * @author appsrox.com
 *
 */
public class Util {
	
	public static final StringBuilder sb = new StringBuilder();
	
	public static String concat(Object... objects) {
		sb.setLength(0);
		for (Object obj : objects) {
			sb.append(obj);
		}
		return sb.toString();
	}	

}
