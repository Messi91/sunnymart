package com.sunnymart.chapter7.profile.service.validation

import com.sunnymart.chapter7.profile.domain._

import java.util.UUID

object Validator {
  def validateName(name: String): Either[ProfileServiceError, String] = {
    Either.cond(name.nonEmpty, name, InvalidName)
  }

  def validateDate(date: String): Either[ProfileServiceError, String] = {
    Either.cond(date.matches("([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))"), date, InvalidDate)
  }

  def validateEmail(email: String): Either[ProfileServiceError, String] = {
    Either.cond(email.matches("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"), email, InvalidEmail)
  }

  def validateOpt(optValue: Option[String], defaultValue: String)(func: String => Either[ProfileServiceError, String]): Either[ProfileServiceError, String] = {
    optValue
      .map(value => func(value))
      .getOrElse(Right(defaultValue))
  }

  def checkForDuplicate(profileId: UUID, potentialDuplicate: Option[Profile]): Either[ProfileServiceError, Unit] = {
    Either.cond(potentialDuplicate.isEmpty || potentialDuplicate.exists(_.id == profileId), (), DuplicateEmail)
  }
}
