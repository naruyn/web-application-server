package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            String url = splited[1];
            if (url.startsWith("/.well-known")) {
                return;
            }

            Map<String, String> headers = getHeaders(reader);

            boolean isRedirect = false;
            Boolean login = null;
            if (url.equals("/user/create")) {
                String contentLength = headers.get("Content-Length");
                String requestBody = IOUtils.readData(reader, Integer.parseInt(contentLength));

                Map<String, String> params = HttpRequestUtils.parseQueryString(requestBody);
                User user = new User(
                        params.get("userId"),
                        params.get("password"),
                        params.get("name"),
                        URLDecoder.decode(params.get("email"), "UTF-8")
                );


                log.debug("New User created : {}", user);
                DataBase.addUser(user);
                isRedirect = true;
            }
            if (url.equals("/user/login")) {
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
            if(url.startsWith("/user/list.html")) {
                String cookie = headers.get("Cookie");

                Map<String, String> cookies = HttpRequestUtils.parseCookies(cookie);
                String logined = cookies.get("logined");
                if(!Boolean.parseBoolean(logined)) {
                    isRedirect = true;
                }
            }

            DataOutputStream dos = new DataOutputStream(out);
            if (isRedirect) {
                response302Header(dos, "http://localhost:8080/index.html");
            } else {
                byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
                if (url.startsWith("/css")) {
                    responseCss200Header(dos, body.length);
                } else if(url.startsWith("/user/list.html")) {
                    String html = new String(body, StandardCharsets.UTF_8);
                    Pattern pattern = Pattern.compile("(?is)<tbody[^>]*>(.*?)</tbody>");
                    Matcher matcher = pattern.matcher(html);

                    if(matcher.find()) {
                        String group = matcher.group(1);
                        StringBuilder builder = new StringBuilder();
                        Collection<User> users = DataBase.findAll();
                        int count = 1;
                        for(User user : users) {
                            builder.append("<tr>");
                            builder.append("<th>").append(count++).append("</th>");
                            builder.append("<td>").append(user.getUserId()).append("</td>");
                            builder.append("<td>").append(user.getName()).append("</td>");
                            builder.append("<td>").append(user.getEmail()).append("</td>");
                            builder.append("<td><a href=\"#\" class=\"btn btn-success\" role=\"button\">수정</a></td>");
                            builder.append("</tr>");
                        }
                        body = html.replaceAll(group, builder.toString()).getBytes(StandardCharsets.UTF_8);
                    }
                    response200Header(dos, body.length, login);
                } else {
                    response200Header(dos, body.length, login);
                }
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

    private void responseCss200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
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

    private HashMap<String, String> getHeaders(BufferedReader reader) throws IOException {
        HashMap<String, String> headers = new HashMap<>();
        String readLine = reader.readLine();
        while (!"".equals(readLine)) {
            if (readLine.split(": ").length == 2) {
                headers.put(readLine.split(": ")[0], readLine.split(": ")[1]);
            }
            readLine = reader.readLine();
        }
        return headers;
    }
}
