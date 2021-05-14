package com.sunnymart.chapter7.profile.service.impl

import com.sunnymart.chapter7.profile.service.validation.Validator._
import com.sunnymart.chapter7.profile.domain._
import com.sunnymart.chapter7.profile.service.ProfileService
import com.sunnymart.chapter7.profile.storage.ProfileStore
import com.sunnymart.chapter7.profile.uuid.UUIDGenerator

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BasicProfileService(database: ProfileStore, uuidGenerator: UUIDGenerator) extends ProfileService {

  override def createProfile(create: CreateProfile): Future[Either[ProfileServiceError, Profile]] = {
    database.getByEmail(create.emailAddress).flatMap { potentialDuplicate =>
      (for {
        _ <- Either.cond(potentialDuplicate.isEmpty, (), DuplicateEmail)
        firstName <- validateName(create.firstName)
        lastName <- validateName(create.lastName)
        dateOfBirth <- validateDate(create.dateOfBirth)
        emailAddress <- validateEmail(create.emailAddress)
      } yield Profile(uuidGenerator.generate(), firstName, lastName, dateOfBirth, emailAddress)) match {
        case error @ Left(_) => Future.successful(error)
        case result @ Right(profile) => database.insert(profile).map(_ => result).recoverWith {
          case _ => Future(Left(UnknownError))
        }
      }
    }
  }

  override def updateProfile(id:  UUID, update:  UpdateProfile): Future[Either[ProfileServiceError, Profile]] = {
    database.get(id).flatMap {
      case Some(existing) =>
        database.getByEmail(update.emailAddress.getOrElse("")).flatMap { potentialDuplicate =>
          (for {
            _ <- checkForDuplicate(id, potentialDuplicate)
            firstName <- validateOpt(update.firstName, existing.firstName)(validateName)
            lastName <- validateOpt(update.lastName, existing.lastName)(validateName)
            dateOfBirth <- validateOpt(update.dateOfBirth, existing.dateOfBirth)(validateDate)
            emailAddress <- validateOpt(update.emailAddress, existing.emailAddress)(validateEmail)
          } yield existing.copy(
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = dateOfBirth,
            emailAddress = emailAddress
          )) match {
            case error @ Left(_) => Future.successful(error)
            case result @ Right(profile) => database.update(id, profile).map(_ => result).recover {
              case _ => Left(UnknownError)
            }
          }
        }
      case None => Future.successful(Left(ProfileNotFound))
    }
  }
}
