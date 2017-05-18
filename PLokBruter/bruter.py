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
plokInvokeLine = 'java  -Xmx2048m -Xms256m -XX:-UseGCOverheadLimit -jar {plokJar}   -H {H} -C {C} -V {vectorAmount} --test -P {P} -L {L} --verbosity error -O {O} --append'

hfgCL_FT_RT      = 'java -jar {plokHfgJar}  --iPolicy FULL_TRACKING  --jPolicy RECENT_TRACKING --vectorSize {N} \
--timeStep {timeStep} --count {count} -O {output_file} --windowSize {jParam} --verbosity error --includeHints'

hfgCL_FT_FT      = 'java -jar {plokHfgJar}  --iPolicy FULL_TRACKING  --jPolicy FULL_TRACKING --vectorSize {N} \
--timeStep {timeStep} --count {count} -O {output_file} --verbosity error --includeHints'

hfgCL_HR_FT      = 'java -jar {plokHfgJar}  --iPolicy HOT_RANGES  --jPolicy FULL_TRACKING --vectorSize {N} \
--timeStep {timeStep} --count {count} -O {output_file} --hotRanges {iParam} --verbosity error --includeHints'

hfgCL_HR_RT      = 'java -jar {plokHfgJar}  --iPolicy HOT_RANGES  --jPolicy  RECENT_TRACKING --vectorSize {N} \
--timeStep {timeStep} --count {count} -O {output_file} --hotRanges {iParam} --windowSize {jParam} --verbosity error --includeHints'

#Constants
queries_count     = 1000
timeStep          = 20
vectorAmount      = queries_count * timeStep
attempts          = 3
iterFactorL       = 1.2
iterFactorP       = 2
max_inv_id        = 10000
P_limiter         = 0.41
est_norm_constant = 1

###############################################################################
if __name__ == "__main__":
    invocation_id = int(time.time()) % 10000
    hostanme   = os.uname()[1]
    real_brute = True
    user_input = raw_input("Start Auto Configured Real Brute?(y/n, default=y) : ")
    quickstart = (user_input == "y" or user_input == "") 
    if not quickstart:
        user_input = raw_input("Real Brute? (y/n, default:y)")
        real_brute = (user_input == "" or user_input == "y") 
        user_input = raw_input("Enter invocation id (0-{}, default=0) : ".format(max_inv_id - 1))
        invocation_id = 0 if user_input == "" else int(user_input) % max_inv_id
    output_file = output_file_format.format(host=hostanme, id=invocation_id)
    
    
    #Bruted parameters
    ##############################################################################################
    ########### brute0:
    #policies = [("FULL_TRACKING", "FULL_TRACKING"), ("FULL_TRACKING", "RECENT_TRACKING"), ("HOT_RANGES", "RECENT_TRACKING")]
    policies = [("HOT_RANGES", "FULL_TRACKING")]
    Ns       = [1019, 103, 10]
    Cs       = [0.003, 0.05, 0.2  ]
    Ws       = [timeStep // 4, timeStep * 4]
    HRs      = ["1-7,52-58,87-100", "1-20,70-90"]
    ##############################################################################################3
    
    if not real_brute:
        vectorAmount = 101
        policies = [("HOT_RANGES", "RECENT_TRACKING"), ("FULL_TRACKING", "RECENT_TRACKING")]
        Ws       = [timeStep]
        HRs      = ["1-20,70-90"]
        Ns       = [100]
        Cs       = [0.001]
    
    assert vectorAmount > 100
    assert all(map(lambda x: x < 1, Cs))
    assert timeStep > 1
    
    calls_rough_est = int ( 
                len(policies) * len(Ws) * len(HRs) * len(Ns) * len(Cs) *  \
                math.ceil(math.log(max(Ns), iterFactorL)) * \
                math.ceil(math.log(vectorAmount * P_limiter, iterFactorP))
            )
    calls_made = 0
    brute_start = time.time()
    for policy_pair in policies:
        for C in Cs:
            for N in Ns:
                iP = policy_pair[0]
                jP = policy_pair[1]
                if (iP == "FULL_TRACKING" and jP == "RECENT_TRACKING"):
                    iParams = [None]
                    jParams = Ws
                    cl_hfg = hfgCL_FT_RT
                if (iP == "HOT_RANGES"    and jP == "RECENT_TRACKING"):
                    iParams = HRs
                    jParams = Ws
                    cl_hfg = hfgCL_HR_RT
                if (iP == "HOT_RANGES"    and jP == "FULL_TRACKING"):
                    iParams = HRs
                    jParams = [None]
                    cl_hfg = hfgCL_HR_FT
                if (iP == "FULL_TRACKING"    and jP == "FULL_TRACKING"):
                    iParams = [None]
                    jParams = [None]
                    cl_hfg = hfgCL_FT_FT
                for jParam in jParams:
                    for iParam in iParams:
                        for k in range(attempts):
                            history_file_name = history_filename_format.format(iP=iP, jP=jP, N=N, k=k)
                            cl_generate_history = cl_hfg.format(plokHfgJar=plok_hfg_jar, iP=iP, jP=jP, N=N,
                                                                    iParam=iParam, 
                                                                    jParam=jParam,
                                                                    timeStep=timeStep, 
                                                                    count=queries_count,
                                                                    output_file=history_file_name
                                                                )
                            print cl_generate_history
                            os.system(cl_generate_history)
                            print "~~~Trying different P,L values for {} # {}, {} # {} # {}, {}  :".format(k, iP, jP, N, jParam, iParam)
                            cacheSizeUnits = int(vectorAmount * N * C)
                            maxL = N
                            L = 1
                            while L <= maxL:
                                maxP = min(cacheSizeUnits // L, int(vectorAmount * 0.41))
                                P = 1
                                while (P <= maxP):
                                    if (P == 1 and L == 1):
                                        break
                                    cl_invoke_plok = plokInvokeLine.format(plokJar=plok_jar, H=history_file_name, C=C, vectorAmount=vectorAmount, O=output_file, P=P, L=L)
                                    print cl_invoke_plok
                                    invoke_timestart = time.time()
                                    os.system(cl_invoke_plok)
                                    invoke_timeend = time.time()
                                    duration = invoke_timeend - invoke_timestart
                                    calls_made += 1
                                    print "_______ PL={}x{} \t took: {}s \t Progress est: {}%".format(P, 
                                                                                                      L,
                                                                                                      round(duration, 2),
                                                                                                      round(100.0 * calls_made / calls_rough_est),
                                                                                                    )
                                    P *= iterFactorP
                                L = int(math.ceil(L * iterFactorL))
                            os.remove(history_file_name)
    print "Done, time spent: {} min".format(round((time.time() - brute_start) / 60, 3))
