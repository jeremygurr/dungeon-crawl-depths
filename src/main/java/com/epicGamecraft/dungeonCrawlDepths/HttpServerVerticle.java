package com.epicGamecraft.dungeonCrawlDepths;

import com.ple.util.IArrayMap;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.annotations.Nullable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.Session;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.SessionHandler;
import io.vertx.reactivex.ext.web.sstore.LocalSessionStore;
import io.vertx.reactivex.ext.web.sstore.SessionStore;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import com.ple.observabilityBridge.RecordingService;

public class HttpServerVerticle extends AbstractVerticle {

  private final RecordingService startupRecordingService;

  public HttpServerVerticle(RecordingService startupRecordingService) {
    this.startupRecordingService = startupRecordingService;
  }

  @Override
  public Completable rxStart() {

    final IArrayMap<String, Object> verticleDim = IArrayMap.make("verticle", "HttpServerVerticle");
    startupRecordingService.open("Deploying", verticleDim);

    final Router router = Router.router(vertx);
    final SessionStore store = LocalSessionStore.create(vertx);
    final SessionHandler mySesh = SessionHandler.create(store);
    mySesh.setSessionTimeout(86400000); //24 hours in milliseconds.

    router.route().handler(this::routeStartHandler);
    router.get("/status").handler(this::statusHandler);
    router.route().handler(mySesh);
    router.get("/static/*").handler(this::staticHandler);
    router.route().handler(BodyHandler.create());
    router.post("/bus/*").handler(this::busHandler);
    router.route().handler(this::routeEndHandler);

    final HttpServer server = vertx.createHttpServer();
    final int port = 8080;

    final Single<HttpServer> rxListen = server
      .requestHandler(router)
      .rxListen(port)
      .doOnSuccess(e -> { startupRecordingService.log("HTTP server running", "port", port); })
      .doOnError(e -> {  startupRecordingService.log(-10, "Could not start HTTP server", "exception", e); })
      .doFinally(() -> { startupRecordingService.close("Deploying", verticleDim); });

    return rxListen.ignoreElement();

  }

  private void routeStartHandler(RoutingContext context) {
    final HttpServerRequest request = context.request();
    @Nullable final String path = request.path();
    final RecordingService rs = startupRecordingService.clone();
    rs.open("http request", "path", path);
    context.data().put("rs", rs);
    context.next();
  }

  private void routeEndHandler(RoutingContext context) {

    final HttpServerRequest request = context.request();
    HttpServerResponse response = context.response();
    @Nullable final String path = request.path();
    final RecordingService rs = (RecordingService) context.data().get("rs");

    if (!response.ended()) {
      response.setStatusCode(404);
      response.end();
    }

    int code = response.getStatusCode();
    rs.close("http request", "path", path, "statusCode", code);

  }

  public void writeStaticHtml(RecordingService rs, HttpServerResponse response, String path) {

    rs.log("Fetching file", "path", path);
    path = path.substring(1);
    final InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    if (stream != null) {
      final String text = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
        .lines()
        .collect(Collectors.joining("\n"));
      if (path.endsWith(".html")) {
        response.putHeader("Content-Type", "text/html");
      } else if (path.endsWith(".css")) {
        response.putHeader("Content-Type", "text/css");
      } else if (path.endsWith(".js")) {
        response.putHeader("Content-Type", "text/javascript");
      } else {
        rs.log(-5, "Filetype unknown", "path", path);
      }
      response.setStatusCode(200);
      response.end(text);
    } else {
      rs.log(-5, "Resource not found: " + path);
      response.setStatusCode(404);
      response.end();
    }
  }

  private void statusHandler(RoutingContext context) {

    final HttpServerResponse response = context.response();
    response.putHeader("Content-Type", "text/html");
    response.setStatusCode(200);
    response.end("<html><body>All is well</body></html>");

    // we are not doing context.next() here because we don't want to create a session for every health check
    routeEndHandler(context);

  }

  private void staticHandler(RoutingContext context) {

    final RecordingService rs = (RecordingService) context.data().get("rs");
    final HttpServerResponse response = context.response();
    final HttpServerRequest request = context.request();
    @Nullable String path = request.path();

    try {

      response.setChunked(true);
      rs.open("Static handler");

      Session session = context.session();
      String username = session.get(SessionKey.username.name());
      if (username != null && path.equals("/static/login.html") || username != null && path.equals("/static/jscrawl.html")) {
        WebUtils.redirect(response, "/static/jscrawl.html");
        rs.log("Rediracting to crawl page");
        rs.close("Static handler");
        return;
      }
      writeStaticHtml(rs, response, path);

    } catch (Exception e) {

      rs.log(-10, "Problem fetching static file", "path", path, "exception", e);
      response.setStatusCode(502);
      response.end();

    }

    rs.close("Static handler");
    context.next();

  }


  private void busHandler(RoutingContext context) {

    final RecordingService rs = (RecordingService) context.data().get("rs");
    rs.open("busHandler");
    try {
      final EventBus eb = vertx.eventBus();

      final HttpServerRequest request = context.request();
      final HttpServerResponse response = context.response();
      final MultiMap params = request.params();
      response.setChunked(true);

      final String absoluteURI = request.absoluteURI();
      rs.log(10, "debug", "absoluteURI", absoluteURI);
      final String busAddress = absoluteURI.replaceAll("^.*/bus/", "");
      rs.log(10, "debug", "busAddress", busAddress);
      Session session = context.session();
      JsonObject object = new JsonObject();
      for (Map.Entry<String, String> entry : params.entries()) {
        object.put(entry.getKey(), entry.getValue());
      }
      eb.rxRequest(busAddress, object.encode())
        .subscribe(e -> {
            final JsonObject json = JsonObject.mapFrom(e.body());
            final String message = json.getString("response");
            final String redirect = json.getString("redirect");
            rs.log("HttpServer Verticle Received reply: " + e.body());

            if (redirect.equals("login.html")) {
              // redirect to login page.
/* TODO: Figure out why this isn't working.
              String html = WebUtils.generateHtml("/home/jaredgurr/training-crawl/03_java_backend/src/main/resources/static/login.html", message);
              response.write(html);
              writeStaticHtml(response, "/static/login.html");
              R.debug("message =" + message);
              R.debug("html = " + html);
*/
            } else if (redirect.equals("jscrawl.html")) {
              session.put(SessionKey.username.name(), object.getString("username"));
              rs.log(5, "session equals: " + session.data());
            }
            WebUtils.redirect(response, "/static/" + redirect);
          },
          err -> {
            rs.log(-10, "Failed login : " + err.getMessage());
            if (err.getMessage() == null) {
              rs.log(-10, "Error with busAddress " + busAddress + " " + err.getMessage());
              WebUtils.redirect(response, "/static/serverError.html");
            }
          });
    } catch (Exception e) {
      rs.log(-10, "Exception in bus handler", "exception", e);
    } finally {
      rs.close("busHandler");
    }

    context.next();

  }

}
