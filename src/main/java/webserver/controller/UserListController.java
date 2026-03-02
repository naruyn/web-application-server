package webserver.controller;

import common.HttpRequest;
import common.HttpResponse;
import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class UserListController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(UserListController.class);

    @Override
    public void doGet(HttpRequest request, HttpResponse response) {
        if (!request.isLogin()) {
            response.sendRedirect("/user/login.html");
            return;
        }

        StringBuilder builder = new StringBuilder();
        Collection<User> users = DataBase.findAll();
        int count = 1;
        for (User user : users) {
            builder.append("<tr>");
            builder.append("<th>").append(count++).append("</th>");
            builder.append("<td>").append(user.getUserId()).append("</td>");
            builder.append("<td>").append(user.getName()).append("</td>");
            builder.append("<td>").append(user.getEmail()).append("</td>");
            builder.append("<td><a href=\"#\" class=\"btn btn-success\" role=\"button\">수정</a></td>");
            builder.append("</tr>");
        }
        response.forwardBody(builder.toString());
    }

    @Override
    public void doPost(HttpRequest request, HttpResponse response) {}
}
