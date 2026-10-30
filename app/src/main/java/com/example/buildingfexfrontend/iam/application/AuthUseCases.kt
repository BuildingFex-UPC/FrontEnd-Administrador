package com.example.buildingfexfrontend.iam.application

import com.example.buildingfexfrontend.core.domain.model.AppException
import com.example.buildingfexfrontend.core.domain.model.SessionRole
import com.example.buildingfexfrontend.core.domain.model.UserProfile
import com.example.buildingfexfrontend.core.domain.repository.SessionRepository
import com.example.buildingfexfrontend.iam.domain.repository.AuthRepository

/**
 * IAM use cases. They own the "who is signed in" decision: the repository
 * returns credentials, the use case persists the session with the right role.
 */
class AuthUseCases(
    private val repository: AuthRepository,
    private val session: SessionRepository,
) {
    val login = LoginUseCase(repository, session)
    val registerAdmin = RegisterAdminUseCase(repository, session)
    val isEmailRegistered = IsEmailRegisteredUseCase(repository)
    val refreshProfile = RefreshProfileUseCase(repository, session)
    val updateProfile = UpdateProfileUseCase(repository, session)
    val changePassword = ChangePasswordUseCase(repository, session)
    val logout = LogoutUseCase(session)
    val ensureValidSession = EnsureValidSessionUseCase(session)
}

class LoginUseCase(
    private val repository: AuthRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(email: String, password: String): UserProfile {
        val (profile, token) = repository.login(email.trim().lowercase(), password)
        // This app is admin-only: a resident must use the resident app instead.
        if (profile.role == "resident") {
            session.clear()
            throw AppException("RESIDENT_LOGIN_BLOCKED", "")
        }
        session.signIn(SessionRole.ADMIN, profile, token)
        return profile
    }
}

class RegisterAdminUseCase(
    private val repository: AuthRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        dni: String,
        address: String,
        company: String,
        ruc: String,
    ): UserProfile {
        val (profile, token) = repository.registerAdmin(
            name = name.trim(),
            email = email.trim().lowercase(),
            password = password,
            dni = dni.trim(),
            address = address.trim(),
            company = company.trim(),
            ruc = ruc.trim(),
        )
        session.signIn(SessionRole.ADMIN, profile.copy(role = "admin"), token)
        return profile
    }
}

class IsEmailRegisteredUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Boolean =
        repository.isEmailRegistered(email.trim().lowercase())
}

class RefreshProfileUseCase(
    private val repository: AuthRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(): UserProfile? {
        val current = session.current() ?: return null
        val fresh = repository.getProfileById(current.profile.id) ?: return current.profile
        session.updateProfile(fresh)
        return fresh
    }
}

/** Saves the editable profile fields and refreshes the cached session. */
class UpdateProfileUseCase(
    private val repository: AuthRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(profile: UserProfile): UserProfile {
        val fresh = repository.updateProfile(profile)
        session.updateProfile(fresh)
        return fresh
    }
}

/**
 * Changes the signed-in account's password (`PATCH users/{id}`). The backend
 * requires the current email alongside the new password, so it is read from
 * the session. Works for any role (admins included).
 */
class ChangePasswordUseCase(
    private val repository: AuthRepository,
    private val session: SessionRepository,
) {
    suspend operator fun invoke(newPassword: String): UserProfile {
        val current = session.current()
            ?: throw AppException("SESSION_NOT_FOUND", "")
        val email = current.profile.email
            ?: throw AppException("NO_EMAIL", "")
        val fresh = repository.updateCredentials(
            id = current.profile.id,
            email = email,
            password = newPassword,
        )
        session.updateProfile(fresh)
        return fresh
    }
}

class LogoutUseCase(private val session: SessionRepository) {
    operator fun invoke() = session.clear()
}

class EnsureValidSessionUseCase(private val session: SessionRepository) {
    operator fun invoke(): Boolean {
        val valid = session.current() != null && session.isTokenValid()
        if (!valid) session.clear()
        return valid
    }
}
