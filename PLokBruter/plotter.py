'''
Created on May 5, 2017

@author: vlad
'''

import math
import os
import time


#Paths
history_filename_format = "../PLok/hs/H{k}__{iP}-{jP}-{N}.csv"
output_img_format      = "../PLok/reports/plok_bruter_report_{host}_{id}.txt"
fname = "../PLok/reports/plok_bruter_report_EliteBook_312.txt"
attempts     = 3


class Record:
    def __init__(self, record):
        parts = record.rstrip().split(";\t")
        self.jP        = str  (parts[8].split("=")[1])
        self.iP        = str  (parts[9].split("=")[1])
        self.W         = str  (parts[10].split("=")[1])
        self.hrs       = str  (parts[11].split("=")[1]) if len(parts) > 11 else None
        self.N         = int  (parts[0].split("-")[2].split(".")[0])
        self.C         = float(parts[1].split("=")[1])
        self.V         = int  (parts[2].split("=")[1])
        self.Q         = int  (parts[4].split("=")[1])
        self.isFUU     = bool (parts[7].split("=")[1])
        #####
        self.P         = int  (parts[5].split("=")[1])
        self.L         = int  (parts[6].split("=")[1])
        self.attempt   = int  (parts[0].split("__")[0].split("/")[-1][1])
        self.missRatio = float(parts[3].split("=")[1])
    
    def get_experiment_code(self):
        return '''Experiment[jP={}; iP={}; W={}; HRS={}; N={}; C={}; V={}; Q={}; isFFU={}]'''.format(
                self.iP, self.jP, self.W, self.hrs, self.N, 
                self.C,self.V, self.Q, self.isFUU
                )


###############################################################################
if __name__ == "__main__":
    plot_start = time.time()
    results = dict()
    with open(fname) as f:
        for line in f:
            record = Record(line)
            exp = record.get_experiment_code()
            point = (record.P, record.L)
            adding = record.missRatio / attempts
            if not (exp in results):
                results[exp] = dict()
            results[exp][point] = results[exp].get(point, 0) + adding  
    assert len(results.keys()) == len(set(results.keys()))
    print results.values()[0]
    print "Done, time spent: {} min".format(round((time.time() - plot_start) / 60, 3))
        
    
