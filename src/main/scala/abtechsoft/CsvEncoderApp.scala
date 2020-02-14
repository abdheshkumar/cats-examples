package abtechsoft
import shapeless._

object CsvEncoderApp extends App {
  trait CsvEncoder[A] {
    def encode(value: A): List[String]
  }

  object CsvEncoder {
    def writeCsv[A](values: List[A])(implicit enc: CsvEncoder[A]): String =
      values.map(value => enc.encode(value).mkString(",")).mkString("\n")
    // "Summoner" method
    // apply method, known as a “summoner” or “materializer”, allows us to summon a type class instance given a target type:
    def apply[A](implicit enc: CsvEncoder[A]): CsvEncoder[A] = //
      enc

    def createEncoder[A](func: A => List[String]): CsvEncoder[A] =
      new CsvEncoder[A] {
        def encode(value: A): List[String] = func(value)
      }

    implicit val stringEncoder: CsvEncoder[String] =
      createEncoder(str => List(str))

    implicit val intEncoder: CsvEncoder[Int] =
      createEncoder(num => List(num.toString))

    implicit val booleanEncoder: CsvEncoder[Boolean] =
      createEncoder(bool => List(if (bool) "yes" else "no"))

    implicit val doubleEncoder: CsvEncoder[Double] =
      createEncoder(d => List(d.toString))

    /**
      * We can combine these building blocks to create an encoder for our HList.
      * We’ll use two rules: one for HNil and one for :: as shown below:
      */
    implicit val hnilEncoder: CsvEncoder[HNil] =
      createEncoder(hnil => Nil)

    implicit def hlistEncoder[H, T <: HList](
        implicit
        hEncoder: CsvEncoder[H],
        tEncoder: CsvEncoder[T]
    ): CsvEncoder[H :: T] =
      createEncoder {
        case h :: t =>
          hEncoder.encode(h) ++ tEncoder.encode(t)
      }

    implicit def genericEncoder[A, R](
        implicit
        gen: Generic.Aux[A, R],
        enc: CsvEncoder[R]
    ): CsvEncoder[A] =
      createEncoder(a => enc.encode(gen.to(a)))

    implicit val cnilEncoder: CsvEncoder[CNil] =
      createEncoder(cnil => throw new Exception("Inconceivable!"))

    implicit def coproductEncoder[H, T <: Coproduct](
        implicit
        hEncoder: CsvEncoder[H],
        tEncoder: CsvEncoder[T]
    ): CsvEncoder[H :+: T] = createEncoder {
      case Inl(h) => hEncoder.encode(h)
      case Inr(t) => tEncoder.encode(t)
    }

  }

  sealed trait Shape
  final case class Rectangle(width: Double, height: Double) extends Shape
  final case class Circle(radius: Double) extends Shape

  val reprEncoder: CsvEncoder[String :: Int :: Boolean :: HNil] =
    implicitly
  reprEncoder.encode("abc" :: 123 :: true :: HNil)

/*  case class A(a: String)
  implicitly[CsvEncoder[A]]

  val s = Generic[Circle]*/

  CsvEncoder.genericEncoder[Circle, Double :: HNil]

  val shapes: List[Shape] = List(
    Rectangle(3.0, 4.0),
    Circle(1.0)
  )
  println(CsvEncoder.writeCsv(shapes))
}
