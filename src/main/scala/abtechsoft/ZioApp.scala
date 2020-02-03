package abtechsoft
import zio.console._
import zio._
import zio.duration._
object ZioApp extends scala.App {
  val runtime = new DefaultRuntime {}
//
val forever = Schedule.forever
val upTo10 = Schedule.recurs(10)
val spaced = Schedule.spaced(Duration.Zero)
  val t = for {
    fiberRef <- FiberRef.make("Hello world!", (a: String, b: String) => a + b)
    child <- fiberRef.set("Hi!")
    result <- fiberRef.get
  } yield result

  println(runtime.unsafeRun((for {
    fiberRef <- FiberRef.make[Int](0)
    _ <- fiberRef.set(10)
    v <- fiberRef.get
  } yield v == 10)))
  println(runtime.unsafeRun(t))
}
