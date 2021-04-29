package com.sunnymart.chapter4.profile.domain

case class CreateProfile(
  firstName: String,
  lastName: String,
  dateOfBirth: String,
  emailAddress: String
)
