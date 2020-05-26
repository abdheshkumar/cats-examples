package abtechsoft

import doobie._
import doobie.implicits._
import cats._
import cats.effect._
import cats.implicits._

object DoobieApp extends App {
  import doobie.util.ExecutionContexts

  // We need a ContextShift[IO] before we can construct a Transactor[IO]. The passed ExecutionContext
  // is where nonblocking operations will be executed. For testing here we're using a synchronous EC.
  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  // A transactor that gets connections from java.sql.DriverManager and executes blocking operations
  // on an our synchronous EC. See the chapter on connection handling for more info.
  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // driver classname
    "jdbc:postgresql:world", // connect URL (driver-specific)
    "postgres", // user
    "", // password
    Blocker
      .liftExecutionContext(ExecutionContexts.synchronous) // just for testing
  )

  val program1: doobie.ConnectionIO[Int] = 42.pure[ConnectionIO]
  program1.transact(xa)
}
