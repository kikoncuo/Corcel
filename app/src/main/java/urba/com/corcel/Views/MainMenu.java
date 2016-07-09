package urba.com.corcel.Views;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
public class MainMenu  extends AppCompatActivity {

    private ListView listView;
    private String name;
    private String usrKey;
    private List<String> usrKeys;
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference usersTemp = root.child("Users");
    private  WaspHash user;
    private ArrayAdapter<String> listAdapter ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        listView = (ListView) findViewById(R.id.listViewMainMenu);
        // Create a list with the items.
        String[] menu_options = new String[] { "Public rooms", "Private messages", "Files", "Edit profile"};
        ArrayList<String> planetList = new ArrayList<String>();
        planetList.addAll( Arrays.asList(menu_options) );

        // Create ArrayAdapter using the planet list.
        listAdapter = new ArrayAdapter<String>(this, R.layout.simple_row, planetList);
        // Set the ArrayAdapter as the ListView's adapter.
        listView.setAdapter( listAdapter );

        // create a database, using the default files dir as path, database name and a password
        String path = getFilesDir().getPath();
        String databaseName = "myDb";
        String password = "passw0rdsdfgbshgv";
        WaspDb db = WaspFactory.openOrCreateDatabase(path,databaseName,password);

        //UNCOMMENT THIS LINE TO RESET WASPDB
        db.removeHash("user");
        //db.removeHash("friends");

        // now create an WaspHash, it's like a sql table
        user = db.openOrCreateHash("user");
        usrKeys = user.getAllValues();
        if (usrKeys.size() == 0){
            request_user_name();
        }else{
            name = usrKeys.get(0).toString();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i==0) {
                    Intent intent = new Intent(getApplicationContext(), RoomSelect.class);
                    intent.putExtra("user_name", name);
                    List<String>  allkeys = user.getAllKeys();
                    intent.putExtra("user_key", allkeys.get(0));
                    startActivity(intent);
                }else if (i == 3){
                    Intent intent = new Intent(getApplicationContext(), EditProfile.class);
                    startActivity(intent);
                }

            }
        });
    }


    private void request_user_name() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter name:");

        final EditText input_field = new EditText(this);

        builder.setView(input_field);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                name = input_field.getText().toString();
                usrKey = usersTemp.push().getKey();
                Map<String,Object> map = new HashMap<>();
                map.put("user_name",name);
                usersTemp.child("User"+usrKey).updateChildren(map);
                user.put("User"+usrKey, name);

            }
        });

        builder.show();
    }

}


