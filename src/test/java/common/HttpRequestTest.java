package common;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class HttpRequestTest {

    private static final String DIRECTORY = "./src/test/resources/";

    @Test
    public void request_Get() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(DIRECTORY + "Http_Get.txt"));

        HttpRequest httpRequest = new HttpRequest(fileInputStream);

        assertEquals("GET", httpRequest.getMethod());
        assertEquals("/user/create", httpRequest.getPath());
        assertEquals("keep-alive", httpRequest.getHeader("Connection"));
        assertEquals("user", httpRequest.getParameter("userId"));
    }

    @Test
    public void request_Post() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(DIRECTORY + "Http_Post.txt"));

        HttpRequest httpRequest = new HttpRequest(fileInputStream);
        assertEquals("POST", httpRequest.getMethod());
        assertEquals("/user/create", httpRequest.getPath());
        assertEquals("keep-alive", httpRequest.getHeader("Connection"));
        assertEquals("user", httpRequest.getParameter("userId"));
    }
}