lazy val akkaHttpVersion = "10.2.3"
lazy val akkaVersion     = "2.6.11"
lazy val monixVersion    = "3.3.0"
lazy val newRelicVersion = "7.1.0-SNAPSHOT"

lazy val newrelicAgentPath = "/home/gdownes/dev/newrelic-java-agent/newrelic-agent/build/newrelicJar/newrelic.jar"

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "com.example",
      scalaVersion := "2.13.4"
    )
  ),
  resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  scalafmtOnCompile := true,
  name := "new-relic-http",
  libraryDependencies ++= Seq(
    "com.typesafe.akka"      %% "akka-http"                      % akkaHttpVersion,
    "com.typesafe.akka"      %% "akka-http-spray-json"           % akkaHttpVersion,
    "com.typesafe.akka"      %% "akka-actor-typed"               % akkaVersion,
    "com.typesafe.akka"      %% "akka-stream"                    % akkaVersion,
    "ch.qos.logback"          % "logback-classic"                % "1.2.3",
    "com.newrelic.agent.java" % "newrelic-api"                   % newRelicVersion,
    "io.monix"               %% "monix"                          % monixVersion,
    "com.typesafe.akka"      %% "akka-http-testkit"              % akkaHttpVersion % Test,
    "com.typesafe.akka"      %% "akka-actor-testkit-typed"       % akkaVersion     % Test,
    "org.scalatest"          %% "scalatest"                      % "3.1.4"         % Test
  )
)

Compile / run / fork := true
run / javaOptions ++= Seq(s"-javaagent:$newrelicAgentPath", "-Dnewrelic.config.log_level=FINEST")
