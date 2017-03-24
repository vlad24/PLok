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

|  Name         | Description                                   |  Constraints                  |  Required                                     |
|:-------------:|:--------------------------------------------------------------------:|:------------------------------:|:---------------------------------------------:|
| N				| vector length  			                       					   | integer ( > 0)			     	|   yes											| 
| H				| history file                              						   | string  (path)                 |   yes											| 
| T				| write time (msec) 							                       | integer	( > 0)				|	yes											| 
| C				| cache ratio 								                           | float (between 0.0 and 1.0)	|	no (default: "0,25")						| 
| p				| idle between two subsequent writes (msec)                            | int (> 0)		                |	no (default: 1)                             | 
| S				| storage type						                       			   | string (sql, plok)			    |	no (default: "ploker")						| 
| O				| output folder path						                           | string (path)		            |	no (default: "./reports")	                | 
| phaseBreak	| break between write and read phases(msec) 	                       | integer ( > 0)				    |	no (default: "2000")						| 
| v				| verbosity level								                       | string (info, debug, fine...)	|	no (default: "info")						| 
| storagePath 	| storage path									                       | string (path)				    |	no (default: "./storages"")                 |
| solverType 	| storage path									                       | string (path)				    |	no (default: "./storages"")                 |
| debug			| debug mode flag 								                       | flag						    |	no (default: false)							| 
| repeatHistory | flag, indicating if provided history should be repeated by generator | flag						    |	no (default: false)							| 

## Output example

After launch a report file (in report folder) named "report_${invocationTime}.txt" will be generated with the following metrics provided:

| Metric        | Description                                                                         |
|:-------------:|:-----------------------------------------------------------------------------------:|
| d             | number of queries served from disk                                                  |
| Q             | total number of queries                                                             |
| d/A           | 100% * d/Q                                                                          |


