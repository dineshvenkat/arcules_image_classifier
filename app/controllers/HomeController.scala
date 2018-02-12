package controllers

import java.util.Calendar
import java.io.File
import java.nio.file.{Files, Path}
import javax.inject._
import play.api.libs.json._
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
import services.StorageService
import services.MySQLHandler
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.sys.process.{Process, _}
import scala.util.Random


/**
  * This controller handles user requests
  */
@Singleton
class HomeController @Inject() (cc:MessagesControllerComponents,storage:StorageService,db:MySQLHandler)
//class HomeController @Inject() (cc:MessagesControllerComponents,db:ScalaJdbcConnectSelect)
//class HomeController @Inject() (cc:MessagesControllerComponents,storage:StorageService)

  extends AbstractController(cc) {

  private val logger = Logger(this.getClass)

  implicit val iResult = Json.writes[ImageResult]
  implicit val uResult = Json.writes[UserResult]



  def classifyImages(user:String,bucketName:String) = Action { implicit request =>
    val id = getUniqueServcieId
    val res = performClassification(id,user,bucketName)
    Ok(id)
  }


  def performClassification(serviceId:String,user:String,bucketName:String) :Future[String] =  {

   
    def downLoadAndDetect(input:String) : Future[ImageResult]  = for {
      currFile <-  storage.downloadFile(bucketName,input)
      dayOrNight <- detectImage(currFile.getPath)
      _ = currFile.delete
    }yield  ImageResult(input,dayOrNight.filter(_ >= ' '))


    def goOverFiles(in:List[String]):Future[List[ImageResult]] = {
      logger.info(s"Number of files to process  for user=${user},bucket=${bucketName},num=${in.length}")
      Future.sequence(in.map(file => downLoadAndDetect(file)))
    }

     logger.info(s"Perform image detection for user=${user},bucket=${bucketName} ")
     val startTime = Calendar.getInstance().getTime().toString
     db.insertRecord(serviceId, startTime, "", "started", "")
    
    val classifiedImgs = for {
      filesToProcess <- storage.getFileNames(bucketName)
      imgRes <- goOverFiles(filesToProcess)
    } yield imgRes

    for {
      imageRes <- classifiedImgs
      endTime = Calendar.getInstance().getTime().toString
      userRes = UserResult(user,serviceId,bucketName,startTime,endTime,imageRes)
      inJson = Json.toJson(userRes)
      inString = Json.stringify(inJson)
      _ <- db.insertRecord(serviceId, startTime, endTime, "finished", inString)
      _ = logger.info(inString)
    } yield inString

  }




  private def detectImage(filePath:String):Future[String] = Future {
    val cmd  = "/usr/bin/python bin/Images_classify.py " + filePath
    val res = executeMLModel(cmd)
    res._3
  }

  def executeMLModel(in: String): (List[String], List[String], String) = {
    val qb = Process(in)
    var out = List[String]()
    var err = List[String]()

    val exit = qb !! ProcessLogger((s) => out ::= s, (s) => err ::= s)

    (out.reverse, err.reverse, exit)
  }


  def  getUniqueServcieId = {
    val strLength = 8
    val alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    Random.alphanumeric.filter(alpha.contains(_)).take(strLength).mkString
  }
}

case class ImageResult(image:String,result:String)
case class UserResult(userName:String,serviceId:String,bucketName:String,startDate:String,endDate:String,results:List[ImageResult])