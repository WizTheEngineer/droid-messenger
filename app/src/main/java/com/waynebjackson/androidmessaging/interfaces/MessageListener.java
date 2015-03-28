package com.waynebjackson.androidmessaging.interfaces;

import com.parse.ParseUser;
import com.waynebjackson.androidmessaging.models.Message;

/**
 * Created by Wayne on 3/27/15.
 */
public interface MessageListener {
    public void onMessageReceived(ParseUser sender, Message message);
}
