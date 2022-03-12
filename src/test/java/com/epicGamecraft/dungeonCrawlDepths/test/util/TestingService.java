package com.epicGamecraft.dungeonCrawlDepths.test.util;

import com.ple.observabilityBridge.RecordingService;
import com.ple.observabilityBridge.SystemOutLogHandler;

public class TestingService {

  public static final RecordingService defaultRecordingService;

  static {
    defaultRecordingService = RecordingService.make(SystemOutLogHandler.only);
    defaultRecordingService.verbosity = 20;
  }

  public static RecordingService getRecordingService(Class aClass) {
    return defaultRecordingService;
  }

}
