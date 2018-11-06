package com.xajd.wechat_01;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wechat.model.User;

import java.util.List;

public class FriendAdapter extends ArrayAdapter<User> {
    private List<User> friends;
    private int res;
    public FriendAdapter(Context context, int res, List<User> friends){
        super(context,res,friends);
        this.friends=friends;
        this.res=res;
    }
    @Override
    public int getCount() {
        return friends.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final User friend=friends.get(position);
        //将资源ID变成View
        View view=LayoutInflater
                .from(getContext())
                .inflate(res,parent,false);
        TextView friendname=view.findViewById(R.id.friendname);
        friendname.setText(friend.getUsername());
        return view;
//        nameTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("john",consumer.getName());
//            }
//        });
//
//        priceTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i("john",consumer.getSalary()+"");
//            }
//        });
//
    }
}
