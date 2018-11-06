package com.xajd.wechat_01;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wechat.model.ChatContent;

import java.util.List;

/**
 * Created by john on 2018/10/27.
 */

public class ChatContentAdapter extends ArrayAdapter<ChatContent> {
    private Context context;
    private int resource;
    private List<ChatContent> data;
    public ChatContentAdapter(@NonNull Context context, int resource, List<ChatContent> data) {
        super(context, resource);
        this.context=context;
        this.resource=resource;
        this.data=data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Nullable
    @Override
    public ChatContent getItem(int position) {
        return data.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ChatContent cc=data.get(position);
        if(convertView==null){
            convertView= LayoutInflater.from(context).inflate(resource,parent,false);
        }
        convertView.findViewById(R.id.layout_left).setVisibility(View.VISIBLE);
        convertView.findViewById(R.id.layout_right).setVisibility(View.VISIBLE);
        if(cc.isMe()){
            convertView.findViewById(R.id.layout_left).setVisibility(View.GONE);
           TextView tv= convertView.findViewById(R.id.content_right);
           tv.setTextSize(27);
           tv.setText(cc.getContent());
        }
        else{
            convertView.findViewById(R.id.layout_right).setVisibility(View.GONE);
            TextView tv= convertView.findViewById(R.id.content_left);
            tv.setTextSize(27);
            tv.setText(cc.getContent());
        }
        return convertView;
    }
}
