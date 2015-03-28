package com.waynebjackson.androidmessaging.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Wayne on 3/27/15.
 */
@ParseClassName("Message")
public class Message extends ParseObject {
    // JSON tags
    public static final String JSON_MESSAGE_TAG = "message";
    public static final String JSON_SENDER_ID_TAG = "senderId";
    public static final String JSON_RECEIVER_ID_TAG = "receiverId";
    public static final String JSON_TIME_SENT_MILLIS_TAG = "timeSentMillis";

    public Message() {}

    public static Message newInstance(String senderId, String receiverId, String messageBody) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setMessageBody(messageBody);
        return message;
    }

    public static Message messageFromJSON(JSONObject messageObject) {
        Message message = new Message();

        try {
            String messageBody = messageObject.getString(JSON_MESSAGE_TAG);
            message.setMessageBody(messageBody);

            String senderId = messageObject.getString(JSON_SENDER_ID_TAG);
            message.setSenderId(senderId);

            String receiverId = messageObject.getString(JSON_RECEIVER_ID_TAG);
            message.setReceiverId(receiverId);

            long timeSentMillis = messageObject.getLong(JSON_TIME_SENT_MILLIS_TAG);
            Date timeSent = new Date(timeSentMillis);

            message.setTimeSent(timeSent);

            // Return the message
            return message;
        } catch (JSONException e) {
            e.printStackTrace();

            // If error was presented return null
            return null;
        }
    }

    public JSONObject toJSON() {
        JSONObject messageObject = new JSONObject();
        try {
            messageObject.put(JSON_SENDER_ID_TAG, getSenderId());
            messageObject.put(JSON_RECEIVER_ID_TAG, getReceiverId());
            messageObject.put(JSON_MESSAGE_TAG, getMessageBody());
            messageObject.put(JSON_TIME_SENT_MILLIS_TAG, getTimeSent().getTime());
            return messageObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setSenderId(String senderId) {
        put("senderId", senderId);
    }

    private void setMessageBody(String messageBody) {
        put("messageBody", messageBody);
    }

    public void setTimeSent(Date timeSent) {
        put("timeSent", timeSent);
    }

    public String getSenderId() {
        return getString("senderId");
    }

    public String getMessageBody() {
        return getString("messageBody");
    }

    public Date getTimeSent() {
        return getDate("timeSent");
    }

    public void setReceiverId(String receiverId) {
        put("receiverId", receiverId);
    }

    public String getReceiverId() {
        return getString("receiverId");
    }

    public String getFormattedTimeSent() {
        Date timeSent = getTimeSent();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:m:s a");
        return dateFormat.format(timeSent);
    }
}
