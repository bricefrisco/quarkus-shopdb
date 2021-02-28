package com.ecocitycraft.shopdb.utils;

import com.ecocitycraft.shopdb.database.User;
import com.ecocitycraft.shopdb.exceptions.ExceptionMessage;
import com.ecocitycraft.shopdb.exceptions.SDBUnauthorizedException;
import org.wildfly.security.password.Password;
import org.wildfly.security.password.PasswordFactory;
import org.wildfly.security.password.WildFlyElytronPasswordProvider;
import org.wildfly.security.password.interfaces.BCryptPassword;
import org.wildfly.security.password.util.ModularCrypt;

import javax.enterprise.context.ApplicationScoped;
import java.security.NoSuchAlgorithmException;

@ApplicationScoped
public class APIKeyValidator {
    private static final String API_USERNAME = "ShopDBAPI";
    final PasswordFactory FACTORY;

    public APIKeyValidator() throws NoSuchAlgorithmException {
        this.FACTORY = PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT, new WildFlyElytronPasswordProvider());
    }

    public void validateAPIKey(String authHeader) throws Exception {
        if (authHeader == null) throw new SDBUnauthorizedException(ExceptionMessage.UNAUTHORIZED);

        String token = authHeader.replace("Bearer ", "");

        User apiUser = User.find("username", API_USERNAME).firstResult();
        if (apiUser == null)
            throw new SDBUnauthorizedException(String.format(ExceptionMessage.USER_NOT_FOUND, API_USERNAME));

        if (!validate(token, apiUser.password)) throw new SDBUnauthorizedException(ExceptionMessage.UNAUTHORIZED);
    }

    private boolean validate(String a, String b) throws Exception {
        Password rawPassword = ModularCrypt.decode(b);
        BCryptPassword restored = (BCryptPassword) FACTORY.translate(rawPassword);
        return FACTORY.verify(restored, a.toCharArray());
    }
}
