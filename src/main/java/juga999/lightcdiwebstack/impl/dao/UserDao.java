package juga999.lightcdiwebstack.impl.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import juga999.lightcdiwebstack.meta.db.DataSource;
import juga999.lightcdiwebstack.meta.auth.model.User;
import juga999.lightcdiwebstack.meta.auth.model.UserPermissions;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.json.Json;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserDao {

    @Inject
    protected DataSource dataSource;

    private final QualifiedType<UserPermissions> userPermissionsQualifiedType =
            QualifiedType.of(UserPermissions.class).with(Json.class);

    public List<User> findAll() {
        return dataSource.withHandle(h -> h.select("select id, login, permissions from user")
                .mapToBean(User.class)
                .list());
    }

    public Optional<User> tryGetFromLoginPassword(String login, String password) {
        byte[] hashedPassword = getLoginSalt(login)
                .map(s -> hashPassword(password, s))
                .orElse(new byte[]{});

        return dataSource.withHandle(h -> h.select("select id, login, permissions from user where password = :password")
                .bind("password", hashedPassword)
                .mapToBean(User.class)
                .findFirst());
    }

    public UserPermissions getUserPermissions(String login) {
        User user = dataSource.withHandle(h -> h.select("select id, login, permissions from user where login = :login")
                .bind("login", login)
                .mapToBean(User.class)
                .one());
        return user.getUserPermissions();
    }

    public void setUserPermissions(String login, UserPermissions userPermissions) {
        dataSource.useHandle(h -> h.createUpdate("update user set permissions = :permissions where login = :login")
                .bind("login", login)
                .bindByType("permissions", userPermissions, userPermissionsQualifiedType)
                .execute());
    }

    private Optional<byte[]> getLoginSalt(String login) {
        return dataSource.withHandle(h -> h.select("select salt from user where login = :login")
                .bind("login", login)
                .mapTo(byte[].class)
                .findFirst());
    }

    private byte[] hashPassword(String password, byte[] salt) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        md.update(salt);

        byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
        return hashedPassword;
    }
}
