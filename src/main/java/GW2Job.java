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

/** MapReduce job to generate the most profitable/flippable items on the GW2 Trading Post. */
public class GW2Job {
    private static int MIN_BUY_PRICE = 500;

    public static class GW2Mapper extends Mapper<LongWritable, Text, IntWritable, GW2Writable> {
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

    public static class GW2Reducer extends Reducer<IntWritable, GW2Writable, IntWritable, GW2Writable> {
        /** Additional filter for max profit with a certain number of buy listings. */
        @Override
        public void reduce(IntWritable key, Iterable<GW2Writable> items, Context context) throws IOException, InterruptedException {
            GW2Writable maxProfitItem = null;
            double maxProfit = 0;

            for (GW2Writable item : items) {

                // With this reduce, want fast moving items that have demand at a set buy threshold.
                if (item.getBuyCountInt() < MIN_BUY_PRICE) {
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
     *
     * Usage: hadoop jar target/golden-moa-1.0.jar GW2Job INPUT OUTPUT [--threshold THRESHOLD] [--minbuycount MINBUY]
     */
    public static void main(String[] args) throws Exception {
        Configuration config = new Configuration();
        Job job = Job.getInstance(config, "GW2Job");
        job.setJarByClass(GW2Job.class);
        job.setMapperClass(GW2Mapper.class);
        job.setCombinerClass(GW2Reducer.class);
        job.setReducerClass(GW2Reducer.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(GW2Writable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // Additional optional arguments
        // [--minbuycount MINBUY]   default: 500
        if (args.length > 2) {
            for (int i = 2; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals("--minbuycount")) {
                    MIN_BUY_PRICE = Integer.valueOf(args[++i]);
                }
            }
        }

        job.waitForCompletion(true);
    }
}
