package com.mipt.nikitabumagin.security;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Decorates a delegate password encoder by appending application-level pepper.
 */
public class PepperPasswordEncoder implements PasswordEncoder {

    private final PasswordEncoder delegate;
    private final String pepper;

    public PepperPasswordEncoder(PasswordEncoder delegate, String pepper) {
        this.delegate = delegate;
        this.pepper = pepper;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return delegate.encode(applyPepper(rawPassword));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return delegate.matches(applyPepper(rawPassword), encodedPassword);
    }

    private String applyPepper(CharSequence rawPassword) {
        return rawPassword + pepper;
    }
}
