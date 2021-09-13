package juga999.lightcdiwebstack.impl.db.transaction;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import juga999.lightcdiwebstack.impl.dao.UserDao;
import juga999.lightcdiwebstack.meta.auth.model.Permission;
import juga999.lightcdiwebstack.meta.auth.model.UserPermissions;
import juga999.lightcdiwebstack.meta.db.TransactionRequired;

@ApplicationScoped
public class ServiceA {

    @Inject
    protected UserDao userDao;

    @Inject
    protected ServiceB serviceB;

    @TransactionRequired
    public UserPermissions updatePermissionsNoThrow() {
        userDao.setUserPermissions("admin", UserPermissions.of(Permission.NONE));
        return serviceB.getUserPermissions();
    }

    @TransactionRequired
    public UserPermissions updatePermissionsWithThrow() {
        userDao.setUserPermissions("admin", UserPermissions.of(Permission.NONE));
        serviceB.methodThatThrows();
        return serviceB.getUserPermissions();
    }
}
