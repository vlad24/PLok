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
from numpy import dtype


#Paths
fname                   = "../PLok/reports/plok_bruter_report_{}_{}.txt"
style                   = "trisurf"
attempts                = 3
plots_3d_needed         = True
plots_2d_needed         =  False 


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
        self.W          = None
        self.hrs        = None
        extraParamPos   = 10
        if self.jP == "RECENT_TRACKING":
            self.W          = int  (parts[extraParamPos].split("=")[1])
            extraParamPos += 1
        if self.iP == "HOT_RANGES":
            self.hrs        = str  (parts[extraParamPos].split("=")[1])
            extraParamPos += 1
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
        return '''iP={}; jP={}; W={}; HRS={}; C={}; N={}; V={}; Q={}; isFFU={}'''.format(
                self.iP, self.jP, self.W, self.hrs, self.C, self.N,
                self.V, self.Q, self.isFUU
                )


###############################################################################


def save_3D_plots(ps, ls, ms, cache_size, code, lower_label="", color=None, with_projections=True, style="trisurf", prefix="plok"):
    fig = plt.figure()
    x = np.asarray(ps)
    y = np.asarray(ls)
    z = np.asarray(ms)    
    if (style != "flat"):
        ax = fig.add_subplot(111, projection='3d')
        ##################################### Extreme point plotting
        z_extreme       = np.amax(z)
        deviation       = np.std(z)
        z_extr_indexes = np.asarray([i for i in range(len(z)) if z[i] == z_extreme])
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
        ax.text2D(0.05, 0.95, code, transform=ax.transAxes)
        if len(extremes) == 1:
            (exP , exL, exM) = extremes[0]
            lower_label = '''
                Extreme: 
                ( P={:.0f}, L={:.0f}, M={:.2f} )
                k = {}, sqrt(k) = {}, 
                P is {:.1f}% of max P, 
                    {:.1f}% of sqrt(k)
                L is {:.1f}% of max L, 
                    {:.1f}% of sqrt(k)
                PL / k = {:.1f}% 
                M deviation={}
                
                                     '''.format(
                                         exP, exL, 100 - exM,
                                         cache_size, 
                                         round(np.sqrt(cache_size), 2),
                                         exP/np.amax(ps) * 100,
                                         round(exP/np.sqrt(cache_size) * 100, 2),
                                         exL/np.amax(ls) * 100,
                                         round(exL/np.sqrt(cache_size) * 100, 2),
                                         (exP * exL) / float(cache_size) * 100,
                                         round(deviation, 2)
                                        )
        ax.text2D(0.85, 0.05, lower_label, transform=ax.transAxes)
        ax.set_xlabel('P')
        ax.set_ylabel('L')
        ax.set_zlabel('100 - M')
        ax.set_xlim(min(x), max(x))
        ax.set_ylim(min(y), max(y))
        ax.set_zlim(0     , 100)
        #### Plot all points
        if (style == "bar"):
            ax.bar(x,y,z, zdir='y')
        else:
            ax.plot_trisurf(x, y, z, cmap=plt.cm.gray if color is None else color, linewidth=0.1, antialiased=False)
    else:
        plt.gray()
        plt.scatter(x,y,c=z, s=75)
    fig.set_size_inches(18.5, 10.5, forward=True)
    policyPair = "__".join(code.split("; ")[:2])
    folder = os.path.join("3dplots", prefix, policyPair)
    if not os.path.exists(folder):
        os.makedirs(folder)
    img_name = "{folder}/{style}_{title}.jpg".format(folder=folder, prefix=prefix, style=style, title=code)
    plt.savefig(img_name)
    print "Saved", img_name
    plt.close()
    #plt.show()


def plot_2d(xs, ys):
    plt.plot(xs,ys, linestyle='-', marker='o', color='orange')
    plt.show()

if __name__ == "__main__":
    plot_start = time.time()
    exp_points = dict()
    exp_cache_sizes = dict()
    records = list()
    invocation_id = 0
    hostanme   = os.uname()[1]
    user_input = raw_input("Enter hostname that generated report(default={}): ".format(hostanme))
    hostanme = hostanme if user_input == "" else user_input 
    user_input = raw_input("Enter id of report (default=0): ")
    invocation_id = 0 if user_input == "" else int(user_input)
    prefix = ""
    if plots_3d_needed:
        user_input = raw_input("Enter prefix of generated images(default={}): ".format(invocation_id))
        prefix = str(invocation_id) if user_input == "" else str(user_input)
    with open(fname.format(hostanme, invocation_id)) as f:
        for line in f:
            if (line.strip() != ''):
                record = Record(line)
                records.append(record)
                exp = record.get_experiment_code()
                point = (record.P, record.L)
                expCacheSize = int(record.N * record.V * record.C) 
                assert(point[0] * point[1] <= expCacheSize)
                adding = record.missRatio / attempts
                if not (exp in exp_points):
                    exp_points[exp] = dict()
                exp_cache_sizes[exp] = expCacheSize
                exp_points[exp][point] = exp_points[exp].get(point, 0) + adding  
    assert len(exp_points.keys()) == len(set(exp_points.keys()))
    if plots_3d_needed:
        i = 0
        for e in sorted(exp_points.iteritems()):
            i += 1
            exp_code = e[0]
            points    = e[1]
            ps = [p[0]       for p in points.keys()] 
            ls = [p[1]       for p in points.keys()] 
            ms = [100 - v    for v in points.values()] 
            save_3D_plots(ps, ls, ms,
                           code=exp_code,
                           cache_size=exp_cache_sizes[exp_code],
                           with_projections=False,
                            style=style, 
                            prefix=prefix)
            
            
            
            
    if plots_2d_needed:
        xs = []
        ys = []
        for r in records:
            if (r.iP == "FULL_TRACKING"   and 
                r.jP == "RECENT_TRACKING" and 
                r.W == 5                  and 
                r.C == 0.03               and 
                r.N == 103                and 
                r.L == 2                  and
                r.exp_series == 1
                ):
                xs.append(r.P)
                ys.append(r.missRatio)
        print xs
        print ys    
        plot_2d(xs, ys) 
    print "Done, time spent: {} min".format(round((time.time() - plot_start) / 60, 3))