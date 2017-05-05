'''
Created on May 5, 2017

@author: vlad
'''

import math
import os
import time
from mpl_toolkits.mplot3d import axes3d
import matplotlib.pyplot as plt
import numpy as np
from matplotlib.ticker import LinearLocator
from matplotlib import cm


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


def plot(ps, ls, ms):
    fig = plt.figure()
    ax = fig.add_subplot(111, projection='3d')
    x = np.asarray(ps)
    y = np.asarray(ls)
    z = np.asarray(ms)    
    zmin = np.argmin(z)# [i for i in range(len(z)) if z[i] == zmin]
    mi = (x[zmin], y[zmin], np.amin(z))
    print mi
    #plot points.
    Ami = np.array([mi]*4)
    print Ami
    for i, v in enumerate([-40,40,-100]):
        Ami[i,i] = v 
    print Ami
    #plot points.
    ax.plot(Ami[:,0], Ami[:,1], Ami[:,2], marker="o", ls="", c=cm.coolwarm(0.))
    ax.set_xlabel('P')
    ax.set_ylabel('L')
    ax.set_zlabel('M')
    ax.set_xlim(min(x), max(x))
    ax.set_ylim(min(y), max(y))
    ax.set_zlim(0,      100)
    #ax.plot_surface(x, y, z, linewidth=1)
    #ax.set_zlim(-1, 1)
    #colors[x, y] = colortuple[(x + y) % len(colortuple)]
    ax.plot_trisurf(x, y, z, linewidth=0.2, antialiased=False)
    plt.show()


if __name__ == "__main__":
    plot_start = time.time()
    experimetns = dict()
    records = list()
    with open(fname) as f:
        for line in f:
            record = Record(line)
            records.append(record)
            exp = record.get_experiment_code()
            point = (record.P, record.L)
            adding = record.missRatio / attempts
            if not (exp in experimetns):
                experimetns[exp] = dict()
            experimetns[exp][point] = experimetns[exp].get(point, 0) + adding  
    assert len(experimetns.keys()) == len(set(experimetns.keys()))
    i = 0
    for e in experimetns.iteritems():
        i += 1
        if (i < 29):
            continue
        code   = e[0]
        points = e[1]
        ps = [p[0] for p in points.keys()] 
        ls = [p[1] for p in points.keys()] 
        ms = [v    for v in points.values()] 
        plot(ps, ls, ms)
        break
        
    
    print "Done, time spent: {} min".format(round((time.time() - plot_start) / 60, 3))
        
    
