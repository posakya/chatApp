package com.roshan.posakya.chatapplication;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.library.bubbleview.BubbleTextView;

class ChatHolder extends RecyclerView.ViewHolder {
//    TextView messageText,messageUser,messageTime;
    TextView messageText, messageUser, messageTime,message_text_other,message_user_other;
    ProgressBar right_progress_bar,left_progress_bar;
    ImageView messageText_image,message_text_other_image;

    public ChatHolder(@NonNull View v) {
        super(v);

//        messageText = itemView.findViewById( R.id.message_text );
//        messageUser = itemView.findViewById( R.id.message_user );
//        messageTime = itemView.findViewById( R.id.message_time );

        left_progress_bar = v.findViewById(R.id.left_progress_bar);
        right_progress_bar = v.findViewById(R.id.right_progress_bar);
        messageText = (BubbleTextView) v.findViewById(R.id.message_text);
        message_text_other = (BubbleTextView) v.findViewById(R.id.message_text_other);
        messageUser = (TextView) v.findViewById(R.id.message_user);
        messageTime = (TextView) v.findViewById(R.id.message_time);
        message_user_other = v.findViewById(R.id.message_user_other);

        messageText_image = v.findViewById(R.id.message_text_image);
        message_text_other_image = v.findViewById(R.id.message_text_other_image);
    }
}
