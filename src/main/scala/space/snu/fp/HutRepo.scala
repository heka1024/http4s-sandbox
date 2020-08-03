package space.snu.fp

import space.snu.fp.model._

import java.util.UUID
import scala.collection.mutable.ListBuffer

import cats.effect._
import cats.implicits._

case class HutRepo[F[_]](var huts: ListBuffer[HutWithId])(implicit e: Sync[F]) {
  
  private def makeId: F[String] = e.delay {  UUID.randomUUID().toString  }
  
  def getAll: F[List[HutWithId]] = e.delay { huts.toList }
  
  def get(id: String): F[Option[HutWithId]] = e.delay { huts.find(_.id == id) }
  
  def add(pnew: Hut): F[String] = for {
    id <- makeId
    _ <- e.delay { huts += HutWithId(id, pnew) }
  } yield id
  
  def update(hutWithId: HutWithId): F[String] = {
    e.delay { huts -= hutWithId }
    e.delay { huts += hutWithId }
    e.delay { "update success" }
  }
  
  def delete(hutId: String): F[Unit] = {
    e.delay { 
      huts.find(_.id == hutId)
        .foreach(h => huts -= h) 
    }
  } 
}

object HutRepo {
  def empty[F[_]](implicit f: Sync[F]): F[HutRepo[F]] = f.delay { HutRepo[F](ListBuffer()) }
}