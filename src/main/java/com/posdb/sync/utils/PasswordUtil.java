package com.posdb.sync.utils;

import jakarta.enterprise.context.ApplicationScoped;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class PasswordUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordUtil.class);
    private static final int BCRYPT_ROUNDS = 10;

    public String hashPassword(String password) {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));
        LOGGER.debug("Password hashed successfully");
        return hash;
    }

    public boolean verifyPassword(String password, String hash) {
        boolean matches = BCrypt.checkpw(password, hash);
        LOGGER.debug("Password verification result: {}", matches);
        return matches;
    }
}

