package com.sunnymart.chapter7.profile.domain

case class UpdateProfile(
  firstName: Option[String],
  lastName: Option[String],
  dateOfBirth: Option[String],
  emailAddress: Option[String]
)
