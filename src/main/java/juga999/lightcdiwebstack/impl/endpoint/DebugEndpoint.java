package juga999.lightcdiwebstack.impl.endpoint;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import juga999.lightcdiwebstack.meta.http.AppEndpoint;
import juga999.lightcdiwebstack.meta.http.BlobProducer;
import juga999.lightcdiwebstack.meta.http.Context;
import juga999.lightcdiwebstack.meta.http.Get;
import juga999.lightcdiwebstack.meta.http.JsonConsumer;
import juga999.lightcdiwebstack.meta.http.JsonProducer;
import juga999.lightcdiwebstack.meta.http.Post;
import juga999.lightcdiwebstack.meta.http.RequiredPermissions;

import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class DebugEndpoint extends AppEndpoint {

    public static final class Message {
        private String text = "";

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    @Get("/debug/ping/{name}")
    @JsonProducer
    public Object pingForGet(Context context) {
        String name = context.getStringParameter("name");
        Map<String, String> data = ImmutableMap.of("message", "Hello " + name);
        return data;
    }

    @Post("/debug/ping/")
    @JsonConsumer
    @JsonProducer
    public Object pingForPost(Context context) throws Exception {
        Message message = context.getPostObject(Message.class);
        String text = Optional.ofNullable(context.getUser())
                .map(u -> u + ": " + message.getText())
                .orElse(message.getText());
        Map<String, String> data = ImmutableMap.of("message", text);
        return data;
    }

    @Post("/debug/private/ping/")
    @JsonConsumer
    @JsonProducer
    @RequiredPermissions({"CAN_ADMIN"})
    public Object pingForPostWithPermissions(Context context) throws Exception {
        Message message = context.getPostObject(Message.class);
        String user = context.getUser();
        Map<String, String> data = ImmutableMap.of("message", user + ": " + message.getText());
        return data;
    }

    @Post("/debug/upload")
    @JsonProducer
    public Object upload(Context context) throws Exception {
        Map<String, String> data;
        Path filePath = context.getFormDataFile("file");
        String name = context.getFormDataString("name");
        Gson gson = new Gson();
        try(FileReader fileReader = new FileReader(filePath.toFile())) {
            Message message = gson.fromJson(fileReader, Message.class);
            data = ImmutableMap.of("message", message.getText() + " " + name);
        }
        return data;
    }

    @Get("/debug/download")
    @BlobProducer
    public Object download(Context context) throws Exception {
        String data = "This is a test";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        return inputStream;
    }

    @Get("/debug/npe")
    public Object generateNpe(Context context) {
        throw new NullPointerException();
    }

}
