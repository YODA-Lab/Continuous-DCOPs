# Continuous_DCOPs

## 1. To compile:
mvn install

## 2. To run:
java -jar target/continuous-dcop-jar-with-dependencies.jar $filename $algorithm $iteration $point

Check out `public void readArguments()` in `src/agent/ContinuousDcopAgent.java` for more information about the arguments.