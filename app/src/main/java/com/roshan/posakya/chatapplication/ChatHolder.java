package com.roshan.posakya.chatapplication;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

class ChatHolder extends RecyclerView.ViewHolder {
    TextView messageText,messageUser,messageTime;
    public ChatHolder(@NonNull View itemView) {
        super(itemView);

        messageText = itemView.findViewById( R.id.message_text );
        messageUser = itemView.findViewById( R.id.message_user );
        messageTime = itemView.findViewById( R.id.message_time );
    }
}
