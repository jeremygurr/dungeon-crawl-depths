package com.epicGamecraft.dungeonCrawlDepths.verticle;

import com.epicGamecraft.dungeonCrawlDepths.GameSnapCache;
import com.epicGamecraft.dungeonCrawlDepths.GameState;
import com.epicGamecraft.dungeonCrawlDepths.model.Rules;
import com.epicGamecraft.dungeonCrawlDepths.rules.Rules1;
import com.epicGamecraft.dungeonCrawlDepths.SharedMaps;
import com.epicGamecraft.dungeonCrawlDepths.model.Game;
import com.epicGamecraft.dungeonCrawlDepths.service.GameService;
import com.ple.observabilityBridge.RecordingService;
import io.vertx.core.Promise;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.shareddata.LocalMap;
import io.vertx.reactivex.core.shareddata.SharedData;

import static com.epicGamecraft.dungeonCrawlDepths.BusEvent.*;

public class GameCreationVerticle extends AbstractVerticle {

  private final RecordingService startupRecordingService;

  public GameCreationVerticle(RecordingService startupRecordingService) {
    this.startupRecordingService = startupRecordingService;
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.eventBus().consumer(newGame.name(), this::handleGameCreate);
  }

  private void handleGameCreate(Message<String> message) {

    startupRecordingService.open("Creating new game");

    final Rules rules = Rules1.make();
    final Game game = Game.make(rules.save());

    final SharedData sharedData = vertx.sharedData();
    final GameState current = GameService.getCurrentState(game, sharedData);

    startupRecordingService.close("Creating new game");

  }

}
