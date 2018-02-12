package services

import java.io.File
import java.nio.file.{Files, Paths}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import javax.inject._
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.streams._
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Bucket
import com.google.cloud.storage.BucketInfo
import scala.collection.JavaConversions._

@Singleton
class StorageService {
  private val logger = Logger(this.getClass)

  val storage = StorageOptions.getDefaultInstance().getService()

  def getFileNames(bucketName:String):Future[List[String]] = Future {
    print(s"Retrieving files names for bucket= ${bucketName}")
    val res = storage.list(bucketName)
    val blobIterator = res.iterateAll.toList
    blobIterator.map(_.getName).toList
  }

  def downloadFile(bucketName:String,fileName:String):Future[File] = Future {
    val read = storage.readAllBytes(BlobId.of(bucketName, fileName))
    val tempFile: File = File.createTempFile(fileName, "")
    Files.write(Paths.get(tempFile.getPath), read)
    tempFile
  }
}