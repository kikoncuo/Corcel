package urba.com.corcel.Views;

import android.content.Context;
import android.content.DialogInterface;
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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.rehacktive.waspdb.WaspDb;
import net.rehacktive.waspdb.WaspFactory;
import net.rehacktive.waspdb.WaspHash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ArrayAdapter<String> listAdapter ;
    private WaspHash user_local;
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

        nofriends();


        button_change_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user_local.remove(user_key);
                DatabaseReference actualuser = users_firebase.child(user_key);
                actualuser.removeValue();
                Map<String, Object> map = new HashMap<>();
                map.put("user_name", edit_name.getText().toString());
                users_firebase.child(user_key).updateChildren(map);
                user_local.put(user_key, edit_name.getText().toString());
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
    }
    private void nofriends(){
        //Right now no one has friends, call method
        String[] no_friends = new String[] { "You have no friends, and that's sad :("};
        ArrayList<String> planetList = new ArrayList<String>();
        planetList.addAll( Arrays.asList(no_friends) );
        // Create ArrayAdapter using the planet list.
        listAdapter = new ArrayAdapter<String>(this, R.layout.simple_row, planetList);
        // Set the ArrayAdapter as the ListView's adapter.
        list_friends.setAdapter( listAdapter );
    }
}
