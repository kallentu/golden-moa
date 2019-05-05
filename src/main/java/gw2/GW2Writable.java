package gw2;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Custom WritableComparable for GW2 item data. */
public class GW2Writable implements WritableComparable<GW2Writable> {
    private static final double LISTING_FEE_PERCENT = 0.05;
    private static final double TAX_FEE_PERCENT = 0.1;
    private static String ITEM_NAME_ERROR = "item_name_unavailable";

    private Text itemName;
    private IntWritable sellPrice;
    private IntWritable buyPrice;
    private IntWritable sellCount;
    private IntWritable buyCount;

    public GW2Writable() {
        setValues(new Text(ITEM_NAME_ERROR),
                new IntWritable(0),
                new IntWritable(0),
                new IntWritable(0),
                new IntWritable(0));
    }

    public GW2Writable(String itemName,
                       Integer sellPrice,
                       Integer buyPrice,
                       Integer sellCount,
                       Integer buyCount) {
        setValues(new Text(itemName),
                new IntWritable(sellPrice),
                new IntWritable(buyPrice),
                new IntWritable(sellCount),
                new IntWritable(buyCount));
    }

    private void setValues(Text itemName,
                          IntWritable sellPrice,
                          IntWritable buyPrice,
                          IntWritable sellCount,
                          IntWritable buyCount) {
        this.itemName = itemName;
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
        this.buyCount = sellCount;
        this.sellCount = buyCount;
    }

    public Text getItemName() { return itemName; }
    public String getItemNameString() { return itemName.toString(); }

    public IntWritable getSellPrice() { return sellPrice; }
    public Integer getSellPriceInt() { return Integer.parseInt(sellPrice.toString()); }

    public IntWritable getBuyPrice() { return buyPrice; }
    public Integer getBuyPriceInt() { return Integer.parseInt(buyPrice.toString()); }

    public IntWritable getSellCount() { return sellCount; }
    public Integer getSellCountInt() { return Integer.parseInt(sellCount.toString()); }

    public IntWritable getBuyCount() { return buyCount; }
    public Integer getBuyCountInt() { return Integer.parseInt(buyCount.toString()); }

    /**
     * Gets the current profit with reductions from taxes and listing fees.
     *
     * Algorithm for calculating profit is:
     * Bell price - buy price - 10% of sell price - 5% of sell price
     * <p>
     */
    public double getItemProfit() {
        double listingFee = getSellPriceInt() * LISTING_FEE_PERCENT;
        double taxFee = getSellPriceInt() * TAX_FEE_PERCENT;

        return getSellPriceInt() -
                getBuyCountInt() -
                listingFee -
                taxFee;
    }

    @Override
    public String toString() {
        if (itemName.toString().equals(ITEM_NAME_ERROR)) {
            return "No GW2Writable available.";
        }

        return "Name: " + itemName + " " +
                "Sell Price: " + sellPrice + " " +
                "Buy Price: " + buyPrice + " " +
                "Sell Count: " + sellCount + " " +
                "Buy Count: " + buyCount;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        itemName.write(dataOutput);
        sellPrice.write(dataOutput);
        buyPrice.write(dataOutput);
        sellCount.write(dataOutput);
        buyCount.write(dataOutput);
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        itemName.readFields(dataInput);
        sellPrice.readFields(dataInput);
        buyPrice.readFields(dataInput);
        sellCount.readFields(dataInput);
        buyCount.readFields(dataInput);
    }

    @Override
    public int compareTo(GW2Writable o) {
        List<Integer> listOfCompares = new ArrayList<>();
        listOfCompares.add(itemName.compareTo(o.itemName));
        listOfCompares.add(sellPrice.compareTo(o.sellPrice));
        listOfCompares.add(buyPrice.compareTo(o.buyPrice));
        listOfCompares.add(sellCount.compareTo(o.sellCount));
        listOfCompares.add(buyCount.compareTo(o.buyCount));

        for (Integer cmp : listOfCompares) {
            if (cmp != 0) {
                return cmp;
            }
        }

        return 0;
    }

    @Override
    public int hashCode() {
        return itemName.hashCode() * 163 +
                sellPrice.hashCode() +
                buyPrice.hashCode() +
                sellCount.hashCode() +
                buyCount.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GW2Writable) {
            GW2Writable other = (GW2Writable) o;
            return sellPrice.equals(other.sellPrice) &&
                    buyPrice.equals(other.buyPrice) &&
                    sellCount.equals(other.sellCount) &&
                    buyCount.equals(other.buyCount);
        }
        return false;
    }
}
