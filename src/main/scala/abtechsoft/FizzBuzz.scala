package abtechsoft

object FizzBuzz {

  import cats._, cats.implicits._

  val fizz: Int => Option[String] = x => {
    println("fizz"); if (x % 3 == 0) {
      Some("fizz")
    } else None
  }
  val buzz: Int => Option[String] = x => {
    println("buzz"); if (x % 5 == 0) {
      Some("buzz")
    } else None
  }
  val bazz: Int => Option[String] = x => if (x % 7 == 0) {
    println("bazz"); Some("bazz")
  } else None

  val funcs = List(fizz, buzz, bazz)

  val fizzbuzzbazz: Int => Option[String] = Foldable[List].fold(funcs)

  val fbbOrInt: Int => String = { i =>
    (fizzbuzzbazz(i) getOrElse i.toString)
  }

  val strings: List[String] = (1 until 2).toList map fbbOrInt

  println(strings)
}
