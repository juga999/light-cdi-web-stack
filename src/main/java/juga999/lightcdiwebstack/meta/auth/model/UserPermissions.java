package juga999.lightcdiwebstack.meta.auth.model;

import com.google.common.collect.Sets;

import java.util.Set;

public class UserPermissions {

    private Set<Permission> permissions;

    public static UserPermissions of(Permission... permissions) {
        return new UserPermissions(permissions);
    }

    public UserPermissions() {
        this.permissions = Sets.newHashSet();
    }

    public UserPermissions(Permission... permissions) {
        this.permissions = Sets.newHashSet(permissions);
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

}
