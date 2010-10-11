package org.vivoweb.ingest.fetch;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Dale Scheppler
 */
public class WebFetch {
	
	/**
	 * @author - Dale Scheppler
	 * @param args Unused
	 * @todo Remove the timing, not necessary in this format.
	 * @todo add configuration and commandline options
	 * @todo document
	 */
	public static void main(String[] args) {
		while(true) {
			scrape();
			System.out.println("Scrape complete, sleeping 15 minutes.");
			try {
				Thread.sleep(15 * 60 * 1000);
			} catch(InterruptedException e) {
				System.out.println("Error, process interrupted.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @author Dale Scheppler
	 * @Description This method reads from the default error log location on an AT&T 2Wire modem, and saves the results.
	 * Good for arguing with the phone company. It will auto-run every 15 minutes until you kill it.
	 * @todo At some point this should read configuration data from a configuration file. If I ever plan to release it.
	 */
	private static void scrape() {
		
		try {
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HH.mm.ss.SSS");
			String dateString = sdf.format(date);
			
			System.out.println("The current date/time is: " + dateString);
			URL url = new URL("http://192.168.1.254/xslt?PAGE=B04");
			URLConnection urlConnect = url.openConnection();
			System.out.println("The current URL we are checking is: " + url.toString());
			BufferedReader in = new BufferedReader(new InputStreamReader(urlConnect.getInputStream()));
			String inputLine;
			BufferedWriter out = new BufferedWriter(new FileWriter("DSL-2Wire-Log-" + dateString + ".html"));
			while((inputLine = in.readLine()) != null) {
				out.write(inputLine);
				out.newLine();
			}
			out.flush();
			out.close();
		} catch(MalformedURLException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
}
