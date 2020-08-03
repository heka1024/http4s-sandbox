package space.snu.fp

import space.snu.fp.CoffeeService

import cats.effect._
import cats.implicits._

import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = 
    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(CoffeeService.service)
      .resource
      .use(_ => IO.never)
      .as(ExitCode.Success)
}



/*


trait HutRouter extends Http4sDsl[IO] {
  implicit val decoder = jsonOf[IO, Hut]
  implicit val decoder1 = jsonOf[IO, HutWithId]
  implicit val encoder = jsonEncoderOf[IO, HutWithId]
  implicit val encoder1 = jsonEncoderOf[IO, List[HutWithId]]
  
  val hutRepo = HutRepo.empty[IO].unsafeRunSync()
  val HUTS = "huts"
  
  val service = HttpRoutes.of[IO] {
    case GET -> Root / HUTS / hutId => 
      hutRepo.get(hutId)
        .flatMap(_.fold(NotFound())(Ok(_)))
    case GET -> Root / HUTS => 
      hutRepo.getAll
        .flatMap(Ok(_))
    case req @ POST -> Root / HUTS =>
      req.as[Hut].flatMap(hutRepo.add).flatMap(Created(_))
    case req @ PUT -> Root / HUTS =>
      req.as[HutWithId]
        .flatMap(hutRepo.update)
        .flatMap(Ok(_))
    case DELETE -> Root / HUTS / hutId =>
      hutRepo.delete(hutId)
        .flatMap(_ => NoContent())
  }.orNotFound
}

import space.snu.fp.CoffeeService

object Main {}
object Main extends IOApp with CoffeeService {
  def run(args: List[String]): IO[ExitCode] = 
    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(service)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
*/