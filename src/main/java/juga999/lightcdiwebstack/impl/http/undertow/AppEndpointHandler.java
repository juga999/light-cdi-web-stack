package juga999.lightcdiwebstack.impl.http.undertow;

import com.auth0.jwt.interfaces.Payload;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.prometheus.client.Histogram;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import juga999.lightcdiwebstack.impl.json.LocalDateTimeAdapter;
import juga999.lightcdiwebstack.impl.metrics.prometheus.Metrics;
import juga999.lightcdiwebstack.meta.auth.AuthenticationProvider;
import juga999.lightcdiwebstack.meta.auth.UnauthorizedException;
import juga999.lightcdiwebstack.meta.http.AppEndpoint;
import juga999.lightcdiwebstack.meta.http.BlobProducer;
import juga999.lightcdiwebstack.meta.http.JsonProducer;
import juga999.lightcdiwebstack.meta.http.RequiredPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AppEndpointHandler implements HttpHandler {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private final Logger logger;

    private final AuthenticationProvider authenticationProvider;

    private final AppEndpoint endpoint;

    private final Method method;

    private final String path;

    private final Set<String> requiredPermissions;

    private final boolean isJsonProducer;

    private final boolean isBlobProducer;

    public AppEndpointHandler(AuthenticationProvider authenticationProvider, AppEndpoint endpoint, Method method, String path) {
        this.logger = LoggerFactory.getLogger(endpoint.getClass());
        this.authenticationProvider = authenticationProvider;
        this.endpoint = endpoint;
        this.method = method;
        this.path = path;

        requiredPermissions = getRequiredPermissions(method);

        isJsonProducer = method.isAnnotationPresent(JsonProducer.class);
        isBlobProducer = method.isAnnotationPresent(BlobProducer.class);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Metrics.incHttpRequestsCount();
        Histogram.Timer timer = Metrics.startHttpDuration(path);
        ExchangeWrapper exchangeWrapper = new ExchangeWrapper(GSON, exchange);
        String user = readAuthorization(exchange);
        exchangeWrapper.setUser(user);
        try {
            if (requiredPermissions.size() > 0) {
                authenticationProvider.checkPermissions(user, requiredPermissions);
            }
            Object data = method.invoke(endpoint, exchangeWrapper);
            if (isJsonProducer) {
                exchangeWrapper.setContentType("application/json");
                exchange.getResponseSender().send(GSON.toJson(data));
            } else if (isBlobProducer) {
                exchangeWrapper.setContentType("application/octet-stream");
                OutputStream outputStream = exchange.getOutputStream();
                InputStream inputStream = (InputStream)data;
                ByteStreams.copy(inputStream, outputStream);
                outputStream.flush();
                inputStream.close();
            } else {
                exchange.getResponseSender().send((String)data);
            }

        } catch (UnauthorizedException e) {
            handleException(exchange, new InvocationTargetException(e));
        } catch (InvocationTargetException e) {
            handleException(exchange, e);
        } finally {
            double duration = timer.observeDuration();
            String logMsg = Optional.ofNullable(user)
                    .map(u -> "{}|{}|{}s|" + u)
                    .orElse("{}|{}|{}s");
            logger.info(logMsg, exchange.getRequestMethod().toString(), path, duration);
        }
    }

    private Set<String> getRequiredPermissions(Method method) {
        if (method.isAnnotationPresent(RequiredPermissions.class)) {
            Set<String> requiredPermissions = Sets.newHashSet(method.getAnnotation(RequiredPermissions.class).value());
            requiredPermissions.forEach(authenticationProvider::validatePermission);
            return requiredPermissions;
        } else {
            return Collections.emptySet();
        }
    }

    private void handleException(HttpServerExchange exchange, InvocationTargetException e) {
        Map<String, Object> data;

        if (e.getCause() instanceof UnauthorizedException) {
            exchange.setStatusCode(HttpURLConnection.HTTP_UNAUTHORIZED);
            data = ImmutableMap.of("message", "unauthorized");
        } else {
            exchange.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
            Throwable cause = e.getCause();
            String message = Optional.ofNullable(cause.getMessage()).orElse("");
            data = ImmutableMap.of(
                    "exception", cause.toString(),
                    "message", message,
                    "stack", cause.getStackTrace());
            logger.error(message, cause);
        }

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        String response = GSON.toJson(data);
        exchange.getResponseSender().send(response);
    }

    private String readAuthorization(HttpServerExchange exchange) {
        String authHeader = Optional.ofNullable(exchange.getRequestHeaders().get("Authorization"))
                .map(HeaderValues::getFirst)
                .orElse("");
        if (authHeader.startsWith("Bearer")) {
            String token = authHeader.split(" ")[1];
            Payload payload = authenticationProvider.decodeToken(token);
            if (payload != null) {
                String user = payload.getSubject();
                exchange.putAttachment(AttachmentKey.create(String.class), user);
                return user;
            }
        }
        return null;
    }

}
