package space.snu.fp

import scala.concurrent.ExecutionContext.Implicits.global

import slick.jdbc.H2Profile.api._

import cats.effect._
import cats.implicits._

trait Models {
  case class Supplier(
    id: Int, 
    name: String, 
    street: String, 
    city: String, 
    state: String, 
    zip: String
  )
  class Suppliers(tag: Tag) extends Table[Supplier](tag, "SUPPLIERS") {
    def id = column[Int]("SUP_ID", O.PrimaryKey)
    def name = column[String]("SUP_NAME")
    def street = column[String]("STREET")
    def city = column[String]("CITY")
    def state = column[String]("STATE")
    def zip = column[String]("ZIP")
    def * = (id, name, street, city, state, zip).mapTo[Supplier]
  }
  val suppliers = TableQuery[Suppliers]
  
  case class Coffee(
    name: String,
    supID: Int,
    price: Double,
    sales: Int,
    total: Int
  )
  class Coffees(tag: Tag) extends Table[Coffee](tag, "COFFEES") {
    def name = column[String]("COF_NAME", O.PrimaryKey)
    def supID = column[Int]("SUP_ID")
    def price = column[Double]("PRICE")
    def sales = column[Int]("SALES")
    def total = column[Int]("TOTAL")
    def * = (name, supID, price, sales, total).mapTo[Coffee]
    def supplier = foreignKey("SUP_FK", supID, suppliers)(_.id)
  }
  val coffees = TableQuery[Coffees]
}

trait ExampleQueries extends Models {
  lazy val db = Database.forConfig("h2mem1")
  
  val setup = DBIO.seq(
    (suppliers.schema ++ coffees.schema).create,
    suppliers += Supplier(101, "Acme, Inc.",      "99 Market Street", "Groundsville", "CA", "95199"),
    suppliers += Supplier( 49, "Superior Coffee", "1 Party Place",    "Mendocino",    "CA", "95460"),
    suppliers += Supplier(150, "The High Ground", "100 Coffee Lane",  "Meadows",      "CA", "93966"),
    coffees ++= Seq(
      Coffee("Colombian",         101, 7.99, 0, 0),
      Coffee("French_Roast",       49, 8.99, 0, 0),
      Coffee("Espresso",          150, 9.99, 0, 0),
      Coffee("Colombian_Decaf",   101, 8.99, 0, 0),
      Coffee("French_Roast_Decaf", 49, 9.99, 0, 0)
    )
  )
  val setupFuture = db.run(setup)
  
  val insertAction: DBIO[Option[Int]] = coffees ++= Seq (
    Coffee("Colombian",         101, 7.99, 0, 0),
    Coffee("French_Roast",       49, 8.99, 0, 0),
    Coffee("Espresso",          150, 9.99, 0, 0),
    Coffee("Colombian_Decaf",   101, 8.99, 0, 0),
    Coffee("French_Roast_Decaf", 49, 9.99, 0, 0)
  )
  val insertActionAndPrint: DBIO[Unit] = insertAction map { res =>
    res foreach { n => println(s"Inserted ${n} rows into the Coffees table") }
  }
  val printCoffees = db.run(coffees.result).map(_.foreach {
    case t =>
      println(
        "  " + t.name + "\t" + t.supID + "\t" + t.price + "\t" + t.sales + "\t" + t.total
      )
  })
}

class ModelRep extends Models with ExampleQueries {
  try {
    setupFuture foreach println
    
  } finally db.close
} 