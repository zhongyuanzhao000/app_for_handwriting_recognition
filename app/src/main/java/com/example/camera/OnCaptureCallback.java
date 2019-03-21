package com.example.camera;


public interface OnCaptureCallback {

    void onCapture(boolean success, String filePath);
}