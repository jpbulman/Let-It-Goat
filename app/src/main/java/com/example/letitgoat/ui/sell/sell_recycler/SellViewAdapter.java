package com.example.letitgoat.ui.sell.sell_recycler;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.Layout;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.letitgoat.SingleShotLocationProvider;
import com.example.letitgoat.WPILocationHelper;
import com.example.letitgoat.db_models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.letitgoat.MainActivity;
import com.example.letitgoat.R;
import com.example.letitgoat.db_models.Item;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    private List<String> usersItemsOnMarketIds;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private List<ItemsViewHolder> itemsViewHolders;

    SellViewAdapter(Context context) {
        this.mContext = context;
        this.db = FirebaseFirestore.getInstance();
        this.usersItemsOnMarket = new ArrayList<>();
        this.usersItemsOnMarketIds = new ArrayList<>();
        this.storage = FirebaseStorage.getInstance();
        this.itemsViewHolders = new ArrayList<>();

        db.collection("Items")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> doc = document.getData();
                                HashMap<String, Object> hash = (HashMap<String, Object>) doc.get("user");
                                User u = new User(hash.get("email").toString(), hash.get("name").toString(), hash.get("profilePicture").toString());

                                if(!u.getEmail().equals(MainActivity.Companion.getUser().getEmail())){
                                    continue;
                                }

                                Date d = ((Timestamp) doc.get("postedTimeStamp")).toDate();
                                WPILocationHelper wpiLocationHelper = new WPILocationHelper();
                                Location l = wpiLocationHelper.getLocationOfGordonLibrary();
                                if(doc.get("pickupLocation") != null){
                                    HashMap<String, Object> mapper = (HashMap<String, Object>) doc.get("pickupLocation");
                                    l = new Location(mapper.get("provider").toString());
                                    l.setLatitude(Double.valueOf(mapper.get("latitude").toString()));
                                    l.setLongitude(Double.valueOf(mapper.get("longitude").toString()));
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
                                usersItemsOnMarketIds.add(document.getId());
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
        private VideoView videoView;
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

    private void updateListItem(int position, File video) {
        RecyclerView view = ((Activity) mContext).findViewById(R.id.sell_recyclerview);
        View v = view.getLayoutManager().findViewByPosition(position);

        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                0,
                0,
                0
        );

        LinearLayout.LayoutParams videoParams = new LinearLayout.LayoutParams(
                0,
                500,
                2
        );
        videoParams.leftMargin = 45;

        ImageView imageView = v.findViewById(R.id.itemImage);
        imageView.setLayoutParams(imageParams);

        VideoView vid = v.findViewById(R.id.itemVideo);
        vid.setVideoURI(Uri.fromFile(video));
        vid.setLayoutParams(videoParams);
        vid.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        vid.start();
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final Item i = this.usersItemsOnMarket.get(position);
        ((ItemsViewHolder)holder).name.setText(i.getName());
        ((ItemsViewHolder)holder).price.setText("$" + i.getPrice());

        SingleShotLocationProvider.requestSingleUpdate(
                mContext,
                new SingleShotLocationProvider.LocationCallback() {
                    @Override public void onNewLocationAvailable(Location location) {

                        DecimalFormat df = new DecimalFormat("###.##");

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

        if(i.getStringsOfBitmapofPicuresOfItem().isEmpty()){
            final String docId = this.usersItemsOnMarketIds.get(position);

            StorageReference storageRef = storage.getReference();

            StorageReference pathReference = storageRef.child(docId + "/VideoFileName.mp4");

            final long FIFTY_MEGABYTES = 1024 * 1024 * 50;
            pathReference.getBytes(FIFTY_MEGABYTES).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    File downloaderFilee = writeByte(bytes, docId);
//                    System.out.println(getFileSizeMegaBytes(downloaderFilee));
//                    System.out.println(downloaderFilee.getName());
                    updateListItem(position, downloaderFilee);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        } else {
            byte[] encodeByte = Base64.decode(i.getStringsOfBitmapofPicuresOfItem().get(0), Base64.DEFAULT);
            Bitmap b = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);

            Matrix matrix = new Matrix();

            matrix.postRotate(90);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, b.getWidth(), b.getHeight(), true);

            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

            ((ItemsViewHolder)holder).image.setImageBitmap(rotatedBitmap);
        }
    }

    private static File writeByte(byte[] bytes, String docId) {
        File localFile = null;
        try {
            localFile = File.createTempFile(docId, ".mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            OutputStream os = new FileOutputStream(localFile);

            // Starts writing the bytes in it
            os.write(bytes);
//            System.out.println("Successfully" + " byte inserted");

            // Close the file
            os.close();
            return localFile;
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        return localFile;
    }

    private static String getFileSizeMegaBytes(File file) {
        return (double) file.length() / (1024 * 1024) + " mb";
    }

    @Override
    public int getItemCount() {
        return this.usersItemsOnMarket.size();
    }

//    public ItemsViewHolder getItem(int positioin){
//
//    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
