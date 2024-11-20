package org.vivoweb.dspacevivo.transformation.harvester.restv7;

import java.util.Iterator;
import java.util.List;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.vivoweb.dspacevivo.model.Item;

public class ItemItr implements Iterator<Item> {

    private int page = 0;
    private int size = 20;
    private List<Item> restPage = Lists.newArrayList();
    private Item nextItem = null;
    private RESTv7Harvester endpoint;

    public ItemItr(RESTv7Harvester endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public boolean hasNext() {
        if (!restPage.isEmpty()) {
            return true;
        }
        JsonNode body = this.endpoint.getItemsPage(this.page, this.size);
        this.page++;
        boolean hasNext = body.getObject().has("_embedded");
        if (hasNext) {
            JSONArray jsonArray = body.getObject().getJSONObject("_embedded").getJSONArray("items");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Item item = this.endpoint.getItem(jsonObject);
                restPage.add(item);
            }

        }
        return !restPage.isEmpty();
    }

    @Override
    public Item next() {
        Item get = restPage.get(0);
        restPage.remove(get);
        return get;
    }

}
