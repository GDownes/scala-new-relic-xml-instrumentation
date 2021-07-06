# scala-new-relic-xml-instrumentation
To configure agent path set `newrelicAgentPath` in `build.sbt`

Create extensions folder in `newrelicAgentPath` location and add xml configuration `extension.xml`

To run the project use `sbt run` and execute the `traceRun.sh` script to simulate HTTP requests.

Review the produced `newrelic_agent.log` to review instrumented classes e.g.

```
Matched method $anonfun$traceRoutes$10(Lcom/example/TraceRoutes;)Lscala/concurrent/Future;
Instrumented com.example.TraceRoutes.$anonfun$traceRoutes$2(Lcom/example/TraceRoutes;)Lscala/Function1;
```