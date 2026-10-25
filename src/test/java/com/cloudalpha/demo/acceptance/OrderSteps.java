package com.cloudalpha.demo.acceptance;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/** Step definitions for the order acceptance scenarios. */
public class OrderSteps {

    private Response response;
    private Integer orderId;

    @Before
    public void target() {
        RestAssured.baseURI = System.getProperty("base.url", "http://localhost:8080");
    }

    @Given("the order service is running")
    public void serviceRunning() {
        assertThat(given().get("/actuator/health").statusCode()).isEqualTo(200);
    }

    @When("a customer orders {int} of {string} at {double} each")
    public void placeOrder(int quantity, String item, double price) {
        response = given().contentType(ContentType.JSON)
                .body(String.format(java.util.Locale.ROOT, "{\"item\":\"%s\",\"quantity\":%d,\"unitPrice\":%.2f}", item, quantity, price))
                .post("/api/orders");
        orderId = response.statusCode() == 201 ? response.path("id") : null;
    }

    @When("the customer confirms the order")
    public void confirm() {
        response = given().post("/api/orders/" + orderId + "/confirm");
    }

    @When("the customer cancels the order")
    public void cancel() {
        response = given().post("/api/orders/" + orderId + "/cancel");
    }

    @Then("the order is accepted with a total of {double}")
    public void accepted(double total) {
        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(((Number) response.path("total")).doubleValue()).isEqualTo(total);
    }

    @Then("the order status is {string}")
    public void status(String status) {
        assertThat((String) given().get("/api/orders/" + orderId).path("status")).isEqualTo(status);
    }

    @Then("the order is refused")
    public void refused() {
        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Then("the change is refused")
    public void changeRefused() {
        assertThat(response.statusCode()).isEqualTo(409);
    }
}
