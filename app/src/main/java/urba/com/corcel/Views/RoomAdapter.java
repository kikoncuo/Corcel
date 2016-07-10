package urba.com.corcel.Views;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import urba.com.corcel.Models.Room;
import urba.com.corcel.R;

/**
 * Created by aleja_000 on 10/07/2016.
 */
public class RoomAdapter<T> extends ArrayAdapter<Room> {
    private final Activity context;
    private final ArrayList<Room> itemname;


    public RoomAdapter(Activity context, ArrayList<Room> itemname) {
        super(context, R.layout.row, itemname);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.itemname=itemname;

    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.row, null,true);

        TextView room_name = (TextView) rowView.findViewById(R.id.room_name);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

        Room room = itemname.get(position);
        room_name.setText(room.getName());
        if(!room.isNoPass()){
            imageView.setImageResource(R.drawable.ic_lock_outline_black_24dp);
        }
        else{
            imageView.setImageResource(R.drawable.ic_lock_open_black_24dp);
        }

        return rowView;

    };
}
