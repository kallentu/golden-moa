package onesilver500buy;

import gw2.GW2Writable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class OneSilver500BuyMapper extends Mapper<LongWritable, Text, IntWritable, GW2Writable> {
    /**
     * Takes data set information and maps to key-value:
     * < silver-buy-price-lowerbound, GW2Writable >
     */
    @Override
    public void map(LongWritable key, Text input, Context context) throws IOException, InterruptedException {
        String line = input.toString();
        String[] itemDataRow = line.split("\t");

        // Check that the data row has all the correct information, no less.
        if (itemDataRow.length == 5) {
            String itemName = itemDataRow[0];
            int sellPrice = Integer.parseInt(itemDataRow[1]);
            int buyPrice = Integer.parseInt(itemDataRow[2]);
            int sellCount = Integer.parseInt(itemDataRow[3]);
            int buyCount = Integer.parseInt(itemDataRow[4]);

            GW2Writable gw2Writable = new GW2Writable(
                    itemName,
                    sellPrice,
                    buyPrice,
                    sellCount,
                    buyCount);

            context.write(new IntWritable(buyPrice), gw2Writable);
        }
    }

}
