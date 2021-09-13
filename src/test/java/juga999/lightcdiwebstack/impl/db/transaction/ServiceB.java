package juga999.lightcdiwebstack.impl.db.transaction;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import juga999.lightcdiwebstack.impl.dao.UserDao;
import juga999.lightcdiwebstack.meta.auth.model.UserPermissions;
import juga999.lightcdiwebstack.meta.db.TransactionRequired;

@ApplicationScoped
public class ServiceB {

    @Inject
    protected UserDao userDao;

    @TransactionRequired
    public void methodThatThrows() {
        throw new RuntimeException();
    }

    @TransactionRequired
    public UserPermissions getUserPermissions() {
        return userDao.getUserPermissions("admin");
    }
}
