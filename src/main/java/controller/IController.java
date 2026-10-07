package controller;

import io.javalin.http.Context;

public interface IController{
    void getAll(Context ctx);
    void getById(Context ctx);
    void create(Context ctx);
    void update(Context ctx);
    void delete(Context ctx);

    default Long getLongId(Context ctx) {
        return ctx.pathParamAsClass("id", Long.class).get();
    }

    default String getStringId(Context ctx) {
        return ctx.pathParamAsClass("id", String.class).get();
    }
}