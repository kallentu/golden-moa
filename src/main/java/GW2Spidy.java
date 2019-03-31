import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;

public class GW2Spidy {
    private static final String GW2_SPIDY_URL = "http://www.gw2spidy.com/api/v0.9/json/";
    private static final String RESULTS_KEY = "results";

    /**
     * Gets list of item types and subtypes.
     * Eg. Armor -> Gloves
     */
    public static JSONObject getTypesJSON() throws IOException {
        return sendRequest("types");
    }

    /**
     * Gets list of crafting disciplines.
     * Eg. Weaponsmithing
     */
    public static JSONObject getDisciplinesJSON() throws IOException {
        return sendRequest("disciplines");
    }

    /**
     * Gets list of item rarities.
     * Eg. Rare, Exotic
     */
    public static JSONObject getRaritiesJSON() throws IOException {
        return sendRequest("rarities");
    }

    /**
     * Gets list of all items.
     */
    public static JSONObject getAllItemsJSON() throws IOException {
        return sendRequest("all-items/all");
    }

    /**
     * Gets list of all items of a certain type.
     * Eg. All gloves which is typeid of 1
     */
    public static JSONObject getItemsOfTypeJSON(int typeId) throws IOException {
        return sendRequest("all-items/" + typeId);
    }

    /**
     * Gets the data of a particular item. High frequency of update.
     * Eg. Result will be as such:
     * {
     * "result" : {
     *     "data_id"              : 23654,
     *     "name"                 : "Fake Item",
     *     "rarity"               : 3,
     *     "restriction_level"    : 72,
     *     "img"                  : "http://www.url-to-offical-gw2-site.com/img.png",
     *     "type_id"              : 1,
     *     "sub_type_id"          : 2,
     *     "price_last_changed"   : "YYYY-MM-DD HH:II:SS UTC",
     *     "max_offer_unit_price" : 6523,
     *     "min_sale_unit_price"  : 9345,
     *     "offer_availability"   : 1235232,
     *     "sale_availability"    : 203203,
     *     "sale_price_change_last_hour"  : 40,  # this is the percentage the item price changed since the last hour
     *     "offer_price_change_last_hour" : 70   # same --^  I know it ain't pretty but have to do with this for now ;)
     * }
     */
    public static JSONObject getItemDataJSON(int itemId) throws IOException {
        return sendRequest("item/" + itemId);
    }

    /**
     * Gets a list of all buy offers for a certain item.
     * Eg. Result will be as such:
     * {
     * "sell-or-buy" : "buy",
     * "count"       : 250,
     * "page"        : 1,
     * "last_page"   : 3,
     * "total"        : 3,
     * "results"     : [
     *     {
     *         "listing_datetime" : "YYYY-MM-DD HH:II:SS UTC",
     *         "unit_price"       : 23,
     *         "quantity"         : 7868,
     *         "listings"         : 11
     *     },
     *     { / listing #2 /
     * }
     */
    public static JSONObject getItemBuyListingsJSON(int itemId) throws IOException {
        return sendRequest("listings/" + itemId + "/buy");
    }

    /**
     * Gets a list of all sell offers for a certain item.
     * Eg. Result will be as such:
     * {
     * "sell-or-buy" : "sell",
     * "count"       : 250,
     * "page"        : 1,
     * "last_page"   : 3,
     * "total"        : 3,
     * "results"     : [
     *     {
     *         "listing_datetime" : "YYYY-MM-DD HH:II:SS UTC",
     *         "unit_price"       : 23,
     *         "quantity"         : 7868,
     *         "listings"         : 11
     *     },
     *     { / listing #2 /
     * }
     */
    public static JSONObject getItemSellListingsJSON(int itemId) throws IOException {
        return sendRequest("listings/" + itemId + "/sell");
    }

    /** Gets related items by name. Might be slow, not recommended. */
    public static JSONObject getItemsOfNameJSON(String name) throws IOException {
        return sendRequest("item-search/" + name);
    }

    /** Get a list of all crafting recipes. */
    public static JSONObject getAllRecipesJSON() throws IOException {
        return sendRequest("recipes/all");
    }

    /** Gets a list of all crafting recipes for a certain discipline. */
    public static JSONObject getRecipesOfDisciplineJSON(int disciplineId) throws IOException {
        return sendRequest("recipes/" + disciplineId);
    }

    /**
     * Get the data of a particular recipe.
     * Eg. Result looks as such:
     * {
     * "result" : {
     *     "data_id"              : 3473,
     *     "name"                 : "Fake Recipe",
     *     "result_count"         : 3,
     *     "result_item_data_id"  : 23654,
     *     "discipline_id"        : 1,
     *     "result_item_max_offer_unit_price" : 6523,
     *     "result_item_min_sale_unit_price"  : 9345,
     *     "crafting_cost"           : 7532,
     *     "rating"                  : 360,
     * }
     * }
     */
    public static JSONObject getRecipeDataJSON(int recipeId) throws IOException {
        return sendRequest("recipes/" + recipeId);
    }

    /** Get the current gem/gold conversion rate. */
    public static JSONObject getGemPriceJSON() throws IOException {
        return sendRequest("gem-price");
    }

    private static JSONObject sendRequest(String arguments) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(GW2_SPIDY_URL + arguments);
            System.out.println("Executing GW2Spidy request " + httpGet.getRequestLine());

            // Returns the response as a String if successful, for further parse into JSON object
            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };

            String responseBody = httpClient.execute(httpGet, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);

            return new JSONObject(responseBody).getJSONObject(RESULTS_KEY);
        }

    }
}
