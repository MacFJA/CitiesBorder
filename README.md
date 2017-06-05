CitiesBorder
============

This library (and command line tool) allow you to get the border of cities.  
A city border is the geometric shape that define the outer line of a city at the administrative level. The border is defined by a list of GPS location which, when connected together, draw the outline of a city.

Installation
------------

Clone the project:
```
git clone https://github.com/MacFJA/CitiesBorder.git
```
Install the project into your local Maven repository:
```
cd CitiesBorder/
mvn clean
mvn install
```
Remove the source:
```
cd ..
rm -r CitiesBorder/
```
Add the dependency in your Maven project:
```xml
<project>
    <!-- ... -->
    <dependencies>
        <!-- ... -->
        <dependency>
            <groupId>io.github.macfja</groupId>
            <artifactId>cities-border</artifactId>
            <version>1.0.0</version>
        </dependency>
        <!-- ... -->
    </dependencies>
    <!-- ... -->
</project>
```

Dependencies
------------

All dependencies are **optional**.  
Because If you do not intend to use the CLI, the CLI argument parser is useless for you.  
Same thing, if you don't intend to transform raw PBF to XML (because you already have a CitiesBorder file or a generated Xml file), all dependencies to Osmosis are not necessary for you.

Usage
-----

### Create a lite OSM Xml

Because, by default, the OSM file contains a lots of information that are not needed for getting the border of a city, we run Osmosis first to reduce the number of elements.

To do so you have 2 possibilities:

 - Get the Osmosis tool and run a command
 - Execute a pre-configured Osmosis command inside this library

The Osmosis parameters configured in the library are:

 - `--read-pbf file=$INPUT_FILE_PATH`
 - `--tf accept-relation admin_level=$ADMINISTRATION_LEVEL`
 - `--tf accept-relation ref:INSEE=*`
 - `--used-way`
 - `--used-node`
 - `--write-xml $OUTPUT_FILE_PATH`

The **INSEE** parameters is for finding cities in France (it's a national city identification code, similar to a ZipCode, but it's more precise).

The method to call in the library is:
```java
io.github.macfja.citiesborder.Worker.runOsmosis(String outputPath, String inputPath, int administrationLevel)
```

### Transform Xml OSM file into a CitiesBorder file

To transform the OSM Xml file into a much compact file (for more detail about the file format, see below) you can use the method:
```java
io.github.macfja.citiesborder.Worker.runBuildCitiesBorderFile(String inputPath, String outputPath, boolean append)
```

### Search city into CitiesBorder file

To search a city (and get its border) you can use the method:
```java
io.github.macfja.citiesborder.Worker.search(String inputPath, String name)
```

CitiesBorder file format
------------------------

The CitiesBorder file is a very simple file format.  
The content is divided into cities, which are divided into 2 main part: the city header, and the list of GPS position.

The whole file is compressed with GZip.

### Format definition

```bnf
FileFormat   ::= 0*(CITY)
CITY         ::= CITY_HEADER "\n" GPS_LIST
CITY_HEADER  ::= "{" 1*(<CHAR>) "}:" 1*(<DIGIT>)
GPS_LIST     ::= 1*( GPS_POSITION  "\n" )
GPS_POSITION ::= LAT " " LON
LAT          ::= 1*(<DIGIT>) "." 1*(<DIGIT>) ; A GPS latitude
LON          ::= 1*(<DIGIT>) "." 1*(<DIGIT>) ; A GPS longitude
```

The number after the name of the city is the number of char for all GPS position (`\n` included). It's for retrieving or skip data (which allow us to rapidly read the file as the majority of the file is GPS data).

### Example

```
{Lannoy}:639
50.663547 3.2092917
50.6635639 3.2093238
50.664 3.2101538
50.6659982 3.2139546
50.6660288 3.2139043
50.6660893 3.2138047
50.6667942 3.2148888
50.6675158 3.2137095
50.6676128 3.2136204
50.6678023 3.2131893
50.6680804 3.212179
50.6680804 3.212179
50.6681387 3.2119827
50.6682581 3.2113993
50.6683653 3.2108751
50.6685022 3.2105947
50.6685022 3.2105947
50.6681897 3.2102959
50.6671891 3.2077656
50.6672082 3.2077477
50.6665476 3.2062119
50.6664682 3.2060274
50.6663932 3.205832
50.6663794 3.2057961
50.663547 3.2092917
50.663747 3.2089819
50.665149 3.2070008
50.6652317 3.2070023
50.6661605 3.2059508
50.6662971 3.2058326
50.6663794 3.2057961
{Beaurain}:768
50.1818918 3.5445628
50.1816404 3.545618
50.1816663 3.5469924
50.1831404 3.5479035
50.1827674 3.5490806
50.1828224 3.5515172
50.1840922 3.552798
50.1711761 3.558626
50.1721295 3.5561344
50.1718745 3.555307
50.1713492 3.5551178
50.172746 3.5511654
50.1723348 3.5508619
50.1743752 3.5465735
50.1740406 3.5461546
50.1751985 3.5434273
50.1780552 3.5439677
50.1799378 3.5440147
50.1807972 3.5446813
50.1818918 3.5445628
50.1840922 3.552798
50.1835394 3.5530961
50.1824564 3.5548128
50.1815223 3.5553422
50.1812211 3.5554197
50.1805283 3.5563805
50.1792652 3.5570292
50.1786376 3.5569708
50.1779876 3.5564441
50.1775252 3.5562623
50.1761873 3.5562286
50.1749867 3.5561133
50.1749752 3.5572187
50.1739328 3.5576603
50.1739446 3.559149
50.1733293 3.5605369
50.1711761 3.558626
```
(See them in Nominatim: [Lannoy](http://nominatim.openstreetmap.org/details.php?place_id=171402909), [Beaurain](http://nominatim.openstreetmap.org/details.php?place_id=171401473))

Limitation
----------

 - If two (or more) cities have the same name, the first found will be return by the search function.
 - As the library was first written for the France context, the Osmosis transformation included in the library only work with cities of France.
 - The way that library read OSM Xml can be very memory unefficient on a large Xml file.
 - The transformation from Xml to CitiesBorder file assume that the Xml first contains all `<node>`, then all `<way>` and finally all `<relation>` (if it's not the case, the code will lead to an uncomplete file)