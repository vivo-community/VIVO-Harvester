#!/bin/bash

# Copyright (c) 2010 Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the new BSD license
# which accompanies this distribution, and is available at
# http://www.opensource.org/licenses/bsd-license.html
# 
# Contributors:
#     Christopher Haines, Dale Scheppler, Nicholas Skaggs, Stephen V. Williams - initial API and implementation

# Set working directory
#cd /usr/share/vivoingest

# Execute Fetch for Pubmed
java -cp bin/ingest-0.6.2.jar:bin/dependency/* org.vivoweb.ingest.fetch.PubmedSOAPFetch -X config/tasks/PubmedFetch.xml