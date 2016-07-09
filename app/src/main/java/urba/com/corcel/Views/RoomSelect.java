package urba.com.corcel.Views;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import urba.com.corcel.Libraries.AesCbcWithIntegrity;
import urba.com.corcel.R;

public class RoomSelect extends AppCompatActivity {

    private FloatingActionButton add_room;
    private EditText room_name;

    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_rooms = new ArrayList<>();
    private String name;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference roomNames = root.child("RoomNames");
    private DatabaseReference messages = root.child("message");
    private String temp_key;
    private String room_pass, current_user_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_select);
        add_room = (FloatingActionButton) findViewById(R.id.btn_add_room);
        listView = (ListView) findViewById(R.id.listView);

        arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,list_of_rooms);

        listView.setAdapter(arrayAdapter);

        name = getIntent().getExtras().get("user_name").toString();
        current_user_key = getIntent().getExtras().get("user_key").toString();


        add_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(RoomSelect.this);
                LinearLayout layout = new LinearLayout(RoomSelect.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                builder.setTitle("Enter the name and password of the room:");

                final EditText room_name = new EditText(RoomSelect.this);
                final EditText room_password = new EditText(RoomSelect.this);
                room_password.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
                layout.addView(room_name);
                layout.addView(room_password);
                builder.setView(layout);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO: can't create 2 rooms with the same name

                        Map<String,Object> map = new HashMap<>();
                        map.put("room_name",room_name.getText().toString());
                        temp_key = roomNames.push().getKey();
                        roomNames.child("Room"+temp_key).updateChildren(map);
                        room_pass = room_password.getText().toString();
                        Intent intent = new Intent(getApplicationContext(),PlayScreen.class);
                        intent.putExtra("room_name",room_name.getText().toString() );
                        intent.putExtra("user_name",name);
                        intent.putExtra("room_pass",room_pass);
                        intent.putExtra("user_key",current_user_key);
                        room_name.setText("");
                        startActivity(intent);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.show();
            }
        });

        roomNames.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<>();
                Iterator i = dataSnapshot.getChildren().iterator();
                while (i.hasNext()){
                    set.add(((DataSnapshot)i.next()).child("room_name").getValue().toString());
                }

                list_of_rooms.clear();
                list_of_rooms.addAll(set);

                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                request_password_room(((TextView)view).getText().toString());
            }
        });

    }


    private void request_password_room(final String roomName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the password for the room:");

        final EditText input_field = new EditText(this);
        input_field.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input_field);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                root.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> i = dataSnapshot.child("message").child(roomName).getChildren();
                        String chat_msg = ((DataSnapshot)i.iterator().next()).child("text").getValue().toString();
                        room_pass = input_field.getText().toString();

                        try {
                            AesCbcWithIntegrity.SecretKeys keys = AesCbcWithIntegrity.generateKeyFromPassword(room_pass, "S4ltyS4ltRand0mWriT1ngoNtheWall".getBytes());
                            AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(chat_msg);
                            AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys);

                            Intent intent = new Intent(getApplicationContext(),PlayScreen.class);
                            intent.putExtra("room_name",roomName );
                            intent.putExtra("user_name",name);
                            intent.putExtra("room_pass",room_pass);
                            intent.putExtra("user_key",current_user_key);
                            startActivity(intent);
                        } catch (Exception exe) {
                            //TODO:Crear Dialogo de error
                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }
}