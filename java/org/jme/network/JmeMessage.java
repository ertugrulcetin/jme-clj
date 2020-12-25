package org.jme.network;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

@Serializable
public class JmeMessage extends AbstractMessage {
    String message;

    public JmeMessage() {
    }

    public JmeMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
