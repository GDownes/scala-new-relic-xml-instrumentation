# scala-new-relic-xml-instrumentation
To configure agent path set `newrelicAgentPath` in `build.sbt`

Create extensions folder in `newrelicAgentPath` location and add xml configuration `extension.xml`

To run the project use `sbt run` and execute the `traceRun.sh` script to simulate HTTP requests.