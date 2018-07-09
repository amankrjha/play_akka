name := "redis"

version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.5.13"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "biz.paluch.redis" % "lettuce" % "4.4.2.Final",
  "junit" % "junit" % "4.12")
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.9.5"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.5"
libraryDependencies += "com.shop" % "shopmessasges_2.12" % "1.1"

mainClass in (Compile, run) := Some("com.shop.Actors.RedisMain")

/*
version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.5.2"

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-core" % "2.9.5",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.5",
  "biz.paluch.redis" % "lettuce" % "4.4.2.Final",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "junit" % "junit" % "4.12")
libraryDependencies += "com.shop" % "shopmessasges_2.12" % "1.0"
*/
