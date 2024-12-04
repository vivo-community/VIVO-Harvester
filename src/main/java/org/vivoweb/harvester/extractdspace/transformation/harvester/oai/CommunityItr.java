package org.vivoweb.harvester.extractdspace.transformation.harvester.oai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.vivoweb.harvester.extractdspace.model.Collection;
import org.vivoweb.harvester.extractdspace.model.Community;
import org.vivoweb.harvester.extractdspace.model.Item;

public class CommunityItr implements Iterator<Community> {

    private final DspaceOAI dspaceHarvester;
    private List<Community> oaiPage = null;
    private final Item nextCommunity = null;

    public CommunityItr(DspaceOAI hr) {
        this.dspaceHarvester = hr;
    }

    @Override
    public boolean hasNext() {
        if (oaiPage != null && !oaiPage.isEmpty()) {
            return true;
        }
        OAIPMHResponse oaipmhResponse;
        boolean hasNext = false;
        boolean iterate = false;
        if (!this.dspaceHarvester.isEmpty()) {
            String responseXML = null;
            do {
                try {
                    responseXML = this.dspaceHarvester.getHttpClient()
                        .doRequest(this.dspaceHarvester.getBaseURI(),
                            this.dspaceHarvester.getVerb(), this.dspaceHarvester.getSet(),
                            this.dspaceHarvester.getFrom(), this.dspaceHarvester.getUntil(),
                            this.dspaceHarvester.getMetadata(),
                            this.dspaceHarvester.getResumptionToken(),
                            this.dspaceHarvester.getIdentifier());

                    oaipmhResponse = new OAIPMHResponse(responseXML, dspaceHarvester.getConf());

                    oaiPage = oaipmhResponse.modelCommunity();

                    HashMap<String, List<Collection>> hmCollection =
                        new HashMap<String, List<Collection>>();
                    Iterator<Collection> CollectionItr = this.dspaceHarvester.harvestCollection();
                    while (CollectionItr.hasNext()) {
                        Collection cl = CollectionItr.next();
                        for (String comId : cl.getIsPartOfCommunityID()) {
                            if (!hmCollection.containsKey(comId)) {
                                hmCollection.put(comId, new ArrayList());

                            }
                            hmCollection.get(comId).add(cl);
                        }

                    }

                    for (Community com : oaiPage) {
                        String id = com.getId();
                        com.setHasCollection(hmCollection.get(id));

                    }
                } catch (Exception ex) {
                    return hasNext;
                }

                Optional<String> resumptionToken = oaipmhResponse.getResumptionToken();
                if (resumptionToken.isPresent() && !resumptionToken.get().isEmpty()) {
                    this.dspaceHarvester.setResumptionToken(resumptionToken.get());
                } else {
                    this.dspaceHarvester.setResumptionToken(null);
                    this.dspaceHarvester.setEmpty(true);

                }

                if (!oaiPage.isEmpty()) {
                    hasNext = true;
                    iterate = false;
                } else if (this.dspaceHarvester.getResumptionToken() != null) {
                    iterate = true;
                } else {
                    hasNext = false;
                    iterate = false;
                }

            } while (iterate);

        }
        return hasNext;
    }

    @Override
    public Community next() {
        Community get = oaiPage.get(0);
        oaiPage.remove(get);

        return get;
    }

}
