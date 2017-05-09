'''
Created on May 5, 2017

@author: vlad
'''

from mpl_toolkits.mplot3d import axes3d
import os
import time

from matplotlib import cm

import matplotlib.pyplot as plt
import numpy as np


#Paths
history_filename_format = "../PLok/hs/H{k}__{iP}-{jP}-{N}.csv"
output_img_format       = "../PLok/reports/plok_bruter_report_{host}_{id}.txt"
fname                   = "../PLok/reports/plok_bruter_report_EliteBook_312.txt"
style                   = "trisurf"
attempts                = 3
plots_3d_needed         = False
plots_2d_needed         = True 


class Record:
    
    def __str__(self):
        return "{}({}, {}) -> {}".format(self.get_experiment_code(), self.P, self.L, self.missRatio)
    
    def __eq__(self, other):
        return other.get_experiment_code() == self.get_experiment_code()
    
    def __init__(self, record):
        #H=../PLok/hs/H0__FULL_TRACKING-RECENT_TRACKING-100.csv;    C=0.01;    V=5000;    missRatio=100.000;    queriesCount=500;    P=1;    L=2;    isFFU=true;    iPolicy=FULL_TRACKING;    jPolicy=RECENT_TRACKING;    jPolicy_RT_window=2
        parts = record.rstrip().split(";\t")
        self.iP         = str  (parts[8].split("=")[1])
        self.jP         = str  (parts[9].split("=")[1])
        self.W          = int  (parts[10].split("=")[1])
        self.hrs        = str  (parts[11].split("=")[1]) if len(parts) > 11 else None
        self.N          = int  (parts[0].split("-")[2].split(".")[0])
        self.C          = float(parts[1].split("=")[1])
        self.V          = int  (parts[2].split("=")[1])
        self.Q          = int  (parts[4].split("=")[1])
        self.isFUU      = bool (parts[7].split("=")[1])
        #####
        self.P          = int  (parts[5].split("=")[1])
        self.L          = int  (parts[6].split("=")[1])
        self.exp_series = int  (parts[0].split("__")[0].split("/")[-1][1])
        self.missRatio = float(parts[3].split("=")[1])
    
    def get_experiment_code(self):
        return '''Experiment[iP={}; jP={}; W={}; HRS={}; C={}; N={}; V={}; Q={}; isFFU={}]'''.format(
                self.iP, self.jP, self.W, self.hrs, self.C, self.N,
                self.V, self.Q, self.isFUU
                )


###############################################################################


def save_3D_plots(ps, ls, ms, title, lower_label="", color=None, with_projections=True, style="trisurf"):
    fig = plt.figure()
    x = np.asarray(ps)
    y = np.asarray(ls)
    calls_made = np.asarray(ms)    
    if (style != "flat"):
        ax = fig.add_subplot(111, projection='3d')
        ##################################### Extreme point plotting
        z_extreme = np.amax(calls_made)
        z_extr_indexes = np.asarray([i for i in range(len(calls_made)) if calls_made[i] == z_extreme])
        extremes = np.asarray([[x[j], y[j], z_extreme] for j in z_extr_indexes])
        ax.plot(extremes[:,0], extremes[:,1], extremes[:,2], marker="o", ls="", c='red')
        ##################################### Extreme point projections plotting
        if (with_projections):
            projections = np.array(list(extremes) * 3)
            for i in range(len(projections)):
                projections[i,i] = 0 
            ax.plot(projections[:,0], projections[:,1], projections[:,2], marker='o', ls='', c='cyan')
            labels = ["(P={:.0f}, L={:.0f}, 100-M={:.2f})".format(*m) for m in extremes]
            for i in range(len(extremes)):
                ax.text(extremes[i][0], extremes[i][1], extremes[i][2], labels[i], color='red', zdir='x')
        ##################################### All points plotting
        ########################Labeling
        ax.text2D(0.05, 0.95, title, transform=ax.transAxes)
        if len(extremes) == 1:
            (exP , exL, _)= extremes[0]
            lower_label = "P is {:.1f}%, L is {:.1f}% of max possible".format(exP/np.amax(ps) * 100, exL/np.amax(ls) * 100)
        ax.text2D(0.05, 0.05, lower_label, transform=ax.transAxes)
        ax.set_xlabel('P')
        ax.set_ylabel('L')
        ax.set_zlabel('100 - missRatio')
        ax.set_xlim(min(x), max(x))
        ax.set_ylim(min(y), max(y))
        ax.set_zlim(0     , 100)
        #### Plot all points
        if (style == "bar"):
            ax.bar(x,y,calls_made, zdir='y')
        else:
            ax.plot_trisurf(x, y, calls_made, cmap=plt.cm.gray if color is None else color, linewidth=0.1, antialiased=False)
    else:
        plt.gray()
        plt.scatter(x,y,c=calls_made, s=75)
    fig.set_size_inches(18.5, 10.5, forward=True)
    img_name = "3dplots/{style}_{title}.jpg".format(style=style, title=title)
    plt.savefig(img_name)
    print "Saved", img_name
    plt.close()
    #plt.show()


def plot_2d(xs, ys):
    plt.plot(xs,ys, linestyle='-', marker='o', color='orange')
    plt.show()

if __name__ == "__main__":
    plot_start = time.time()
    experimetns = dict()
    records = list()
    with open(fname) as f:
        for line in f:
            if (line.strip() != ''):
                record = Record(line)
                records.append(record)
                exp = record.get_experiment_code()
                point = (record.P, record.L)
                adding = record.missRatio / attempts
                if not (exp in experimetns):
                    experimetns[exp] = dict()
                experimetns[exp][point] = experimetns[exp].get(point, 0) + adding  
    assert len(experimetns.keys()) == len(set(experimetns.keys()))
    if plots_3d_needed:
        i = 0
        for e in sorted(experimetns.iteritems()):
            i += 1
            code   = e[0]
            points = e[1]
            ps = [p[0]       for p in points.keys()] 
            ls = [p[1]       for p in points.keys()] 
            ms = [100 - v    for v in points.values()] 
            save_3D_plots(ps, ls, ms, code,with_projections=True, style=style)
    if plots_2d_needed:
        xs = []
        ys = []
        for r in records:
            if (r.iP == "FULL_TRACKING"   and 
                r.jP == "RECENT_TRACKING" and 
                r.W == 2                  and 
                r.C == 0.01               and 
                r.N == 100                and 
                r.L == 2                  and
                r.exp_series == 1
                ):
                xs.append(r.P)
                ys.append(r.missRatio)
        print xs
        print ys    
        plot_2d(xs, ys) 
    print "Done, time spent: {} min".format(round((time.time() - plot_start) / 60, 3))