package com.example.coursework.ui.features.auth.login

import androidx.lifecycle.viewModelScope
import com.example.coursework.data.CourseWorkSession
import com.example.coursework.data.FoodApi
import com.example.coursework.data.models.SignInRequest
import com.example.coursework.data.remote.ApiResponse
import com.example.coursework.data.remote.safeApiCall
import com.example.coursework.ui.features.auth.BaseAuthViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SignInViewModel @Inject constructor(
    override val foodApi: FoodApi,
    val session: CourseWorkSession
) :
    BaseAuthViewModel(foodApi) {

    private val _uiState = MutableStateFlow<SignInEvent>(SignInEvent.Nothing)
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<SigInNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun onPasswordChange(password: String) {
        _password.value = password
    }

    fun onSignInClick() {
        viewModelScope.launch {
            _uiState.value = SignInEvent.Loading
            val response = safeApiCall {
                foodApi.signIn(
                    SignInRequest(
                        email = email.value, password = password.value
                    )
                )
            }
            when (response) {
                is ApiResponse.Success -> {
                    _uiState.value = SignInEvent.Success
                    session.storeToken(response.data.token)
                    _navigationEvent.emit(SigInNavigationEvent.NavigateToHome)
                }

                else -> {
                    val errr = (response as? ApiResponse.Error)?.code ?: 0
                    error = "Вход не удался"
                    errorDescription = "Не удалось войти"
                    when (errr) {
                        400 -> {
                            error = "Неверные данные"
                            errorDescription = "Пожалуйста введите корректные данные."
                        }
                    }
                    _uiState.value = SignInEvent.Error
                }
            }
        }
    }

    fun onSignUpClicked() {
        viewModelScope.launch {
            _navigationEvent.emit(SigInNavigationEvent.NavigateToSignUp)
        }
    }

    sealed class SigInNavigationEvent {
        object NavigateToSignUp : SigInNavigationEvent()
        object NavigateToHome : SigInNavigationEvent()
    }

    sealed class SignInEvent {
        object Nothing : SignInEvent()
        object Success : SignInEvent()
        object Error : SignInEvent()
        object Loading : SignInEvent()
    }

    override fun loading() {
        viewModelScope.launch {
            _uiState.value = SignInEvent.Loading
        }
    }

    override fun onGoogleError(msg: String) {
        viewModelScope.launch {
            errorDescription = msg
            error = "Не удалось войти через Google"
            _uiState.value = SignInEvent.Error
        }
    }

    override fun onFacebookError(msg: String) {
        viewModelScope.launch {
            errorDescription = msg
            error = "Не удалось войти через Facebook"
            _uiState.value = SignInEvent.Error
        }
    }

    override fun onSocialLoginSuccess(token: String) {
        viewModelScope.launch {
            session.storeToken(token)
            _uiState.value = SignInEvent.Success
            _navigationEvent.emit(SigInNavigationEvent.NavigateToHome)
        }
    }
}