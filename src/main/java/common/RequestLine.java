package common;

import util.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

public class RequestLine {

    private final HttpMethod method;
    private String path;
    private Map<String, String> params = new HashMap<>();

    public RequestLine(String requestLine) {

        String[] tokens = requestLine.split(" ");
        if (tokens.length != 3) {
            throw new IllegalArgumentException("잘못된 요청 형식입니다");
        }

        this.method = HttpMethod.valueOf(tokens[0]);
        this.path = tokens[1];
        if (method.isPost()) {
            return;
        }

        if (path.contains("?")) {
            path = tokens[1].substring(0, tokens[1].indexOf("?"));
            String query = tokens[1].substring(tokens[1].indexOf("?") + 1);
            params = HttpRequestUtils.parseQueryString(query);
        }
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
