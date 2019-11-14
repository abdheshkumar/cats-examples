package abtechsoft
import cats.implicits._
import cats.effect._
import org.http4s._, org.http4s.dsl.io._, org.http4s.implicits._
import cats.data.Kleisli

object MonoidApp extends App {

  val words = List("asa", "asa", "asas", "asaa")
  def step(word: String) = (1, word.length, Map(word -> 1))
  val (totalWords, chars, occurences) = words.foldMap(step)
  println(totalWords, chars, occurences)
}
