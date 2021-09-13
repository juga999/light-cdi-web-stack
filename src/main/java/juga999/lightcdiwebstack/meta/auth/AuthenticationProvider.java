package juga999.lightcdiwebstack.meta.auth;

import com.auth0.jwt.interfaces.Payload;

import java.util.Set;

public interface AuthenticationProvider {

    Payload decodeToken(String token);

    void validatePermission(String permission);

    void checkPermissions(String user, Set<String> requiredPermissions) throws UnauthorizedException;
}
