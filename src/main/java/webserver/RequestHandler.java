package webserver;

import db.DataBase;
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
            if (url.startsWith("/.well-known")) {
                return;
            }

            Boolean login = null;
            if("POST".equals(method)) {
                if (url.equals("/user/create")) {
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
                    DataBase.addUser(user);
                    log.debug("database : {}", DataBase.findAll());
                }
                if (url.equals("/user/login")) {
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
                    User user = DataBase.findUserById(params.get("userId"));
                    if(user == null) {
                        return;
                    }
                    if(user.getPassword().equals(params.get("password"))) {
                        login = true;
                    } else {
                        login = false;
                    }
                    url = "/user/login.html";
                }
            }

            DataOutputStream dos = new DataOutputStream(out);
            if (url.equals("/user/create")) {
                response302Header(dos, "http://localhost:8080/index.html");
            } else {
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                response200Header(dos, body.length, login);
                responseBody(dos, body);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, Boolean login) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            if (login != null) {
                dos.writeBytes("Set-Cookie: logined=" + login + "\r\n");
            }
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String redirectUrl) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
            dos.writeBytes("Location: " + redirectUrl + "\r\n");
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
