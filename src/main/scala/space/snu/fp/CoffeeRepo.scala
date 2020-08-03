package space.snu.fp

import space.snu.fp.Models

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import slick.jdbc.H2Profile.api._

object CoffeeRepo extends Models {
  val db = Database.forConfig("h2mem1")
  val init = DBIO.seq(
    (suppliers.schema ++ coffees.schema).create,
    suppliers ++= Seq(
      Supplier(101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199"),
      Supplier( 49, "Superior Coffee", "1 Party Place", "Mendocino", "CA", "95460"),
      Supplier(150, "The High Ground", "100 Coffee Lane", "Meadows", "CA", "93966")
    ),
    coffees ++= Seq(
      Coffee("Colombian", 101, 7.99, 0, 0),
      Coffee("French_Roast", 49, 8.99, 0, 0),
      Coffee("Espresso", 150, 9.99, 0, 0),
      Coffee("Colombian_Decaf", 101, 8.99, 0, 0),
      Coffee("French_Roast_Decaf", 49, 9.99, 0, 0)
    )
  )    
  db.run { init }
  def count: Future[Int] = db.run { coffees.length.result }
  def getAll: Future[Seq[Coffee]] = db.run { coffees.result }
  def get(n: String): Future[Option[Coffee]] = db.run { 
    val q = coffees.filter(_.name === n).result.headOption
    q
  }
  def delete(n: String): Future[Option[Int]] = db.run {
    coffees.filter(_.name === n).delete
  } map { num => 
    if (num == 0) None 
    else Some(num) 
  }
  def create(pnew: Coffee): Future[Option[Int]] = db.run {
    coffees += pnew
  } map { num =>
    if (num == 0) None 
    else Some(num)
  }
  // def update(n: String, pnew: Coffee) = db.run {
  //   val q = for { c <- coffees if c.name === "Espresso" } yield c.price
  // }
}