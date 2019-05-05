package gw2;

import java.io.IOException;

/** Generates GW2 trading post dataset from GW2Spidy. */
public class GW2SpidyMain {

    /** Usage: hadoop jar JAR gw2.GW2SpidyMain [--threshold THRESHOLD] */
    public static void main(String[] args) throws IOException{
        GW2SpidyParser gw2SpidyParser = new GW2SpidyParser();

        // Additional optional arguments.
        // [--threshold THRESHOLD]  default: 100
        if (args.length != 0) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals("--threshold")) {
                    gw2SpidyParser.setRoundingBound(Integer.valueOf(args[++i]));
                }
            }
        }
        gw2SpidyParser.createAllItemsDataSetFile();
    }
}
