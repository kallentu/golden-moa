package onesilver500buy;

import gw2.GW2Writable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class OneSilver500BuyReducer extends Reducer<IntWritable, GW2Writable, IntWritable, GW2Writable> {
    private static final double LISTING_FEE_PERCENT = 0.05;
    private static final double TAX_FEE_PERCENT = 0.1;

    /**
     * Algorithm for calculating profit is:
     * Bell price - buy price - 10% of sell price - 5% of sell price
     *
     * For this particular set, we want over 500 buy counts and max profit with no other conditions.
     */
    @Override
    public void reduce(IntWritable key, Iterable<GW2Writable> items, Context context) throws IOException, InterruptedException {
        GW2Writable maxProfitItem = null;
        double maxProfit = 0;

        for (GW2Writable item : items) {

            // With this reduce, want fast moving items that have demand at a 500 buy threshold.
            if (item.getBuyCountInt() < 500) {
                continue;
            }

            double itemProfit = getItemProfit(item);
            if (itemProfit > maxProfit) {
                maxProfit = itemProfit;
                maxProfitItem = item;
            }
        }

        // Profitable item in this margin
        if (maxProfitItem != null) {
            // Truncation to int, simplifies data.
            context.write(new IntWritable((int) maxProfit), maxProfitItem);
        }
    }

    private double getItemProfit(GW2Writable item) {
        double listingFee = item.getSellPriceInt() * LISTING_FEE_PERCENT;
        double taxFee = item.getSellPriceInt() * TAX_FEE_PERCENT;

        return item.getSellPriceInt() -
                item.getBuyCountInt() -
                listingFee -
                taxFee;
    }
}
