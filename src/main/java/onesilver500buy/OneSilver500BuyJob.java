package onesilver500buy;

import gw2.GW2Writable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class OneSilver500BuyJob {
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
        job.setJarByClass(OneSilver500BuyJob.class);
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
