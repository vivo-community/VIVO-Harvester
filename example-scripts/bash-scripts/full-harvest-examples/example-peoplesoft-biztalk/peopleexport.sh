#!/bin/bash

# Copyright (c) 2010-2011 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams, James Pence, Michael Barbieri - initial API and implementation

# set to the directory where the harvester was installed or unpacked
# HARVESTER_INSTALL_DIR is set to the location of the installed harvester
#       If the deb file was used to install the harvester then the
#       directory should be set to /usr/share/vivo/harvester which is the
#       current location associated with the deb installation.
#       Since it is also possible the harvester was installed by
#       uncompressing the tar.gz the setting is available to be changed
#       and should agree with the installation location
export HARVESTER_INSTALL_DIR=/data/vivo/harvester/harvester_1.3
export HARVEST_NAME=people-seeding
export DATE=`date +%Y-%m-%d'T'%T`

# Add harvester binaries to path for execution
# The tools within this script refer to binaries supplied within the harvester
#       Since they can be located in another directory their path should be
#       included within the classpath and the path environment variables.
export PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/bin/harvester.jar:$HARVESTER_INSTALL_DIR/bin/dependency/*
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*

# Faculty Member Export
harvester-jenaconnect -j /data/vivo/harvester/vivo-auto-harvest/courses/course-ingest/vivo.model.xml -q "PREFIX core: <http://vivoweb.org/ontology/core#> PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX bibo: <http://purl.org/ontology/bibo/> PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> PREFIX ufVivo: <http://vivo.ufl.edu/ontology/vivo-ufl/> CONSTRUCT { ?x rdf:type core:FacultyMember. ?x core:primaryEmail ?email . ?x core:primaryPhoneNumber ?phone . ?x core:faxNumber ?fax . ?x foaf:firstName ?fname . ?x foaf:lastName ?lname . ?x core:middleName ?mname . ?x bibo:prefixName ?prename . ?x bibo:suffixName ?suffname . ?x rdfs:label ?label . ?x ufVivo:ufid ?ufid . ?x ufVivo:gatorlink ?glid . ?x core:preferredTitle ?preftitle . ?x ufVivo:homeDept ?dept . ?x ufVivo:privacyFlag ?flag . } WHERE { ?x rdf:type core:FacultyMember. OPTIONAL{?x core:primaryEmail ?email .} OPTIONAL{?x core:primaryPhoneNumber ?phone . } OPTIONAL{?x core:faxNumber ?fax . } OPTIONAL{?x foaf:firstName ?fname . } OPTIONAL{?x foaf:lastName ?lname . } OPTIONAL{?x core:middleName ?mname . } OPTIONAL{?x bibo:prefixName ?prename . } OPTIONAL{?x bibo:suffixName ?suffname . } OPTIONAL{?x rdfs:label ?label . } OPTIONAL{?x ufVivo:ufid ?ufid . } OPTIONAL{?x ufVivo:gatorlink ?gliD . } OPTIONAL{?x core:preferredTitle ?preftitle .} OPTIONAL{?x ufVivo:homeDept ?dept . } OPTIONAL{?x ufVivo:privacyFlag ?flag . } }" >> FacultyMember.xml
# Non-Academic Export
harvester-jenaconnect -j /data/vivo/harvester/vivo-auto-harvest/courses/course-ingest/vivo.model.xml -q "PREFIX core: <http://vivoweb.org/ontology/core#> PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX bibo: <http://purl.org/ontology/bibo/> PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> PREFIX ufVivo: <http://vivo.ufl.edu/ontology/vivo-ufl/> CONSTRUCT { ?x rdf:type core:NonAcademic. ?x core:primaryEmail ?email . ?x core:primaryPhoneNumber ?phone . ?x core:faxNumber ?fax . ?x foaf:firstName ?fname . ?x foaf:lastName ?lname . ?x core:middleName ?mname . ?x bibo:prefixName ?prename . ?x bibo:suffixName ?suffname . ?x rdfs:label ?label . ?x ufVivo:ufid ?ufid . ?x ufVivo:gatorlink ?glid . ?x core:preferredTitle ?preftitle . ?x ufVivo:homeDept ?dept . ?x ufVivo:privacyFlag ?flag . } WHERE { ?x rdf:type core:NonAcademic. OPTIONAL{?x core:primaryEmail ?email .} OPTIONAL{?x core:primaryPhoneNumber ?phone . } OPTIONAL{?x core:faxNumber ?fax . } OPTIONAL{?x foaf:firstName ?fname . } OPTIONAL{?x foaf:lastName ?lname . } OPTIONAL{?x core:middleName ?mname . } OPTIONAL{?x bibo:prefixName ?prename . } OPTIONAL{?x bibo:suffixName ?suffname . } OPTIONAL{?x rdfs:label ?label . } OPTIONAL{?x ufVivo:ufid ?ufid . } OPTIONAL{?x ufVivo:gatorlink ?gliD . } OPTIONAL{?x core:preferredTitle ?preftitle .} OPTIONAL{?x ufVivo:homeDept ?dept . } OPTIONAL{?x ufVivo:privacyFlag ?flag . } }" >>Non-academic.xml
# Emeritus Professor Export
harvester-jenaconnect -j /data/vivo/harvester/vivo-auto-harvest/courses/course-ingest/vivo.model.xml -q "PREFIX core: <http://vivoweb.org/ontology/core#> PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX bibo: <http://purl.org/ontology/bibo/> PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> PREFIX ufVivo: <http://vivo.ufl.edu/ontology/vivo-ufl/> CONSTRUCT { ?x rdf:type core:EmeritusProfessor . ?x core:primaryEmail ?email . ?x core:primaryPhoneNumber ?phone . ?x core:faxNumber ?fax . ?x foaf:firstName ?fname . ?x foaf:lastName ?lname . ?x core:middleName ?mname . ?x bibo:prefixName ?prename . ?x bibo:suffixName ?suffname . ?x rdfs:label ?label . ?x ufVivo:ufid ?ufid . ?x ufVivo:gatorlink ?glid . ?x core:preferredTitle ?preftitle . ?x ufVivo:homeDept ?dept . ?x ufVivo:privacyFlag ?flag . } WHERE { ?x rdf:type core:EmeritusProfessor . OPTIONAL{?x core:primaryEmail ?email .} OPTIONAL{?x core:primaryPhoneNumber ?phone . } OPTIONAL{?x core:faxNumber ?fax . } OPTIONAL{?x foaf:firstName ?fname . } OPTIONAL{?x foaf:lastName ?lname . } OPTIONAL{?x core:middleName ?mname . } OPTIONAL{?x bibo:prefixName ?prename . } OPTIONAL{?x bibo:suffixName ?suffname . } OPTIONAL{?x rdfs:label ?label . } OPTIONAL{?x ufVivo:ufid ?ufid . } OPTIONAL{?x ufVivo:gatorlink ?gliD . } OPTIONAL{?x core:preferredTitle ?preftitle .} OPTIONAL{?x ufVivo:homeDept ?dept . } OPTIONAL{?x ufVivo:privacyFlag ?flag . } }" >> EmeritusProfessor.xml
# Consultant Export
harvester-jenaconnect -j /data/vivo/harvester/vivo-auto-harvest/courses/course-ingest/vivo.model.xml -q "PREFIX core: <http://vivoweb.org/ontology/core#> PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX bibo: <http://purl.org/ontology/bibo/> PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> PREFIX ufVivo: <http://vivo.ufl.edu/ontology/vivo-ufl/> CONSTRUCT { ?x rdf:type ufVivo:Consultant . ?x core:primaryEmail ?email . ?x core:primaryPhoneNumber ?phone . ?x core:faxNumber ?fax . ?x foaf:firstName ?fname . ?x foaf:lastName ?lname . ?x core:middleName ?mname . ?x bibo:prefixName ?prename . ?x bibo:suffixName ?suffname . ?x rdfs:label ?label . ?x ufVivo:ufid ?ufid . ?x ufVivo:gatorlink ?glid . ?x core:preferredTitle ?preftitle . ?x ufVivo:homeDept ?dept . ?x ufVivo:privacyFlag ?flag . } WHERE { ?x rdf:type  ufVivo:Consultant . OPTIONAL{?x core:primaryEmail ?email .} OPTIONAL{?x core:primaryPhoneNumber ?phone . } OPTIONAL{?x core:faxNumber ?fax . } OPTIONAL{?x foaf:firstName ?fname . } OPTIONAL{?x foaf:lastName ?lname . } OPTIONAL{?x core:middleName ?mname . } OPTIONAL{?x bibo:prefixName ?prename . } OPTIONAL{?x bibo:suffixName ?suffname . } OPTIONAL{?x rdfs:label ?label . } OPTIONAL{?x ufVivo:ufid ?ufid . } OPTIONAL{?x ufVivo:gatorlink ?gliD . } OPTIONAL{?x core:preferredTitle ?preftitle .} OPTIONAL{?x ufVivo:homeDept ?dept . } OPTIONAL{?x ufVivo:privacyFlag ?flag . } }" >>ConsultantExport.xml
# Recent Employee Export
harvester-jenaconnect -j /data/vivo/harvester/vivo-auto-harvest/courses/course-ingest/vivo.model.xml -q "PREFIX core: <http://vivoweb.org/ontology/core#> PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX bibo: <http://purl.org/ontology/bibo/> PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> PREFIX ufVivo: <http://vivo.ufl.edu/ontology/vivo-ufl/> CONSTRUCT { ?x rdf:type ufVivo:RecentEmployee . ?x core:primaryEmail ?email . ?x core:primaryPhoneNumber ?phone . ?x core:faxNumber ?fax . ?x foaf:firstName ?fname . ?x foaf:lastName ?lname . ?x core:middleName ?mname . ?x bibo:prefixName ?prename . ?x bibo:suffixName ?suffname . ?x rdfs:label ?label . ?x ufVivo:ufid ?ufid . ?x ufVivo:gatorlink ?glid . ?x core:preferredTitle ?preftitle . ?x ufVivo:homeDept ?dept . ?x ufVivo:privacyFlag ?flag . } WHERE { ?x rdf: ufVivo:RecentEmployee . OPTIONAL{?x core:primaryEmail ?email .} OPTIONAL{?x core:primaryPhoneNumber ?phone . } OPTIONAL{?x core:faxNumber ?fax . } OPTIONAL{?x foaf:firstName ?fname . } OPTIONAL{?x foaf:lastName ?lname . } OPTIONAL{?x core:middleName ?mname . } OPTIONAL{?x bibo:prefixName ?prename . } OPTIONAL{?x bibo:suffixName ?suffname . } OPTIONAL{?x rdfs:label ?label . } OPTIONAL{?x ufVivo:ufid ?ufid . } OPTIONAL{?x ufVivo:gatorlink ?gliD . } OPTIONAL{?x core:preferredTitle ?preftitle .} OPTIONAL{?x ufVivo:homeDept ?dept . } OPTIONAL{?x ufVivo:privacyFlag ?flag . } }" >>RecentEmployeeExport.xml
# Deceased Employee Export
harvester-jenaconnect -j /data/vivo/harvester/vivo-auto-harvest/courses/course-ingest/vivo.model.xml -q "PREFIX core: <http://vivoweb.org/ontology/core#> PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX bibo: <http://purl.org/ontology/bibo/> PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> PREFIX ufVivo: <http://vivo.ufl.edu/ontology/vivo-ufl/> CONSTRUCT { ?x rdf:type ufVivo:Deceased . ?x core:primaryEmail ?email . ?x core:primaryPhoneNumber ?phone . ?x core:faxNumber ?fax . ?x foaf:firstName ?fname . ?x foaf:lastName ?lname . ?x core:middleName ?mname . ?x bibo:prefixName ?prename . ?x bibo:suffixName ?suffname . ?x rdfs:label ?label . ?x ufVivo:ufid ?ufid . ?x ufVivo:gatorlink ?glid . ?x core:preferredTitle ?preftitle . ?x ufVivo:homeDept ?dept . ?x ufVivo:privacyFlag ?flag . } WHERE { ?x rdf: ufVivo:Deceased . OPTIONAL{?x core:primaryEmail ?email .} OPTIONAL{?x core:primaryPhoneNumber ?phone . } OPTIONAL{?x core:faxNumber ?fax . } OPTIONAL{?x foaf:firstName ?fname . } OPTIONAL{?x foaf:lastName ?lname . } OPTIONAL{?x core:middleName ?mname . } OPTIONAL{?x bibo:prefixName ?prename . } OPTIONAL{?x bibo:suffixName ?suffname . } OPTIONAL{?x rdfs:label ?label . } OPTIONAL{?x ufVivo:ufid ?ufid . } OPTIONAL{?x ufVivo:gatorlink ?gliD . } OPTIONAL{?x core:preferredTitle ?preftitle .} OPTIONAL{?x ufVivo:homeDept ?dept . } OPTIONAL{?x ufVivo:privacyFlag ?flag . } }" >>DeceasedEmployeeExport.xml
# Courtesy Faculty Export
harvester-jenaconnect -j /data/vivo/harvester/vivo-auto-harvest/courses/course-ingest/vivo.model.xml -q "PREFIX core: <http://vivoweb.org/ontology/core#> PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX bibo: <http://purl.org/ontology/bibo/> PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> PREFIX ufVivo: <http://vivo.ufl.edu/ontology/vivo-ufl/> CONSTRUCT { ?x rdf:type ufVivo:CourtesyFaculty . ?x core:primaryEmail ?email . ?x core:primaryPhoneNumber ?phone . ?x core:faxNumber ?fax . ?x foaf:firstName ?fname . ?x foaf:lastName ?lname . ?x core:middleName ?mname . ?x bibo:prefixName ?prename . ?x bibo:suffixName ?suffname . ?x rdfs:label ?label . ?x ufVivo:ufid ?ufid . ?x ufVivo:gatorlink ?glid . ?x core:preferredTitle ?preftitle . ?x ufVivo:homeDept ?dept . ?x ufVivo:privacyFlag ?flag . } WHERE { ?x rdf: ufVivo:CourtesyFaculty . OPTIONAL{?x core:primaryEmail ?email .} OPTIONAL{?x core:primaryPhoneNumber ?phone . } OPTIONAL{?x core:faxNumber ?fax . } OPTIONAL{?x foaf:firstName ?fname . } OPTIONAL{?x foaf:lastName ?lname . } OPTIONAL{?x core:middleName ?mname . } OPTIONAL{?x bibo:prefixName ?prename . } OPTIONAL{?x bibo:suffixName ?suffname . } OPTIONAL{?x rdfs:label ?label . } OPTIONAL{?x ufVivo:ufid ?ufid . } OPTIONAL{?x ufVivo:gatorlink ?gliD . } OPTIONAL{?x core:preferredTitle ?preftitle .} OPTIONAL{?x ufVivo:homeDept ?dept . } OPTIONAL{?x ufVivo:privacyFlag ?flag . } }" >>CourtesyFacultyExport.xml
# All Others Export
harvester-jenaconnect -j /data/vivo/harvester/vivo-auto-harvest/courses/course-ingest/vivo.model.xml -q "PREFIX core: <http://vivoweb.org/ontology/core#> PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX foaf: <http://xmlns.com/foaf/0.1/> PREFIX bibo: <http://purl.org/ontology/bibo/> PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#> PREFIX ufVivo: <http://vivo.ufl.edu/ontology/vivo-ufl/> CONSTRUCT { ?x rdf:type foaf:Person . ?x core:primaryEmail ?email . ?x core:primaryPhoneNumber ?phone . ?x core:faxNumber ?fax . ?x foaf:firstName ?fname . ?x foaf:lastName ?lname . ?x core:middleName ?mname . ?x bibo:prefixName ?prename . ?x bibo:suffixName ?suffname . ?x rdfs:label ?label . ?x ufVivo:ufid ?ufid . ?x ufVivo:gatorlink ?glid . ?x core:preferredTitle ?preftitle . ?x ufVivo:homeDept ?dept . ?x ufVivo:privacyFlag ?flag . } WHERE { ?x rdf:type foaf:Person . FILTER NOT EXISTS { ?x rdf:type core:FacultyMember . } FILTER NOT EXISTS { ?x rdf:type core:NonAcademic . } FILTER NOT EXISTS { ?x rdf:type ufVivo:CourtesyFaculty . } FILTER NOT EXISTS { ?x rdf:type core:EmeritusProfessor . } FILTER NOT EXISTS { ?x rdf:type ufVivo:Consultant . } FILTER NOT EXISTS { ?x rdf:type ufVivo:RecentEmployee . } FILTER NOT EXISTS { ?x rdf:type ufVivo:Deceased . } OPTIONAL{?x core:primaryEmail ?email .} OPTIONAL{?x core:primaryPhoneNumber ?phone . } OPTIONAL{?x core:faxNumber ?fax . } OPTIONAL{?x foaf:firstName ?fname . } OPTIONAL{?x foaf:lastName ?lname . } OPTIONAL{?x core:middleName ?mname . } OPTIONAL{?x bibo:prefixName ?prename . } OPTIONAL{?x bibo:suffixName ?suffname . } OPTIONAL{?x rdfs:label ?label . } OPTIONAL{?x ufVivo:ufid ?ufid . } OPTIONAL{?x ufVivo:gatorlink ?gliD . } OPTIONAL{?x core:preferredTitle ?preftitle .} OPTIONAL{?x ufVivo:homeDept ?dept . } OPTIONAL{?x ufVivo:privacyFlag ?flag . } }" >> AllOthersExport.xml


# Check size of Faculty Member Export, if 513b then skip it
FILESIZE=$(stat -c %s "FacultyMember.xml")
echo $FILESIZE
if [ $FILESIZE -ne 513 ]; then
	harvester-transfer -o /data/vivo/harvester/vivo-auto-harvest/courses/course-ingest/previous-harvest.model.xml -r FacultyMember.xml -R RDF/XML
fi

# Check size of Non-Academic Export, if 513b then skip it
FILESIZE=$(stat -c %s "Non-academic.xml")
echo $FILESIZE
if [ $FILESIZE -ne 513 ]; then
	harvester-transfer -o /data/vivo/harvester/vivo-auto-harvest/courses/course-ingest/previous-harvest.model.xml -r Non-academic.xml -R RDF/XML
fi

# Check size of Emeritus Professor Export, if 513b then skip it
FILESIZE=$(stat -c %s "EmeritusProfessor.xml")
echo $FILESIZE
if [ $FILESIZE -ne 513 ]; then
	harvester-transfer -o /data/vivo/harvester/vivo-auto-harvest/courses/course-ingest/previous-harvest.model.xml -r EmeritusProfessor.xml -R RDF/XML
fi

# Check size of Consultant Export, if 513b then skip it
FILESIZE=$(stat -c %s "ConsultantExport.xml")
echo $FILESIZE
if [ $FILESIZE -ne 513 ]; then
	harvester-transfer -o /data/vivo/harvester/vivo-auto-harvest/courses/course-ingest/previous-harvest.model.xml -r ConsultantExport.xml -R RDF/XML
fi

# Check size of Recent Employee Export, if 513b then skip it
FILESIZE=$(stat -c %s "RecentEmployeeExport.xml")
echo $FILESIZE
if [ $FILESIZE -ne 513 ]; then
	harvester-transfer -o /data/vivo/harvester/vivo-auto-harvest/courses/course-ingest/previous-harvest.model.xml -r RecentEmployeeExport.xml -R RDF/XML
fi

# Check size of Deceased Employee Export, if 513b then skip it
FILESIZE=$(stat -c %s "DeceasedEmployeeExport.xml")
echo $FILESIZE
if [ $FILESIZE -ne 513 ]; then
	harvester-transfer -o /data/vivo/harvester/vivo-auto-harvest/courses/course-ingest/previous-harvest.model.xml -r DeceasedEmployeeExport.xml -R RDF/XML
fi

# Check size of Courtesy Faculty Member Export, if 513b then skip it
FILESIZE=$(stat -c %s "CourtesyFacultyExport.xml")
echo $FILESIZE
if [ $FILESIZE -ne 513 ]; then
	harvester-transfer -o /data/vivo/harvester/vivo-auto-harvest/courses/course-ingest/previous-harvest.model.xml -r CourtesyFacultyExport.xml -R RDF/XML
fi

# Check size of Faculty Member Export, if 513b then skip it
FILESIZE=$(stat -c %s "AllOthersExport.xml")
echo $FILESIZE
if [ $FILESIZE -ne 513 ]; then
	harvester-transfer -o /data/vivo/harvester/vivo-auto-harvest/courses/course-ingest/previous-harvest.model.xml -r AllOthersExport.xml -R RDF/XML
fi

harvester-transfer -i /data/vivo/harvester/vivo-auto-harvest/courses/course-ingest/previous-harvest.model.xml -d completeOutput.xml
