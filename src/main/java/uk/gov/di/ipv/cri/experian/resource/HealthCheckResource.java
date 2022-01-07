package uk.gov.di.ipv.cri.experian.resource;

import spark.Request;
import spark.Response;
import spark.Route;

import javax.servlet.http.HttpServletResponse;

public class HealthCheckResource {
    public final Route getCurrentHealth =
            (Request request, Response response) -> {
                response.status(HttpServletResponse.SC_OK);
                response.type("application/json;charset=UTF-8");
                response.body("{\"experian=api\": \"ok\"}");
                return response.body();
            };
}
