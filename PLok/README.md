# PLok

## Overview


We consider storage systems storing high frequent input vector data in block form.
Those blocks are of fixed size.
PLok is a project aimed to investigate dependency of optimal block sizes on query distribution in order to minimize disk access.

## Project Structure


* **PLok**       - program entry point
* **PLokHfg**    - program entry point
* **PLokBruter** - program entry point


### Program configurations
Command line options:

|  Name         | Description                                                          |  Constraints                   |  Required                                         |
|:-------------:|:--------------------------------------------------------------------:|:------------------------------:|:-------------------------------------------------:|
| H				| history file                              						   | string  (path)                 |   yes											    | 
| V				| write vectors amount (width of time-grid)                            | integer  ( > 0)                |   yes                                             | 
| C				| cache ratio 								                           | float (between 0.0 and 1.0)	|	no (default: "0.25")					     	| 
| O				| output file       						                           | string (path)		            |	no (default: null, self-generated will be used) | 
| append		| append to output flag 								               | flag						    |	no (default: true)							  	| 
| verbosity 	| verbosity level								                       | string (info, debug, fine...)	|	no (default: "info")							|  
| storagePath 	| storage path									                       | string (path)				    |	no (default: "./storages")                      |
| solve			| solve mode flag 								                       | flag						    |	no (default: false)							    | 
| test			| test mode flag 								                       | flag						    |	no (default: false)							    | 
| P  			| tested P if test flag is up   				                       | int (>1)					    |	yes, if test flag is up						    | 
| L  			| tested L if test flag is up   				                       | int (>1)					    |	yes, if test flag is up						    | 

### Launch example
PLok
```{java}
java -jar plok.jar  -H "hs/h1.csv" -V 2000 -C 0.25 --verbosity trace
java -jar plok.jar  -H "hs/h2.csv" -V 2000 -C 0.25 --verbosity debug --test -P 2 -L 5 --append
java -jar plok.jar  -H "hs/h3.csv" -V 2000 -C 0.25 --verbosity info  --solve
```
PLok History File Generator
```{java}
java -jar plokHfg.jar  --iPolicy FULL_TRACKING  --jPolicy  RECENT_TRACKING --vectorSize 10 --timeStep 24 --count 20 -O ../PLok/hs/h3.csv --windowSize 24 --verbosity error
```
   

## Output

After launch a report file (in report folder) named "report_${invocationTime}.txt" will be generated with the following metrics provided:

| Metric        | Description                                                                         |
|:-------------:|:-----------------------------------------------------------------------------------:|
| d             | number of queries served from disk                                                  |
| Q             | total number of queries                                                             |
| d/Q           | 100% * d/Q                                                                          |


