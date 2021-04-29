package com.sunnymart.chapter7.profile.service

import java.util.UUID

import cats.data.NonEmptyList
import com.sunnymart.chapter7.profile.domain._

import scala.concurrent.Future

trait BetterProfileService {
  def createProfile(create: CreateProfile): Future[Either[NonEmptyList[ProfileServiceError], Profile]]
  def updateProfile(id: UUID, update: UpdateProfile): Future[Either[NonEmptyList[ProfileServiceError], Profile]]
}
