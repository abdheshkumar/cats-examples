package abtechsoft
import cats.Applicative, cats.implicits._
object TraverseApp extends App {
  //Proof why built-in used foldRight
  def traverse[G[_], A, B](
      fa: Vector[A]
  )(f: A => G[B])(implicit G: Applicative[G]): G[Vector[B]] =
    fa.foldLeft(G.pure(Vector.empty[B])) {
      case (accE, a) => G.map2(accE, f(a))(_ :+ _)
    }

  val xs = Vector(1, 2, 3)
  def f(x: Int): Option[Int] = { println(x); if (x < 2) Some(x) else None }
  val r1 = xs.traverse(f)
  val r = traverse(xs)(f)
  println(r1, r)

  val x: List[Option[Int]] = List(Some(1), None, Some(2))
  //Combine All Some if one is None the result should be None
  val r2: List[Int] = x.sequence.orEmpty
  val r3: List[Int] = x.sequence.combineAll
  println(r2, r3)
  //Combine only Some //foldLeft/empty/combine
  println("CombineAll" + x.combineAll)
  import scala.util._

  //List[Try[String]] -> Option[List[String]]
  val l: List[Try[String]] = List(Success("Hello"), Failure(new Exception("erro")))
  val re = l.traverse(_.toOption)
  println(re)
  val ll: List[Try[String]] = List(Success("Hello"))
  val ree: Option[List[String]] = ll.traverse(_.toOption)
  println(ree)
}
