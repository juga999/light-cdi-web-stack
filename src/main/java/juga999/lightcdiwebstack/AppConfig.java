package juga999.lightcdiwebstack;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

@ApplicationScoped
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    private final Properties props = new Properties();

    public AppConfig() {
    }

    @PostConstruct
    void init() {
        try {
            props.load(getClass().getClassLoader().getResourceAsStream("app.properties"));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public Integer getWebServerPort() {
        return Integer.valueOf(props.getProperty("port"));
    }

    public String getJdbcUrl() {
        return props.getProperty("jdbcUrl");
    }

    public String getSecret() {
        return props.getProperty("secret");
    }

    public Properties getProperties() {
        return props;
    }
}
