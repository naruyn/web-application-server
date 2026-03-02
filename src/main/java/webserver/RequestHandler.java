package webserver;

import common.HttpRequest;
import common.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.inferface.Controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);

            Controller controller = RequestMapping.getController(request.getPath());
            if(controller == null) {
                response.forward(getDefaultPath(request.getPath()));
                return;
            }

            controller.service(request, response);

        } catch (Exception e) {
            log.error("request handler error", e);
        }
    }

    private String getDefaultPath(String path) {
        if("/".equals(path)) {
            return "index.html";
        }
        return path;
    }
}
