package juga999.lightcdiwebstack.impl.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Payload;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import juga999.lightcdiwebstack.AppConfig;
import juga999.lightcdiwebstack.impl.dao.UserDao;
import juga999.lightcdiwebstack.meta.auth.AuthenticationProvider;
import juga999.lightcdiwebstack.meta.auth.UnauthorizedException;
import juga999.lightcdiwebstack.meta.auth.model.Permission;
import juga999.lightcdiwebstack.meta.auth.model.User;
import juga999.lightcdiwebstack.meta.auth.model.UserPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class AuthService implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private Algorithm algorithm;

    private JWTVerifier verifier;

    @Inject
    private AppConfig config;

    @Inject
    private UserDao userDao;

    public String getToken(String subject, String password) {
        if (Strings.isNullOrEmpty(subject) || Strings.isNullOrEmpty(password)) {
            return null;
        }

        Optional<User> person = userDao.tryGetFromLoginPassword(subject, password);
        if (!person.isPresent()) {
            logger.trace("Invalid login or password for " + subject);
            return null;
        }

        Instant now = Instant.now();
        Instant expirationInstant = Instant.now().plus(Duration.ofMinutes(20));

        String token = JWT.create()
                .withIssuer("auth0")
                .withSubject(subject)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expirationInstant))
                .sign(getAlgorithm());

        return token;
    }

    @Override
    public void validatePermission(String permissionValue) {
        Permission permission = Permission.valueOf(permissionValue);
        try {
            Objects.requireNonNull(permission);
        } catch(Exception e) {
            logger.error("Invalid permission value: " + permissionValue, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void checkPermissions(String user, Set<String> permissionValues) throws UnauthorizedException {
        UserPermissions userPermissions = userDao.getUserPermissions(user);
        Set<Permission> requiredPermissions = permissionValues.stream().map(Permission::valueOf).collect(Collectors.toSet());
        if (Sets.intersection(userPermissions.getPermissions(), requiredPermissions).isEmpty()) {
            throw new UnauthorizedException("Restricted access");
        }
    }

    @Override
    public Payload decodeToken(String token) {
        try {
            Payload verifiedToken = getVerifier().verify(token);
            return verifiedToken;
        } catch(JWTVerificationException e) {
            return null;
        }
    }

    private Algorithm getAlgorithm() {
        if (algorithm == null) {
            String secret = config.getSecret();
            if (Strings.isNullOrEmpty(secret)) {
                throw new IllegalStateException("Secret key is not defined");
            }
            algorithm = Algorithm.HMAC512(secret);
        }
        return algorithm;
    }

    private JWTVerifier getVerifier() {
        if (verifier == null) {
            verifier = JWT.require(getAlgorithm()).withIssuer("auth0").build();
        }
        return verifier;
    }
}
