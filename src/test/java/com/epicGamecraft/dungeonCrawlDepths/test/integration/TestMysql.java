package com.epicGamecraft.dungeonCrawlDepths.test.integration;

import com.epicGamecraft.dungeonCrawlDepths.verticle.MysqlVerticle;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.reactivex.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.epicGamecraft.dungeonCrawlDepths.BusEvent.*;

@ExtendWith(VertxExtension.class)
public class TestMysql {

  private static final Logger LOGGER = LoggerFactory.getLogger(TestMysql.class);

  @Test
  void forgotPassword(Vertx vertx, VertxTestContext context) throws Throwable {

    vertx.rxDeployVerticle(new MysqlVerticle())
      .subscribe(e -> {
          vertx.eventBus().rxRequest(mysqlPass.name(), "{\"username\":\"billybob\",\"email\":\"som@gmail.com\"}")
            .subscribe(ar -> {
                LOGGER.debug("TestMysql.forgotPassword received reply : " + ar.body());
                context.completeNow();
              },
              err -> {
                LOGGER.debug("Communication error between Test.forgotPassword and MysqlVerticle : " + err.getMessage());
                context.failNow(err);
              });
        },
        err -> {
          LOGGER.debug("TestMysql.forgotPassword issue deploying verticle : " + err.getMessage());
          context.failNow(err);
        });
  }

  @Test
  void retrieveGameList(Vertx vertx, VertxTestContext context) throws Throwable {

    vertx.rxDeployVerticle(new MysqlVerticle())
      .subscribe(e -> {
          vertx.eventBus().rxRequest(mysqlGameList.name(), "score")
            .subscribe(ar -> {
                if (ar.body() != null) {
                  LOGGER.debug("TestMysql.retrieveGameList received reply : " + ar.body());
                  context.completeNow();
                } else {
                  context.failNow(new Exception("Failed to retrieve lobby data."));
                }
              },
              err -> {
                LOGGER.debug("Communication between Test.retrieveGameList error : " + err.getMessage());
                context.failNow(err);
              });
        },
        err -> {
          LOGGER.debug("TestMysql.retrieveGameList issue deploying verticle : " + err.getMessage());
          context.failNow(err);
        });
  }

}
