package gw2;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GW2SpidyParser {
    /**
     * Each row of the file output will look like such:
     * name    sellPrice    buyPrice    sellCount    buyCount
     */
    public static void addAllItemsToFile() throws IOException {
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

            // Round down to nearest 100
            // Amount that people are selling for on the market, can be immediately purchased
            // and always higher than buying price.
            int sellingUnitPrice = (item.getInt("min_sale_unit_price") - 100) / 100 * 100;
            sellingUnitPrice = sellingUnitPrice > 0 ? sellingUnitPrice : 0;

            // Round up to nearest 100
            // Amount that buyers want to buy for, can be immediately sold to
            // and usually lower than selling price.
            int buyingUnitPrice = (item.getInt("max_offer_unit_price") + 100) / 100 * 100;

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
