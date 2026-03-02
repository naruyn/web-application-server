package common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RequestLineTest {

    private static final String GET_REQUEST = "GET /user/create?userId=user&password=userpass&name=name&email@mail.com HTTP/1.1";
    private static final String GET_REQUEST_NO_PARAM = "GET /user/create HTTP/1.1";
    private static final String POST_REQUEST = "POST /user/create HTTP/1.1";

    @Test
    public void test_Get_request() {

        RequestLine requestLine = new RequestLine(GET_REQUEST);
        assertEquals("GET", requestLine.getMethod().name());
        assertEquals("/user/create", requestLine.getPath());
        assertEquals("user", requestLine.getParams().get("userId"));
    }

    @Test
    public void test_Get_request_no_param() {
        RequestLine requestLine = new RequestLine(GET_REQUEST_NO_PARAM);

        assertEquals("GET", requestLine.getMethod().name());
        assertEquals("/user/create", requestLine.getPath());
        assertNull(requestLine.getParams().get("userId"));
    }

    @Test
    public void test_Post_request() {
        RequestLine requestLine = new RequestLine(POST_REQUEST);
        assertEquals("POST", requestLine.getMethod().name());
        assertEquals("/user/create", requestLine.getPath());
    }
}