package com.waynebjackson.androidmessaging.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pubnub.api.Pubnub;
import com.waynebjackson.androidmessaging.MessagingApp;
import com.waynebjackson.androidmessaging.R;
import com.waynebjackson.androidmessaging.activities.MessagingActivity;
import com.waynebjackson.androidmessaging.adapters.MessageAdapter;
import com.waynebjackson.androidmessaging.models.Message;
import com.waynebjackson.androidmessaging.models.MessagingUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wayne on 3/26/15.
 */
public class ConversationThreadFragment extends Fragment {
    private static final String TAG = "ConversationThread";
    private static final String USERNAME_EXTRA = "Username";

    private String username;
    private EditText msgField;
    private ListView msgListView;
    private Button btnSend;
    private ParseUser friend;
    private MessageAdapter msgAdapter;
    private List<Message> messages;

    public ConversationThreadFragment() {}

    public static final ConversationThreadFragment newInstance(String username) {
        ConversationThreadFragment fragment = new ConversationThreadFragment();
        Bundle args = new Bundle();
        args.putString(USERNAME_EXTRA, username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.username = getArguments().getString(USERNAME_EXTRA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_conversation_thread, container, false);
        msgField = (EditText) v.findViewById(R.id.messageBodyField);
        msgListView = (ListView) v.findViewById(R.id.listMessages);
        messages = new ArrayList<>();
        msgAdapter = new MessageAdapter((MessagingActivity) getActivity(), messages);
        msgListView.setAdapter(msgAdapter);
        btnSend = (Button) v.findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        loadConversation();
        return v;
    }

    private void loadConversation() {
        ((MessagingActivity) getActivity()).showProgressDialog("Loading Conversation", "One moment please...");
        ParseQuery<ParseUser> friendQuery = ParseUser.getQuery();
        friendQuery.whereEqualTo("username", username);

        friendQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {

                if (e == null) {
                    friend = parseUsers.get(0);
                    ParseQuery<Message> sentMessagesQuery = ParseQuery.getQuery(Message.class);
                    sentMessagesQuery.whereEqualTo("senderId", ParseUser.getCurrentUser().getObjectId());
                    sentMessagesQuery.whereEqualTo("receiverId", friend.getObjectId());

                    ParseQuery<Message> receivedMessagesQuery = ParseQuery.getQuery(Message.class);
                    receivedMessagesQuery.whereEqualTo("senderId", friend.getObjectId());
                    receivedMessagesQuery.whereEqualTo("receiverId", ParseUser.getCurrentUser().getObjectId());

                    // Combine the queries
                    List<ParseQuery<Message>> queries = new ArrayList<>();
                    queries.add(sentMessagesQuery);
                    queries.add(receivedMessagesQuery);

                    // Get the messages
                    ParseQuery<Message> mainQuery = ParseQuery.or(queries);
                    mainQuery.orderByAscending("timeSent");
                    mainQuery.findInBackground(new FindCallback<Message>() {

                        @Override
                        public void done(List<Message> messages, ParseException e) {
                            ((MessagingActivity) getActivity()).hideProgressDialog();
                            if (e == null) {
                                msgAdapter.addMessages(messages);
                            } else {
                                showConversationFetchError(e);
                            }
                        }
                    });
                } else {
                    showConversationFetchError(e);
                    ((MessagingActivity) getActivity()).hideProgressDialog();
                }
            }
        });
    }

    private void showConversationFetchError(ParseException e) {
        Log.e(TAG, "Error fetching conversation: " + e.getMessage(), e);
        Toast.makeText(getActivity(), "Error fetching conversation", Toast.LENGTH_LONG).show();

        // Go back
        getFragmentManager().popBackStack();
    }

    private void sendMessage() {
        String messageBody = msgField.getText().toString().trim();

        if (!messageBody.isEmpty()) {
            ((MessagingActivity) getActivity()).showProgressDialog("Sending Message", "please wait...");

            // Empty the field
            msgField.setText("");

            MessagingUser sender = (MessagingUser) ParseUser.getCurrentUser();

            String receiverId = friend.getObjectId();
            String senderId = sender.getObjectId();

            // Create message object
            Message message = Message.newInstance(senderId, receiverId, messageBody);

            // Create Pubnub object
            Pubnub pubnub = new Pubnub(getString(R.string.pubnub_publish_key),
                    getString(R.string.pubnub_subscribe_key));

            // Send the message
            sender.sendMessage(getActivity(), pubnub, message, msgAdapter);
        } else {
            Toast.makeText(getActivity(), "Please enter message in field", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MessagingApp) getActivity().getApplication()).onConversationResumed(this);

        // Set the title to the name of the person
        getActivity().setTitle(username);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MessagingApp) getActivity().getApplication()).onConversationPaused();

        // Set the title back
        getActivity().setTitle(getString(R.string.title_activity_messaging));
    }

    public boolean belongsToSender(ParseUser sender) {
        return sender.getUsername().equals(username);
    }

    public void addMessage(Message message) {
        msgAdapter.addMessage(message);
    }
}
