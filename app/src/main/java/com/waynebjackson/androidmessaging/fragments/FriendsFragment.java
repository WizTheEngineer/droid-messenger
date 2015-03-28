package com.waynebjackson.androidmessaging.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.waynebjackson.androidmessaging.R;
import com.waynebjackson.androidmessaging.activities.MessagingActivity;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {
    private static final String TAG = "FriendsFragment";
    private ListView friendsListView;
    private ArrayAdapter<String> friendsAdapter;
    private ArrayList<String> friendNames;

    public static FriendsFragment newInstance() {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends, container, false);
        friendsListView = (ListView) v.findViewById(R.id.friends_list_view);
        populateListWithUsers();
        return v;
    }

    private void populateListWithUsers() {
        ((MessagingActivity) getActivity()).showProgressDialog("Loading Friends", "One moment please...");
        friendNames = new ArrayList<>();
        String currentUserId = ParseUser.getCurrentUser().getObjectId();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("objectId", currentUserId);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                ((MessagingActivity) getActivity()).hideProgressDialog();
                if (e == null) {
                    for (int i=0; i< parseUsers.size(); i++) {
                        friendNames.add(parseUsers.get(i).getUsername().toString());
                    }
                    friendsAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                                    R.layout.user_list_item, friendNames);
                    friendsListView.setAdapter(friendsAdapter);
                    friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> a, View v, int i, long l) {
                            openConversation(i);
                        }
                    });
                } else  {
                    Log.e(TAG, "Error finding users:" + e.getMessage(), e);
                }
            }
        });
    }

    private void openConversation(int position) {
        String username = friendNames.get(position);
        ((MessagingActivity) getActivity()).openConversationThread(username);
    }
}
