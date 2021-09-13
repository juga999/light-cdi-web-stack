package juga999.lightcdiwebstack.impl.http.undertow;

import com.google.gson.Gson;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import juga999.lightcdiwebstack.meta.http.Context;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Optional;

public class ExchangeWrapper implements Context {

    private final Gson gson;

    private final HttpServerExchange exchange;

    private final PathTemplateMatch pathMatch;

    private final FormData attachment;

    private String user;

    public ExchangeWrapper(Gson gson, HttpServerExchange exchange) {
        this.gson = gson;
        this.exchange = exchange;
        this.pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        this.attachment = exchange.getAttachment(FormDataParser.FORM_DATA);
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public void setContentType(String type) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, type);
    }

    @Override
    public String getQueryString() {
        return exchange.getQueryString();
    }

    @Override
    public String getStringParameter(String key) {
        return pathMatch.getParameters().get(key);
    }

    @Override
    public Long getLongParameter(String key) {
        Long value = Optional.ofNullable(pathMatch.getParameters().get(key))
                .map(Long::valueOf)
                .orElse(null);
        return value;
    }

    @Override
    public Path getFormDataFile(String key) {
        FormData.FormValue fileValue = attachment.get(key).getFirst();
        return fileValue.getPath();
    }

    @Override
    public String getFormDataString(String key) {
        FormData.FormValue value = attachment.get(key).getFirst();
        return value.getValue();
    }

    @Override
    public <T> T getPostObject(Class<T> klass) throws IOException {
        T obj;
        try (InputStreamReader inputStreamReader = new InputStreamReader(exchange.getInputStream())) {
            obj = gson.fromJson(inputStreamReader, klass);
        }

        return obj;
    }
}
