package com.mipt.nikitabumagin.service.v1;

import com.mipt.nikitabumagin.dto.v1.LoginRequestDto;
import com.mipt.nikitabumagin.dto.v1.LoginResponseDto;
import com.mipt.nikitabumagin.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthContractService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthContractService(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public LoginResponseDto login(LoginRequestDto request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtUtils.generateToken(principal);
            return new LoginResponseDto(accessToken, "Bearer", jwtUtils.getExpirationSeconds());
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }
}
