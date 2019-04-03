import gw2.GW2SpidyAPI;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class GW2SpidyAPITest extends TestCase {
    // Armorsmithing
    private static final int DISCIPLINE_ID = 4;

    // Type id for bags eg. Starter Backpack
    private static final int TYPE_ID = 2;

    // Honed Swindler Mask
    private static final int ITEM_ID = 200;
    private static final String ITEM_NAME = "Sunrise";

    // Bolt of Linen
    // Recipe Id is different than Item Id
    private static final int RECIPE_ID = 10;

    public GW2SpidyAPITest(String name) {
        super(name);
    }

    public void test_getTypesJSON_resultsReturned() {
        JSONArray results = null;
        try {
            results = GW2SpidyAPI.getTypesJSON();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(results);
        assertTrue(results.length() > 0);
    }

    public void test_getDisciplinesJSON_resultsReturned() {
        JSONArray results = null;
        try {
            results = GW2SpidyAPI.getDisciplinesJSON();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(results);
        assertTrue(results.length() > 0);
    }

    public void test_getRaritiesJSON_resultsReturned() {
        JSONArray results = null;
        try {
            results = GW2SpidyAPI.getRaritiesJSON();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(results);
        assertTrue(results.length() > 0);
    }

    public void test_getAllItemsJSON_resultsReturned() {
        JSONArray results = null;
        try {
            results = GW2SpidyAPI.getAllItemsJSON();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(results);
        assertTrue(results.length() >  0);
    }

    public void test_getItemsOfTypeJSON_resultsReturned() {
        JSONArray results = null;
        try {
            results = GW2SpidyAPI.getItemsOfTypeJSON(TYPE_ID);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(results);
        assertTrue(results.length() >  0);
    }

    public void test_getItemDataJSON_resultsReturned() {
        JSONObject result = null;
        try {
            result = GW2SpidyAPI.getItemDataJSON(ITEM_ID);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(result);
        assertNotNull(result.get("data_id"));
        assertNotNull(result.get("name"));
        assertNotNull(result.get("rarity"));
        assertNotNull(result.get("type_id"));
        assertNotNull(result.get("sub_type_id"));
        assertNotNull(result.get("price_last_changed"));
        assertNotNull(result.get("max_offer_unit_price"));
        assertNotNull(result.get("min_sale_unit_price"));
        assertNotNull(result.get("offer_availability"));
        assertNotNull(result.get("sale_availability"));
        assertNotNull(result.get("sale_price_change_last_hour"));
        assertNotNull(result.get("offer_price_change_last_hour"));
    }

    public void test_getItemBuyListingsJSON_resultsReturned() {
        JSONObject result = null;
        try {
            result = GW2SpidyAPI.getItemBuyListingsJSON(ITEM_ID);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(result);
        assertEquals(result.getString("sell-or-buy"), "buy");
        assertTrue(result.getInt("count") > 0);
    }

    public void test_getItemSellListingsJSON_resultsReturned() {
        JSONObject result = null;
        try {
            result = GW2SpidyAPI.getItemSellListingsJSON(ITEM_ID);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(result);
        assertEquals(result.getString("sell-or-buy"), "sell");
        assertTrue(result.getInt("count") > 0);
    }

    public void test_getItemsOfNameJSON_resultsReturned() {
        JSONArray results = null;
        try {
            results = GW2SpidyAPI.getItemsOfNameJSON(ITEM_NAME);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(results);
        assertTrue(results.length() > 0);
    }

    public void test_getAllRecipesJSON_resultsReturned() {
        JSONArray results = null;
        try {
            results = GW2SpidyAPI.getAllRecipesJSON();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(results);
        assertTrue(results.length() > 0);
    }

    public void test_getRecipesOfDisciplineJSON_resultsReturned() {
        JSONArray results = null;
        try {
            results = GW2SpidyAPI.getRecipesOfDisciplineJSON(DISCIPLINE_ID);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(results);
        assertTrue(results.length() > 0);
    }

    public void test_getRecipeDataJSON_resultsReturned() {
        JSONObject result = null;
        try {
            result = GW2SpidyAPI.getRecipeDataJSON(RECIPE_ID);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    public void test_getGemPriceJSON_resultsReturned() {
        JSONObject result = null;
        try {
            result = GW2SpidyAPI.getGemPriceJSON();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        assertNotNull(result);
        assertTrue(result.length() > 0);
        assertNotNull(result.get("gem_to_gold"));
        assertNotNull(result.get("gold_to_gem"));
    }
}
