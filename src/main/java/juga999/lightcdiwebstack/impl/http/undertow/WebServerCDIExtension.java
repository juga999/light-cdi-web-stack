package juga999.lightcdiwebstack.impl.http.undertow;

import com.google.common.collect.Lists;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.cache.DirectBufferCache;
import io.undertow.server.handlers.form.EagerFormParsingHandler;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import io.undertow.server.handlers.resource.CachingResourceManager;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBean;
import juga999.lightcdiwebstack.AppConfig;
import juga999.lightcdiwebstack.meta.auth.AuthenticationProvider;
import juga999.lightcdiwebstack.meta.http.AppEndpoint;
import juga999.lightcdiwebstack.meta.http.Get;
import juga999.lightcdiwebstack.meta.http.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class WebServerCDIExtension implements Extension {

    private static final Logger logger = LoggerFactory.getLogger(WebServerCDIExtension.class);

    private static final WebServerCDIExtension instance = new WebServerCDIExtension();

    private List<Bean<?>> endpointBeans = Lists.newArrayList();

    private Undertow.Builder httpServerBuilder = Undertow.builder();

    private Undertow httpServer = null;

    private Bean<?> appConfigBean = null;

    private Bean<?> authenticationProviderBean = null;

    private AppConfig appConfig = null;

    private AuthenticationProvider authenticationProvider = null;

    public static WebServerCDIExtension get() {
        return instance;
    }

    private WebServerCDIExtension() {
    }

    public void startHttpServer(int port) {
        httpServer = httpServerBuilder
                .addHttpListener(port, "localhost")
                .build();
        httpServer.start();
        logger.info("Http server running on port " + port);
    }

    public void stopHttpServer() {
        httpServer.stop();
        httpServer = null;
    }

    public void reset() {
        endpointBeans.clear();
        httpServerBuilder = Undertow.builder();
        httpServer = null;
    }

    @SuppressWarnings("unused")
    <T> void collectBeans(@Observes ProcessBean<T> event) {
        if (AppEndpoint.class.isAssignableFrom(event.getBean().getBeanClass())) {
            endpointBeans.add(event.getBean());
        } else if (AuthenticationProvider.class.isAssignableFrom(event.getBean().getBeanClass())) {
            authenticationProviderBean = event.getBean();
        } else if (AppConfig.class.isAssignableFrom(event.getBean().getBeanClass())) {
            appConfigBean = event.getBean();
        }
    }

    @SuppressWarnings("unused")
    void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
        appConfig = Optional.ofNullable(appConfigBean)
                .map(b -> (AppConfig) beanManager.getReference(
                        b, b.getBeanClass(), beanManager.createCreationalContext(b)))
                .orElse(null);
        authenticationProvider = Optional.ofNullable(authenticationProviderBean)
                .map(b -> (AuthenticationProvider) beanManager.getReference(
                        b, b.getBeanClass(), beanManager.createCreationalContext(b)))
                .orElse(null);

        PathHandler pathHandler = Handlers.path();
        registerApiEndpoints(pathHandler, beanManager);

        ResourceHandler resourceHandler = registerStaticResources(pathHandler);

        httpServerBuilder.setHandler(new EagerFormParsingHandler(
                FormParserFactory.builder()
                        .addParsers(new MultiPartParserDefinition())
                        .build())
                .setNext(resourceHandler));
    }

    protected ResourceHandler registerStaticResources(HttpHandler next) {
        ResourceManager classPathManager = new ClassPathResourceManager(
                WebServerCDIExtension.class.getClassLoader(), "");
        ResourceManager resourceManager =
                new CachingResourceManager(100, 65536,
                        new DirectBufferCache(1024, 10, 10480),
                        classPathManager,
                        3600);
        ResourceHandler handler = new ResourceHandler(resourceManager, next);
        return handler;
    }

    protected PathHandler registerApiEndpoints(PathHandler pathHandler, BeanManager beanManager) {
        RoutingHandler routingHandler = Handlers.routing();

        for (Bean<?> endpointBean : endpointBeans) {
            AppEndpoint endpoint = (AppEndpoint)beanManager.getReference(endpointBean,
                    endpointBean.getBeanClass(), beanManager.createCreationalContext(endpointBean));
            for (Method method : endpoint.getClass().getDeclaredMethods()) {
                String path = null;
                HttpString httpString = null;
                if (method.isAnnotationPresent(Get.class)) {
                    path = method.getAnnotation(Get.class).value();
                    httpString = Methods.GET;
                } else if (method.isAnnotationPresent(Post.class)) {
                    path = method.getAnnotation(Post.class).value();
                    httpString = Methods.POST;
                }
                if (path != null) {
                    HttpHandler httpHandler = new AppEndpointHandler(authenticationProvider, endpoint, method, path);
                    routingHandler.add(httpString, path, new BlockingHandler(httpHandler));
                    logger.info("Registered {} [{}]", path, httpString.toString());
                }
            }
        }

        return pathHandler.addPrefixPath("/api", routingHandler);
    }

}
