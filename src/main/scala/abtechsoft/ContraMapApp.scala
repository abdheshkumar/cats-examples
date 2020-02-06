package abtechsoft

import cats._, cats.implicits._

object ContraMapApp {

  case class Money(amount: Int)

  case class Salary(size: Money)

  implicit val showMoney: Show[Money] = Show.show(m => s"$$${m.amount}")
  implicit val showSalary: Show[Salary] = showMoney.contramap(_.size)
}
