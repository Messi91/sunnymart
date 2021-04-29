package com.sunnymart.chapter4.profile.service

import com.sunnymart.chapter4.profile.domain._

import java.util.UUID
import scala.concurrent.Future

trait ProfileService {
  def createProfile(create: CreateProfile): Future[Either[ProfileServiceError, Profile]]
  def updateProfile(id: UUID, update: UpdateProfile): Future[Either[ProfileServiceError, Profile]]
}
