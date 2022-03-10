package com.epicGamecraft.dungeonCrawlDepths.model;

import io.vertx.core.shareddata.Shareable;

import java.util.Optional;

public class Game implements Shareable {

  public final RulesId initialRules;
  public final long seed;
  public final Optional<GameActionHistoryId> lastAction;
  public final Optional<GameSnapId> lastSnap;

  public Game(RulesId initialRules, long seed, Optional<GameActionHistoryId> lastAction, Optional<GameSnapId> lastSnap) {
    this.initialRules = initialRules;
    this.seed = seed;
    this.lastAction = lastAction;
    this.lastSnap = lastSnap;
  }

  public static Game make(RulesId rules) {
    return new Game(rules, 0, Optional.empty(), Optional.empty());
  }

}
