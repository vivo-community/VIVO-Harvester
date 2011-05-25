package org.vivoweb.harvester.translate;

import java.io.IOException;
import org.slf4j.Logger;
import org.vivoweb.harvester.util.recordhandler.Record;
import org.vivoweb.harvester.util.recordhandler.RecordHandler;

/**
 * Translator abstract class
 * @author Christopher Haines hainesc@ufl.edu
 */
public abstract class Translator {
	
	/**
	 * Get the logger for this translator
	 * @return the logger
	 */
	protected abstract Logger getLog();
	
	/**
	 * Translate the input
	 * @param input the string to be translated
	 * @return the translated output
	 */
	public abstract String translate(String input);
	
	/**
	 * Translate each record in input and create a corresponding record in output containing the translated output
	 * @param input the input recordhandler
	 * @param output the output recordhandler
	 * @throws IOException error writting to output recordhandler
	 */
	public void translateRecordHandler(RecordHandler input, RecordHandler output) throws IOException {
		translateRecordHandler(input, output, false);
	}
	
	/**
	 * Translate each record in input and create a corresponding record in output containing the translated output
	 * @param input the input recordhandler
	 * @param output the output recordhandler
	 * @param force force translation of all records
	 * @throws IOException error writting to output recordhandler
	 */
	public void translateRecordHandler(RecordHandler input, RecordHandler output, boolean force) throws IOException {
		int translated = 0;
		int skipped = 0;
		for(Record r : input) {
			if(force || r.needsProcessed(this.getClass())) {
				getLog().trace("Translating record " + r.getID());
				output.addRecord(r.getID(), translate(r.getData()), this.getClass());
				translated++;
			} else {
				getLog().trace("No translation needed for record " + r.getID());
				skipped++;
			}
		}
		getLog().info(translated + " records translated");
		getLog().info(skipped + " records did not need translation");
	}
}