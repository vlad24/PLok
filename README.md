# XBenchmarker

## Overview

*XBenchmarker* is a Java library for stress testing of *XStore* systems.
It emulates high load on the system by inserting data and querying it concurrently with configurable period.
All parameters of the benchmarker are typically set in configuration file, path to which is provided as a program argument.

## Structure
Main classes of XBenchmarker: 

* **Tester** - program entry point
* **Configurator** - class responsible for parsing property file and preparing all objects for stress attack
* **ReadFlow** - class responsible for giving out demanded read stress load
* **ReaderStream** - class performing queries with different time intervals and target sensor IDs. Multiple readers are supposed to be launched simultaneously
* **WriterStream** - class performing write a set of operations on XStore system. Operations are performed on regular basis with configurable delay. One writer is supposed to be launched

## Configuration file
Java properties file needed for **Configurator** class to configure the system. 
By default, the **Configurator** will search for file *configuration.properties* file as a resource on the classpath. If another filename is preferred it can be set via the first program argument. The configuration file can be logically divided into several sections which contain settings for different parts of the XBenchmarker system.


### Program configurations
|  Name  | Description  |  Type |  Default |
|:------:|:------------:|:-----:|:--------:|
|  phaseBreak    | Pause before launching writer and readers in seconds |  number | 3  | 
|  reportOutput  | Path to file where the report will be written. If special word \$timed is supplied the report is saved under report${MillisSinceEpoch}.txt |  path or $timed |  $timed | 

### Sensor configurations
|  Name  | Description  |  Type |  Default |
|:------:|:------------:|:-----:|:--------:|
| sensorsDir  | Directory with sensor measurements. Measurements for each sensor is supposed to be stored in a separate file  | path | static_data/  |  
| sensorsGroupPrefixLength  |Number of sections in a sensors' filename separated by .(dot) that identifies sensors' groups     | number| 2 |
|  sensorsPrecision  | Precision for double numbers representing sensors' measurements  |  number | 6  | 

### Reader configurations
|  Name              | Description               |  Type  |  Default |
|:------------------:|:-------------------------:|:------:|:--------:|
| readStreamsAmount    | Amount of reader threads  | number | 16       |  
| readQueryCount       | Max number of measurements that will be read. \$max stands for max possible Java integer | number or $max| 10000 |
| readQueryResultLimit | Maximum capacity of query result. $max stands for max possible Java integer |  number or $max| $max | 
| readDelay            | Pause (in *readDelayUnit*) taken between two sequentional queries  |  number | 1  | 
| readDelayUnit        | Unit to measure *readDelay* pause in |  Java TimeUnit string | milliseconds | 
| intervalWeights      | Weights of intervals that will be randomly chosen for queries | k1:w1,k2:w2 formatted string |5 : 0.5, 4 : 0.2, 7 : 0.2,10 : 0.1, 3 : 0.1  | 
|  readIntervalUnit    | Unit to measure any of query interval in |  Java ChronoUnit string | minutes | 

### Writer configurations
|  Name  | Description  |  Type |  Default |
|:------:|:------------:|:-----:|:--------:|
|  writeDelay  | Pause (in *writeDelayUnit*) taken between two sequentional writes  |  number | 3  | 
|  writeDelayUnit  | Unit to measure *writeDelay* pause in |  Java TimeUnit string | milliseconds |
|  writeMaxSamples  | Number of samples that will be inserted in the system by the writer. If $max property is provided the whole source will be read | number or $max | 1000000
|writeStreamSource | Source file of sensors' measurements for the writer |path| write_source.csv

## Configuration file example

```sh
#Program configuration
phaseBreak = 3
reportOutput = $timed
#Sensor configurations
sensorsDir = some_path/
sensorsGroupPrefixLength = 2
sensorsPrecision = 6
#Readers configuration
readStreamsAmount = 16
readQueryCount = 100
readQueryResultLimit = $max
readDelay = 1
readDelayUnit = milliseconds
intervalWeights = 5 : 0.5, 4 : 0.2, 7 : 0.2, 10 : 0.1, 3 : 0.1 
readIntervalUnit = minutes
#Writers configuration
writeDelay = 1
writeDelayUnit = milliseconds
writeMaxSamples = 100
writeStreamSource = some_file.csv
```

## XBenchmarker results

Start the tester with the configuration set in provided configuration.properties file.
After launch a report file will be generated with the following metrics provided:

| Metric        | Description                                                                         |
|:-------------:|:-----------------------------------------------------------------------------------:|
|writerRunTime  | Time in  taken by the writer to upload all records to the system, millseconds|
|writeMaxIdle   | Maximum time which it took to perform a read request by some reader, milliseconds|
|writeMinIdle   | Minimum time in milliseconds which it took to perform a read request by some reader, milliseconds|
|writeAvgIdle   | Average time of performing read request by a reader, milliseconds|
|readFlowRunTime| Time taken by the writer to upload all records to the system, milliseconds|
|readMaxIdle    | Maximum time which it took to perform a read request by some reader, milliseconds |
|readMinIdle    | Minimum time which it took to perform a read request by some reader, milliseconds |
|readAvgIdle    | Average time of performing read request by a reader, milliseconds|
|readMaxDuration| Maximum time taken by some reader to perform all requests, milliseconds|
|readMinDuration| Minimum time taken by some reader to perform all requests, milliseconds|
|readAvgDuration| Average time taken by readers to perform all requests, milliseconds|
|readQuePerSec  | Average amount of queries served during one second for a reader |