package filters


import java.util.concurrent.atomic.AtomicInteger
import javax.inject.{Singleton, Inject}

import akka.stream.Materializer


import play.Logger
import play.api.Configuration

import play.api.mvc._


import scala.concurrent.{Future, ExecutionContext, Await}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class RateLimitFilter @Inject() (implicit val mat: Materializer,  configuration:Configuration) extends Filter {

  val ignorePath = Set ("/",  "/about/health")

  lazy val rateLimit = {
    val limit = 20
    Logger.info(s"Rate Limit Filter set to $limit")
    limit
  }
  val inFlight = new AtomicInteger(0)

  def apply(nextFilter: (RequestHeader) => Future[Result])
            (requestHeader: RequestHeader): Future[Result] = {
    requestHeader.path match {
      case path if ignorePath.contains(requestHeader.path) => nextFilter(requestHeader)
      case path => {
        val curr = inFlight.incrementAndGet()
        if(curr <= rateLimit) {
          Logger.debug(s"Request=${requestHeader.method}, Path=${requestHeader.path}")
          nextFilter(requestHeader).map(result => {
            val curr = inFlight.decrementAndGet()
            Logger.debug(s"Decremented InFlightRequest to =${curr}")
            result
          }).recoverWith {
            case e:Exception => {
              Logger.error (e.getMessage, e)
              val newVal = inFlight.decrementAndGet()
              if(newVal < 0) {
                Logger.error(s"In-Flight went negative ${newVal}")
                inFlight.set(0)
              }
              val res = Results.InternalServerError(e.getMessage)
              Future(res)
            }
          }
        } else {
          val newVal = inFlight.decrementAndGet()
          if(newVal < 0) {
            Logger.error(s"In-Flight went negative ${newVal}")
            inFlight.set(0)
          }
          Logger.debug(s"Decremented InFlightRequest to =${inFlight}")
          val res = Results.TooManyRequests("Too Many Requests")
          Future (res)
        }
      }
    }
  }
}
