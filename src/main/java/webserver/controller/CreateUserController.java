package webserver.controller;

import common.HttpRequest;
import common.HttpResponse;
import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;

public class CreateUserController extends AbstractController {
    private static final Logger logger = LoggerFactory.getLogger(CreateUserController.class);

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {}

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {
        try {
            User user = new User(
                    request.getParameter("userId"),
                    request.getParameter("password"),
                    request.getParameter("name"),
                    URLDecoder.decode(request.getParameter("email"), "UTF-8")
            );

            DataBase.addUser(user);
            response.sendRedirect("/index.html");
        } catch (Exception e) {
            logger.error("create user error", e);
        }
    }
}
