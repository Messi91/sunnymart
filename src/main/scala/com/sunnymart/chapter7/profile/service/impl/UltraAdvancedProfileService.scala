package com.sunnymart.chapter7.profile.service.impl

import cats.data.{EitherT, NonEmptyList, Validated, ValidatedNel}
import cats.syntax.apply._
import com.sunnymart.chapter7.profile.service.validation.Validator._
import com.sunnymart.chapter7.profile.domain._
import com.sunnymart.chapter7.profile.storage.ProfileStore
import com.sunnymart.chapter7.profile.uuid.UUIDGenerator

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UltraAdvancedProfileService(database: ProfileStore, uuidGenerator: UUIDGenerator) {

  def createProfile(create: CreateProfile): Future[Either[NonEmptyList[ProfileServiceError], Profile]] = {
    (for {
      potentialDuplicate <- EitherT.liftF(database.getByEmail(create.emailAddress))
      valid <- EitherT.fromEither[Future](validateCreation(create, potentialDuplicate).toEither)
      profile = Profile(
        id = uuidGenerator.generate(),
        firstName = valid.firstName,
        lastName = valid.lastName,
        dateOfBirth = valid.dateOfBirth,
        emailAddress = valid.emailAddress
      )
      _ <- EitherT.liftF[Future, NonEmptyList[ProfileServiceError], Unit](database.insert(profile))
    } yield profile).value.recover {
      case _ => Left(NonEmptyList.of(UnknownError))
    }
  }

  def updateProfile(id: UUID, update: UpdateProfile): Future[Either[NonEmptyList[ProfileServiceError], Profile]] = {
    (for {
      existing <- EitherT.fromOptionF[Future, NonEmptyList[ProfileServiceError], Profile](database.get(id), NonEmptyList.of(ProfileNotFound))
      potentialDuplicate <- EitherT.liftF(database.getByEmail(update.emailAddress.getOrElse("")))
      profile <- EitherT.fromEither[Future](validateUpdate(update, existing, potentialDuplicate).toEither)
      _ <- EitherT.liftF[Future, NonEmptyList[ProfileServiceError], Unit](database.update(existing.id, profile))
    } yield profile).value.recover {
      case _ => Left(NonEmptyList.of(UnknownError))
    }
  }

  private def validateCreation(creation: CreateProfile, potentialDuplicate: Option[Profile]): ValidatedNel[ProfileServiceError, CreateProfile] = {
    (
      Validated.condNel[ProfileServiceError, String](creation.firstName.nonEmpty, creation.firstName, InvalidName),
      Validated.condNel[ProfileServiceError, String](creation.lastName.nonEmpty, creation.lastName, InvalidName),
      Validated.fromEither(validateDate(creation.dateOfBirth)).toValidatedNel,
      Validated.fromEither(validateEmail(creation.emailAddress)).toValidatedNel,
      Validated.fromEither(Either.cond(potentialDuplicate.isEmpty, (), DuplicateEmail)).toValidatedNel
    ).mapN { case (firstName, lastName, dateOfBirth, emailAddress, _) =>
      creation.copy(
        firstName = firstName,
        lastName = lastName,
        dateOfBirth = dateOfBirth,
        emailAddress = emailAddress
      )
    }
  }

  private def validateUpdate(update: UpdateProfile, existing: Profile, potentialDuplicate: Option[Profile]): ValidatedNel[ProfileServiceError, Profile] = {
    (
      Validated.fromEither(validateOpt(update.firstName, existing.firstName)(validateName)).toValidatedNel,
      Validated.fromEither(validateOpt(update.lastName, existing.lastName)(validateName)).toValidatedNel,
      Validated.fromEither(validateOpt(update.dateOfBirth, existing.dateOfBirth)(validateDate)).toValidatedNel,
      Validated.fromEither(validateOpt(update.emailAddress, existing.emailAddress)(validateEmail)).toValidatedNel,
      Validated.fromEither(checkForDuplicate(existing.id, potentialDuplicate)).toValidatedNel
    ).mapN { case (firstName, lastName, dateOfBirth, emailAddress, _) =>
      existing.copy(
        firstName = firstName,
        lastName = lastName,
        dateOfBirth = dateOfBirth,
        emailAddress = emailAddress
      )
    }
  }
}
