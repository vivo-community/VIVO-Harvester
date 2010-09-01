package org.vivoweb.ingest.fetch;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WebFetch {

	/**
	 * @author - Dale Scheppler
	 * @param args - Unused
	 * @todo Remove the timing, not necessary in this format.
	 */
	public static void main(String[] args) {
		
		while (true) {
	         scrapeTwoWireLogs();
	         System.out.println("Scrape complete, sleeping 15 minutes.");
	         try {
				Thread.sleep(15*60*1000);
			} catch (InterruptedException e) {
				System.out.println("Error, process interrupted.");
				e.printStackTrace();
			}
	      }
	}
	
	/**
	 * @author Dale Scheppler <br>
	 * 
	 * @Description This method reads from the default error log location on an AT&T 2Wire modem, and saves the results.
	 * Good for arguing with the phone company.
	 * It will auto-run every 15 minutes until you kill it.
	 * @todo At some point this should read configuration data from a configuration file. If I ever plan to release it.
	 */
	private static void scrapeTwoWireLogs(){

		Date date = new Date();
		SimpleDateFormat sdf = new
		SimpleDateFormat("dd-MMM-yyyy_HH.mm.ss.SSS");
		String dateString = sdf.format(date);

		System.out.println("Beginning parse of HTML-based logs on 2Wire modem.");
		System.out.println("The current date/time is: " + dateString);
		URL twoWire = null;
		URLConnection urlConnectTwoWire = null;
		try {
			twoWire = new URL("http://192.168.1.254/xslt?PAGE=B04");
		} catch (MalformedURLException e) {
			System.out.println("Malformed URL. Please enter a new URL and try again.");
			e.printStackTrace();
		}
		System.out.println("The current URL we are checking is: " + twoWire.toString());
		try {
			urlConnectTwoWire = twoWire.openConnection();
		} catch (IOException e) {
			System.out.println("I/O Exception while attempting to connect to: " + twoWire.toString());
			e.printStackTrace();
		}
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(urlConnectTwoWire.getInputStream()));
		} catch (IOException e) {
			System.out.println("I/O Exception while attempting to establish Buffered Reader Stream from: " + twoWire.toString());
			e.printStackTrace();
		}
		String inputLine;
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter("DSL-2Wire-Log-" + dateString + ".html"));
		} catch (IOException e1) {
			System.out.println("Error while trying to initialize logging functionality.");
			e1.printStackTrace();
		}
		try {
			while ((inputLine = in.readLine()) != null) {
					out.write(inputLine);
					out.newLine();
					
					
			}
			if(out != null)
			{
				out.flush();
				out.close();
			}
			else
			{
				out.close();	
			}
			
		} catch (IOException e) {
			System.out.println("I/O Exception while trying to parse output.");
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			System.out.println("I/O Exception while trying to close buffered reader.");
			e.printStackTrace();
		}
	}
	/**
	 * @author Dale Scheppler
	 * @param baseString - The base string that you want to strip something out of
	 * @param stringToRemove - The string you wish to remove from baseString
	 * @return Returns a string minus what you don't want in it.
	 */
	@SuppressWarnings("unused")
	private static String stripString(String baseString, String stringToRemove)
	{
		baseString = baseString.replace(stringToRemove, "");
		return baseString;
		
	}

}
