# PLok

## Overview


We consider storage systems storing high frequent input vector data in block form.
Those blocks are of fixed size.
PLok is a project aimed to investigate dependency of optimal block sizes on query distribution in order to minimize disk access.

## Structure


* **Tester**    - program entry point
* **Generator** - class performing queries with different time intervals.
* **Client**    - class performing write operations. Operations are performed on regular basis with configurable delay. One writer is supposed to be launched
* **Others**    - ...documentation in progress




### Program configurations
Command line options:

|  Name         | Description                                                          |  Constraints                   |  Required                                     |
|:-------------:|:--------------------------------------------------------------------:|:------------------------------:|:---------------------------------------------:|
| N				| vector length  			                       					   | integer ( > 0)			     	|   yes											| 
| H				| history file                              						   | string  (path)                 |   yes											| 
| V				| write vectors amount 							                       | integer	( > 0)				|	yes											| 
| C				| cache ratio 								                           | float (between 0.0 and 1.0)	|	no (default: "0,25")						| 
| r 			| idle between two subsequent reads  (msec)                            | int (> 0)		                |	no (default: 10)                            | 
| O				| output folder path						                           | string (path)		            |	no (default: "./reports")	                | 
| v				| verbosity level								                       | string (info, debug, fine...)	|	no (default: "info")						|  
| storagePath 	| storage path									                       | string (path)				    |	no (default: "./storages")                  |
| debug			| debug mode flag 								                       | flag						    |	no (default: false)							| 
| repeatHistory | flag, indicating if provided history should be repeated by generator | flag						    |	no (default: false)							| 
| test			| test mode flag 								                       | flag						    |	no (default: false)							| 
| P  			| tested P if test flag is up   				                       | int (>1)					    |	yes, if test flag is up						| 
| L  			| tested L if test flag is up   				                       | int (>1)					    |	yes, if test flag is up						| 

### Launch example
```{java}
java -jar PLok -H "hs/h1.csv" -N 10 -W 2000 -C 0.01  -pr 10 -v debug
java -jar PLok -H "hs/h1.csv" -N 10 -W 2000 -C 0.01  -pr 10 -test -P 21 -L 11
```

## Output

After launch a report file (in report folder) named "report_${invocationTime}.txt" will be generated with the following metrics provided:

| Metric        | Description                                                                         |
|:-------------:|:-----------------------------------------------------------------------------------:|
| d             | number of queries served from disk                                                  |
| Q             | total number of queries                                                             |
| d/A           | 100% * d/Q                                                                          |


