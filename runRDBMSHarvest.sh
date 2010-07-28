#!/bin/bash

# Execute Fetch for PubMed
java -cp target/ingest-0.4.jar:target/dependency/* org.vivoweb.ingest.fetch.JDBCFetch -c config/tasks/JDBCFetchTask.xml

# Execute Translate

# Execute Score

# Execute Qualify


# Execute Transfer
