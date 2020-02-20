package abtechsoft

import abtechsoft.Data.Hello
import cats.effect.Sync
import cats.implicits._
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
object Data {
  case class Hello(greeting: String)

  object Hello {
    implicit val helloJson: Encoder.AsObject[Hello] = deriveEncoder[Hello]
    implicit def helloJsonEncoder[F[_]]: EntityEncoder[F, Hello] = jsonEncoderOf
  }

  case class User(name: String)

  object User {
    implicit val userJson: Encoder.AsObject[User] = deriveEncoder[User]
    implicit val userDecode: Decoder[User] = deriveDecoder[User]
    implicit def userJsonEncoder[F[_]]: EntityEncoder[F, User] = jsonEncoderOf
    implicit def userJsonDecoder[F[_]: Sync]: EntityDecoder[F, User] = jsonOf

  }
}

class Http4SApp[F[_]: Sync] extends Http4sDsl[F] {
  def route: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "hello" =>
      Ok("Test")
    case GET -> Root / id =>
      val user = Data.User(name = s"Test user ${id}")
      Ok(user)
    case req @ POST -> Root / "json" =>
      for {
        // Decode a User request
        user <- req.as[Data.User]
        // Encode a hello response
        resp <- Ok(Hello(user.name))
      } yield (resp)

    case req @ POST -> Root / "attempt" =>

      for {
        // Decode a User request
        user <- req.as[Data.User]
        // Encode a hello response
        resp <- Ok(Hello(user.name))
      } yield (resp)

  }
}
