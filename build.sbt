name := "specs2-mockito-example"
organization := "org.example"
version := "2.0.0"

scalaVersion in ThisBuild := "2.11.8"
run <<= run in Compile in core

lazy val core = (project in file("core")).settings(
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.1.7",
      "org.jboss.netty" % "netty" % "3.2.6.Final",
      "org.specs2" %% "specs2-core" % "3.8.3",
      "org.specs2" %% "specs2-mock" % "3.8.3",
      "org.specs2" %% "specs2-junit" % "3.8.3"
)
)

resolvers += "mvn" at "http://mvnrepository.com/artifact/org.jboss.netty/netty"