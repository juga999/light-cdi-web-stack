package juga999.lightcdiwebstack.meta.http;

import java.io.IOException;
import java.nio.file.Path;

public interface Context {
    String getUser();

    void setContentType(String type);

    String getQueryString();

    String getStringParameter(String key);

    Long getLongParameter(String key);

    <T> T getPostObject(Class<T> klass) throws IOException;

    Path getFormDataFile(String key);

    String getFormDataString(String key);
}
