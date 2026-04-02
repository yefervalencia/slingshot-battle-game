package com.slingshot.network;

public interface NetworkObserver {
  void onMessageReceived(String payload);
}
