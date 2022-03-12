package com.epicGamecraft.dungeonCrawlDepths.test.unit;

import com.epicGamecraft.dungeonCrawlDepths.test.util.TestingService;
import com.epicGamecraft.dungeonCrawlDepths.verticle.GameListVerticle;
import com.epicGamecraft.dungeonCrawlDepths.verticle.HttpServerVerticle;
import com.epicGamecraft.dungeonCrawlDepths.verticle.UserVerticle;
import com.ple.observabilityBridge.RecordingService;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.epicGamecraft.dungeonCrawlDepths.BusEvent.gameList;
import static com.epicGamecraft.dungeonCrawlDepths.BusEvent.userLogin;

@ExtendWith(VertxExtension.class)
public class TestCrawlInit {

  final RecordingService rs = TestingService.getRecordingService(this.getClass());

  @Test
  void verticle_deployed(Vertx vertx, VertxTestContext context) throws Throwable {
    vertx.deployVerticle(new HttpServerVerticle(rs), context.succeeding(id -> context.completeNow()));
    context.completeNow();
  }

  @Test
  void loginSuccessMysql(Vertx vertx, VertxTestContext context) throws Throwable {
    FakeMysqlVerticle mysqlVerticle = new FakeMysqlVerticle();
    mysqlVerticle.response = "{\"email\":\"bob@yahoo.com\",\"hashword\":\"1216985755\",\"name\":\"Bob John\"}";
    vertx.rxDeployVerticle(mysqlVerticle)
      .flatMap(e -> {
        return vertx.rxDeployVerticle(new UserVerticle(rs));   //This is how you deploy more than one verticle. Just use more .flatmaps().
      })
      .subscribe(e -> {
          vertx.eventBus().rxRequest(userLogin.name(), "{\"usernameOrEmail\":\"billybob\",\"password\":\"password\"}")
            .subscribe(ar -> {
                rs.log(-5, "Test Verticle received reply: " + ar.body());
                JsonObject json = new JsonObject(ar.body().toString());
                if (json.getString("redirect").equals("serverError.html")) {
                  context.failNow(new Exception("User Verticle failed to retrieve data from FakeMysqlVerticle."));
                } else {
                  context.completeNow();
                }
              },
              err -> {
                rs.log(10, "An error occurred retrieving data from Mysql.");
              });
        },
        err -> {
          context.failNow(err);
        });
  }

  @Test
  void loginFailureMysql(Vertx vertx, VertxTestContext context) throws Throwable {
    FakeMysqlVerticle mysqlVerticle = new FakeMysqlVerticle();
    mysqlVerticle.response = null;
    vertx.rxDeployVerticle(mysqlVerticle)
      .flatMap(e -> vertx.rxDeployVerticle(new UserVerticle(rs)))
      .subscribe(e -> {
          vertx.eventBus().rxRequest(userLogin.name(), "{\"usernameOrEmail\":\"b\",\"password\":\"pass\"}")
            .subscribe(ar -> {
                rs.log(-5, "Test Verticle received reply: " + ar.body());
                JsonObject json = new JsonObject(ar.body().toString());
                if (json.getString("redirect").equals("serverError.html")) {
                  context.failNow(new Exception("User Verticle failed to retrieve data from FakeMysqlVerticle."));
                } else {
                  context.completeNow();
                }
              },
              err -> {
                rs.log(10, "An error occurred retrieving data from Mysql." + err.getMessage());
              });
        },
        err -> {
          context.failNow(err);
        });
  }

  @Test
  void gameListSuccess(Vertx vertx, VertxTestContext context) throws Throwable {
    FakeMysqlVerticle mysqlVerticle = new FakeMysqlVerticle();
    mysqlVerticle.response = "{\"game_id\":1,\"character_Name\":\"Ross the cold Enchanter\",\"status\":\"paused\",\"floorNumber\":\"1\",\"playtime\":\"00:00:00\",\"creation_time\":\"2021-04-10 18:28:57\",\"user\":\"billybob\",\"scenario\":\"normal\"}";
    vertx.rxDeployVerticle(mysqlVerticle)
      .flatMap(e -> vertx.rxDeployVerticle(new GameListVerticle(rs)))
      .subscribe(e -> {
          vertx.eventBus().rxRequest(gameList.name(), "basic")
            .subscribe(ar -> {
              rs.log(-5, "Test Verticle received reply: " + ar.body());
                if (ar.body().toString().equals("failed")) {
                  context.failNow(new Exception("User Verticle failed to retrieve data from FakeMysqlVerticle."));
                } else {
                  context.completeNow();
                }
              },
              err -> {
                rs.log(10, "An error occurred retrieving game list." + err.getMessage());
              });
        },
        err -> {
          context.failNow(err);
        });
  }

  @Test
  void gameListFailure(Vertx vertx, VertxTestContext context) throws Throwable {
    FakeMysqlVerticle mysqlVerticle = new FakeMysqlVerticle();
    mysqlVerticle.response = null;
    vertx.rxDeployVerticle(mysqlVerticle)
      .flatMap(e -> vertx.rxDeployVerticle(new GameListVerticle(rs)))
      .subscribe(e -> {
          vertx.eventBus().rxRequest(gameList.name(), "basic")
            .subscribe(ar -> {
                rs.log(-5, "Test Verticle received reply: " + ar.body());
                if (ar.body().toString().equals("success")) {
                  context.failNow(new Exception("User Verticle failed to retrieve data from FakeMysqlVerticle."));
                } else {
                  context.completeNow();
                }
              },
              err -> {
                rs.log(10, "An error occurred retrieving game list." + err.getMessage());
                context.failNow(err);
              });
        },
        err -> {
          context.failNow(err);
        });
  }

}
