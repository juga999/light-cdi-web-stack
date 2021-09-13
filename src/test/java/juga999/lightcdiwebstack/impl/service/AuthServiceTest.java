package juga999.lightcdiwebstack.impl.service;

import com.auth0.jwt.interfaces.Payload;
import jakarta.inject.Inject;
import juga999.lightcdiwebstack.impl.dao.UserDao;
import juga999.lightcdiwebstack.impl.db.DataSourceTest;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

public class AuthServiceTest extends DataSourceTest {

    @Rule
    public final TestRule chain = getRuleChain();

    public AuthServiceTest() {
        super(Lists.newArrayList(UserDao.class, AuthService.class));
    }

    @Inject
    protected AuthService authService;

    @Test
    public void testLoginWithValidCredentials() {
        String token = authService.getToken("admin", "pwd");
        Assertions.assertThat(token).isNotEmpty();
    }

    @Test
    public void testLoginWithInvalidCredentials() {
        String token = authService.getToken("anon", "pwd");
        Assertions.assertThat(token).isNull();
    }

    @Test
    public void testDecodeValidToken() {
        String token = authService.getToken("admin", "pwd");
        Assertions.assertThat(token).isNotEmpty();

        Payload payload = authService.decodeToken(token);
        Assertions.assertThat(payload.getSubject()).isEqualTo("admin");
    }
}
