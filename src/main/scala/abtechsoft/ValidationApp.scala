package abtechsoft

import shapeless._
import shapeless.ops.coproduct._

object ValidationApp extends App {
  sealed trait NodeConfig //It's our proto message
  final case class CallCare(a: String) extends NodeConfig
  final case class DialNode() extends NodeConfig
  type ValidationResult[A] = Either[String, A]
  trait Validation[T] extends (T => ValidationResult[T])

  object Validation {
    implicit val a: Validation[CallCare] = new Validation[CallCare] {
      def apply(t: CallCare): ValidationResult[CallCare] = Right(t)
    }
    implicit val b: Validation[DialNode] = new Validation[DialNode] {
      def apply(t: DialNode): ValidationResult[DialNode] = Right(t)
    }
    def apply[A](implicit A: Validation[A]): Validation[A] = A

    def instance[A](func: A => ValidationResult[A]): Validation[A] =
      new Validation[A] {
        override def apply(v1: A): ValidationResult[A] = func(v1)
      }

    implicit val hnilValidation: Validation[HNil] =
      instance(hnil => Right(hnil))

    implicit def genericValidation[A, R](
        implicit
        gen: Generic.Aux[A, R],
        validation: Validation[R]
    ): Validation[A] =
      new Validation[A] {
        override def apply(v1: A): ValidationResult[A] =
          validation(gen.to(v1)).map(gen.from)
      }

    //We don't need Validation for HList as we don't want to validate fields of each type
    /*
    implicit def hlistValidation[H, T <: HList](
        implicit
        hValidation: Validation[H],
        tValidation: Validation[T]
    ): Validation[H :: T] =
      instance {
        case h :: t =>
          for {
            v1 <- hValidation(h)
            v2 <- tValidation(t)
          } yield v1 :: v2

      }*/

    implicit val cnilValidation: Validation[CNil] =
      instance[CNil](cnil => Right[String, CNil](cnil))

    implicit def hI[H, T <: Coproduct]: Inject[H :+: T, H] =
      new Inject[H :+: T, H] {
        override def apply(i: H): H :+: T = Inl(i)
      }

    implicit def tI[H, T <: Coproduct]: Inject[H :+: T, T] =
      new Inject[H :+: T, T] {
        override def apply(i: T): H :+: T = Inr(i)
      }

    implicit def coproductValidation[H, T <: Coproduct](
        implicit
        headValidation: Validation[H],
        tailValidation: Validation[T]
    ): Validation[H :+: T] = {

      new Validation[H :+: T] {
        override def apply(v1: H :+: T): ValidationResult[H :+: T] = v1 match {
          case Inl(h) =>
            headValidation(h) match {
              case Right(value) => Right(Coproduct[H :+: T](value))
              case Left(value)  => Left(value)
            }

          case Inr(t) =>
            tailValidation(t) match {
              case Right(value) =>
                Right(Coproduct[H :+: T](value))
              case Left(value) => Left(value)
            }
        }
      }

    }
  }
  object ValidationHelper {
    /* implicit class Ops[A](private val self: A) {
      def validate(implicit v: Validation[A]): ValidationResult[A] = v(self)
    }*/
    /*def validate(n: NodeConfig): ValidationResult[NodeConfig] = n match {
      case a: CallCare => a.validate
      case a: DialNode => a.validate
      //The only issue here, we will have to add pattern match as we add new subtype
    }*/
    def validate[A](a: A)(implicit v: Validation[A]): ValidationResult[A] = v(a)
  }

  val nc: NodeConfig = CallCare("")

  val result = ValidationHelper.validate(nc)
  println(result)
}
