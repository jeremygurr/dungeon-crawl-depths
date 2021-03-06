package com.epicGamecraft.dungeonCrawlDepths.service;

import com.epicGamecraft.dungeonCrawlDepths.GameState;
import com.epicGamecraft.dungeonCrawlDepths.SharedMaps;
import com.epicGamecraft.dungeonCrawlDepths.model.Game;
import io.vertx.reactivex.core.shareddata.LocalMap;
import io.vertx.reactivex.core.shareddata.SharedData;

public class GameService {

  public static GameState getCurrentState(Game game, SharedData sharedData) {
    return SharedDataService.getCurrentGameStates(sharedData).get(game);
  }

}
