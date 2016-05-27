package com.example

import org.slf4j.LoggerFactory

/**
  * Adds the lazy val logger of type [[$Logger]] to the class into which this trait is mixed.
  *
  * If you need a not-lazy [[$Logger]], which would probably be a special case,
  * use [[com.allegrogroup.reco.logging.StrictLogging]].
  *
  * Code based on [[com.typesafe.scalalogging.slf4j.Logging]]
  *
  * @define Logger com.allegrogroup.reco.logging.Logger
  */
trait Logging {

  protected lazy val logger: org.slf4j.Logger =
    LoggerFactory.getLogger(getClass.getName)
}

/**
  * Adds the not-lazy val logger of type [[$Logger]] to the class into which this trait is mixed.
  *
  * If you need a lazy [[$Logger]], which would probably be preferrable,
  * use [[com.allegrogroup.reco.logging.Logging]].
  *
  * @define Logger com.allegrogroup.reco.logging.Logger
  */
trait StrictLogging {

  protected val logger =
    LoggerFactory.getLogger(getClass.getName)
}
