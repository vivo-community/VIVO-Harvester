package org.vivoweb.harvester.extractdspace.transformation.harvester.oai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.vivoweb.harvester.extractdspace.model.Item;

public class ItemItr implements Iterator<Item> {

    private final DspaceOAI dspaceHarvester;
    private List<Item> oaiPage = null;
    private final Item nextItem = null;
    private boolean finalValue;
    private List<String> setsList = null;

    public ItemItr(DspaceOAI hr) {
        this.dspaceHarvester = hr;
        this.finalValue = false;
        setsList = new ArrayList<>();
    }

    @Override
    public boolean hasNext() {
        if (oaiPage != null && !oaiPage.isEmpty()) {
            return true;
        }
        OAIPMHResponse oaipmhResponse;
        boolean hasNext = false;
        boolean iterate = false;
        if (!finalValue) {
            String responseXML;
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
                    oaiPage = oaipmhResponse.modelItemsxoai();
                    this.dspaceHarvester.setRecoverSets(new ArrayList<>());
                    for (String spec : oaipmhResponse.getSetSpec()) {
                        if (!this.dspaceHarvester.getRecoverSets().contains(spec)) {
                            this.dspaceHarvester.getRecoverSets().add(spec);
                        }

                    }
                } catch (Exception ex) {
                    return hasNext;
                }

                Optional<String> resumptionToken = oaipmhResponse.getResumptionToken();
                if (resumptionToken.isPresent() && !resumptionToken.get().isEmpty()) {
                    this.dspaceHarvester.setResumptionToken(resumptionToken.get());
                } else {
                    this.dspaceHarvester.setResumptionToken(null);
                    finalValue = true;

                }

                if (!oaiPage.isEmpty()) {
                    hasNext = true;
                    iterate = false;
                } else if (this.dspaceHarvester.getResumptionToken() != null) {
                    iterate = true;
                } else {
                    iterate = false;
                }

            } while (iterate);

        }
        return hasNext;
    }

    @Override
    public Item next() {
        Item get = oaiPage.get(0);
        oaiPage.remove(get);

        return get;
    }

}
