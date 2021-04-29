package com.sunnymart.chapter4.profile.service.impl

import cats.data.EitherT
import com.sunnymart.chapter4.profile.service.validation.Validator._
import com.sunnymart.chapter4.profile.domain._
import com.sunnymart.chapter4.profile.service.ProfileService
import com.sunnymart.chapter4.profile.storage.ProfileStore
import com.sunnymart.chapter4.profile.uuid.UUIDGenerator

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AdvancedProfileService(database: ProfileStore, uuidGenerator: UUIDGenerator) extends ProfileService {

  def createProfile(create: CreateProfile): Future[Either[ProfileServiceError, Profile]] = {
    (for {
      firstName <- EitherT.fromEither[Future](validateName(create.firstName))
      lastName <- EitherT.fromEither[Future](validateName(create.lastName))
      dateOfBirth <- EitherT.fromEither[Future](validateDate(create.dateOfBirth))
      emailAddress <- EitherT.fromEither[Future](validateEmail(create.emailAddress))
      potentialDuplicate <- EitherT.liftF(database.getByEmail(create.emailAddress))
      _ <- EitherT.cond[Future](potentialDuplicate.isEmpty, (), DuplicateEmail)
      profile = Profile(
        id = uuidGenerator.generate(),
        firstName = firstName,
        lastName = lastName,
        dateOfBirth = dateOfBirth,
        emailAddress = emailAddress
      )
      _ <- EitherT.liftF[Future, ProfileServiceError, Unit](database.insert(profile))
    } yield profile).value.recover {
      case _ => Left(UnknownError)
    }
  }

  def updateProfile(id: UUID, update: UpdateProfile): Future[Either[ProfileServiceError, Profile]] = {
    (for {
      existing <- EitherT.fromOptionF[Future, ProfileServiceError, Profile](database.get(id), ProfileNotFound)
      firstName <- EitherT.fromEither[Future](validateOpt(update.firstName, existing.firstName)(validateName))
      lastName <- EitherT.fromEither[Future](validateOpt(update.lastName, existing.lastName)(validateName))
      dateOfBirth <- EitherT.fromEither[Future](validateOpt(update.dateOfBirth, existing.dateOfBirth)(validateDate))
      emailAddress <- EitherT.fromEither[Future](validateOpt(update.emailAddress, existing.emailAddress)(validateEmail))
      potentialDuplicate <- EitherT.liftF(database.getByEmail(emailAddress))
      _ <- EitherT.fromEither[Future](checkForDuplicate(id, potentialDuplicate))
      profile = Profile(
        id = existing.id,
        firstName = firstName,
        lastName = lastName,
        dateOfBirth = dateOfBirth,
        emailAddress = emailAddress
      )
      _ <- EitherT.liftF[Future, ProfileServiceError, Unit](database.update(existing.id, profile))
    } yield profile).value.recover {
      case _ => Left(UnknownError)
    }
  }
}
