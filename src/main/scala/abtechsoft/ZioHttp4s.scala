package abtechsoft

import abtechsoft.configuration.DbConfig
import zio._
import pureconfig.ConfigSource
import cats.effect.{Blocker, ExitCode}
import doobie.h2.H2Transactor
import doobie.{Query0, Transactor, Update0}
import doobie.implicits._
import zio.interop.catz._
import io.circe.generic.auto._
import org.http4s.circe._
import io.circe.{Decoder, Encoder}
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio.blocking.Blocking
import zio.clock.Clock
import org.http4s.implicits._
import cats.instances.string._
import scala.concurrent.ExecutionContext

final case class User(id: Long, name: String)

final case class UserNotFound(id: Int) extends Exception

trait Configuration extends Serializable {
  val config: Configuration.Service[Any]
}

object Configuration {

  trait Service[R] {
    val load: RIO[R, configuration.Config]
  }

  trait Live extends Configuration {
    val config: Service[Any] = new Service[Any] {

      import pureconfig.generic.auto._

      val load: Task[configuration.Config] = Task.effect(ConfigSource.default.loadOrThrow[configuration.Config])
    }
  }

  object Live extends Live

  trait Test extends Configuration {
    val config: Service[Any] = new Service[Any] {
      val load: Task[configuration.Config] = Task.effectTotal(
        configuration.Config(configuration.ApiConfig("loacalhost", 8080), configuration.DbConfig("localhost", "", "")))
    }
  }

}


/**
 * Persistence Service
 */
trait Persistence extends Serializable {
  val userPersistence: Persistence.Service[Any]
}

object Persistence {

  trait Service[R] {
    def get(id: Int): RIO[R, User]

    def create(user: User): RIO[R, User]

    def delete(id: Int): RIO[R, Boolean]
  }

  /**
   * Persistence Module for production using Doobie
   */
  trait Live extends Persistence {

    protected def tnx: Transactor[Task]

    val userPersistence: Service[Any] = new Service[Any] {

      def get(id: Int): Task[User] =
        SQL
          .get(id)
          .option
          .transact(tnx)
          .foldM(
            err => Task.fail(err),
            maybeUser => Task.require(UserNotFound(id))(Task.succeed(maybeUser))
          )

      def create(user: User): Task[User] =
        SQL
          .create(user)
          .run
          .transact(tnx)
          .foldM(err => Task.fail(err), _ => Task.succeed(user))

      def delete(id: Int): Task[Boolean] =
        SQL
          .delete(id)
          .run
          .transact(tnx)
          .fold(_ => false, _ => true)
    }

    object SQL {

      def get(id: Int): Query0[User] =
        sql"""SELECT * FROM USERS WHERE ID = $id """.query[User]

      def create(user: User): Update0 =
        sql"""INSERT INTO USERS (id, name) VALUES (${user.id}, ${user.name})""".update

      def delete(id: Int): Update0 =
        sql"""DELETE FROM USERS WHERE id = $id""".update
    }

  }

  def mkTransactor(
                    conf: DbConfig,
                    connectEC: ExecutionContext,
                    transactEC: ExecutionContext
                  ): Managed[Throwable, H2Transactor[Task]] = {

    val xa = H2Transactor
      .newH2Transactor[Task](conf.url, conf.user, conf.password, connectEC, Blocker.liftExecutionContext(transactEC))

    val res = xa.allocated.map {
      case (transactor, cleanupM) =>
        Reservation(ZIO.succeed(transactor), _ => cleanupM.orDie)
    }.uninterruptible

    Managed(res)
  }

}

/**
 * Helper that will access to the Persistence Service
 */
object db extends Persistence.Service[Persistence] {

  def get(id: Int): RIO[Persistence, User] = RIO.accessM(_.userPersistence.get(id))

  def create(user: User): RIO[Persistence, User] = RIO.accessM(_.userPersistence.create(user))

  def delete(id: Int): RIO[Persistence, Boolean] = RIO.accessM(_.userPersistence.delete(id))
}

final case class Api[R <: Persistence](rootUri: String) {

  type UserTask[A] = RIO[R, A]

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[UserTask, A] = jsonOf[UserTask, A]

  implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[UserTask, A] = jsonEncoderOf[UserTask, A]

  val dsl: Http4sDsl[UserTask] = Http4sDsl[UserTask]

  import dsl._

  def route: HttpRoutes[UserTask] = {

    HttpRoutes.of[UserTask] {
      case GET -> Root / IntVar(id) => Ok(db.get(id))
      case request@POST -> Root =>
        request.decode[User] { user =>
          Created(db.create(user))
        }
    }
  }

}

object ZioHttp4s extends zio.App {

  type AppEnvironment = Clock with Blocking with Persistence

  type AppTask[A] = RIO[AppEnvironment, A]

  override def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val program: ZIO[ZEnv, Throwable, Unit] = for {
      conf <- configuration.loadConfig.provide(Configuration.Live)

      blockingEnv <- ZIO.environment[Blocking]
      blockingEC <- blockingEnv.blocking.blockingExecutor.map(_.asEC)

      transactorR = Persistence.mkTransactor(
        conf.dbConfig,
        platform.executor.asEC,
        blockingEC
      )

      httpApp = Router[AppTask](
        "/users" -> Api(s"${conf.api.endpoint}/users").route
      ).orNotFound

      server = ZIO.runtime[AppEnvironment].flatMap { implicit rts =>
        BlazeServerBuilder[AppTask]
          .bindHttp(conf.api.port, "0.0.0.0")
          .withHttpApp(CORS(httpApp))
          .serve
          .compile[AppTask, AppTask, ExitCode]
          .drain
      }
      program <- transactorR.use { transactor =>
        server.provideSome[ZEnv] { _ =>
          new Clock.Live with Blocking.Live
            with Persistence.Live {
            override protected def tnx: doobie.Transactor[Task] = transactor
          }
        }
      }
    } yield program

    program.foldM(
      err => zio.console.putStrLn(s"Execution failed with: $err") *> IO.succeed(1),
      _ => IO.succeed(0)
    )
  }
}
