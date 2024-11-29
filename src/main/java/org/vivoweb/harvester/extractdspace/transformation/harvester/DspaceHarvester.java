package org.vivoweb.harvester.extractdspace.transformation.harvester;

import java.util.Iterator;
import java.util.Properties;
import org.vivoweb.harvester.extractdspace.model.Collection;
import org.vivoweb.harvester.extractdspace.model.Community;
import org.vivoweb.harvester.extractdspace.model.Item;
import org.vivoweb.harvester.extractdspace.model.Repository;

public abstract class DspaceHarvester {

    protected Properties conf = null;

    public DspaceHarvester(Properties conf) {
        this.conf = conf;
    }

    public Properties getConf() {
        return conf;
    }

    public abstract void connect();

    public abstract Iterator<Item> harvestItems();

    public abstract Iterator<Community> harvestCommunity();

    public abstract Iterator<Collection> harvestCollection();

    public abstract Iterator<Repository> harvestRepository();
}
