name := "elastic"

version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.5.13"
val elasticVersion = "5.6.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "junit" % "junit" % "4.12")
libraryDependencies += "org.elasticsearch" % "elasticsearch" % elasticVersion
libraryDependencies += "org.elasticsearch.client" % "transport" % elasticVersion
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.9.5"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.5"
libraryDependencies += "com.shop" % "shopmessasges_2.12" % "1.1"

mainClass in (Compile, run) := Some("com.shop.Actor.ProductESDaoMain")