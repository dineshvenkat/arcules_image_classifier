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

  val instanceConnectionName = sys.env.get("INST_NAME").getOrElse("reflected-night-194318:us-east1:dineshmysql")
  val databaseName = "test"
  val userName = sys.env.get("U_NAME").getOrElse("test")
  val password = sys.env.get("PASS").getOrElse("test123")
  

  val jdbcUrl = String.format("jdbc:mysql://google/%s?cloudSqlInstance=%s&" + "socketFactory=com.google.cloud.sql.mysql.SocketFactory", databaseName, instanceConnectionName)
  private val logger = Logger(this.getClass)  

  
  val conn = DriverManager.getConnection(jdbcUrl, userName, password)
  

  val insertQuery = "insert into results (ServiceID, ServiceStartTime, ServiceEndTime, Status,Results)  values (?, ?, ?, ?, ?)"

  val updateQuery = "update results SET ServiceEndTime =? , Status=? ,Results=? where id=?"
  
  logger.info("Got the connection")

  def insertRecord(id: String, sDate: String, eDate: String, status: String, finalResult: String):Future[Unit] = Future {
    try {
      //Class.forName(driver)
      logger.info("Inside the function....")
      
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