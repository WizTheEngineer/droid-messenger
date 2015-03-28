package com.waynebjackson.androidmessaging;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.waynebjackson.androidmessaging.activities.MessagingActivity;
import com.waynebjackson.androidmessaging.fragments.ConversationThreadFragment;
import com.waynebjackson.androidmessaging.interfaces.MessageListener;
import com.waynebjackson.androidmessaging.models.Message;
import com.waynebjackson.androidmessaging.models.MessagingUser;

import org.json.JSONObject;

/**
 * Created by Wayne on 3/26/15.
 */
public class MessagingApp extends Application {
    private static String TAG = "MessagingApp";
    private Pubnub pubnub;
    private ConversationThreadFragment conversationFragment;

    @Override
    public void onCreate() {
        super.onCreate();
        initParse();
    }

    private void initParse() {
        ParseObject.registerSubclass(MessagingUser.class);
        ParseObject.registerSubclass(Message.class);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, getString(R.string.parse_application_id), getString(R.string.parse_client_id));
    }

    public void subscribeToMessagingChannel() {
        String userId = ParseUser.getCurrentUser().getObjectId();
        pubnub = new Pubnub(getString(R.string.pubnub_publish_key), getString(R.string.pubnub_subscribe_key));
        try {
            pubnub.subscribe(userId, new Callback() {

                @Override
                public void successCallback(String channel, Object messageObject) {

                    // Create Parse Object from JSON
                    final Message message = Message.messageFromJSON((JSONObject) messageObject);

                    // Use the message sender ID to find the sender
                    ParseUser.getQuery().getInBackground(message.getSenderId(), new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser sender, ParseException e) {
                            if (e == null) {
                                // Make a call to the callback method of the message listener
                                messageListener.onMessageReceived(sender, message);
                            } else {
                                Log.e(TAG, "Error fetching user for message", e);
                            }
                        }
                    });
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    Log.d(TAG, "SUBSCRIBE : ERROR on channel " + channel
                            + " : " + error.toString());
                }

            });
        } catch (PubnubException e) {
            e.printStackTrace();
            Log.e(TAG, "Error subscribing to channel:" + e.getMessage());
        }
    }

    public void unsubscribeFromMessageChannel(String userId) {
        pubnub.unsubscribe(userId);
    }

    private MessageListener messageListener = new MessageListener() {

        @Override
        public void onMessageReceived(ParseUser sender, Message message) {

            if (conversationFragment != null && conversationFragment.belongsToSender(sender)) {
                // If senders conversation thread is open just publish message to adapter
                conversationFragment.addMessage(message);
            } else  {
                // Otherwise send a notification
                sendMessageNotification(sender, message);
            }

        }
    };

    // For updating UI if conversation is visible
    public void onConversationResumed(ConversationThreadFragment conversationFragment) {
        this.conversationFragment = conversationFragment;
    }

    public void onConversationPaused() {
        this.conversationFragment = null;
    }

    private void sendMessageNotification(ParseUser sender, Message message) {
        final int notificationId = 100;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon)
                        .setContentTitle("Message from " + sender.getUsername())
                        .setContentText(message.getMessageBody())
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MessagingActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MessagingActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // notification id  allows you to update the notification later on.
        notificationManager.notify(notificationId, mBuilder.build());
    }
}
