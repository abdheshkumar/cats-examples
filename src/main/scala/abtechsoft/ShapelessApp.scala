/*
package abtechsoft

import cats.SemigroupK
import cats.kernel.{Monoid, Semigroup}
import shapeless.{:+:, Coproduct, HList, HNil}
import cats.implicits._
object ShapelessApp extends App {
  implicitly[Monoid[String=>String]]
  val myList: HList = 12 :: "test" :: false :: HNil
  val intCoproduct: Int :+: String :+: Boolean :+: HNil = Coproduct(1)

}
 */

object ListApp extends App {



  def containsSlice(l: List[Int], s: List[Int]): Boolean = {
    def inner(
        ll: List[Int],
        ss: List[Int],
        found: Boolean,
        li: Option[Int]
    ): Boolean =
      ll match {
        case Nil => found
        case h :: t =>
          val _f = ss.indexOf(h)
          val v = _f != -1 && (li.isEmpty || li.exists(_ + 1 == _f))
          if (v) //loop
            inner(t, ss, _f != -1, Some(_f))
          else false

      }

    inner(l, s, false, None)
  }

  println(containsSlice(List(1, 2, 4), List(1, 2, 5, 4)))
}
