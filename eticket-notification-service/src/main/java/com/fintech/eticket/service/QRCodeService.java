package com.fintech.eticket.service;

public interface QRCodeService {
    byte[] generateQRCodeImage(String text, int width, int height);
    String generateQRCodeBase64(String text, int width, int height);
}
