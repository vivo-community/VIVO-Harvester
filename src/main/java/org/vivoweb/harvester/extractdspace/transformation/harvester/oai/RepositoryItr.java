package org.vivoweb.harvester.extractdspace.transformation.harvester.oai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.vivoweb.harvester.extractdspace.model.Community;
import org.vivoweb.harvester.extractdspace.model.Item;
import org.vivoweb.harvester.extractdspace.model.Repository;

public class RepositoryItr implements Iterator<Repository> {

    private DspaceOAI dspaceHarvester;
    private List<Repository> oaiPage = null;
    private Item nextRepository = null;

    public RepositoryItr(DspaceOAI hr) {
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
                    responseXML = this.dspaceHarvester.getHttpClient().doRequest(this.dspaceHarvester.getBaseURI(), this.dspaceHarvester.getVerb(), this.dspaceHarvester.getSet(), this.dspaceHarvester.getFrom(), this.dspaceHarvester.getUntil(), this.dspaceHarvester.getMetadata(),
                            this.dspaceHarvester.getResumptionToken(), this.dspaceHarvester.getIdentifier());

                    oaipmhResponse = new OAIPMHResponse(responseXML, dspaceHarvester.getConf());

                    oaiPage = oaipmhResponse.modelRepository();

                    Iterator<Community> CommunityItr = this.dspaceHarvester.harvestCommunity();
                    Repository base = oaiPage.get(0);
                    base.setHasCommunity(new ArrayList());
                    while (CommunityItr.hasNext()) {
                        Community com = CommunityItr.next();
                        base.getHasCommunity().add(com);

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
                } else if (this.dspaceHarvester.getResumptionToken() != null) {
                    iterate = true;
                }

            } while (iterate);

        }
        return hasNext;
    }

    @Override
    public Repository next() {
        Repository get = oaiPage.get(0);
        oaiPage.remove(get);

        return get;
    }

}
