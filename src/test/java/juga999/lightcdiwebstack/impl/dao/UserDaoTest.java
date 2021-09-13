package juga999.lightcdiwebstack.impl.dao;

import jakarta.inject.Inject;
import juga999.lightcdiwebstack.impl.db.DataSourceTest;
import juga999.lightcdiwebstack.meta.auth.model.Permission;
import juga999.lightcdiwebstack.meta.auth.model.User;
import juga999.lightcdiwebstack.meta.auth.model.UserPermissions;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UserDaoTest extends DataSourceTest {

    @Rule
    public final TestRule chain = getRuleChain();

    public UserDaoTest() {
        super(Lists.newArrayList(UserDao.class));
    }

    @Inject
    protected UserDao userDao;

    @Test
    public void testFindAllUser() {
        List<User> users = userDao.findAll();
        Assertions.assertThat(users).hasSize(2);
        Assertions.assertThat(
                users.stream().filter(u -> u.getLogin().equals("admin")).findFirst())
                .isPresent();
    }

    @Test
    public void testGetFromLoginPassword() {
        Optional<User> user = userDao.tryGetFromLoginPassword("admin", "pwd");
        Assertions.assertThat(user).isPresent();
        UserPermissions userPermissions = user.map(User::getUserPermissions).orElse(null);
        Assertions.assertThat(Objects.requireNonNull(userPermissions).getPermissions()).contains(Permission.CAN_ADMIN);
    }

    @Test
    public void testGetFromInvalidLoginPassword() {
        Optional<User> user = userDao.tryGetFromLoginPassword("anon", "pwd");
        Assertions.assertThat(user).isEmpty();
    }

    @Test
    public void testGetFromLoginWithInvalidPassword() {
        Optional<User> user = userDao.tryGetFromLoginPassword("admin", "___");
        Assertions.assertThat(user).isEmpty();
    }

    @Test
    public void testGetUserPermissions() {
        UserPermissions permissions = userDao.getUserPermissions("admin");
        Assertions.assertThat(permissions.getPermissions()).contains(Permission.CAN_ADMIN);
    }

    @Test
    public void testSetUserPermissions() {
        userDao.setUserPermissions("admin", UserPermissions.of(Permission.NONE));

        UserPermissions permissions = userDao.getUserPermissions("admin");
        Assertions.assertThat(permissions.getPermissions()).doesNotContain(Permission.CAN_ADMIN);
        Assertions.assertThat(permissions.getPermissions()).contains(Permission.NONE);
    }
}
