'''
Created on Apr 23, 2017

@author: vlad
'''

import os

plok_jar     = "../PLok/target/plok.jar"
plok_hfg_jar = "../PLokHfg/target/plokHfg.jar"


plokInvokeLine = 'java -jar {plokJar}   -H {H}  -V {V} --verbosity error -O {O} --append --test -P {P} -L {L}'
genCLIArg      = 'java -jar {plokHfgJar}   {iP}    {jP} {N} {timeStep} {count} {H} {W}'

history_filename_format = "../PLok/hs/h-{iP}-{jP}-{N}--{i}.csv"

output="r1.txt"

m   = 4
dimProbs = 5
attempts = 3


V = 5000
W = 5
timeStep = 10
historyLength = 250

policies = [("FULL_TRACKING", "RECENT_TRACKING")]
Ns = [10, 41]
Cs = [0.2]

#
#Brute force script for PLok.
#Gets different (iP, jP, C, P, L) 
#

for policy_pair in policies:
    for C in Cs:
        for N in Ns:
            iP = policy_pair[0]
            jP = policy_pair[1]
            print "_____Trying for iP={}, jP={}, C={}, N={}".format(iP, jP, C, N)
            print "____Generating history files set"
            for i in range(attempts):
                print "___Generating {}th history file".format(i)
                history_file_name = history_filename_format.format(iP=iP, jP=jP, N=N, i=i)
                cl_generate_history = genCLIArg.format(plokHfgJar=plok_hfg_jar, iP=iP, jP=jP, W=W, timeStep=timeStep, N=N, count=historyLength, H=history_file_name)
                print cl_generate_history
                os.system(cl_generate_history)
                cacheSizeUnits = int(m * V * C)
                for P in range(1, cacheSizeUnits, cacheSizeUnits // dimProbs):
                    maxL = cacheSizeUnits // P
                    for L in range(1, maxL, maxL // dimProbs):
                        if (P == 1 and L == 1):
                            continue
                        cl_invoke_plok = plokInvokeLine.format(plokJar=plok_jar, H=history_file_name, C=C, V=V, O=output, P=P, L=L)
                        print cl_invoke_plok
                        os.system(cl_invoke_plok)
print "Done brutting"
