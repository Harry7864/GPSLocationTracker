package com.example.gpslocationtracker.service;

public interface OtpReceivedInterface {
    void onOtpReceived(String otp);

    void onOtpTimeout();
}
