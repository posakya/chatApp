package com.roshan.posakya.chatapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.library.bubbleview.BubbleTextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.roshan.posakya.chatapplication.progressDialog.ShowProgress;
import com.scottyab.aescrypt.AESCrypt;


import java.security.GeneralSecurityException;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class MainActivity extends AppCompatActivity {

    private static int SIGN_IN_REQUEST_CODE = 1;
//    private FirebaseRecyclerAdapter adapter;
    private FirebaseListAdapter<ChatMessage> adapter;
    RelativeLayout activity_main;
    ShowProgress progress;

    //Add Emojicon
    EmojiconEditText emojiconEditText;
    ImageView emojiButton,submitButton;
    EmojIconActions emojIconActions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main = (RelativeLayout)findViewById(R.id.activity_main);

        progress = new ShowProgress(MainActivity.this);

        //Add Emoji
        emojiButton = findViewById(R.id.emoji_button);
        submitButton = findViewById(R.id.submit_button);
        emojiconEditText = findViewById(R.id.emojicon_edit_text);
        emojIconActions = new EmojIconActions(getApplicationContext(),activity_main,emojiButton,emojiconEditText);
        emojIconActions.ShowEmojicon();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    String password = String.valueOf(R.string.app_name);
                    String encryptedMsg = AESCrypt.encrypt(password,emojiconEditText.getText().toString());
                    FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(encryptedMsg,
                            FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));

                    emojiconEditText.setText("");
                    emojiconEditText.requestFocus();
                    adapter.notifyDataSetChanged();
                }catch (GeneralSecurityException e){
                    //handle error
                }

            }
        });

        //Check if not sign-in then navigate Signing page
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
        progress.showProgress();

        final ListView listOfMessage = (ListView)findViewById(R.id.list_of_message);

        /// using query method ///
        final Query query = FirebaseDatabase.getInstance().getReference();



        FirebaseApp.initializeApp(this);

        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .setLayout(R.layout.list_item)
                .build();




        adapter = new FirebaseListAdapter<ChatMessage>(options)
        {
            @Override
            protected void populateView(View v, final ChatMessage model, final int position) {
                progress.hideProgress();
                //Get references to the views of list_item.xml
                TextView messageText, messageUser, messageTime,message_text_other;
                messageText = (BubbleTextView) v.findViewById(R.id.message_text);
                message_text_other = (BubbleTextView) v.findViewById(R.id.message_text_other);
                messageUser = (TextView) v.findViewById(R.id.message_user);
                messageTime = (TextView) v.findViewById(R.id.message_time);

                String messageAfterDecrypt = "";

                try {
                    String password = String.valueOf(R.string.app_name);
                    messageAfterDecrypt = AESCrypt.decrypt(password, model.getMessageText());
                    messageText.setText(messageAfterDecrypt);
                    message_text_other.setText(messageAfterDecrypt);
                }catch (GeneralSecurityException e){
                    //handle error - could be due to incorrect password or tampered encryptedMsg
                }

                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:aa)", model.getMessageTime()));


                String author = model.getMessageUser();
                System.out.println("Current User : "+FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                System.out.println("User : "+author);

                if (author != null && author.equals(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())){

                    message_text_other.setVisibility(View.VISIBLE);
                    messageText.setVisibility(View.GONE);

                }else{
                    message_text_other.setVisibility(View.GONE);
                    messageText.setVisibility(View.VISIBLE);

                    }


                listOfMessage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {


                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_HOLO_LIGHT).create();

                        alertDialog.setTitle(R.string.app_name);
                        alertDialog.setMessage(Html.fromHtml("Would you like to delete?"));

                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        adapter.getRef(i).removeValue();
                                        adapter.notifyDataSetChanged();
                                        // Snackbar.make(activity_main,"We couldn't sign you in.Please try again later", Snackbar.LENGTH_SHORT).show();
                                        Snackbar.make(activity_main, " Deleted",Snackbar.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });

                        alertDialog.show();
                        final Button neutralButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                        final Button positveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        neutralButton.setTextColor(getResources().getColor(R.color.colorAccent));
                        positveButton.setTextColor(getResources().getColor(R.color.colorAccent));


                    }
                });

            }
        };

        if (adapter.getCount() == 0){
            progress.hideProgress();
            Snackbar.make(activity_main," Chat is empty...",Snackbar.LENGTH_SHORT).show();

        }
        listOfMessage.setAdapter(adapter);



    }





//    private void displayChatMessage() {
//        progress.showProgress();
//
//        final RecyclerView listOfMessage = findViewById(R.id.list_of_message );
//
//        /// using query method ///
//        final Query query = FirebaseDatabase.getInstance().getReference();
//        FirebaseApp.initializeApp(this);
//
//
//        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
//        linearLayoutManager.setStackFromEnd(true);
//        listOfMessage.setLayoutManager(linearLayoutManager);
//        listOfMessage.setHasFixedSize(true);
//
//
//        FirebaseRecyclerOptions<ChatMessage> options =
//                new FirebaseRecyclerOptions.Builder<ChatMessage>()
//                        .setQuery(query, ChatMessage.class)
//                        .build();
//
//
//         adapter = new FirebaseRecyclerAdapter<ChatMessage, ChatHolder>(options) {
//
//            @Override
//            protected void onBindViewHolder(@NonNull ChatHolder holder, int position, @NonNull ChatMessage model) {
//                progress.hideProgress();
//
//                adapter.getItemCount();
//
//                listOfMessage.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//                    @Override
//
//                    public void onLayoutChange(View v, int left, int top, int right,int bottom, int oldLeft, int oldTop,int oldRight, int oldBottom)
//                    {
//
//                        linearLayoutManager.scrollToPosition(adapter.getItemCount()-1);
//
//                    }
//                });
//
//
////                listOfMessage.scrollToPosition(adapter.getItemCount()-1);
//                System.out.println("Count : "+adapter.getItemCount());
//                holder.messageText.setText( model.getMessageText() );
//                holder.messageUser.setText( model.getMessageUser() );
//                holder.messageTime.setText( DateFormat.format("dd-MM-yyyy (HH:mm:ss)",model.getMessageTime()) );
//
//            }
//
//            @Override
//            public ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//                // Create a new instance of the ViewHolder, in this case we are using a custom
//                // layout called R.layout.message for each item
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.list_item, parent, false);
//
//                return new ChatHolder(view);
//            }
//
//
//        };
//
//        listOfMessage.setAdapter(adapter);
//
//
//    }
    @Override
    protected void onStart() {
        super.onStart();
        try {
            adapter.startListening();
        }catch (Exception e){

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        try{
            adapter.stopListening();
        }catch (Exception e){
            e.printStackTrace();
        }

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
