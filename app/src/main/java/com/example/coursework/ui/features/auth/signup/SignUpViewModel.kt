package com.example.coursework.ui.features.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursework.data.FoodApi
import com.example.coursework.data.CourseWorkSession
import com.example.coursework.data.models.SignUpRequest
import com.example.coursework.data.remote.ApiResponse
import com.example.coursework.data.remote.safeApiCall
import com.example.coursework.ui.features.auth.AuthScreenViewModel.AuthEvent
import com.example.coursework.ui.features.auth.BaseAuthViewModel
import com.example.coursework.ui.features.auth.login.SignInViewModel.SigInNavigationEvent
import com.example.coursework.ui.features.auth.login.SignInViewModel.SignInEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(override val foodApi: FoodApi, val session:CourseWorkSession) :
    BaseAuthViewModel(foodApi) {
    private val _uiState = MutableStateFlow<SignupEvent>(SignupEvent.Nothing)
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<SigupNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun onPasswordChange(password: String) {
        _password.value = password
    }

    fun onNameChange(name: String) {
        _name.value = name
    }

    fun onSignUpClick() {
        viewModelScope.launch {
            _uiState.value = SignupEvent.Loading
            try {
                val response = safeApiCall {
                    foodApi.signUp(
                        SignUpRequest(
                            name = name.value,
                            email = email.value,
                            password = password.value
                        )
                    )
                }
                when (response) {
                    is ApiResponse.Success -> {
                        _uiState.value = SignupEvent.Success
                        session.storeToken(response.data.token)
                        _navigationEvent.emit(SigupNavigationEvent.NavigateToHome)
                    }

                    else -> {
                        val errr = (response as? ApiResponse.Error)?.code ?: 0
                        error = "Не удалось зарегистрироваться"
                        errorDescription = "Ошибка регистрации"
                        when (errr) {
                            400 -> {
                                error = "Неверные данные"
                                errorDescription = "Пожалуйста введите корректные данные."
                            }
                        }
                        _uiState.value = SignupEvent.Error
                    }
                }


            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = SignupEvent.Error
            }

        }

    }

    fun onLoginClicked() {
        viewModelScope.launch {
            _navigationEvent.emit(SigupNavigationEvent.NavigateToLogin)
        }
    }

    override fun loading() {
        viewModelScope.launch {
            _uiState.value = SignupEvent.Loading
        }
    }

    override fun onGoogleError(msg: String) {
        viewModelScope.launch {
            errorDescription = msg
            error = "Не удалось войти через Google"
            _uiState.value = SignupEvent.Error
        }
    }

    override fun onFacebookError(msg: String) {
        viewModelScope.launch {
            errorDescription = msg
            error = "Не удалось войти через Facebook"
            _uiState.value = SignupEvent.Error
        }
    }

    override fun onSocialLoginSuccess(token: String) {
        viewModelScope.launch {
            session.storeToken(token)
            _uiState.value = SignupEvent.Success
            _navigationEvent.emit(SigupNavigationEvent.NavigateToHome)
        }
    }

    sealed class SigupNavigationEvent {
        object NavigateToLogin : SigupNavigationEvent()
        object NavigateToHome : SigupNavigationEvent()
    }

    sealed class SignupEvent {
        object Nothing : SignupEvent()
        object Success : SignupEvent()
        object Error : SignupEvent()
        object Loading : SignupEvent()
    }
}