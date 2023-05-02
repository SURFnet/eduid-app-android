package nl.eduid.screens.deleteaccountsecondconfirm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import nl.eduid.ErrorData
import nl.eduid.R
import nl.eduid.di.repository.StorageRepository
import nl.eduid.screens.personalinfo.PersonalInfoRepository
import org.tiqr.data.repository.IdentityRepository
import org.tiqr.data.service.DatabaseService
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class DeleteAccountSecondConfirmViewModel @Inject constructor(
    private val repository: PersonalInfoRepository,
    private val db: DatabaseService,
    private val identity: IdentityRepository,
    private val storage: StorageRepository
) :
    ViewModel() {
    var uiState by mutableStateOf(UiState())
        private set

    fun onInputChange(newValue: String) {
        uiState = uiState.copy(fullName = newValue)
    }

    fun onDeleteAccountPressed() = viewModelScope.launch {
        uiState = uiState.copy(inProgress = true)
        val userDetails = repository.getUserDetails()
        if (userDetails != null) {
            val knownFullName = "${userDetails.givenName} ${userDetails.familyName}"
            val typedFullName = uiState.fullName
            if (knownFullName == typedFullName) {
                val deleteOk = repository.deleteAccount()
                val allIdentities = db.getAllIdentities()
                storage.clearAll()
                try {
                    allIdentities.forEach {
                        identity.delete(it)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to cleanup existing identities when deleting account")
                }
                uiState =
                    uiState.copy(
                        inProgress = false,
                        isDeleted = if (deleteOk) Unit else null,
                        errorData = if (deleteOk) null else ErrorData(
                            titleId = R.string.err_title_delete_fail,
                            messageId = R.string.error_msg_delete_fail
                        ),
                    )
            } else {
                uiState =
                    uiState.copy(
                        inProgress = false,
                        errorData = ErrorData(
                            titleId = R.string.err_title_delete_fail,
                            messageId = R.string.error_msg_delete_name_missmatch
                        )
                    )
            }
        }
    }

    fun clearErrorData() {
        uiState = uiState.copy(errorData = null)
    }
}