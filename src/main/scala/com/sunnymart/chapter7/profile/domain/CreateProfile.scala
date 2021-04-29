package com.sunnymart.chapter7.profile.domain

case class CreateProfile(
  firstName: String,
  lastName: String,
  dateOfBirth: String,
  emailAddress: String
)
