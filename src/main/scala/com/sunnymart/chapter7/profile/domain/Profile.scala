package com.sunnymart.chapter7.profile.domain

import java.util.UUID

case class Profile(
  id: UUID,
  firstName: String,
  lastName: String,
  dateOfBirth: String,
  emailAddress: String
)
