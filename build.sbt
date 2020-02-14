lazy val root = project
  .in(file("."))
  .settings(
    name := "cats-examples",
    version := "1.0",
    scalaVersion := "2.13.0",
    scalacOptions ++= Seq("-Ymacro-annotations"),
    //addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
    resolvers += Resolver.sonatypeRepo("snapshots"),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.0.0",
      "org.typelevel" %% "cats-effect" % "2.0.0",
      "dev.zio" %% "zio" % "1.0.0-RC17",
      "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC10",
      "org.http4s" %% "http4s-dsl" % "0.21.0-SNAPSHOT",
      "org.http4s" %% "http4s-circe" % "0.21.0-SNAPSHOT",
      "org.http4s" %% "http4s-blaze-server" % "0.21.0-SNAPSHOT",
      "org.http4s" %% "http4s-blaze-client" % "0.21.0-SNAPSHOT",
      "com.github.pureconfig" %% "pureconfig" % "0.12.1",
      "com.typesafe.akka" %% "akka-http" % "10.1.10",
      "com.typesafe.akka" %% "akka-stream" % "2.5.23",
      "eu.timepit" %% "refined" % "0.9.10",
      // Start with this one
      "org.tpolecat" %% "doobie-core" % "0.8.6",
      // And add any of these as needed
      "org.tpolecat" %% "doobie-h2" % "0.8.6", // H2 driver 1.4.200 + type mappings.
      "org.tpolecat" %% "doobie-hikari" % "0.8.6", // HikariCP transactor.
      "org.tpolecat" %% "doobie-postgres" % "0.8.6", // Postgres driver 42.2.8 + type mappings.
      "org.tpolecat" %% "doobie-quill" % "0.8.6", // Support for Quill 3.4.10
      "org.tpolecat" %% "doobie-specs2" % "0.8.6" % "test", // Specs2 support for typechecking statements.
      "org.tpolecat" %% "doobie-scalatest" % "0.8.6" % "test", // ScalaTest support for typechecking statements.
      "com.chuusai" %% "shapeless" % "2.3.3",
      "io.estatico" %% "newtype" % "0.4.3"
    ) ++ Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
      "io.circe" %% "circe-refined"
    ).map(_ % "0.12.3")
  )
