#!/bin/bash
algorithm="dpop"
#ef_dpop, dpop, af_dpop, caf_dpop, maxsum, hybrid_maxsum, caf_maxsum, discrete_dpop, discrete_dsa, continuous_dsa
pre="rep_"
mid="_d"
suf=".dzn"

for agent in 25
do
  for point in 3
  do
    for iteration in 1
    #for iteration in `seq 5 5 20`
    do
      for id in {0..19}
      do
        filename=$pre$id$mid$agent$suf
        java -jar continuous-dcop-jar-with-dependencies.jar $filename $algorithm $iteration $point
      done
    done  
  done
done
