#!/bin/bash

echo "MONITORING_PERIODIC_DELTADIRAC"
#./experiments.sh 5 MONITORING_CONTINUOUS_DELTADIRAC xtream_monitoring_continuous_deltadirac.jar
#./experiments.sh 5 MONITORING_PERIODIC_DELTADIRAC xtream_monitoring_periodic_deltadirac.jar

#./experiments.sh 10 LSRM_ULTIMATE xtream_lsrm_ultimate.jar
#./experiments.sh 10 ADMISSION_ULTIMATE xtream_admission_ultimate.jar
#./experiments.sh 10 FLS_ULTIMATE xtream_fls_ultimate.jar

#./experiments.sh 10 OVERHEAD_PURE xtream_overhead_pure.jar
#./experiments.sh 10 OVERHEAD_ADMISSION xtream_overhead_admission.jar
#./experiments.sh 10 OVERHEAD_LSRM xtream_overhead_lsrm.jar
#./experiments.sh 10 OVERHEAD_FLS_PERIODIC xtream_overhead_fls_periodic.jar
#./experiments.sh 10 OVERHEAD_FLS_CONTINUOUS xtream_overhead_fls_continuous.jar

./experiments.sh 10 FLS2_LINEAR xtream_fls2_linear.jar

echo "============================"
echo "         E   N   D          "
echo "============================"

