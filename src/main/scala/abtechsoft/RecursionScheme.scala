package abtechsoft

import cats.Functor

object Recursion extends App {

  sealed trait Json

  final case object JsNull extends Json

  final case class JsStr(value: String) extends Json

  final case class JsNum(value: Long) extends Json

  final case class JsBool(value: Boolean) extends Json

  final case class JsArr(v: List[Json]) extends Json

  final case class JsObj(v: Map[String, Json]) extends Json

  def serialize(json: Json): String = json match {
    case JsNull        => "null"
    case JsStr(value)  => s""""$value""""
    case JsNum(value)  => value.toString
    case JsBool(value) => if (value) "true" else "false"
    case JsArr(v)      => v.map(serialize).mkString("[", ",", "]")
    case JsObj(v) =>
      v.view
        .mapValues(serialize)
        .map {
          case (k, v) =>
            s""""$k:$v"""
        }
        .mkString("{", ",", "}")

  }

  println(serialize(JsObj(Map("k1" -> JsStr("k1value"), "k2" -> JsNum(1)))))
}

object RecursionScheme extends App {

  sealed trait Json[+A]

  final case object JsNull extends Json[Nothing]

  final case class JsStr(value: String) extends Json[Nothing]

  final case class JsNum(value: Long) extends Json[Nothing]

  final case class JsBool(value: Boolean) extends Json[Nothing]

  final case class JsArr[A](v: List[A]) extends Json[A]

  final case class JsObj[A](v: Map[String, A]) extends Json[A]

  final case class Fix[F[_]](unFix: F[Fix[F]]) { //Fix[Json]
    def cata[A](algebra: F[A] => A)(implicit F: Functor[F]): A = {
      /*
      F.map(unFix)
       */
      val partial = F.map(unFix)(_.cata(algebra))
      algebra(partial)
    }
  }

  //type Fix[F[_]] = F[Fix[F]] // F=>Json - Fix[F[_]] -> Fix[Json[Fix[Json[Fix[Json]]]]]
  trait Functor[F[_]] {
    def map[A, B](fa: F[A])(f: A => B): F[B]
  }

  implicit val functorJson: Functor[Json] = new Functor[Json] {
    override def map[A, B](fa: Json[A])(f: A => B): Json[B] = fa match {
      case JsNull        => JsNull
      case JsStr(value)  => JsStr(value)
      case JsNum(value)  => JsNum(value)
      case JsBool(value) => JsBool(value)
      case JsArr(v)      => JsArr(v.map(f))
      case JsObj(v)      => JsObj(v.view.mapValues(f).toMap)
    }
  }

  def f[A](j: Json[A]):String = j match {
    case JsNull        => "null"
    case JsStr(value)  => s""""$value""""
    case JsNum(value)  => value.toString
    case JsBool(value) => if (value) "true" else "false"
    case JsArr(values) =>
      values.mkString("[", ", ", "]") // look ma, no recursion
    case JsObj(values) =>
      values.map { case (k, v) => s"$k: $v" }.mkString("{", ", ", "}")
  }
  def stringify(json: Fix[Json]): String = {
    json.cata[String] {
      case JsNull        => "null"
      case JsStr(value)  => s""""$value""""
      case JsNum(value)  => value.toString
      case JsBool(value) => if (value) "true" else "false"
      case JsArr(values) =>
        values.mkString("[", ", ", "]") // look ma, no recursion
      case JsObj(values) =>
        values.map { case (k, v) => s"$k: $v" }.mkString("{", ", ", "}")
    }

  }
//Json[Json[Json[Jstyr]]]
  val json: Fix[Json] =
    Fix(
      JsArr(
        List(
          Fix[Json](JsStr("k1value")),
          Fix[Json](JsStr("k2"))
        )
      )
    )

  println(stringify(json))
}
