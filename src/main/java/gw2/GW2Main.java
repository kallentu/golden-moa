package gw2;

import java.io.IOException;

public class GW2Main {
    public static void main(String[] args) {
        try {
            GW2SpidyParser.allItemsToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
