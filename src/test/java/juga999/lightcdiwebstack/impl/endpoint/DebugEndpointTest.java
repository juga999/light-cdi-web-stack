package juga999.lightcdiwebstack.impl.endpoint;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import jakarta.inject.Inject;
import juga999.lightcdiwebstack.impl.service.AuthService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class DebugEndpointTest extends EndpointTest {

    @Rule
    public final TestRule chain = getRuleChainWithHttpServer();

    @Inject
    protected AuthService authService;

    public DebugEndpointTest() {
        super(Lists.newArrayList(DebugEndpoint.class));
    }

    @Test
    public void testPingForGet() {
        given().port(getPort())
                .expect()
                .body("message", is("Hello john"))
                .when()
                .get("/api/debug/ping/john");
    }

    @Test
    public void testPingForPost() {
        given().port(getPort())
                .body(ImmutableMap.of("text", "This is a message"))
                .expect()
                .body("message", is("This is a message"))
                .when()
                .post("/api/debug/ping/");
    }

    @Test
    public void testPingForPostAuthenticated() {
        String token = authService.getToken("admin", "pwd");
        Assert.assertNotNull(token);

        given().port(getPort())
                .header("Authorization", "Bearer " + token)
                .body(ImmutableMap.of("text", "This is a message"))
                .expect()
                .body("message", is("admin: This is a message"))
                .when()
                .post("/api/debug/ping/");
    }

    @Test
    public void testPingForPostWithValidPermissions() {
        String token = authService.getToken("admin", "pwd");
        Assert.assertNotNull(token);

        given().port(getPort())
                .header("Authorization", "Bearer " + token)
                .body(ImmutableMap.of("text", "This is a message"))
                .expect()
                .body("message", is("admin: This is a message"))
                .when()
                .post("/api/debug/private/ping/");
    }

    @Test
    public void testPingForPostWithInvalidPermissions() {
        String token = authService.getToken("guest", "pwd");
        Assert.assertNotNull(token);

        given().port(getPort())
                .header("Authorization", "Bearer " + token)
                .body(ImmutableMap.of("text", "This is a message"))
                .expect()
                .statusCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .body("message", is("unauthorized"))
                .when()
                .post("/api/debug/private/ping/");
    }

    @Test
    public void testUpload() throws Exception {
        Path tempFile = Files.createTempFile("chorem-", "-import");

        DebugEndpoint.Message message = new DebugEndpoint.Message();
        message.setText("This is a message in the file");
        try (FileWriter fileWriter = new FileWriter(tempFile.toFile())) {
            Gson gson = new Gson();
            gson.toJson(message, fileWriter);
        }

        given().port(getPort())
                .multiPart("file", tempFile.toFile())
                .multiPart("name", "MyFile")
                .expect()
                .body("message", is("This is a message in the file MyFile"))
                .when()
                .post("/api/debug/upload");
    }

    @Test
    public void testDownload() throws Exception {
        byte[] bytes = given().port(getPort())
                .when()
                .get("/api/debug/download")
                .asByteArray();
        String message = new String(bytes);
        Assertions.assertThat(message).isEqualTo("This is a test");
    }

    @Test
    public void testNpe() {
        given().port(getPort())
                .expect()
                .statusCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .body("exception", is("java.lang.NullPointerException"))
                .when()
                .get("/api/debug/npe");
    }

}
