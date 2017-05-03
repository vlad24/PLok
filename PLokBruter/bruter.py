'''
Created on Apr 23, 2017

@author: vlad
'''

import os
import time
import math

#Paths
plok_jar                = "../PLok/target/plok.jar"
plok_hfg_jar            = "../PLokHfg/target/plokHfg.jar"
history_filename_format = "../PLok/hs/H{k}__{iP}-{jP}-{N}.csv"
output_file_format      = "../PLok/reports/plok_bruter_report_{host}_{id}.txt"

#CL commands
plokInvokeLine = 'java -jar {plokJar}   -H {H} -C {C} -V {vectorAmount} --test -P {P} -L {L} --verbosity error -O {O} --append'

hfgCL_FT_RT      = 'java -jar {plokHfgJar}  --iPolicy FULL_TRACKING  --jPolicy RECENT_TRACKING --vectorSize {N} \
--timeStep {timeStep} --count {count} -O {output_file} --windowSize {jParam} --verbosity error --includeHints'

hfgCL_HR_RT      = 'java -jar {plokHfgJar}  --iPolicy HOT_RANGES  --jPolicy  RECENT_TRACKING --vectorSize {N} \
--timeStep {timeStep} --count {count} -O {output_file} --hotRanges {iParam} --windowSize {jParam} --verbosity error --includeHints'

#Constants
vectorAmount = 5000
attempts     = 3
timeStep     = 10
iterFactorL  = 1.2
iterFactorP  = 2


###############################################################################
if __name__ == "__main__":
    invocation_id = int(time.time()) % 1000
    hostanme   = os.uname()[1]
    real_brute = True
    user_input = raw_input("Start Auto Configured Real Brute?(y/n, default=y) : ")
    quickstart = (user_input == "y" or user_input == "") 
    if not quickstart:
        user_input = raw_input("Real Brute? (y/n, default:y)")
        real_brute = (user_input == "" or user_input == "y") 
        user_input = raw_input("Enter invocation id (y/n, default=y) : ")
        invocation_id = int(user_input)
    output_file = output_file_format.format(host=hostanme, id=invocation_id)
    #Bruted parameters
    policies = [("FULL_TRACKING", "RECENT_TRACKING"), ("HOT_RANGES", "RECENT_TRACKING")]
    Ws       = [timeStep // 2, timeStep * 2]
    HRs      = [("1-7", "52-58", "87-100")]
    Ns       = [100, 1019]
    Cs       = [0.01, 0.1]
    if not real_brute:
        vectorAmount = 101
        policies = [("FULL_TRACKING", "RECENT_TRACKING"), ("HOT_RANGES", "RECENT_TRACKING")]
        Ws       = [timeStep]
        HRs      = [("1-7", "52-58", "87-100")]
        Ns       = [100]
        Cs       = [0.001]
    
    assert vectorAmount > 100
    assert all(map(lambda x: x < 1, Cs))
    assert timeStep > 1
    
    brute_start = time.time()
    for policy_pair in policies:
        for C in Cs:
            for N in Ns:
                iP = policy_pair[0]
                jP = policy_pair[1]
                history_length = vectorAmount / timeStep
                if (iP == "FULL_TRACKING" and jP == "RECENT_TRACKING"):
                    iParams = [None]
                    jParams = Ws
                    cl_hfg = hfgCL_FT_RT
                if (iP == "HOT_RANGES"    and jP == "RECENT_TRACKING"):
                    iParams = HRs
                    jParams = Ws
                    cl_hfg = hfgCL_HR_RT
                for jParam in jParams:
                    for iParam in iParams:
                        for k in range(attempts):
                            history_file_name = history_filename_format.format(iP=iP, jP=jP, N=N, k=k)
                            cl_generate_history = cl_hfg.format(plokHfgJar=plok_hfg_jar, iP=iP, jP=jP, N=N,
                                                                    iParam=jParam, 
                                                                    jParam=jParam,
                                                                    timeStep=timeStep, 
                                                                    count=history_length,
                                                                    output_file=history_file_name
                                                                )
                            #print cl_generate_history
                            os.system(cl_generate_history)
                            print "~~~Trying different P,L values for {} # {}, {} # {} # {}, {}  :".format(k, iP, jP, N, jParam, iParam)
                            cacheSizeUnits = int(vectorAmount * N * C)
                            maxL = N + 1
                            L = 1
                            while L <= maxL:
                                maxP = min(cacheSizeUnits // L, vectorAmount * 0.41)
                                P = 1
                                while (P <= maxP):
                                    if (P == 1 and L == 1):
                                        break
                                    cl_invoke_plok = plokInvokeLine.format(plokJar=plok_jar, H=history_file_name, C=C, vectorAmount=vectorAmount, O=output_file, P=P, L=L)
                                    print cl_invoke_plok
                                    invoke_timestart = time.time()
                                    os.system(cl_invoke_plok)
                                    invoke_timeend = time.time()
                                    print "_______ PL={}x{} \t took: {}s".format(P, L, round(invoke_timeend - invoke_timestart, 3))
                                    P *= iterFactorP
                                L = int(math.ceil(L * iterFactorL))
                            
    print "Done, time spent: {} min".format(round((time.time() - brute_start) / 60, 3))
