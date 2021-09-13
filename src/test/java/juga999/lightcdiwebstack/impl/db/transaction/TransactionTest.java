package juga999.lightcdiwebstack.impl.db.transaction;

import jakarta.inject.Inject;
import juga999.lightcdiwebstack.impl.dao.UserDao;
import juga999.lightcdiwebstack.impl.db.DataSourceTest;
import juga999.lightcdiwebstack.meta.auth.model.Permission;
import juga999.lightcdiwebstack.meta.auth.model.UserPermissions;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class TransactionTest extends DataSourceTest {

    @Rule
    public final TestRule chain = getRuleChain();

    public TransactionTest() {
        super(Lists.newArrayList(UserDao.class, ServiceA.class, ServiceB.class));
    }

    @Inject
    protected UserDao userDao;

    @Inject
    protected ServiceA serviceA;

    @Test
    public void testTransactionNoThrow() {
        UserPermissions permissions = serviceA.updatePermissionsNoThrow();
        Assertions.assertThat(permissions.getPermissions()).contains(Permission.NONE);
    }

    @Test
    public void testTransactionWithThrow() {
        try {
            serviceA.updatePermissionsWithThrow();
        } catch (Exception e) {}
        UserPermissions permissions = userDao.getUserPermissions("admin");
        Assertions.assertThat(permissions.getPermissions()).contains(Permission.CAN_ADMIN);
    }
}
