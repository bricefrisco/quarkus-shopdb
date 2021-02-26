package com.ecocitycraft.shopdb.utils;

import com.ecocitycraft.shopdb.database.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.WildFlyElytronPasswordProvider;
import org.wildfly.security.password.interfaces.BCryptPassword;
import org.wildfly.security.password.util.ModularCrypt;

import javax.enterprise.context.ApplicationScoped;
import java.security.NoSuchAlgorithmException;

@ApplicationScoped
public class APIKeyValidator {
    final Logger LOGGER = LoggerFactory.getLogger(APIKeyValidator.class);
    final PasswordFactory FACTORY;

    public APIKeyValidator() throws NoSuchAlgorithmException {
        this.FACTORY = PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT, new WildFlyElytronPasswordProvider());
    }

    public void validateAPIKey(String authHeader) throws Exception {
        if (authHeader == null) throw new RuntimeException("Unauthorized.");
        String token = authHeader.replace("Bearer ", "");
        User apiUser = User.find("username", "ShopDBAPI").firstResult();
        if (apiUser == null) throw new RuntimeException("API user not found.");
        if (!validate(token, apiUser.password)) throw new RuntimeException("Invalid token.");
    }

    private boolean validate(String a, String b) throws Exception {
        Password rawPassword = ModularCrypt.decode(b);
        BCryptPassword restored = (BCryptPassword) FACTORY.translate(rawPassword);
        return FACTORY.verify(restored, a.toCharArray());
    }
}
