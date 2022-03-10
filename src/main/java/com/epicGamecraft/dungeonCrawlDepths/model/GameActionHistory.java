package com.epicGamecraft.dungeonCrawlDepths.model;

public class GameActionHistory {
  public final GameActionHistory previous;

  protected GameActionHistory(GameActionHistory previous) {
    this.previous = previous;
  }

}
