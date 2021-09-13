package juga999.lightcdiwebstack.meta.auth.model;

import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.json.Json;

import java.util.UUID;

public class User {
    private UUID id;

    private String login;

    private UserPermissions userPermissions;

    @ColumnName("id")
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @ColumnName("login")
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Json
    @ColumnName("permissions")
    public UserPermissions getUserPermissions() {
        return userPermissions;
    }

    public void setUserPermissions(UserPermissions userPermissions) {
        this.userPermissions = userPermissions;
    }
}
