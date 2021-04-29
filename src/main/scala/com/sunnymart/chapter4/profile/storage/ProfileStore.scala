package com.sunnymart.chapter4.profile.storage

import com.sunnymart.chapter4.profile.domain.Profile

import java.util.UUID
import scala.concurrent.Future

class ProfileStore() {
  def insert(profile: Profile): Future[Unit] = ???
  def update(id: UUID, profile: Profile): Future[Unit] = ???
  def get(id: UUID): Future[Option[Profile]] = ???
  def getByEmail(email: String): Future[Option[Profile]] = ???
}
