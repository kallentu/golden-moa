package onesilver500buy;

import gw2.GW2Writable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class OneSilver500Buy {

    public static class OneSilver500BuyMapper extends Mapper<LongWritable, Text, IntWritable, GW2Writable> {
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

    public static class OneSilver500BuyReducer extends Reducer<IntWritable, GW2Writable, IntWritable, GW2Writable> {
        /**
         * Algorithm for calculating profit is:
         * Bell price - buy price - 10% of sell price - 5% of sell price
         * <p>
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

                double itemProfit = item.getItemProfit();
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
    }


    /**
     * Mapper categorizes by buy price because we are looking for items at each bracket.
     * Meanwhile, reducer provides the profit information since the original mapper buy price is not
     * as important for the final result.
     *
     * Example line of resulting MapReduce:
     * 246  Name: Rampager's Rogue Pants of Divinity Sell Price: 1300 Buy Price: 2000 Sell Count: 746 Buy Count: 859
     */
    public static void main(String[] args) throws Exception {
        Configuration config = new Configuration();
        Job job = Job.getInstance(config, "OneSilver500BuyJob");
        job.setJarByClass(OneSilver500Buy.class);
        job.setMapperClass(OneSilver500BuyMapper.class);
        job.setCombinerClass(OneSilver500BuyReducer.class);
        job.setReducerClass(OneSilver500BuyReducer.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(GW2Writable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.waitForCompletion(true);
    }
}
