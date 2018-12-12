package com.roshan.posakya.chatapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.github.library.bubbleview.BubbleTextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class MainActivity extends AppCompatActivity {

    private static int SIGN_IN_REQUEST_CODE = 1;
    private FirebaseListAdapter<ChatMessage> adapter;
    RelativeLayout activity_main;

    //Add Emojicon
    EmojiconEditText emojiconEditText;
    ImageView emojiButton,submitButton;
    EmojIconActions emojIconActions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main = (RelativeLayout)findViewById(R.id.activity_main);

        //Add Emoji
        emojiButton = (ImageView)findViewById(R.id.emoji_button);
        submitButton = (ImageView)findViewById(R.id.submit_button);
        emojiconEditText = (EmojiconEditText)findViewById(R.id.emojicon_edit_text);
        emojIconActions = new EmojIconActions(getApplicationContext(),activity_main,emojiButton,emojiconEditText);
        emojIconActions.ShowEmojicon();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(emojiconEditText.getText().toString(),
                        FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                emojiconEditText.setText("");
                emojiconEditText.requestFocus();
            }
        });

        //Check if not sign-in then navigate Signin page
        if(FirebaseAuth.getInstance().getCurrentUser() == null)
        {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(),SIGN_IN_REQUEST_CODE);
        }
        else
        {
            Snackbar.make(activity_main,"Welcome "+FirebaseAuth.getInstance().getCurrentUser().getEmail(),Snackbar.LENGTH_SHORT).show();
            //Load content
            displayChatMessage();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SIGN_IN_REQUEST_CODE)
        {
            if(resultCode == RESULT_OK)
            {
                Snackbar.make(activity_main,"Successfully signed in.Welcome!", Snackbar.LENGTH_SHORT).show();
                displayChatMessage();
            }
            else{
                Snackbar.make(activity_main,"We couldn't sign you in.Please try again later", Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void displayChatMessage() {
        ListView listOfMessage = (ListView) findViewById( R.id.list_of_message );
        Query query = FirebaseDatabase.getInstance().getReference().child("chats");
        FirebaseApp.initializeApp(this);
        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .setLayout(R.layout.list_item)
                .build();
        //Finally you pass them to the constructor here:
        adapter = new FirebaseListAdapter<ChatMessage>(options){

            //    ListView listOfMessage = (ListView) findViewById( R.id.list_of_message );

            //  adapter = new FirebaseListAdapter<ChatMessage>( MainActivity.this,ChatMessage.class,R.layout.layout2,FirebaseDatabase.getInstance().getReference() ) {

            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                //Get reference to the view of list item
                TextView messageText,messageUser,messageTime;
                messageText = (TextView)findViewById( R.id.message_text );
                messageUser = (TextView)findViewById( R.id.message_user );
                messageTime = (TextView)findViewById( R.id.message_time );

                messageText.setText( model.getMessageText() );

                messageUser.setText( model.getMessageUser() );

                messageTime.setText( DateFormat.format("dd-MM-yyyy (HH:mm:ss)",model.getMessageTime()) );

            }
        };
        listOfMessage.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_sign_out)
        {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Snackbar.make(activity_main,"You have been signed out.", Snackbar.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
        return true;
    }
}
