package com.epicGamecraft.dungeonCrawlDepths.model;

import com.epicGamecraft.dungeonCrawlDepths.GameState;
import com.ple.jerbil.data.DbRecord;

public class GameSnap extends DbRecord {

  public final GameState state;
  public final Long previousSnapshotId;

  protected GameSnap(GameState state, Long previousSnapshotId) {
    this.state = state;
    this.previousSnapshotId = previousSnapshotId;
  }

  public static GameSnap make(GameState state, Long previousSnapshotId) {
    return new GameSnap(state, previousSnapshotId);
  }

}
