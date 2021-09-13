package juga999.lightcdiwebstack.impl.endpoint;

import jakarta.inject.Inject;
import juga999.lightcdiwebstack.impl.service.AuthService;
import juga999.lightcdiwebstack.meta.auth.UnauthorizedException;
import juga999.lightcdiwebstack.meta.http.AppEndpoint;
import juga999.lightcdiwebstack.meta.http.Context;
import juga999.lightcdiwebstack.meta.http.JsonConsumer;
import juga999.lightcdiwebstack.meta.http.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginEndpoint extends AppEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(LoginEndpoint.class);

    public static final class LoginData {
        private String login;
        private String password;

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Inject
    protected AuthService authService;

    @Post("/login")
    @JsonConsumer
    public Object login(Context context) throws Exception {
        LoginData loginData = context.getPostObject(LoginData.class);
        String user = loginData.getLogin();
        String password = loginData.getPassword();
        if (user == null || password == null) {
            throw new RuntimeException("Empty login or password");
        }

        String token = authService.getToken(user, password);
        if (token != null) {
            logger.info("Successful login for user " + user);
            return token;
        } else {
            logger.warn("Failed login for user " + user);
            throw new UnauthorizedException("");
        }
    }

}
