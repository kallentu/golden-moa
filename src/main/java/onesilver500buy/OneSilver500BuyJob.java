package onesilver500buy;

import gw2.GW2Writable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class OneSilver500BuyJob {
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
