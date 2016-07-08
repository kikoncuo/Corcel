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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

    private String user_name,room_name,room_pass;
    private DatabaseReference root =  FirebaseDatabase.getInstance().getReference();
    DatabaseReference room_root;
    private String temp_key;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);

        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.messageEdit);
        sendBtn = (Button) findViewById(R.id.chatSendButton);


        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
        adapter = new ChatAdapter(PlayScreen.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);
        user_name = getIntent().getExtras().get("user_name").toString();
        room_name = getIntent().getExtras().get("room_name").toString();
        room_pass = getIntent().getExtras().get("room_pass").toString();
        setTitle(" Room - "+room_name);

        root = FirebaseDatabase.getInstance().getReference().child("message");
        room_root = root.child(room_name);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map<String,Object> map = new HashMap<>();
                temp_key = root.push().getKey();
                root.updateChildren(map);
                room_root.updateChildren(map);
                DatabaseReference message_root = room_root.child("Msg"+temp_key);
                Map<String,Object> map2 = new HashMap<>();
                map2.put("user_name",user_name);
                //Encrypt the txt
                String encrypted_txt="";
                try{
                    AesCbcWithIntegrity.SecretKeys keys = AesCbcWithIntegrity.generateKeyFromPassword(room_pass, "S4ltyS4ltRand0mWriT1ngoNtheWall".getBytes());
                    AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = AesCbcWithIntegrity.encrypt(messageET.getText().toString(), keys);
                    encrypted_txt = (cipherTextIvMac.toString());
                }catch(Exception exe){
                    //TODO:Handle this at least in console
                }
                //Save encrypted text
                map2.put("text",encrypted_txt);
                //TODO: store the ID of the room instead of the name
                map2.put("room_name",room_name);

                message_root.updateChildren(map2);

                messageET.setText("");
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








    private String chat_msg,chat_user_name,chat_room,clear_msg;

    private void append_chat_conversation(DataSnapshot dataSnapshot) {

        //TODO: make this async
        String test = dataSnapshot.getKey();
        chat_room = dataSnapshot.child("room_name").getValue().toString();
        chat_msg = dataSnapshot.child("text").getValue().toString();
        //Try to decipher
        try {
            AesCbcWithIntegrity.SecretKeys keys = AesCbcWithIntegrity.generateKeyFromPassword(room_pass, "S4ltyS4ltRand0mWriT1ngoNtheWall".getBytes());
            AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(chat_msg);
            clear_msg = AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys);
        } catch (Exception exe) {
            //TODO:Handle this at least in console
        }
        chat_user_name = dataSnapshot.child("user_name").getValue().toString();
        //chat_conversation.append(chat_user_name + " : " + clear_msg + " \n");
        if(clear_msg != null){
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setId(122);//dummy
            chatMessage.setMessage(clear_msg);
            chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
            chatMessage.setUser(chat_user_name);
            chatMessage.setMe(chat_user_name.equals(user_name));

            displayMessage(chatMessage);
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