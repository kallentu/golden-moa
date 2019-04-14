package gw2;

import java.io.IOException;

public class GW2SpidyMain {
    public static void main(String[] args) {
        try {
            GW2SpidyParser.addAllItemsToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
