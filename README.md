# GoldenMoa

This Hadoop MapReduce calculates the most profitable items to flip in GW2.

**What GoldenMoa is used for**
- Discovering less-known niches to invest your gold in.
- Getting a good sense of what groups of items are more profitable.
- Finding new items to attempt flipping.
- Comparing your usual item profits with new markets.

**What GoldenMoa is _not_ used for**
- Guaranteeing that the generated dataset will earn you profit (the market _will_ change).
- Blinding investing in all listed items in the dataset.

## Commands

### GW2SpidyMain

#### Description

Generates a dataset called `allitemsdataset.txt` in folder of `pom.xml` using GW2Spidy API requests and some additional
parsing. 

#### Usage

```
hadoop jar JAR gw2.GW2SpidyMain [OPTIONS]
```

| Name             | Default       | Description                                                                 |
| ---------------- | ------------- | --------------------------------------------------------------------------- |
| `--threshold`    | 100           | Sets the bucket threshold by copper amount (eg. 100 is 100 copper/1 silver) |

### GW2Job

#### Description

Calculates most profitable GW2 items based on bucket threshold and margins. Requires `allitemsdataset.txt`.

#### Usage

```
hadoop jar JAR GW2Job INPUT OUTPUT [OPTIONS]
```

| Name               | Default       | Description                                                               |
| ------------------ | ------------- | ------------------------------------------------------------------------- |
| `--minbuycount`    | 500           | Sets the minimum buy listings required by each item, ensures item demand. |

## How to Run

_Prerequsite(s):_ Installed Hadoop MapReduce, started all services.

1.  Create a jar file by running the following in the main folder where `pom.xml` is located.
    ```
    mvn clean install
    ```
    This creates a jar with the relative path `target/golden-moa-1.0.jar`

2.  Generate our dataset from current trading post data. See section above for command usage.
    ```
    hadoop jar target/golden-moa-1.0.jar gw2.GW2SpidyMain
    ```
    This generates `allitemsdataset.txt`.
    
3.  Create input folder and add dataset.
    ```
    hadoop fs -mkdir -p ~/input/
    hadoop fs -put allitemsdataset.txt ~/input/
    
    // We can see the file has been successfully added.
    hadoop fs -ls ~/input/
    ```

4.  Run MapReduce. See section above for command usage.
    ```
    hadoop jar target/golden-moa-1.0.jar GW2Job ~/input/ ~/output/
    ```
    
5.  View output of the MapReduce.
    ```
    // Copy output files locally to view
    hadoop fs -copyToLocal ~/output/ .
    
    // View top 10, lowest profit items
    cat output/part-r-00000 | sort -n -k1 | head -n10
    
    // Clean up
    hadoop fs -rm -r ~/output/
    ```

## Example MapReduce Result

Result from `cat output/part-r-00000 | sort -n -k1 | head -n10`. These are lowest profitable items with buy counts over 
500 and mapped into 1 silver buckets which are the default values.
```
246    Name: Rampager's Rogue Pants of Divinity Sell Price: 2000 Buy Price: 1300 Sell Count: 746 Buy Count: 859
449    Name: Ruby Platinum Earring Sell Price: 2200 Buy Price: 1200 Sell Count: 467 Buy Count: 571
450    Name: Rampager's Iron Shield of the Geomancer Sell Price: 2600 Buy Price: 1400 Sell Count: 31 Buy Count: 740
552    Name: Mighty Bronze Greatsword Sell Price: 3800 Buy Price: 1300 Sell Count: 303 Buy Count: 553
576    Name: Valkyrie Masquerade Leggings Sell Price: 6000 Buy Price: 1500 Sell Count: 272 Buy Count: 699
601    Name: Berserker's Seer Mantle of Vampirism Sell Price: 3600 Buy Price: 2300 Sell Count: 439 Buy Count: 1354
656    Name: Rampager's Gladiator Helm Sell Price: 1900 Buy Price: 1600 Sell Count: 339 Buy Count: 704
713    Name: Carnelian Silver Amulet Sell Price: 7200 Buy Price: 1700 Sell Count: 430 Buy Count: 732
766    Name: Penetrating Krytan Greatsword of the Night Sell Price: 16300 Buy Price: 1600 Sell Count: 83 Buy Count: 594
985    Name: Rejuvenating Steel Plated Inscription Sell Price: 5100 Buy Price: 2700 Sell Count: 785 Buy Count: 1310
```

As an example of custom thresholds and arguments, this is a MapReduce using `--threshold 1000` and `--minbuycount 1000`.
These results are also displayed using `cat output/part-r-00000 | sort -n -k1 | head -n10`, showing the lowest profit
items with buckets of 10 silver and at least 1000 buy listings.

```
5439    Name: Hearty Intricate Cotton Insignia Sell Price: 8000 Buy Price: 5000 Sell Count: 143 Buy Count: 1361
9841    Name: Mini Infinirarium Sell Price: 13000 Buy Price: 12000 Sell Count: 525 Buy Count: 1209
13693    Name: Oiled Orichalcum Helmet Casing Sell Price: 18000 Buy Price: 13000 Sell Count: 249 Buy Count: 1607
16680    Name: Mini Captain Magnus Sell Price: 21000 Buy Price: 16000 Sell Count: 559 Buy Count: 1170
17152    Name: Endless Blue Quaggan Tonic Sell Price: 23000 Buy Price: 19000 Sell Count: 528 Buy Count: 2398
17173    Name: Feast of Truffle Steak Dinner Sell Price: 22000 Buy Price: 17000 Sell Count: 602 Buy Count: 1527
24623    Name: Soldier's Benthic Waterbreather of the Afflicted Sell Price: 31000 Buy Price: 29000 Sell Count: 421 Buy Count: 1727
33016    Name: Carbonized Mithrillium Ingot Sell Price: 41000 Buy Price: 36000 Sell Count: 685 Buy Count: 1834
33173    Name: Mini Undead Chicken Sell Price: 41000 Buy Price: 30000 Sell Count: 334 Buy Count: 1677
98019    Name: 20 Slot Gossamer Bag Sell Price: 117000 Buy Price: 100000 Sell Count: 1444 Buy Count: 1431
``` 
