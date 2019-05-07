package gw2;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Parses API data and formats them into a dataset readable for MapReducing*/
public class GW2SpidyParser {
    // Rounded up/down to this copper bucket.
    // eg. 100 is 1 silver or 100 copper.
    private int roundingBound;

    public GW2SpidyParser() {
        this(100);
    }

    public GW2SpidyParser(int roundingBound) {
        this.roundingBound = roundingBound;
    }

    public void setRoundingBound(int newRoundingBound) { roundingBound = newRoundingBound; }

    /**
     * Each row of the file output will look like such:
     * name    sellPrice    buyPrice    sellCount    buyCount
     */
    public void createAllItemsDataSetFile() throws IOException {
        // Clear input file ready for writing
        Path file = Paths.get("allitemsdataset.txt");
        Files.deleteIfExists(file);
        Files.createFile(file);

        List<String> fileLines = new ArrayList<>();

        // Retrieve all items to round up offer and round down selling.
        // This will cause some error with calculation, but can be treated as rounding as we will earn more.
        JSONArray allItemsJson = GW2SpidyAPI.getAllItemsJSON();
        for (int i = 0; i < allItemsJson.length(); i++) {
            JSONObject item = allItemsJson.getJSONObject(i);
            String name = item.getString("name");

            // Round down to nearest roundingBound value.
            // Amount that people are selling for on the market, can be immediately purchased
            // and always higher than buying price.
            int sellingUnitPrice = (item.getInt("min_sale_unit_price") - roundingBound) / roundingBound * roundingBound;
            sellingUnitPrice = sellingUnitPrice > 0 ? sellingUnitPrice : 0;

            // Round up to nearest roundingBound value.
            // Amount that buyers want to buy for, can be immediately sold to
            // and usually lower than selling price.
            int buyingUnitPrice = (item.getInt("max_offer_unit_price") + roundingBound) / roundingBound * roundingBound;

            String itemLine = name + "\t" +
                    sellingUnitPrice + "\t" +
                    buyingUnitPrice + "\t" +
                    item.getInt("offer_availability") + "\t" +
                    item.getInt("sale_availability") + "\n";
            fileLines.add(itemLine);
        }

        Files.write(file, fileLines);
    }
}
