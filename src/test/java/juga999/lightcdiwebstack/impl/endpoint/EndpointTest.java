package juga999.lightcdiwebstack.impl.endpoint;

import com.google.common.collect.Lists;
import juga999.lightcdiwebstack.AppConfig;
import juga999.lightcdiwebstack.impl.dao.UserDao;
import juga999.lightcdiwebstack.impl.db.h2.H2InMemoryDataSource;
import juga999.lightcdiwebstack.impl.http.undertow.WebServerCDIExtension;
import juga999.lightcdiwebstack.impl.service.AuthService;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

public abstract class EndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(EndpointTest.class);

    private class WebServerResource extends ExternalResource {
        @Override
        protected void before() {
            port = findFreePort();
            WebServerCDIExtension.get().startHttpServer(port);
        }

        @Override
        protected void after() {
            WebServerCDIExtension.get().stopHttpServer();
            WebServerCDIExtension.get().reset();
        }
    }

    private final Class<?>[] injections;

    protected int port;

    public EndpointTest(List<Class<?>> otherInjections) {
        List<Class<?>> injectionList = Lists.newArrayList(
                AppConfig.class,
                H2InMemoryDataSource.class,
                UserDao.class,
                AuthService.class);

        injectionList.addAll(otherInjections);

        injections = injectionList.toArray(new Class<?>[injectionList.size()]);
    }

    protected Class<?>[] getInjections() {
        return injections;
    }

    protected TestRule getRuleChainWithHttpServer() {
        Weld weld = WeldInitiator.createWeld()
                .addBeanClasses(getInjections())
                .addExtensions(WebServerCDIExtension.get());
        return RuleChain
                .outerRule(WeldInitiator.from(weld).inject(this).build())
                .around(new WebServerResource());
    }

    protected int getPort() {
        return port;
    }

    /**
     * Returns a free port number on localhost.
     * <p/>
     * Heavily inspired from org.eclipse.jdt.launching.SocketUtil (to avoid a dependency to JDT just because of this).
     * Slightly improved with close() missing in JDT. And throws exception instead of returning -1.
     *
     * @return a free port number on localhost
     * @throws IllegalStateException if unable to find a free port
     */
    private static int findFreePort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            socket.setReuseAddress(true);
            int port = socket.getLocalPort();
            try {
                socket.close();
            } catch (IOException ignored) {
                // Ignore IOException on close()
            }
            return port;
        } catch (IOException ignored) {
            // Ignore IOException on open
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                    // Ignore IOException on close()
                }
            }
        }
        throw new IllegalStateException("Could not find a free TCP/IP port");
    }
}
