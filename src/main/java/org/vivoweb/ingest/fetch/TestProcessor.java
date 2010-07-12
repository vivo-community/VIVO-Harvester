package org.vivoweb.ingest.fetch;

import de.fuberlin.wiwiss.d2r.*;
import de.fuberlin.wiwiss.d2r.exception.D2RException;

import java.io.*;
import com.hp.hpl.jena.rdf.model.* ;


/**
 * Test Class for the D2R processor. To install and test D2R processor: 
 * <BR>1. Follow the instructions in Installation.txt
 * <BR>2. Execute this test class or run D2rProcessor from the comand line.
 *
 * <BR><BR>History: 
 * <BR>09-25-2003   : Changed for Jena2.
 * <BR>01-15-2003   : Initial version of this class.
 * @author Chris Bizer chris@bizer.de
 * @version V0.2
 */
public class TestProcessor {
    private final static String D2RMap = "C:/D2RV03/D2R/examples/iswc/iswcMap.d2r.xml";
    private final static String Outputfile = "C:/D2RV03/D2R/examples/iswc/output/output.rdf.xml";
    static D2rProcessor processor;

    public static void main(String[] args) {
        try {
            System.out.println("D2R test started ....");
            processor = new D2rProcessor();
            System.out.println("D2R processor created ....");
            processor.readMap(D2RMap);
            System.out.println("D2R file read ....");
            Model output = processor.getAllInstancesAsModel();
            output.write(System.out);
            System.out.println("RDF data exported ....");
        } catch (D2RException d2rex) {
            System.out.println("\n*** D2R Exception caught ***\n");
            System.out.println("Message:  " + d2rex.getMessage());
            System.out.println("");
            d2rex.printStackTrace();
        }
        catch (IOException ioex) {
            System.out.println("\n*** IO Exception caught ***\n");
            System.out.println("Message:  " + ioex.getMessage());
            System.out.println("");
            ioex.printStackTrace();
        }
        catch (java.lang.Throwable ex) {
            System.out.println("\n*** General Exception caught ***\n");
            System.out.println("Message:  " + ex.getMessage());
            System.out.println("");
            ex.printStackTrace();
        }
    }
}
