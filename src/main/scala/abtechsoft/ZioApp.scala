package abtechsoft

import zio.console._
import zio._
import zio.duration._
import cats.data.Reader
import zio.internal.{Platform, PlatformLive}

object ZioApp extends scala.App {
  val runtime = new Runtime[A with B] {
    override val environment: A with B = new A with B {
      override val a: A.Service = new A.Service {}
      override val b: B.Service = new B.Service {}
    }
    override val platform: Platform = PlatformLive.Default
  }
  val p = putStrLn("Hello! What is your name?")

  trait B {
    val b: B.Service
  }

  object B {

    trait Service {
      def t = ZIO.succeed("B dsasd")
    }

  }

  trait A {
    val a: A.Service
  }

  object A {

    trait Service {
      def t = ZIO.succeed("A dsasd")
    }

  }

  trait C {
    val a: C.Service
  }

  object C {

    trait Service {
      def c = ZIO.succeed("C dsasd")
    }

  }

  val a: String => ZIO[A, Throwable, Unit] = _ => ZIO.succeed(())
  val b: ZIO[B, Throwable, String] = ZIO.succeed("B test")

  val f = b.flatMap(a) // B=>R, R1 => A
  val result = for {
    bb <- b
    aa <- a(bb)
    cc <- ZIO.access[C](_.a.c)
  } yield ()

  val pgrm = for {
    r <- ZIO.accessM[A with B](_.b.t)
  } yield r

  runtime.unsafeRun(pgrm)
  /*  val program =
      for {
        _    <- putStrLn("Hello! What is your name?")
        name <- getStrLn
        _    <- putStrLn(s"Hello, ${name}, welcome to ZIO!")
      } yield ()*/

  /*//
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
    println(runtime.unsafeRun(t))*/
}
