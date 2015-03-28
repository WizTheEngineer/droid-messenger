package com.waynebjackson.androidmessaging.models;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.waynebjackson.androidmessaging.activities.MessagingActivity;
import com.waynebjackson.androidmessaging.adapters.MessageAdapter;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Wayne on 3/27/15.
 */
@ParseClassName("_User")
public class MessagingUser extends ParseUser {
    private static final String TAG = "MessagingUser";

    public MessagingUser() {}

    public void sendMessage(final Activity activity, final Pubnub pubnub, final Message message, final MessageAdapter msgAdapter) {
        final String receiverId = message.getReceiverId();
        message.setTimeSent(new Date());

        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    JSONObject messageObject = message.toJSON();

                    try {
                        pubnub.subscribe(receiverId, new Callback() {
                            @Override
                            public void successCallback(String channel, Object message) {
                                super.successCallback(channel, message);
                            }

                            @Override
                            public void errorCallback(String channel, PubnubError error) {
                                super.errorCallback(channel, error);
                            }
                        });
                    } catch (PubnubException pubNubException) {
                        pubNubException.printStackTrace();
                    }

                    Callback messageCallback = new Callback() {
                        public void successCallback(String channel, Object response) {
                            Log.d(TAG, response.toString());

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Add the message to the adapter
                                    msgAdapter.addMessage(message);

                                    // Hide the dialog
                                    ((MessagingActivity) activity).hideProgressDialog();
                                }
                            });

                            // Unsubscribe from the channel once the message is sent
                            pubnub.unsubscribe(receiverId);
                        }

                        public void errorCallback(String channel, PubnubError error) {
                            Log.d(TAG, "Error sending message:" + error.toString());

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, "Error sending message", Toast.LENGTH_LONG).show();

                                    // Hide the dialog
                                    ((MessagingActivity) activity).hideProgressDialog();
                                }
                            });

                            // Unsubscribe from the channel once the message is sent
                            pubnub.unsubscribe(receiverId);
                        }
                    };

                    pubnub.publish(receiverId, messageObject, messageCallback);
                } else {
                    Log.d(TAG, "Error saving message:" + e.toString());

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "Error sending message", Toast.LENGTH_LONG).show();

                            // Hide the dialog
                            ((MessagingActivity) activity).hideProgressDialog();

                        }
                    });
                }
            }
        });


    }
}
