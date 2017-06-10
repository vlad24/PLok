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
history_filename_format = "../PLok/hs/H{k}__solve__{iP}-{jP}-{N}.csv"
output_file_format      = "../PLok/reports/plok_solve_bruter_report_{host}_{id}.txt"

#CL commands
plok_run_line = 'java  -ea -Xmx2048m -Xms256m -XX:-UseGCOverheadLimit -jar {plokJar} -H {H} -V {vectorAmount} -C {C} -O {O} --append --verbosity error --fakeIO'

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
max_inv_id        = 10000

###############################################################################
if __name__ == "__main__":
    invocation_id = int(time.time()) % 10000
    hostanme   = os.uname()[1]
    real_brute = True
    user_input = raw_input("Start Auto Configured Solving Brute?(y/n, default=y) : ")
    quickstart = (user_input == "y" or user_input == "") 
    if not quickstart:
        real_brute = (user_input == "" or user_input == "y") 
        user_input = raw_input("Enter invocation id (0-{}, default=0) : ".format(max_inv_id - 1))
        invocation_id = 0 if user_input == "" else int(user_input) % max_inv_id
    output_file = output_file_format.format(host=hostanme, id=invocation_id)
    
    
    #Bruted parameters
    ##############################################################################################
    policies = [("HOT_RANGES",    "RECENT_TRACKING")]
    Ns       = [103]
    Cs       = [0.001]
    Ws       = [timeStep * 4]
    HRs      = ["1-20,70-90"]
    ##############################################################################################3
    
    assert vectorAmount > 100
    assert all(map(lambda x: x < 1, Cs))
    assert timeStep > 1
    
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
                            print "~~~Solving for H{} # {}, {} # {} # {}, {}  :".format(k, iP, jP, N, jParam, iParam)
                            cl_invoke_plok = plok_run_line.format(
                                                plokJar=plok_jar,
                                                H=history_file_name, 
                                                C=C,
                                                vectorAmount=vectorAmount,
                                                O=output_file
                                            )
                            print cl_invoke_plok
                            invoke_timestart = time.time()
                            os.system(cl_invoke_plok)
                            invoke_timeend = time.time()
                            duration = invoke_timeend - invoke_timestart
                            calls_made += 1
                            print "_______ Solved \t took: {}s \t ".format(round(duration, 2))
                            os.remove(history_file_name)
    print "Done, time spent: {} min".format(round((time.time() - brute_start) / 60, 3))
