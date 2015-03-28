package com.waynebjackson.androidmessaging.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.ParseUser;
import com.waynebjackson.androidmessaging.R;
import com.waynebjackson.androidmessaging.activities.MessagingActivity;
import com.waynebjackson.androidmessaging.models.Message;

import java.util.List;

/**
 * Created by Wayne on 3/27/15.
 */
public class MessageAdapter extends BaseAdapter {

    // Direction of message
    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;

    private List<Message> messages;
    private LayoutInflater inflater;

    public MessageAdapter(MessagingActivity msgActivity, List<Message> messages) {
        this.messages = messages;
        inflater = LayoutInflater.from(msgActivity);
    }

    public void addMessage(Message msg) {
        messages.add(msg);
        notifyDataSetChanged();
    }

    public void addMessages(List<Message> messageList) {
        this.messages.addAll(messageList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Message getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int direction = getItemViewType(position);

        int res = 0;
        if (direction == DIRECTION_INCOMING) {
            res = R.layout.message_right;
        } else if (direction == DIRECTION_OUTGOING) {
            res = R.layout.message_left;
        }
        convertView = inflater.inflate(res, parent, false);

        Message message = messages.get(position);

        TextView txtMessage = (TextView) convertView.findViewById(R.id.txtMessage);
        txtMessage.setText(message.getMessageBody());

        TextView txtDate = (TextView) convertView.findViewById(R.id.txtDate);
        txtDate.setText(message.getFormattedTimeSent());

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);
        return getMessageDirection(msg);
    }

    private int getMessageDirection(Message msg) {
        if (msg.getSenderId().equals(ParseUser.getCurrentUser().getObjectId())) {
            return DIRECTION_OUTGOING;
        }

        return DIRECTION_INCOMING;
    }
}
