package com.example.letitgoat.ui.sell.sell_recycler;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.letitgoat.AddingItemToMarketplace;
import com.example.letitgoat.SingleShotLocationProvider;
import com.example.letitgoat.WPILocationHelper;
import com.example.letitgoat.db_models.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letitgoat.MainActivity;
import com.example.letitgoat.R;
import com.example.letitgoat.db_models.Item;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ItemClickListener mClickListener;
    private Context mContext;
    private List<Item> usersItemsOnMarket;
    private FirebaseFirestore db;

    SellViewAdapter(Context context) {
        this.mContext = context;
        this.db = FirebaseFirestore.getInstance();
        this.usersItemsOnMarket = new ArrayList<>();

        db.collection("Items")
                .whereEqualTo("user", MainActivity.Companion.getUser())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> doc = document.getData();
                                HashMap<String, Object> hash = (HashMap<String, Object>) doc.get("user");
                                User u = new User(hash.get("email").toString(), hash.get("name").toString(), hash.get("profilePicture").toString());
                                Date d = ((Timestamp) doc.get("postedTimeStamp")).toDate();
                                Location l = (Location) doc.get("pickupLocation");
                                WPILocationHelper wpiLocationHelper = new WPILocationHelper();
                                if (l == null) {
                                    l = wpiLocationHelper.getLocationOfGordonLibrary();
                                }
                                Item i = new Item(
                                        doc.get("name").toString(),
                                        Double.valueOf(doc.get("price").toString()),
                                        u,
                                        doc.get("description").toString(),
                                        d,
                                        (List<String>) doc.get("stringsOfBitmapofPicuresOfItem"),
                                        l
                                );
                                usersItemsOnMarket.add(i);
                                notifyDataSetChanged();
                            }
                        } else {
                            System.out.println("Could not get the user's items for selling from the DB");
                        }
                    }
                });
    }

    // Provide a reference to the views for each data single_buy
    // Complex data items may need more than one view per single_buy, and
    // you provide access to all the views for a data single_buy in a view holder
    public class ItemsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView image;
        private TextView name;
        private TextView price;
        private TextView date;
        private TextView pickupLocation;

        ItemsViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.itemImage);
            name = v.findViewById(R.id.name);
            price = v.findViewById(R.id.price);
            date = v.findViewById(R.id.date);
            pickupLocation = v.findViewById(R.id.location);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return position;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;
        view = inflater.inflate(R.layout.single_sell, parent, false);
        viewHolder = new ItemsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        final Item i = this.usersItemsOnMarket.get(position);
        ((ItemsViewHolder)holder).name.setText(i.getName());
        ((ItemsViewHolder)holder).price.setText("$" + i.getPrice());

        final Location[] l = {null};
        SingleShotLocationProvider.requestSingleUpdate(
                mContext,
                new SingleShotLocationProvider.LocationCallback() {
                    @Override public void onNewLocationAvailable(Location location) {
                        Log.d("Location", "my location is " + location.getLatitude() + "  " + location.getLongitude());

                        DecimalFormat df = new DecimalFormat("###.##");

                        System.out.println(i.getPickupLocation().getLatitude() + " " + i.getPickupLocation().getLongitude());
                        ((ItemsViewHolder)holder).pickupLocation.setText(
                                i.getPickupLocation().getProvider() + ": " +
                                        df.format(location.distanceTo(i.getPickupLocation()) * 0.000621371)
                                        + " miles away"
                        );
                    }
                });
        ((ItemsViewHolder)holder).pickupLocation.setText(i.getPickupLocation().getProvider());

        //Extra zero if the price doesn't have one
        if(((ItemsViewHolder)holder).price.getText().toString().split("\\.")[1].length() == 1){
            ((ItemsViewHolder)holder).price.setText("$" + i.getPrice() + "0");
        }

        ((ItemsViewHolder)holder).date.setText(i.getPostedTimeStamp().toString());

        byte[] encodeByte = Base64.decode(i.getStringsOfBitmapofPicuresOfItem().get(0), Base64.DEFAULT);
        Bitmap b = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);

        Matrix matrix = new Matrix();

        matrix.postRotate(90);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, b.getWidth(), b.getHeight(), true);

        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        ((ItemsViewHolder)holder).image.setImageBitmap(rotatedBitmap);
    }

    @Override
    public int getItemCount() {
        return this.usersItemsOnMarket.size();
    }


    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
