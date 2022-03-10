package com.epicGamecraft.dungeonCrawlDepths;

import com.epicGamecraft.dungeonCrawlDepths.model.Game;
import com.epicGamecraft.dungeonCrawlDepths.model.GameSnap;
import com.epicGamecraft.dungeonCrawlDepths.model.GameSnapId;
import com.ple.util.IHashMap;
import com.ple.util.IMap;
import io.vertx.core.shareddata.Shareable;
import io.vertx.reactivex.core.shareddata.SharedData;

public class GameSnapCache implements Shareable {

  public static final GameSnapCache empty = new GameSnapCache(IHashMap.empty);

  public final IMap<GameSnapId, GameSnap> snaps;

  protected GameSnapCache(IMap<GameSnapId, GameSnap> snaps) {
    this.snaps = snaps;
  }

  public static GameSnapCache make(IMap<GameSnapId, GameSnap> snapMap) {
    return new GameSnapCache(snapMap);
  }

}
