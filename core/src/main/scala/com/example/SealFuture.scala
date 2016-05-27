package com.example

import java.util.concurrent.TimeUnit

import org.jboss.netty.util.{HashedWheelTimer, Timeout, TimerTask}

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent._
import scala.util.{Failure, Success, Try}

object SealFuture extends SealFuture with Logging {

  val TimerFrequency: Long = 10

  def apply[B](actionFun: => Future[B], futureDescription: String)(implicit ec: ExecutionContext, duration: FiniteDuration): Future[B] = {
    seal(actionFun, futureDescription)
  }
}

trait SealFuture extends RichFuture {
  this: Logging =>

  object TimeoutScheduler {
    val timer = new HashedWheelTimer(SealFuture.TimerFrequency, TimeUnit.MILLISECONDS)

    def scheduleTimeout(promise: Promise[_], after: Duration, description: String) = {
      timer.newTimeout(new TimerTask {
        def run(timeout: Timeout): Unit = {
          val message: String = s"Timeout after ${after.toMillis}ms while waiting for $description"
          val ex = new TimeoutException(message)
          logger.error(message, ex)
          promise.failure(ex)
        }
      }, after.toNanos, TimeUnit.NANOSECONDS)
    }
  }

  def tryFuture[B](future: => Future[B])(implicit ec: ExecutionContext) = Try(future) match {
    case Success(f) => f.logOnError("Underlying future failed")
    case Failure(ex) => Future.failed(ex)
  }

  def seal[B](futureFactory: => Future[B], futureDescription: String)(implicit ec: ExecutionContext, duration: FiniteDuration): Future[B] = {
    val prom = Promise[B]()
    val timeout = TimeoutScheduler.scheduleTimeout(prom, duration, futureDescription)
    val evaluatedFuture = tryFuture(futureFactory)
    val combinedFut = Future.firstCompletedOf(List(evaluatedFuture, prom.future))
    evaluatedFuture onComplete { case result: Any => timeout.cancel() }
    combinedFut
  }
}
