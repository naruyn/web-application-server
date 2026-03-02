package webserver.inferface;

import common.HttpRequest;
import common.HttpResponse;

public interface Controller {
    void service(HttpRequest request, HttpResponse response);
}
