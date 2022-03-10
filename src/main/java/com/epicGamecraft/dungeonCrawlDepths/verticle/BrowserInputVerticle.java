package com.epicGamecraft.dungeonCrawlDepths.verticle;

import static com.epicGamecraft.dungeonCrawlDepths.BusEvent.*;

import com.ple.observabilityBridge.RecordingService;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;

public class BrowserInputVerticle extends AbstractVerticle {

  private final RecordingService startupRecordingService;

  public BrowserInputVerticle(RecordingService recordingService) {
    this.startupRecordingService = recordingService;
  }

  @Override
	public void start(Promise<Void> promise) throws Exception {
		vertx.eventBus().consumer(browserInput.name(), this::handleKey);
	}

	private void handleKey(Message<String> message) {
		startupRecordingService.log(-10, "Received message: " + message.body());
	}

}
