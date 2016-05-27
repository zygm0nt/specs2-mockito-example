package com.example

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import org.slf4j.Logger
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise, TimeoutException}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class SealFutureSpec extends Specification with Mockito {

  private implicit val duration = FiniteDuration(1L, TimeUnit.SECONDS)

  "SealFuture" should {

    "succeed if future succeeds before timeout" in new Context {
      // given
      val future = Future.successful("ok")
      val factoryCalls = new AtomicInteger()

      // when
      val sealedFuture = seal({
        factoryCalls.incrementAndGet()
        future
      }, "future description")

      // then
      eventually {
        factoryCalls.get() must_== 1
        sealedFuture.value must beSome(Success("ok"))
      }
    }

    "return failed Future if underlying Future fails" in new Context {
      // given
      val exception = new RuntimeException("test")
      val future = Future.failed[String](exception)

      // when
      val sealedFuture = seal(future, "future description")

      // then
      eventually {
        sealedFuture.value must beSome(Failure(exception))
        there was one(logger).error("Underlying future failed", exception)
      }
    }

    "timeout if underlying Future doesn't complete" in new Context {
      // given
      val future = Promise[String]().future

      // when
      val sealedFuture = seal(future, "future description")

      // then
      eventually {
        sealedFuture.value.get.failed.get must beAnInstanceOf[TimeoutException]
        there was one(logger).error(===("Timeout after 1000ms while waiting for future description"), any[TimeoutException])
      }
    }

    "timeout if underlying Future times out (and log it only once)" in new Context {
      // given
      val future = Future.failed[String](new TimeoutException())

      // when
      val sealedFuture = seal(future, "future description")

      // then
      eventually {
        sealedFuture.value.get.failed.get must beAnInstanceOf[TimeoutException]
        there was one(logger).error(===("Underlying future failed"), any[TimeoutException])
      }
    }

    "return failed Future if factory function throws exception" in new Context {
      // given
      val exception = new RuntimeException("test")

      // when
      val sealedFuture = seal[String](throw exception, "future description")

      // then
      eventually(sealedFuture.value must beSome(Failure(exception)))
    }
  }

  trait Context extends Scope with SealFuture with Logging {

    val loggerMock = smartMock[Logger]

    protected override lazy val logger = loggerMock
  }

}
