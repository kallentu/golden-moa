import gw2.GW2Writable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.Iterator;

/** MapReduce job to generate the most profitable/flippable items on the GW2 Trading Post. */
public class GW2Job extends Configured implements Tool {
    private static final String MIN_BUY_COUNT_CONF = "golden-moa.minbuycount";

    public static class GW2Mapper implements Mapper<LongWritable, Text, IntWritable, GW2Writable> {
        @Override
        public void configure(JobConf jobConf) { }

        /**
         * Takes data set information and maps to key-value:
         * < buy-price-lowerbound, GW2Writable >
         */
        @Override
        public void map(LongWritable key, Text input, OutputCollector<IntWritable, GW2Writable> output, Reporter reporter) throws IOException {
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

                output.collect(new IntWritable(buyPrice), gw2Writable);
            }
        }

        @Override
        public void close() throws IOException { }
    }

    public static class GW2Reducer implements Reducer<IntWritable, GW2Writable, IntWritable, GW2Writable> {
        // Minimum buy listings count, ensures item is in demand.
        private int minBuyCount;

        @Override
        public void configure(JobConf jobConf) {
            minBuyCount = jobConf.getInt(MIN_BUY_COUNT_CONF, 500);
        }

        /** Additional filter for max profit with a certain number of buy listings. */
        @Override
        public void reduce(IntWritable intWritable, Iterator<GW2Writable> items, OutputCollector<IntWritable, GW2Writable> output, Reporter reporter) throws IOException {
            GW2Writable maxProfitItem = null;
            double maxProfit = 0;

            while(items.hasNext()) {
                GW2Writable item = items.next();

                // With this reduce, want fast moving items that have demand at a set buy threshold.
                if (item.getBuyCountInt() < minBuyCount) {
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
                output.collect(new IntWritable((int) maxProfit), maxProfitItem);
            }
        }

        @Override
        public void close() throws IOException { }
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
    public int run(String[] args) throws Exception {
        JobConf job = new JobConf(getConf(), GW2Job.class);
        job.setJobName("GW2Job");
        job.setJarByClass(GW2Job.class);

        job.setMapperClass(GW2Mapper.class);
        job.setCombinerClass(GW2Reducer.class);
        job.setReducerClass(GW2Reducer.class);

        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(GW2Writable.class);

        job.setInputFormat(TextInputFormat.class);
        job.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // Additional optional arguments
        // [--minbuycount MINBUY]   default: 500
        if (args.length > 2) {
            for (int i = 2; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals("--minbuycount")) {
                    job.setInt(MIN_BUY_COUNT_CONF, Integer.valueOf(args[++i]));
                }
            }
        }

        JobClient.runJob(job);
        return 0;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new GW2Job(), args);
        System.exit(res);
    }
}
