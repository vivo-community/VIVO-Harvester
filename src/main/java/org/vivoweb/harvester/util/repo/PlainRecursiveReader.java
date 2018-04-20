package org.vivoweb.harvester.util.repo;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vivoweb.harvester.util.FileAide;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Read-only record handler for transferring RDF records into a Jena model.
 */
public class PlainRecursiveReader extends RecordHandler {
    /**
     * SLF4J Logger
     */
    private static final Logger log = LoggerFactory.getLogger(PlainRecursiveReader.class);

    /**
     * The directory to store record files in
     */
    private String fileDir;

    /**
     * Default Constructor
     */
    public PlainRecursiveReader() {
        // Nothing to do here
        // Used by config construction
        // Should only be used in conjunction with setParams()
    }

    /**
     * Constructor
     * @param fileDir directory to store records in
     * @throws java.io.IOException error accessing directory
     */
    public PlainRecursiveReader(String fileDir) throws IOException {
        setFileDirObj(fileDir);
    }

    /**
     * Setter for fileDir
     * @param fileDir the directory path String
     * @throws java.io.IOException unable to connect
     */
    private void setFileDirObj(String fileDir) throws IOException {
        if(!FileAide.exists(fileDir)) {
            log.debug("Directory '" + fileDir + "' Does Not Exist, attempting to create");
            FileAide.createFolder(fileDir);
        }
        this.fileDir = fileDir;
    }

    @Override
    public void setParams(Map<String, String> params) throws IllegalArgumentException, IOException {
        setFileDirObj(getParam(params, "fileDir", true));
    }

    /**
     * Sanitizes a record id
     * @param id the record id
     * @return null if not needed, else the new id
     */
    private String sanitizeID(String id) {
        String s = id.replaceAll("\\n", "_-_NEWLINE_-_").replaceAll("\\r", "_-_RETURN_-_").replaceAll("\\t", "_-_TAB_-_").replaceAll(" ", "_-_SPACE_-_").replaceAll("\\\\", "_-_BACKSLASH_-_").replaceAll("/", "_-_FORWARDSLASH_-_").replaceAll(":", "_-_COLON_-_").replaceAll("\\*", "_-_STAR_-_").replaceAll("\\?", "_-_QUESTIONMARK_-_").replaceAll("\"", "_-_DOUBLEQUOTE_-_").replaceAll("<", "_-_LESSTHAN_-_").replaceAll(">", "_-_GREATERTHAN_-_").replaceAll("\\|", "_-_PIPE_-_");
        if(s.equals(id)) {
            return null;
        }
        log.debug("record id sanitized from '" + id + "' to '" + s + "'");
        return s;
    }

    @Override
    public boolean addRecord(Record rec, Class<?> operator, boolean overwrite) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates the metadata file for a given record
     * @param recID the record id
     * @throws java.io.IOException error writing metadata file
     */
    private void createMetaDataFile(String recID) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delRecord(String recID) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRecordData(String recID) throws IllegalArgumentException, IOException {
        String fo = null;
        BufferedReader br = null;
        try {
            StringBuilder sb = new StringBuilder();
            fo = this.fileDir+"/"+recID;
            if(!FileAide.exists(fo)) {
                throw new IllegalArgumentException("Record " + recID + " does not exist!");
            }
            br = new BufferedReader(new InputStreamReader(FileAide.getInputStream(fo)));
            String line;
            while((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();
            return sb.toString().trim();
        } catch(IOException e) {
            if(br != null) {
                try {
                    br.close();
                } catch(Exception ignore) {
                    // Ignore
                }
            }
            throw e;
        } finally {
            if(br != null) {
                try {
                    br.close();
                } catch(Exception ignore) {
                    // Ignore
                }
            }
        }
    }

    @Override
    protected void delMetaData(String recID) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void addMetaData(Record rec, RecordMetaData rmd) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected SortedSet<RecordMetaData> getRecordMetaData(String recID) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Record> iterator() {
        return new FileRecordIterator();
    }

    /**
     * Iterator for TextFileRecordHandler
     * @author cah
     */
    private class FileRecordIterator implements Iterator<Record> {
        /**
         * Iterator over the files
         */
        Iterator<String> fileNameIterator;

        /**
         * Default Constructor
         */
        protected FileRecordIterator() {
            Set<String> allFileListing = new TreeSet<String>();
            log.debug("Compiling list of records");
            addAllFiles(allFileListing, "", new File(fileDir));
            this.fileNameIterator = allFileListing.iterator();
            log.debug("List compiled");
        }

        private void addAllFiles(Set<String> listing, String prefix, File dir) {
            if (dir != null && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            addAllFiles(listing, prefix + file.getName() + File.separator, file);
                        } else {
                            if (StringUtils.isEmpty(prefix)) {
                                listing.add(file.getName());
                            } else {
                                listing.add(prefix + file.getName());
                            }
                        }
                    }
                }
            }
        }

        @Override
        public boolean hasNext() {
            return this.fileNameIterator.hasNext();
        }

        @Override
        public Record next() {
            try {
                return getRecord(this.fileNameIterator.next());
            } catch(IOException e) {
                throw new NoSuchElementException(e.getMessage());
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void close() throws IOException {
        // Do nothing
    }

    @Override
    public Set<String> find(String idText) {
        Set<String> retVal = new TreeSet<String>();
        for(Record r : this) {
            if(r.getID().contains(idText)) {
                retVal.add(r.getID());
            }
        }
        return retVal;
    }
}