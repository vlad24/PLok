# PLok

## Overview


We consider storage systems storing high frequent input vector data in block form.
Those blocks are of fixed size.
PLok is a project aimed to investigate dependency of optimal block sizes on query distribution in order to minimize disk access.

## Structure


* **Tester** - program entry point
* **Generator** - class performing queries with different time intervals.
* **Client** - class performing write operations. Operations are performed on regular basis with configurable delay. One writer is supposed to be launched
* **Others** - ...documentation in progress




### Program configurations
|  Name              | Description               |  Type  |  Required |
|:------------------:|:-------------------------:|:------:|:--------:|
| N				| vector length  								|  	integer ( > 0)				|	yes											| 
| T				| write time (msec) 							| 	integer	( > 0)				|	yes											| 
| C				| cache ratio 									|	float (between 0.0 and 1.0)	|	no (default: "0,25")						| 
| V				| distribution									|	string ($exp/$norm/$uni or csv matrix file path)		|	yes 										| 
| S				| storage type									|	string (sql, plok)			|	no (default: "ploker")						| 
| O				| output folder path								|	string (path)				|	no (default: "./reports")	| 
| phaseBreak	| break between write and read phases(msec) 	|	integer ( > 0)				|	no (default: "2000")						| 
| debug			| debug mode flag 								|	flag						|	no (default: false)							| 
| storagePath 	| storage path									|	string (path)				|	no (default: "./tmp_file_storage/")			|
| P			 	| width of block (values)						|	integer (> 0)				|	yes 										|
| L			 	| height of block (values)						|	integer (> 0)				|	yes 										|

## Output example

After launch a report file (in report folder) named "report_${invocationTimestamp}.txt" will be generated with the following metrics provided:

| Metric        | Description                                                                         |
|:-------------:|:-----------------------------------------------------------------------------------:|
| a | number of queries served from disk|
| A | total number of queries|
| a/A | 100% * a/A|


