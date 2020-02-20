import abtechsoft.{Data, Http4SApp}
import cats.effect.IO
import org.http4s.{EntityDecoder, Method, Request, Response, Status}
import org.scalatest.matchers.should.Matchers
import org.http4s.implicits._
import org.scalatest.flatspec.FixtureAnyFlatSpecLike
import org.scalatest.{Outcome, flatspec}
class Http4SAppSpec extends FixtureAnyFlatSpecLike with Matchers {
  type FixtureParam = Http4SApp[IO]

  def check[A](
      actual: IO[Response[IO]],
      expectedStatus: Status,
      expectedBody: Option[A]
  )(
      implicit ev: EntityDecoder[IO, A]
  ): Boolean = {
    val actualResp = actual.unsafeRunSync
    val statusCheck = actualResp.status == expectedStatus
    val bodyCheck = expectedBody.fold[Boolean](
      actualResp.body.compile.toVector.unsafeRunSync.isEmpty
    )( // Verify Response's body is empty.
      expected => actualResp.as[A].unsafeRunSync == expected
    )
    statusCheck && bodyCheck
  }

  override def withFixture(test: OneArgTest): Outcome =
    test(new Http4SApp[IO]())
  "This" should "work" in { app =>
    //val app = new Http4SApp[IO]()
    //val request = Request(method = Method.GET, uri = uri"/hello")
    val response = app.route
      .orNotFound(Request(method = Method.GET, uri = uri"/hello"))
      .unsafeRunSync()
    response.status shouldBe Status.Ok
    response.as[String].unsafeRunSync() shouldBe "Test"
  }

  it should "get user by id" in { app =>
    //val app = new Http4SApp[IO]()
    //val request = Request(method = Method.GET, uri = uri"/hello")
    val response = app.route
      .orNotFound(Request(method = Method.GET, uri = uri"/userid"))
      .unsafeRunSync()
    response.status shouldBe Status.Ok
    response
      .as[String]
      .unsafeRunSync() shouldBe """{"name":"Test user userid"}"""
  }

  it should "send user" in { app =>
    //import app._
    //val app = new Http4SApp[IO]()
    //val request = Request(method = Method.GET, uri = uri"/hello")
    val response = app.route
      .orNotFound(
        /*Request(method = Method.POST, uri = uri"/json")
          .withEntity("""{"name":"Bob"}""")*/
        Request(method = Method.POST, uri = uri"/json")
          .withEntity(Data.User("Bob"))
      )
      .unsafeRunSync()
    response.status shouldBe Status.Ok
    response
      .as[String]
      .unsafeRunSync() shouldBe """{"greeting":"Bob"}"""
  }
}
