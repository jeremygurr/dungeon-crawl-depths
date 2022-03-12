package com.epicGamecraft.dungeonCrawlDepths.service;

import com.epicGamecraft.dungeonCrawlDepths.GameState;
import com.epicGamecraft.dungeonCrawlDepths.SharedMaps;
import com.epicGamecraft.dungeonCrawlDepths.model.Game;
import io.vertx.reactivex.core.shareddata.LocalMap;
import io.vertx.reactivex.core.shareddata.SharedData;

import java.util.Map;

public class SharedDataService {

  public static LocalMap<Game, GameState> getCurrentGameStates(SharedData sharedData) {
    return sharedData.getLocalMap(SharedMaps.currentGameStates.name());
  }

}
