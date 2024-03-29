package com.fsociety.authapi.app.login;

import com.fsociety.authapi.http.login.dto.LoginRequestDTO;
import com.fsociety.authapi.http.login.dto.LoginResponseDTO;
import com.fsociety.authapi.utils.LoginAuthenticationException;

public interface LoginService {
  LoginResponseDTO login(LoginRequestDTO dto) throws LoginAuthenticationException;
}
