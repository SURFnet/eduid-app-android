package nl.eduid.screens.authorize

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import nl.eduid.BaseViewModel
import nl.eduid.graphs.Account
import nl.eduid.screens.biometric.BiometricSignIn
import org.tiqr.data.model.AuthenticationChallenge
import org.tiqr.data.model.AuthenticationCompleteRequest
import org.tiqr.data.model.ChallengeCompleteFailure
import org.tiqr.data.model.ChallengeCompleteResult
import org.tiqr.data.model.SecretCredential
import org.tiqr.data.model.SecretType
import org.tiqr.data.repository.AuthenticationRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EduIdAuthenticationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    moshi: Moshi,
    val repository: AuthenticationRepository,
) : BaseViewModel(moshi) {

    val challenge = MutableLiveData<AuthenticationChallenge?>()
    val challengeComplete =
        MutableLiveData<ChallengeCompleteResult<ChallengeCompleteFailure>?>(null)

    private val _otpGenerate = MutableLiveData<SecretCredential>()
    val otp = _otpGenerate.switchMap { credential ->
        liveData {
            challenge.value?.let { challenge ->
                challenge.identity?.let {
                    emit(repository.completeOtp(credential, it, challenge))
                }
            }
        }
    }

    val userId = challenge.map { it?.identity?.identifier }

    init {
        val authorizeChallenge =
            savedStateHandle.get<String>(Account.RequestAuthentication.challengeArg) ?: ""
        val challengeUrl = Uri.decode(authorizeChallenge)
        val adapter = moshi.adapter(AuthenticationChallenge::class.java)
        challenge.value = try {
            adapter.fromJson(challengeUrl)
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse enrollment challenge")
//            uiState.value = uiState.value?.copy(
//                errorData = ErrorData(
//                    title = "Failed to parse challenge",
//                    message = "Could not parse enrollment challenge"
//                )
//            )
            null
        }
        val pin = savedStateHandle.get<String>(Account.OneTimePassword.pinArg)
        if (pin != null) {
            generateOTP(pin)
        }
    }

    fun clearCompleteChallenge() {
        challengeComplete.value = null
    }

    fun authenticateWithPin(pin: String) = authenticate(SecretCredential.pin(pin))
    fun authenticateWithBiometric(biometricSignIn: BiometricSignIn) {
        if (biometricSignIn is BiometricSignIn.Success) authenticate(SecretCredential.biometric())
    }

    private fun authenticate(credential: SecretCredential) = viewModelScope.launch {
        val it = challenge.value ?: return@launch
        val challengeRequest =
            AuthenticationCompleteRequest(it, credential.password, credential.type)
        val challengeResult: ChallengeCompleteResult<ChallengeCompleteFailure> =
            repository.completeChallenge(challengeRequest)
        challengeComplete.postValue(challengeResult)
    }

    /**
     * Perform OTP generation
     */
    private fun generateOTP(password: String) {
        val type =
            if (password == SecretType.BIOMETRIC.key) SecretType.BIOMETRIC else SecretType.PIN
        _otpGenerate.value = SecretCredential(password = password, type = type)
    }

}