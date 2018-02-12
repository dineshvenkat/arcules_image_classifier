package services
import javax.inject._
import java.sql.{Connection,DriverManager}
import java.sql._
import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.streams._
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart.FileInfo
import scala.sys.process.Process
import sys.process._
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import scala.util.Random
import  java.util.Calendar
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class MySQLHandler {
  // connect to the database named "mysql" on port 8889 of localhost
  import java.sql.Connection
  import java.sql.DriverManager

  val instanceConnectionName = "reflected-night-194318:us-east1:dineshmysql"
  val databaseName = "test"
  val userName = "test"
  val password = "test123"

  val jdbcUrl = String.format("jdbc:mysql://google/%s?cloudSqlInstance=%s&" + "socketFactory=com.google.cloud.sql.mysql.SocketFactory", databaseName, instanceConnectionName)
  private val logger = Logger(this.getClass)  

  /*private val logger = Logger(this.getClass)
  println("Before the connection")

  val hostStr = "MYSQL_PORT_3306_TCP_ADDR"
  val portStr = "MYSQL_PORT_3306_TCP_PORT"

  val host = sys.env(hostStr)
  val port = sys.env(portStr)
  println(s"host=${host},port=${port}")
  val url = s"jdbc:mysql://${host}:${port}/test"
  //val url = "jdbc:mysql://:3306/test"
  val driver = "com.mysql.jdbc.Driver"
  val username = "root"
  val password = "test123"
  */
  
  val conn = DriverManager.getConnection(jdbcUrl, userName, password)
  //var conn:Connection = _

  val insertQuery = "insert into results (ServiceID, ServiceStartTime, ServiceEndTime, Status,Results)  values (?, ?, ?, ?, ?)"

  val updateQuery = "update results SET ServiceEndTime =? , Status=? ,Results=? where id=?"
  
  logger.info("Got the connection")

  def insertRecord(id: String, sDate: String, eDate: String, status: String, finalResult: String):Future[Unit] = Future {
    try {
      //Class.forName(driver)
      logger.info("Inside the function....")
      //conn = DriverManager.getConnection(url, username, password)
      var preparedStmt = conn.prepareStatement(insertQuery)
      preparedStmt.setString(1, id)
      preparedStmt.setString(2, sDate)
      preparedStmt.setString(3, eDate)
      preparedStmt.setString(4, status)
      preparedStmt.setString(5, finalResult)

      // execute the preparedstatement
      preparedStmt.execute()

    } catch {
      case e: Exception => e.printStackTrace
    }
    //conn.close()
  }

 def updateRecord(id: String, eDate: String, status: String, finalResult: String):Future[Unit] = Future {
    try {
      //Class.forName(driver)
      logger.info("Inside the update function....")
      //conn = DriverManager.getConnection(url, username, password)
      var preparedStmt = conn.prepareStatement(updateQuery)
      preparedStmt.setString(1, id)
      preparedStmt.setString(2, eDate)
      preparedStmt.setString(3, status)
      preparedStmt.setString(4, finalResult)

      // execute the preparedstatement
      preparedStmt.execute()

    } catch {
      case e: Exception => e.printStackTrace
    }
    //conn.close()
  }
}