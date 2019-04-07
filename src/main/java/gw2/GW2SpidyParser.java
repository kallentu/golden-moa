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
    public static void allItemsToFile() throws IOException {
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

            // Round up to nearest 100
            int maxOfferUnitPrice = (item.getInt("max_offer_unit_price") + 100) / 100 * 100;
            // Round down to nearest 100
            int minSaleUnitPrice = (item.getInt("min_sale_unit_price") - 100) / 100 * 100;

            String itemLine = name + "\t" +
                    maxOfferUnitPrice + "\t" +
                    minSaleUnitPrice + "\t" +
                    item.getInt("offer_availability") + "\t" +
                    item.getInt("sale_availability") + "\n";
            fileLines.add(itemLine);
        }

        Files.write(file, fileLines);
    }
}
