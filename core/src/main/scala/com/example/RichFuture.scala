package com.example

import scala.concurrent.{ExecutionContext, Future}

trait RichFuture {
  this: Logging =>

  implicit class FutureOps[T](f: Future[T]) {

    implicit def logOnError(msg: => String)(implicit ec: ExecutionContext): Future[T] = {
      f.onFailure {
        case e: Throwable => logger.error(msg, e)
      }
      f
    }
  }

}