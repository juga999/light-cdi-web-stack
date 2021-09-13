package juga999.lightcdiwebstack;

import juga999.lightcdiwebstack.impl.http.undertow.WebServerCDIExtension;
import juga999.lightcdiwebstack.meta.db.DataSource;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppRunner {

    private static final Logger logger = LoggerFactory.getLogger(AppRunner.class);

    public static void runApp() {
        Weld.newInstance()
                .addExtensions(WebServerCDIExtension.get())
                .initialize();

        DataSource dataSource = WeldContainer.current().select(DataSource.class).get();
        logger.trace(dataSource.toString());

        AppConfig appConfig = WeldContainer.current().select(AppConfig.class).get();
        WebServerCDIExtension.get().startHttpServer(appConfig.getWebServerPort());
    }

}
