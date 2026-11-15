package com.lautaroroldan.epakerompimo.service;
import com.lautaroroldan.epakerompimo.entity.Notification;

public interface EmailService {
    void send(Notification notification);
}
