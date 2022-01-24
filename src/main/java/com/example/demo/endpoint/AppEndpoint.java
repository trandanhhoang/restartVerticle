package com.example.demo.endpoint;

import com.example.demo.model.Employee;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;

import java.util.HashMap;

public class AppEndpoint extends AbstractVerticle {
    private final static int PORT = 8079;
    private EventBus bus;
    private final HashMap employees = new HashMap();

    public void createExampleData() {
        employees.put(1, new Employee(1, "Mr Obama", "Obama@gmail.com"));
        employees.put(2, new Employee(2, "Mr Donald Trump", "Trump@gmail.com"));
        employees.put(3, new Employee(3, "Mr Putin", "Putin@gmail.com"));
    }

    public void login(RoutingContext context, JWTAuth jwtAuth) {
        try {
            //lấy dữ liệu là 1 JsonObject ở client truyền lên
            JsonObject data = new JsonObject(context.getBody());
            System.out.println(data);

            System.out.println(data);
            //tạo token, hạn dùng 60 giây
            String token = jwtAuth.generateToken(new JsonObject(),
                    new JWTOptions().setExpiresInSeconds(60));
            //dùng cookie lưu xuống phía client, trong vòng 60 giây truy cấp được các API thoải mái
            Cookie cookie = Cookie.cookie("auth", token);
            cookie.setHttpOnly(true).setPath("/").encode();
            context.addCookie(cookie).response()
                    .putHeader("content-type", "text/plain")
                    .putHeader("Authorization", token)
                    .end(token);
        } catch (Exception ex) {
            System.err.println("Err while take JWT for client");
            context.response().setStatusCode(401)
                    .putHeader("content-type", "application/json")
                    .end(ex.getMessage());
        }
    }

    public JWTAuthHandler authHandler(JWTAuth jwtAuth) {
        return JWTAuthHandler.create(jwtAuth, "/api/login");
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        createExampleData();
        // this vertx in paramater is a reference of the VERT object in main
        Router router = Router.router(vertx);
        this.bus = vertx.eventBus();

        JWTAuthOptions config = new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setBuffer("keyboard cat"));

        JWTAuth jwt = JWTAuth.create(vertx, config);

        jwt.authenticate(new JsonObject().put("token", "BASE64-ENCODED-STRING"))
                .onSuccess(user -> System.out.println("User: " + user.principal()))
                .onFailure(err -> {
                    // Failed!
                    System.err.println("Authen failed");
                });

        // in order head to tail.
        router.route("/api*").handler(BodyHandler.create());
        router.post("/api/login").handler(ctx -> {
            login(ctx, jwt);
        });
        // all req with path below need auth
//        router.route("/api/*").handler(authHandler(jwt));

        router.get("/api/subjects").handler(this::getSubjectList);
        router.get("/api/employees").handler(this::getAllEmployees);

        router.route("/api/employees*").handler(BodyHandler.create());
        router.post("/api/employees").handler(this::insertNewEmployee);

        router.get("/secure/chat/:id").handler(this::getChatById);
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(config().getInteger("http.port", PORT),
                        result -> {
                            if (result.succeeded()) {
                                System.out.println("Created a server on port " + PORT);
                                startPromise.complete();
                            } else {
                                System.out.println("Can't start server");
                                startPromise.fail(result.cause());
                            }
                        }

                );
    }

    private void getChatById(RoutingContext ctx) {
        //Create a request
        String chatId = ctx.pathParam("id");
        JsonObject request = new JsonObject();
        request.put("chatId", chatId);

        //send request-reply to consumer
        bus.request("consumer.service.messages.rabbitMQ", request, res -> {
            if (res.succeeded()) {
                ctx.response().setStatusCode(200).end(Json.encode(res.result().body()));

//                ctx.response().setStatusCode(200).end();
            } else {
                System.out.println("Unable to send to rabbitMQ event bus:\n" + res.cause().getLocalizedMessage());
                ctx.response().setStatusCode(404).end();
            }
        });
    }

    private void insertNewEmployee(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json;charset=UTF8");
        try {
            //routingContext.getBody() lấy dữ liệu từ client gửi lên, nó có định dạng Json
            //Json.decodeValue(routingContext.getBody(),Employee.class);->đưa Json đó về Employee
            Employee emp = Json.decodeValue(routingContext.getBody(), Employee.class);
            //đưa vào HashMap
            employees.put(emp.getId(), emp);
            //xuất kết quả là true xuống cho client nếu lưu thành công
            response.end("true");
        } catch (Exception ex) {
            response.end(ex.getMessage());//lưu thất bại (khác true)
        }
    }

    private void getAllEmployees(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json;charset=UTF-8");
        response.end(Json.encodePrettily(employees.values()));
    }

    private void getSubjectList(RoutingContext routingContext) {
        JsonObject res = new JsonObject().put("hehe", "haha");
        routingContext.response()
                .putHeader("Content-Type", "application/json")
                .end(Json.encode(res));
    }

}
