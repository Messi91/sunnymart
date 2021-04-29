package com.sunnymart.chapter7.profile.service

import cats.data.NonEmptyList
import com.sunnymart.chapter7.profile.domain._
import com.sunnymart.chapter7.profile.service.impl.UltraAdvancedProfileService
import com.sunnymart.chapter7.profile.storage.ProfileStore
import com.sunnymart.chapter7.profile.uuid.UUIDGenerator
import org.mockito.scalatest.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.UUID
import scala.concurrent.Future

class UltraAdvancedProfileServiceTest extends AnyFlatSpec with ScalaFutures with Matchers with MockitoSugar {

  private val database = mock[ProfileStore]
  private val uuidGenerator = mock[UUIDGenerator]
  private val service = new UltraAdvancedProfileService(database, uuidGenerator)

  it should "reject a profile creation attempt with invalid data" in {
    val request = CreateProfile(
      firstName = "",
      lastName = "",
      dateOfBirth = "February 9th, 1991",
      emailAddress = "charles.mathau.gmail.com"
    )

    when(database.getByEmail("charles.mathau.gmail.com")).thenReturn(Future.successful(None))

    service.createProfile(request).futureValue should be (Left(NonEmptyList.of(InvalidName, InvalidName, InvalidDate, InvalidEmail)))
  }

  it should "reject a profile creation attempt with a duplicate email" in {
    val existing = Profile(
      id = UUID.randomUUID(),
      firstName = "Charles",
      lastName = "Mathau",
      dateOfBirth = "1991-02-09",
      emailAddress = "charles.mathau@gmail.com"
    )
    val request = CreateProfile(
      firstName = "",
      lastName = "",
      dateOfBirth = "February 9th, 1991",
      emailAddress = "charles.mathau@gmail.com"
    )

    when(database.getByEmail("charles.mathau@gmail.com")).thenReturn(Future.successful(Some(existing)))

    service.createProfile(request).futureValue should be (Left(NonEmptyList.of(InvalidName, InvalidName, InvalidDate, DuplicateEmail)))
  }

  it should "handle a profile creation attempt with a failed database transaction" in {
    val request = CreateProfile(
      firstName = "Charles",
      lastName = "Mathau",
      dateOfBirth = "1991-02-09",
      emailAddress = "charles.mathau@gmail.com"
    )
    val expected = Profile(
      id = UUID.randomUUID(),
      firstName = "Charles",
      lastName = "Mathau",
      dateOfBirth = "1991-02-09",
      emailAddress = "charles.mathau@gmail.com"
    )

    when(uuidGenerator.generate()).thenReturn(expected.id)
    when(database.getByEmail("charles.mathau@gmail.com")).thenReturn(Future.successful(None))
    when(database.insert(expected)).thenReturn(Future.failed(new Exception))

    service.createProfile(request).futureValue should be (Left(NonEmptyList.of(UnknownError)))
  }

  it should "create a new user profile with valid data" in {
    val request = CreateProfile(
      firstName = "Charles",
      lastName = "Mathau",
      dateOfBirth = "1991-02-09",
      emailAddress = "charles.mathau@gmail.com"
    )
    val expected = Profile(
      id = UUID.randomUUID(),
      firstName = "Charles",
      lastName = "Mathau",
      dateOfBirth = "1991-02-09",
      emailAddress = "charles.mathau@gmail.com"
    )

    when(uuidGenerator.generate()).thenReturn(expected.id)
    when(database.getByEmail("charles.mathau@gmail.com")).thenReturn(Future.successful(None))
    when(database.insert(expected)).thenReturn(Future.successful())

    service.createProfile(request).futureValue should be (Right(expected))
  }

  it should "reject a profile update attempt on a non-existing user" in {
    val id = UUID.randomUUID()
    val request = UpdateProfile(
      firstName = None,
      lastName = None,
      dateOfBirth = None,
      emailAddress = Some("charles.mathau@outlook.com")
    )

    when(database.get(id)).thenReturn(Future.successful(None))

    service.updateProfile(id, request).futureValue should be (Left(NonEmptyList.of(ProfileNotFound)))
  }

  it should "reject a profile update attempt with invalid data" in {
    val id = UUID.randomUUID()
    val existing = Profile(
      id = id,
      firstName = "Charles",
      lastName = "Mathau",
      dateOfBirth = "1991-02-09",
      emailAddress = "charles.mathau@gmail.com"
    )
    val request = UpdateProfile(
      firstName = Some(""),
      lastName = Some(""),
      dateOfBirth = Some("February 9th, 1991"),
      emailAddress = Some("charles.mathau.outlook.com")
    )

    when(database.get(id)).thenReturn(Future.successful(Some(existing)))
    when(database.getByEmail("charles.mathau.outlook.com")).thenReturn(Future.successful(None))

    service.updateProfile(id, request).futureValue should be (Left(NonEmptyList.of(InvalidName, InvalidName, InvalidDate, InvalidEmail)))
  }

  it should "reject a profile update attempt with a duplicate email" in {
    val id = UUID.randomUUID()
    val toBeUpdated = Profile(
      id = id,
      firstName = "Charles",
      lastName = "Mathau",
      dateOfBirth = "1991-02-09",
      emailAddress = "charles.mathau@gmail.com"
    )
    val otherProfile = Profile(
      id = UUID.randomUUID(),
      firstName = "Charles",
      lastName = "Mathau",
      dateOfBirth = "1991-02-09",
      emailAddress = "charles.mathau@outlook.com"
    )
    val request = UpdateProfile(
      firstName = Some(""),
      lastName = Some(""),
      dateOfBirth = Some("February 9th, 1991"),
      emailAddress = Some("charles.mathau@outlook.com")
    )

    when(database.get(id)).thenReturn(Future.successful(Some(toBeUpdated)))
    when(database.getByEmail("charles.mathau@outlook.com")).thenReturn(Future.successful(Some(otherProfile)))

    service.updateProfile(id, request).futureValue should be (Left(NonEmptyList.of(InvalidName, InvalidName, InvalidDate, DuplicateEmail)))
  }

  it should "handle a profile update attempt with a failed database transaction" in {
    val id = UUID.randomUUID()
    val existing = Profile(
      id = id,
      firstName = "Charles",
      lastName = "Mathau",
      dateOfBirth = "1991-02-09",
      emailAddress = "charles.mathau@gmail.com"
    )
    val request = UpdateProfile(
      firstName = None,
      lastName = None,
      dateOfBirth = None,
      emailAddress = Some("charles.mathau@outlook.com")
    )
    val expected = existing.copy(
      emailAddress = "charles.mathau@outlook.com"
    )

    when(database.get(id)).thenReturn(Future.successful(Some(existing)))
    when(database.getByEmail("charles.mathau@outlook.com")).thenReturn(Future.successful(None))
    when(database.update(id, expected)).thenReturn(Future.failed(new Exception))

    service.updateProfile(id, request).futureValue should be (Left(NonEmptyList.of(UnknownError)))
  }

  it should "update an existing user profile with valid data" in {
    val id = UUID.randomUUID()
    val existing = Profile(
      id = id,
      firstName = "Charles",
      lastName = "Mathau",
      dateOfBirth = "1991-02-09",
      emailAddress = "charles.mathau@gmail.com"
    )
    val request = UpdateProfile(
      firstName = None,
      lastName = None,
      dateOfBirth = None,
      emailAddress = Some("charles.mathau@outlook.com")
    )
    val expected = existing.copy(
      emailAddress = "charles.mathau@outlook.com"
    )

    when(database.get(id)).thenReturn(Future.successful(Some(existing)))
    when(database.getByEmail("charles.mathau@outlook.com")).thenReturn(Future.successful(None))
    when(database.update(id, expected)).thenReturn(Future.successful())

    service.updateProfile(id, request).futureValue should be (Right(expected))
  }
}
