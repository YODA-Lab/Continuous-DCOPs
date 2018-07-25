# Continuous_DCOPs

## 1. To compile:
mvn install

dcop_jtools.jar. It should be in the target folder.

## 2. To run:
dcop_jtool FILE.xml [options]  
  where options is one of the following:  
  --repair (-r) [GDBR, TDBR(default)]. The DLNS repair phase.  
  --destroy (-d) [RAND(default), MEETINGS]. The DLNS destroy phase.  
  --iterations (-i) (default=500). The number of iterations of DLNS.  
  --timeout (-t) (default=no timeout (0)). The simulated time maximal execution time.
