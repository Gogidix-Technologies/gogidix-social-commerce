import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class SocialCommerceLoadTest extends Simulation {
  
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .userAgentHeader("Gatling/3.8.3")
    .connectionHeader("keep-alive")
  
  // Test data
  val productIds = (1 to 1000).map(_ => s"prod_${scala.util.Random.nextString(10)}")
  val userIds = (1 to 500).map(_ => s"user_${scala.util.Random.nextString(10)}")
  
  // Scenarios
  val productSearch = scenario("Product Search")
    .during(2.minutes) {
      exec(
        http("Search Products")
          .get("/api/products/search")
          .queryParam("query", "electronics")
          .queryParam("page", session => session("page").asOption[Int].getOrElse(scala.util.Random.nextInt(10)))
          .queryParam("size", "20")
          .check(status.is(200))
          .check(jsonPath("$.content").exists)
          .check(responseTimeInMillis.lte(100))
      )
      .pause(500.milliseconds, 2.seconds)
    }
  
  val orderCreation = scenario("Order Creation")
    .feed(Iterator.continually(Map(
      "userId" -> userIds(scala.util.Random.nextInt(userIds.length)),
      "productId" -> productIds(scala.util.Random.nextInt(productIds.length)),
      "quantity" -> scala.util.Random.nextInt(5) + 1,
      "unitPrice" -> scala.util.Random.nextInt(100) + 10
    )))
    .exec(
      http("Create Order")
        .post("/api/orders")
        .body(StringBody(session => s"""{
          "userId": "${session("userId").as[String]}",
          "items": [{
            "productId": "${session("productId").as[String]}",
            "quantity": ${session("quantity").as[Int]},
            "unitPrice": ${session("unitPrice").as[Int]}
          }],
          "shippingAddress": {
            "street": "123 Test St",
            "city": "Test City",
            "country": "Test Country",
            "postalCode": "12345"
          },
          "paymentMethodId": "pm_test_${scala.util.Random.nextString(10)}"
        }"""))
        .check(status.is(201))
        .check(jsonPath("$.orderId").saveAs("orderId"))
        .check(responseTimeInMillis.lte(500))
    )
    .pause(1.second, 5.seconds)
  
  val paymentProcessing = scenario("Payment Processing")
    .feed(Iterator.continually(Map(
      "orderId" -> s"order_${scala.util.Random.nextString(10)}",
      "amount" -> scala.util.Random.nextInt(450) + 50,
      "paymentMethodId" -> s"pm_${scala.util.Random.nextString(10)}"
    )))
    .exec(
      http("Process Payment")
        .post("/api/payments/process")
        .body(StringBody(session => s"""{
          "orderId": "${session("orderId").as[String]}",
          "amount": ${session("amount").as[Int]},
          "currency": "USD",
          "paymentMethodId": "${session("paymentMethodId").as[String]}",
          "cvv": "123"
        }"""))
        .check(status.in(200, 201))
        .check(jsonPath("$.status").in("COMPLETED", "PROCESSING"))
        .check(responseTimeInMillis.lte(1000))
    )
    .pause(2.seconds, 10.seconds)
  
  val userRegistration = scenario("User Registration")
    .feed(Iterator.continually(Map(
      "email" -> s"test${scala.util.Random.nextString(5)}@example.com",
      "name" -> s"Test User ${scala.util.Random.nextString(5)}"
    )))
    .exec(
      http("Register User")
        .post("/api/users/register")
        .body(StringBody(session => s"""{
          "email": "${session("email").as[String]}",
          "name": "${session("name").as[String]}",
          "password": "testPassword123",
          "role": "CUSTOMER"
        }"""))
        .check(status.is(201))
        .check(jsonPath("$.userId").saveAs("userId"))
        .check(responseTimeInMillis.lte(300))
    )
    .pause(5.seconds, 15.seconds)
  
  val socialMediaIntegration = scenario("Social Media Integration")
    .feed(Iterator.continually(Map(
      "userId" -> userIds(scala.util.Random.nextInt(userIds.length)),
      "platform" -> Array("facebook", "instagram", "twitter")(scala.util.Random.nextInt(3)),
      "productId" -> productIds(scala.util.Random.nextInt(productIds.length))
    )))
    .exec(
      http("Share Product")
        .post("/api/social/share")
        .body(StringBody(session => s"""{
          "userId": "${session("userId").as[String]}",
          "platform": "${session("platform").as[String]}",
          "productId": "${session("productId").as[String]}",
          "message": "Check out this amazing product!"
        }"""))
        .check(status.is(200))
        .check(responseTimeInMillis.lte(800))
    )
    .pause(10.seconds, 30.seconds)
  
  // Load patterns
  val productSearchLoad = productSearch.inject(
    nothingFor(5.seconds),
    atOnceUsers(10),
    rampUsers(200).during(2.minutes),
    constantUsersPerSec(50).during(5.minutes),
    rampUsersPerSec(50).to(100).during(2.minutes),
    constantUsersPerSec(100).during(2.minutes),
    rampUsersPerSec(100).to(0).during(2.minutes)
  )
  
  val orderCreationLoad = orderCreation.inject(
    nothingFor(10.seconds),
    rampUsers(50).during(1.minute),
    constantUsersPerSec(10).during(3.minutes),
    heavisideUsers(100).during(2.minutes)
  )
  
  val paymentProcessingLoad = paymentProcessing.inject(
    nothingFor(15.seconds),
    rampUsers(30).during(1.minute),
    constantUsersPerSec(5).during(3.minutes)
  )
  
  val userRegistrationLoad = userRegistration.inject(
    nothingFor(5.seconds),
    rampUsers(20).during(1.minute),
    constantUsersPerSec(2).during(5.minutes)
  )
  
  val socialMediaLoad = socialMediaIntegration.inject(
    nothingFor(20.seconds),
    rampUsers(40).during(1.minute),
    constantUsersPerSec(8).during(3.minutes)
  )
  
  // Run all scenarios
  setUp(
    productSearchLoad,
    orderCreationLoad,
    paymentProcessingLoad,
    userRegistrationLoad,
    socialMediaLoad
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.percentile3.lte(2000),
      global.responseTime.mean.lte(500),
      global.successfulRequests.percent.gte(95),
      forAll.failedRequests.percent.lte(5)
    )
}
