package space.snu.fp

import space.snu.fp.{Models, CoffeeRepo}

import scala.util.{Failure, Success, Try}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

import cats.effect._
import cats.implicits._

import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.circe._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze._


import io.circe._
import io.circe.literal._
import io.circe.syntax._
import io.circe.generic.auto._

object CoffeeService extends Models {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val encoder = jsonEncoderOf[IO, Coffee]
  implicit val decoder = jsonOf[IO, CoffeeRepo.Coffee]
  
  case class Message(message: String, data: Json)
  
  implicit class FutureOps[+T](f: Future[T]) {
    final def toIO: IO[T] = IO.async { callback =>
      f.onComplete {
        case Success(v) => callback(Right(v))
        case Failure(e) => callback(Left(e))
      }
    }
  }
  
  val service = HttpRoutes.of[IO] {
    case GET -> Root / "count" =>
      val p = CoffeeRepo.count
      p.toIO.flatMap(Ok(_))
    
    case GET -> Root / "coffees" / name =>
      val x = CoffeeRepo.get(name)
      x.toIO.flatMap { _.fold(NotFound()){ i: CoffeeRepo.Coffee =>
          Ok(i.asJson)        
        }
      }
    
    case GET -> Root / "coffees" =>
      val x = CoffeeRepo.getAll.map(_.asJson)
      x.toIO.flatMap(Ok(_))
    
    case req @ POST -> Root / "create" => 
      for {
        pnew <- req.as[CoffeeRepo.Coffee]
        _ <- IO { CoffeeRepo.create(pnew) }
        resp <- Created(Message("created successfully", pnew.asJson).asJson)
      } yield resp
    case req @ DELETE -> Root / "delete" / name =>
      val x = CoffeeRepo.delete(name)
      x.toIO.flatMap(_.fold(NotFound())(x => Ok("delete successfully")))
  }.orNotFound
}


