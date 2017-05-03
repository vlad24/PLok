'''
Created on Apr 23, 2017

@author: vlad
'''

import os

#Paths
plok_jar                = "../PLok/target/plok.jar"
plok_hfg_jar            = "../PLokHfg/target/plokHfg.jar"
history_filename_format = "../PLok/hs/h-{iP}-{jP}-{N}--{i}.csv"
output_file             = "r2.txt"

#CL commands
plokInvokeLine = 'java -jar {plokJar}   -H {H}  -V {V} --verbosity error -O {O} --append --test -P {P} -L {L}'
genCLIArg      = 'java -jar {plokHfgJar}  --iPolicy {iP}  --jPolicy  {jP} --vectorSize {N} --timeStep {timeStep} --count {count} -O {output_file} --windowSize {W} --verbosity error'

#Constants
attempts  = 3
m         = 4
V         = 5000
timeStep  = 10
stepPartL = 0.1
stepPartP = 0.2

#Bruted parameters
policies = [("FULL_TRACKING", "RECENT_TRACKING")]
Ws = [timeStep // 2, 2 * timeStep]
Ns = [10, 41]
Cs = [0.1, 0.3]




#Brute force script for PLok.
#Gets different (iP, jP, C, P, L) 
for policy_pair in policies:
    for C in Cs:
        for N in Ns:
            iP = policy_pair[0]
            jP = policy_pair[1]
            history_length = V / timeStep
            print "_____Trying for iP={}, jP={}, C={}, N={}".format(iP, jP, C, N)
            print "____Generating history files set"
            for i in range(attempts):
                print "___Generating {}th history file with length {}".format(i, history_length)
                history_file_name = history_filename_format.format(iP=iP, jP=jP, N=N, i=i)
                w_border = len(Ws) if (jP == "RECENT_TRACKING") else 1 
                for W in Ws[:w_border]:
                    cl_generate_history = genCLIArg.format(plokHfgJar=plok_hfg_jar,
                                                    iP=iP, 
                                                    jP=jP, 
                                                    N=N, 
                                                    W=W,
                                                    timeStep=timeStep,
                                                    count=history_length,
                                                    output_file=history_file_name
                                                )
                    print cl_generate_history
                    os.system(cl_generate_history)
                    cacheSizeUnits = int(m * V * C)
                    maxL = N
                    for L in range(1, cacheSizeUnits, max(1, int(maxL * stepPartL))):
                        maxP = cacheSizeUnits // L
                        for P in range(1, maxP, max(1, int(maxP * stepPartP))):
                            if (P == 1 and L == 1):
                                continue
                            print "_______________________________________", P, L
                            cl_invoke_plok = plokInvokeLine.format(plokJar=plok_jar, H=history_file_name, C=C, V=V, O=output_file, P=P, L=L)
                            print cl_invoke_plok
                            os.system(cl_invoke_plok)
                        
print "Done brutting"
