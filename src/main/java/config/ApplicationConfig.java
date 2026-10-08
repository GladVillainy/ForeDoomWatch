package config;

import exceptions.*;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routes.Routes;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ApplicationConfig {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    private final AtomicInteger count = new AtomicInteger(1);
    private final Routes routes;
    private Javalin app;

    public ApplicationConfig(Routes routes) {
        this.routes = routes;
    }

    private void configuration(JavalinConfig config) {
        config.showJavalinBanner = false;
        config.router.contextPath = "/api";
        config.bundledPlugins.enableRouteOverview("/routes");
        config.router.apiBuilder(routes.getRoutes());
    }

    public Javalin startServer(int port) {
        app = Javalin.create(this::configuration);

        app.after(this::afterRequest);

        app.exception(ApiException.class, (e, ctx) -> handleError(e.getCode(), e.getMessage(), ctx));
        app.exception(DatabaseException.class, (e, ctx) -> handleError(e.getCode(), e.getMessage(), ctx));
        app.exception(MissingInputException.class, (e, ctx) -> handleError(e.getCode(), e.getMessage(), ctx));
        app.exception(EntityException.class, (e, ctx) -> handleError(e.getCode(), e.getMessage(), ctx));

        app.exception(Exception.class, this::generalExceptionHandler);

        app.start(port);
        return app;
    }

    public void stopServer() {
        if (app != null) {
            app.stop();
        }
    }

    private void afterRequest(Context ctx) {
        String requestInfo = ctx.req().getMethod() + " " + ctx.req().getRequestURI();
        logger.info("Request {} - {} was handled with status code {}",
                count.getAndIncrement(), requestInfo, ctx.status());
    }

    private void generalExceptionHandler(Exception e, Context ctx) {
        logger.error("An unhandled exception occurred", e);
        handleError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", ctx);
    }

    private void handleError(HttpStatus status, String message, Context ctx) {
        logger.warn("Error {}: {}", status.getCode(), message);
        ctx.status(status);
        ctx.json(Map.of("status", status.getCode(), "msg", message));
    }
}