package urba.com.corcel.Views;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.rehacktive.waspdb.WaspDb;
import net.rehacktive.waspdb.WaspFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import urba.com.corcel.Libraries.AesCbcWithIntegrity;
import urba.com.corcel.Models.Room;
import urba.com.corcel.R;

public class RoomSelect extends AppCompatActivity {

    private FloatingActionButton add_room;
    private EditText room_name;

    private SearchView input_search;
    EditText password_join_editText;

    private ListView listView;
    private ArrayAdapter<Room> arrayAdapter;
    private ArrayList<Room> list_of_rooms = new ArrayList<>();
    private String name;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference roomNames = root.child("RoomNames");
    private DatabaseReference messages = root.child("message");
    private DataSnapshot dataSnapshot_downloaded;
    private String temp_key;
    private String room_pass, current_user_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_select);
        // Adding Toolbar to Main screen
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        add_room = (FloatingActionButton) findViewById(R.id.btn_add_room);
        listView = (ListView) findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,list_of_rooms);

        listView.setAdapter(arrayAdapter);

        //We get the name from local db now
        String path = getFilesDir().getPath();
        String databaseName = "myDb";
        String password = "passw0rdsdfgbshgv";
        WaspDb db = WaspFactory.openOrCreateDatabase(path,databaseName,password);
        name = db.openOrCreateHash("user").getAllValues().get(0).toString();


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
                        room_pass = room_password.getText().toString();

                        //TODO: limpiar esto con un metodo que haga la encriptacion y desencriptacion completas
                        String encrypted_pass="";
                        if (!room_password.getText().toString().equals("")) {
                            try {
                                AesCbcWithIntegrity.SecretKeys keys = AesCbcWithIntegrity.generateKeyFromPassword(room_pass, "shortsalt".getBytes());
                                AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = AesCbcWithIntegrity.encrypt(room_password.getText().toString(), keys);
                                encrypted_pass = (cipherTextIvMac.toString());
                            } catch (Exception exe) {
                                //TODO:Handle this at least in console
                            }
                        }


                        map.put("room_hash",encrypted_pass);
                        temp_key = roomNames.push().getKey();
                        roomNames.child("Room"+temp_key).updateChildren(map);

                        startChatRoomActivity(room_name.getText().toString(), room_pass, room_pass.equals(""), name, current_user_key);

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
                dataSnapshot_downloaded = dataSnapshot;
                refreshRooms(dataSnapshot_downloaded);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

               Room room = (Room) adapterView.getItemAtPosition(i);

                request_password_room(room);
            }
        });
    }


    private void request_password_room(final Room room) {
        if (!room.isNoPass()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter the password for the room:");

                password_join_editText = new EditText(this);
                password_join_editText.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);

            builder.setView(password_join_editText);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    joinRoom(room);
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            builder.show();
        }else{
            joinRoom(room);
        }
    }

    private void joinRoom (Room room){


        if(room.isNoPass()) {
            room_pass = "";
            startChatRoomActivity(room.getName(), room_pass, room.isNoPass(), name, current_user_key);

        }else{
            room_pass = password_join_editText.getText().toString();
            try {
                AesCbcWithIntegrity.SecretKeys keys = AesCbcWithIntegrity.generateKeyFromPassword(room_pass, "shortsalt".getBytes());
                AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(room.getHash());
                AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys);

                startChatRoomActivity(room.getName(), room_pass, room.isNoPass(), name, current_user_key);

            } catch (Exception exe) {
                //TODO:Crear Dialogo de error
                AlertDialog.Builder builder = new AlertDialog.Builder(RoomSelect.this);
                LinearLayout layout = new LinearLayout(RoomSelect.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                builder.setTitle("Wrong password");
                builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.show();
            }
        }
    }

    public void refreshRooms(DataSnapshot dataSnapshot){
        Set<Room> set = new HashSet<>();
        Iterator i = dataSnapshot.getChildren().iterator();
        while (i.hasNext()){
            DataSnapshot roomSnapshot = (DataSnapshot)i.next();
            String room_name = roomSnapshot.child("room_name").getValue().toString();
            String room_id = roomSnapshot.getKey().toString();
            String room_hash = roomSnapshot.child("room_hash").getValue().toString();
            boolean room_no_pass = room_hash.equals("");
            set.add(new Room(room_name, room_id, room_hash, room_no_pass));
        }

        list_of_rooms.clear();
        list_of_rooms.addAll(set);

        arrayAdapter.notifyDataSetChanged();
    }

    private void startChatRoomActivity(String room_name, String room_pass, boolean no_pass, String user_name, String user_key){
        Intent intent = new Intent(getApplicationContext(), PlayScreen.class);
        intent.putExtra("room_no_pass", no_pass);
        intent.putExtra("room_name", room_name);
        intent.putExtra("user_name", user_name);
        intent.putExtra("room_pass", room_pass);
        intent.putExtra("user_key", user_key);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.room_select_menu, menu);

        MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        input_search = (SearchView) myActionMenuItem.getActionView();
        input_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                RoomSelect.this.arrayAdapter.getFilter().filter(newText);
                return true;
            }
        });

        return true;
    }
}