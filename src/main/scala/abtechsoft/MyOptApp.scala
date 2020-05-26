package abtechsoft

object MyOptApp extends App {
  trait MyOpt[A] {
    type Out

    def apply(a: A): Out
  }

  trait LowPriorityImplicits {
    implicit def noOptionWrapped[T]: MyOpt[T] = new MyOpt[T] {
      override type Out = Option[T]

      override def apply(a: T): Out = Option(a)
    }
  }
  object MyOpt extends LowPriorityImplicits {
    implicit def optionOfOption[A]: MyOpt[Option[A]] = new MyOpt[Option[A]] {
      override type Out = Option[A]

      override def apply(a: Option[A]): Out = a
    }
  }

  def mkOption[A](a: A)(implicit op: MyOpt[A]): op.Out = op.apply(a)
  val p0: MyOpt[String]#Out = mkOption("12")
  val p1: MyOpt[Option[String]]#Out = mkOption(Option("12"))
  val p2: MyOpt[Int]#Out = mkOption(12)

}
