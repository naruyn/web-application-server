package webserver;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            // ex) GET /index.html
            String readLine = reader.readLine();
            if (readLine == null) {
                return;
            }

            String[] splited = readLine.split(" ");
            String method = splited[0];
            String url = splited[1];

            if("GET".equals(method)) {
                if (url.startsWith("/user/create")) {
                    String query = url.substring(url.indexOf("?") + 1);
                    url = url.substring(0, url.indexOf("?"));

                    Map<String, String> params = HttpRequestUtils.parseQueryString(query);
                    User user = new User(
                            params.get("userId"),
                            params.get("password"),
                            params.get("name"),
                            params.get("email")
                    );

                    log.debug("New User created : {}", user);
                }
            }
            if("POST".equals(method)) {
                if (url.startsWith("/user/create")) {
                    Map<String, String> headers = new HashMap<>();
                    while (!"".equals(readLine)) {
                        if (readLine.split(": ").length == 2) {
                            headers.put(readLine.split(": ")[0], readLine.split(": ")[1]);
                        }
                        readLine = reader.readLine();
                    }

                    String contentLength = headers.get("Content-Length");
                    String requestBody = IOUtils.readData(reader, Integer.parseInt(contentLength));

                    Map<String, String> params = HttpRequestUtils.parseQueryString(requestBody);
                    User user = new User(
                            params.get("userId"),
                            params.get("password"),
                            params.get("name"),
                            params.get("email")
                    );

                    log.debug("New User created : {}", user);
                }
            }


            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
