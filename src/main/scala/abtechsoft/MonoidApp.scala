package abtechsoft
import cats.implicits._

object MonoidApp extends App {

  val words = List("asa", "asa", "asas", "asaa")
  def step(word: String) = (1, word.length, Map(word -> 1))
  val (totalWords, chars, occurences) = words.foldMap(step)
  println(totalWords, chars, occurences)

  val names = List(("a", 2), ("b", 3), ("a", 8))
  println(names.foldMap{case (f,s)=>Map(f->s)})
}
