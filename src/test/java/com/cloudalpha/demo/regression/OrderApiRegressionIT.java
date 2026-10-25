package com.cloudalpha.demo.regression;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Stage 10, regression test: runs against a deployed environment.
 *
 *   mvn -Pregression verify -Dbase.url=https://app-dev-123.example
 */
class OrderApiRegressionIT {

    @BeforeAll
    static void target() {
        RestAssured.baseURI = System.getProperty("base.url", "http://localhost:8080");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void healthIsUp() {
        given().when().get("/actuator/health").then().statusCode(200).body("status", equalTo("UP"));
    }

    @Test
    void createReadConfirmAnOrder() {
        int id = given().contentType(ContentType.JSON).body("{\"item\":\"Regression item\",\"quantity\":3,\"unitPrice\":10.00}")
                .when().post("/api/orders")
                .then().statusCode(201).body("total", equalTo(30.0f)).body("id", notNullValue())
                .extract().path("id");

        given().when().get("/api/orders/" + id).then().statusCode(200).body("status", equalTo("NEW"));
        given().when().post("/api/orders/" + id + "/confirm").then().statusCode(200).body("status", equalTo("CONFIRMED"));
        given().when().post("/api/orders/" + id + "/cancel").then().statusCode(409);
    }

    @Test
    void discountForLargeOrders() {
        given().contentType(ContentType.JSON).body("{\"item\":\"Bulk\",\"quantity\":10,\"unitPrice\":100.00}")
                .when().post("/api/orders")
                .then().statusCode(201).body("total", equalTo(900.0f));
    }

    @Test
    void invalidInputIsRefused() {
        given().contentType(ContentType.JSON).body("{\"item\":\"\",\"quantity\":0,\"unitPrice\":0}")
                .when().post("/api/orders").then().statusCode(400);
        given().when().get("/api/orders/999999").then().statusCode(404);
    }
}
