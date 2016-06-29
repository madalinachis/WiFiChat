package com.example.madalina.wifigroupchat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.example.madalina.wifigroupchat.R;
import com.example.madalina.wifigroupchat.model.PeersUser;

/**
 * Created by Madalina on 5/10/2016.
 */
public class UsersAdapter extends ArrayAdapter<PeersUser> {

    @Bind(R.id.username_view)
    TextView usernameText;
    @Bind(R.id.hobby_view)
    TextView hobbyText;
    @Bind(R.id.group_owner_view)
    TextView GOtext;

    public UsersAdapter(Context context, List<PeersUser> users) {
        super(context, R.layout.user_item, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.user_item, parent, false);
        }
        ButterKnife.bind(this, convertView);
        PeersUser user = getItem(position);
        if (user != null) {
            usernameText.setText(user.getUser().getUsername());
            hobbyText.setText(user.getUser().getHobby());
            GOtext.setText(user.getDevice().isGroupOwner() ? "GO" : "Client");
        }
        return convertView;
    }
}
