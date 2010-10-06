/**
 * 
 */
package org.vivoweb.ingest.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.vfs.VFS;
import org.vivoweb.ingest.util.repo.JenaConnect;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author Christopher Haines
 *
 */
public class EatVIVOOrgs {
  private static List<Property> otherProperties;
  private static JenaConnect model;
  
  private static void eatOrg(String URI) throws MalformedURLException, IOException {
    System.out.println("eating: "+URI);
    URLConnection conn = new URL(URI).openConnection();
    conn.setRequestProperty("Accept", "application/rdf+xml");
    JenaConnect tempModel = new JenaConnect();
    tempModel.loadRDF(conn.getInputStream(), null);
    Resource orgRes = tempModel.getJenaModel().createResource(URI);
    //eat suborgs
    StmtIterator subOrgItr = orgRes.listProperties(model.getJenaModel().createProperty("http://vivoweb.org/ontology/core#","hasSubOrganization"));
    while(subOrgItr.hasNext()) {
      Statement subOrgStmt = subOrgItr.next();
      model.getJenaModel().add(subOrgStmt);
      String subOrgURI = subOrgStmt.getResource().getURI();
      eatOrg(subOrgURI);
    }
    //eat the rest
    for(Property prop : otherProperties) {
      StmtIterator otherPropItr = orgRes.listProperties(prop);
      while(otherPropItr.hasNext()) {
        model.getJenaModel().add(otherPropItr.next());
      }
    }
  }

  /**
   * @param args cmd line args
   * @throws IOException 
   * @throws MalformedURLException 
   */
  public static void main(String[] args) throws MalformedURLException, IOException {
    String ufURI = "http://vivo.ufl.edu/individual/UniversityofFlorida";
    model = new JenaConnect();
    otherProperties = new LinkedList<Property>();
    otherProperties.add(model.getJenaModel().createProperty("http://vivo.ufl.edu/ontology/vivo-ufl/", "deptID"));
    otherProperties.add(model.getJenaModel().createProperty("http://vivoweb.org/ontology/core#", "subOrganizationWithin"));
    otherProperties.add(model.getJenaModel().createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type"));
    otherProperties.add(model.getJenaModel().createProperty("http://www.w3.org/2000/01/rdf-schema#", "label"));
    eatOrg(ufURI);
    model.exportRDF(VFS.getManager().resolveFile(new File("."), "XMLVault/ufOrgs.rdf.xml").getContent().getOutputStream(false));
  }

}
