'''
Created on Apr 23, 2017

@author: vlad
'''

import os

plok_jar     = "../PLok/target/plok.jar"
plok_hfg_jar = "../PLokHfg/target/plokHfg.jar"


plokInvokeLine = 'java -jar {plokJar}   -H {H}  -V {V} --verbosity error -O {O} --append --test -P {P} -L {L}'
genCLIArg      = 'java -jar {plokHfgJar}  --iPolicy {iP}  --jPolicy  {jP} --vectorSize {N} --timeStep {timeStep} --count {count} -O {output} --windowSize {W} --verbosity error'

history_filename_format = "../PLok/hs/h-{iP}-{jP}-{N}--{i}.csv"

output="r1.txt"

m   = 4
attempts = 3


V = 5000
W = 5
timeStep = 10

policies = [("FULL_TRACKING", "RECENT_TRACKING")]
Ns = [10, 41]
Cs = [0.2]

stepPartL = 0.1
stepPartP = 0.2
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
                history_length = V / timeStep
                print "___Generating {}th history file with length {}".format(i, history_length)
                history_file_name = history_filename_format.format(iP=iP, jP=jP, N=N, i=i)
                cl_generate_history = genCLIArg.format(plokHfgJar=plok_hfg_jar, N=N, iP=iP, jP=jP, W=W,
                                                        timeStep=timeStep, count=history_length,
                                                         output=history_file_name)
                print cl_generate_history
                os.system(cl_generate_history)
                cacheSizeUnits = int(m * V * C)
                maxL = N
                for L in range(1, cacheSizeUnits, max(1, int(maxL * stepPartL))):
                    maxP = cacheSizeUnits // L
                    for P in range(1, maxP, max(1, int(maxP * stepPartP))):
                        if (P == 1 and L == 1):
                            continue
                        cl_invoke_plok = plokInvokeLine.format(plokJar=plok_jar, H=history_file_name, C=C, V=V, O=output, P=P, L=L)
                        print cl_invoke_plok
                        os.system(cl_invoke_plok)
print "Done brutting"
