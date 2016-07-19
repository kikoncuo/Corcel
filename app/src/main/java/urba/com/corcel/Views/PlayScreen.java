package urba.com.corcel.Views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.rehacktive.waspdb.WaspDb;
import net.rehacktive.waspdb.WaspFactory;
import net.rehacktive.waspdb.WaspHash;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import urba.com.corcel.Libraries.AesCbcWithIntegrity;
import urba.com.corcel.Models.ChatMessage;
import urba.com.corcel.R;


public class PlayScreen extends AppCompatActivity {
    private EditText messageET;
    private ListView messagesContainer;
    private Button sendBtn;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;
    List<String> list_local_messages_keys;

    Date data_newest_local = new Date(12);
    SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa");

    private String user_name,room_name,room_pass, current_user_key, room_key;
    private boolean room_no_pass;
    private DatabaseReference root =  FirebaseDatabase.getInstance().getReference();
    DatabaseReference room_root;
    private String temp_key;
    AesCbcWithIntegrity.SecretKeys keys;
    private WaspHash messages_local;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);

        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.messageEdit);
        sendBtn = (Button) findViewById(R.id.chatSendButton);

        user_name = getIntent().getExtras().get("user_name").toString();
        room_name = getIntent().getExtras().get("room_name").toString();
        room_pass = getIntent().getExtras().get("room_pass").toString();
        current_user_key = getIntent().getExtras().get("user_key").toString();
        room_key = getIntent().getExtras().get("room_key").toString();
        room_no_pass = (boolean)getIntent().getExtras().get("room_no_pass");
        adapter = new ChatAdapter(PlayScreen.this, new ArrayList<ChatMessage>(), current_user_key);
        messagesContainer.setAdapter(adapter);

        //Initialize local db
        //TODO: create diferent branch in local db for each room
        String path = getFilesDir().getPath();
        String databaseName = "messages";
        String password = "passw0rdsdfgbshgvv";
        WaspDb db = WaspFactory.openOrCreateDatabase(path,databaseName,password);
        messages_local = db.openOrCreateHash(room_key);

        list_local_messages_keys = messages_local.getAllKeys();
        List<ChatMessage> list_local_messages = messages_local.getAllValues();


        for (ChatMessage message : list_local_messages)
        {
            try{
                Date date_local = formatter.parse(message.getDate());
                displayMessage(message);
                if (data_newest_local.compareTo(date_local)<0)
                {
                    data_newest_local=date_local;
                }
            }catch (ParseException e1){
                e1.printStackTrace();
            }

        }

        setTitle(" Room - "+room_name);



        root = FirebaseDatabase.getInstance().getReference().child("message");
        room_root = root.child(room_name);

        //TODO: make this an async task
        try {
            keys = AesCbcWithIntegrity.generateKeyFromPassword(room_pass, "shortsalt".getBytes());
        } catch (Exception exe) {

        }

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!messageET.getText().toString().equals("")) {
                    Map<String, Object> map = new HashMap<>();
                    temp_key = root.push().getKey();
                    root.updateChildren(map);
                    room_root.updateChildren(map);
                    DatabaseReference message_root = room_root.child("Msg" + temp_key);
                    Map<String, Object> map2 = new HashMap<>();
                    map2.put("user_key", current_user_key);
                    //Encrypt the txt
                    String encrypted_txt = "";
                    //TODO: make this async
                    if (!room_no_pass){
                        try {
                            AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = AesCbcWithIntegrity.encrypt(messageET.getText().toString(), keys);
                            encrypted_txt = (cipherTextIvMac.toString());
                        } catch (Exception exe) {
                            //TODO:Handle this at least in console
                        }
                    }else{
                        encrypted_txt = messageET.getText().toString();
                    }
                    //Save encrypted text
                    map2.put("text", encrypted_txt);
                    //TODO: store the ID of the room instead of the name
                    map2.put("room_name", room_name);

                    map2.put("user_name", user_name);

                    map2.put("msg_time", DateFormat.getDateTimeInstance().format(new Date()));

                    message_root.updateChildren(map2);


                    messageET.setText("");
                }
            }
        });

        room_root.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                append_chat_conversation(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                append_chat_conversation(dataSnapshot);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    private String chat_msg,chat_user_key,chat_room,clear_msg;

    private void append_chat_conversation(DataSnapshot dataSnapshot) {
        String message_time = dataSnapshot.child("msg_time").getValue().toString();
        String messageKey = dataSnapshot.getKey();
        try{
            Date date_firebase = formatter.parse(message_time);
            //TODO: Optimize this so it only does the check once
            if (data_newest_local.compareTo(date_firebase)<0)
            {
                chat_room = dataSnapshot.child("room_name").getValue().toString();
                chat_msg = dataSnapshot.child("text").getValue().toString();
                //Try to decipher
                try {
                    AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(chat_msg);
                    clear_msg = AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys);
                } catch (Exception exe) {
                    clear_msg = chat_msg;
                }
                chat_user_key = dataSnapshot.child("user_key").getValue().toString();
                if (clear_msg != null) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setId(messageKey);
                    chatMessage.setMessage(clear_msg);
                    chatMessage.setDate(dataSnapshot.child("msg_time").getValue().toString());
                    chatMessage.setUserId(dataSnapshot.child("user_key").getValue().toString());
                    chatMessage.setUser(dataSnapshot.child("user_name").getValue().toString());
                    chatMessage.setMe(chat_user_key.equals(current_user_key));
                    displayMessage(chatMessage);
                    messages_local.put(messageKey, chatMessage);
                }
            }
        }catch (ParseException e1){
            e1.printStackTrace();
        }



    }
    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }


}