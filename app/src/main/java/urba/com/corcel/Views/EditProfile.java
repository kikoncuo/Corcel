package urba.com.corcel.Views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.rehacktive.waspdb.WaspDb;
import net.rehacktive.waspdb.WaspFactory;
import net.rehacktive.waspdb.WaspHash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import urba.com.corcel.R;

/**
 * Created by Enrique on 7/6/2016.
 */
public class EditProfile extends AppCompatActivity {

    private EditText edit_name;
    private TextView text_view_user_key;
    private Button button_change_name;
    private Button button_key_explanation;
    private ListView list_friends;
    private EditText edit_search_friends;
    private Button button_search_friends;
    private List<String> list_friends_names;
    private ArrayAdapter<String> listAdapter ;
    private WaspHash user_local;
    private WaspHash friends_local;
    private DatabaseReference root_firebase = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference users_firebase = root_firebase.child("Users");

    String user_key;
    String current_name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        edit_name = (EditText) findViewById(R.id.editName);
        text_view_user_key = (TextView) findViewById(R.id.userKey);
        button_change_name = (Button) findViewById(R.id.buttonChangeName);
        button_key_explanation = (Button) findViewById(R.id.buttonKeyExplanation);
        list_friends = (ListView) findViewById(R.id.listFriends);
        edit_search_friends = (EditText) findViewById(R.id.editTextSearchFriends);
        button_search_friends = (Button) findViewById(R.id.buttonSearchFriends);


        // create a database, using the default files dir as path, database name and a password
        String path = getFilesDir().getPath();
        String databaseName = "myDb";
        String password = "passw0rdsdfgbshgv";
        WaspDb db = WaspFactory.openOrCreateDatabase(path, databaseName, password);
        user_local = db.openOrCreateHash("user");

        List<String> usrNames = user_local.getAllValues();
        List<String> usrKeys = user_local.getAllKeys();
        current_name = usrNames.get(0).toString();
        user_key = usrKeys.get(0).toString();
        edit_name.setText(current_name);
        text_view_user_key.setText(user_key);

        friends_local = db.openOrCreateHash("friends");
        list_friends_names = friends_local.getAllValues();
        if (list_friends_names.size() == 0) {
            List<String> nofriends = new ArrayList<String>();
            nofriends.add("You have no friends, and that's sad :(");
            addFriends(nofriends);
        } else {
            addFriends(list_friends_names);
        }


        friends_local = db.openOrCreateHash("friends");


        button_change_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!current_name.equals(edit_name.getText().toString())) {
                    user_local.remove(user_key);
                    DatabaseReference actualuser = users_firebase.child(user_key);
                    actualuser.removeValue();
                    Map<String, Object> map = new HashMap<>();
                    map.put("user_name", edit_name.getText().toString());
                    users_firebase.child(user_key).updateChildren(map);
                    user_local.put(user_key, edit_name.getText().toString());
                    Toast.makeText(view.getContext(), "Your name was changed", Toast.LENGTH_LONG).show();
                }
            }
        });

        button_key_explanation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Description:");

                final TextView explanation = new TextView(view.getContext());
                explanation.setText("This random id is unique to you and it's what you should use when looking for your friends");
                builder.setView(explanation);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                    }
                });

                builder.show();
            }

        });

        button_search_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean[] existsFriend = {true};
                final boolean[] flag = {false};
                String key_searched_friend = edit_search_friends.getText().toString();
                DatabaseReference friend_found = users_firebase.child(key_searched_friend);
                final String[] name_searched_friend = {""};
                /*friend_found.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if(snapshot.child("user_name").exists()) {
                            name_searched_friend[0] = snapshot.child("user_name").getValue().toString();
                        } else {
                            existsFriend[0] = false;
                        }
                        flag[0] = true;
                    }
                    @Override
                    public void onCancelled(DatabaseError firebaseError) {
                    }
                });*/

                if(!name_searched_friend[0].equals(null)){
                    friends_local.put(key_searched_friend, name_searched_friend[0]);
                    addFriends(list_friends_names);
                }else{
                    //If friend is not found we tell them so in a popup
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this);
                    builder.setTitle("Friend not found");
                    builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    builder.show();
                }
            }
        });
    }
    private void addFriends(List<String> friends){

        ArrayList<String> planetList = new ArrayList<String>();
        planetList.addAll(friends);
        // Create ArrayAdapter using the planet list.
        listAdapter = new ArrayAdapter<String>(this, R.layout.simple_row, planetList);
        // Set the ArrayAdapter as the ListView's adapter.
        list_friends.setAdapter( listAdapter );
    }
}
