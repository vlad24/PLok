'''
Created on Apr 23, 2017

@author: vlad
'''

import os

plokCLIArg = 'java -jar Tester --test -H  {H} -P {P} -L {L} -C {C} -V {V} -O {O} -v debug'
genCLIArg  = 'java -jar {iP}   {jP}   {N} {r} {Q} {H}  {W}'

W = 5
H = "hSmall.csv"
N = 500 
R = 10
Q = 250
V = 500

m   = 4
Cs  = [0.2, 0.4]
Vs  = [2000, 3000]
Rs  = []
Qs  = []
attempts = 3

P_step = 3 
L_step = 3 

for i in range(attempts):
    print "Generating new history file {}".format(H)
    os.system(genCLIArg.format(iP="FULL_TRACKING", jP="RECENT_TRACKING", r=R, Q=Q, N=N, W=W, H=H))
    for C in Cs:
        maxS = int(m * V * C)
        print "MaxS:", maxS
        for P in range(2, maxS, P_step):
            maxL = maxS // P
            for L in range(2, maxL, L_step):
                os.system(plokCLIArg.format(H=H, P=P, L=L, C=C, V=V, O="{H}{i}.txt".format(H=H, i=i)))
print "Done brutting"
