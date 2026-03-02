package common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private final Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();
    private RequestLine requestLine;

    public HttpRequest(InputStream is) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            if (line == null) {
                return;
            }

            requestLine = new RequestLine(line);

            while(!"".equals(line)) {
                HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
                if (pair != null) {
                    headers.put(pair.getKey(), pair.getValue());
                }
                line = br.readLine();
            }

            if(getMethod().isPost()) {
                String contentLength = headers.get("Content-Length");
                String body = IOUtils.readData(br, Integer.parseInt(contentLength));
                params = HttpRequestUtils.parseQueryString(body);
            } else {
                params = requestLine.getParams();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public HttpMethod getMethod() {
        return requestLine.getMethod();
    }
    public String getPath() {
        return requestLine.getPath();
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public String getParameter(String key) {
        return params.get(key);
    }

    public String getCookie(String key) {
        String cookie = headers.get("Cookie");
        if(cookie == null) {
            return null;
        }
        Map<String, String> cookies = HttpRequestUtils.parseCookies(cookie);
        return cookies.get(key);
    }

    public boolean isLogin() {
        String logined = getCookie("logined");
        if(logined == null) {
            return false;
        }
        return Boolean.parseBoolean(logined);
    }
}
