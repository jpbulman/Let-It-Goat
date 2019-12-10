package com.example.letitgoat.ui.home.buy_recycler;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.example.letitgoat.R;
import com.example.letitgoat.SingleShotLocationProvider;
import com.example.letitgoat.WPILocationHelper;
import com.example.letitgoat.db_models.Item;
import com.example.letitgoat.db_models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
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
import java.util.Random;

class BuyViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ItemClickListener mClickListener;
    private Context mContext;
    private List<Item> itemsOnMarket;
    private List<String> itemsOnMarketIds;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private boolean isSearchResult = false;

    BuyViewAdapter(Context mContext, String category) {
        this(mContext, category, null);

    }

    public BuyViewAdapter(@Nullable Context context, String category, final String searchQuery) {
        this.mContext = context;
        this.storage = FirebaseStorage.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.itemsOnMarket = new ArrayList<>();
        this.itemsOnMarketIds = new ArrayList<>();
        if (searchQuery != null) {
            isSearchResult = true;
        }

        CollectionReference dbItems = db.collection("Items");
        Query subset;
        if (!isSearchResult && !category.equals("All")) {
            subset = dbItems.whereEqualTo("category", category);
        } else {
            subset = dbItems.limit(100);
        }

        subset.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> doc = document.getData();
                                HashMap<String, Object> hash = (HashMap<String, Object>) doc.get("user");
                                User u = new User(hash.get("email").toString(), hash.get("name").toString(), hash.get("profilePicture").toString());
                                Date d = ((Timestamp) doc.get("postedTimeStamp")).toDate();
                                Log.d("check_buy_item", doc.get("name").toString());
                                WPILocationHelper wpiLocationHelper = new WPILocationHelper();
                                Location l = wpiLocationHelper.getLocationOfGordonLibrary();
                                if (doc.get("pickupLocation") != null) {
                                    HashMap<String, Object> mapper = (HashMap<String, Object>) doc.get("pickupLocation");
                                    l = new Location(mapper.get("provider").toString());
                                    l.setLatitude(Double.valueOf(mapper.get("latitude").toString()));
                                    l.setLongitude(Double.valueOf(mapper.get("longitude").toString()));
                                }
                                if (isSearchResult && !doc.get("name").toString().toLowerCase().contains(searchQuery.toLowerCase())) {
                                    continue;
                                }
                                String category = "other";
                                if (doc.containsKey("category")) {
                                    category = doc.get("category").toString();
                                }

                                Item i = new Item(
                                        doc.get("name").toString(),
                                        Double.valueOf(doc.get("price").toString()),
                                        u,
                                        doc.get("description").toString(),
                                        d,
                                        (List<String>) doc.get("stringsOfBitmapofPicuresOfItem"),
                                        l,
                                        category
                                );
                                itemsOnMarket.add(i);
                                itemsOnMarketIds.add(document.getId());
                                notifyDataSetChanged();
                            }
                        } else {
                            System.out.println("Could not get the user's items for selling from the DB");
                        }
                    }
                });
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return localFile;
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
        if (viewType == 0 && !isSearchResult) {
            view = inflater.inflate(R.layout.slider, parent, false);
            viewHolder = new SliderViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.single_buy, parent, false);
            viewHolder = new ItemsViewHolder(view);
        }
        return viewHolder;
    }

    private void updateListItem(int position, File video) {
        RecyclerView view = ((Activity) mContext).findViewById(R.id.buy_recyclerview);
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

    public static <T> List<T> getRandomSubList(List<T> input, int subsetSize)
    {
        Random r = new Random();
        int inputSize = input.size();

        if(inputSize < subsetSize){
            subsetSize = inputSize;
        }

        for (int i = 0; i < subsetSize; i++)
        {

            int indexToSwap = i + r.nextInt(inputSize - i);
            T temp = input.get(i);
           input.set(i, input.get(indexToSwap));
            input.set(indexToSwap, temp);
        }
        return input.subList(0, subsetSize);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (this.itemsOnMarket.size() == 0) return;

        if (holder instanceof ItemsViewHolder) {
            final Item i = this.itemsOnMarket.get(position - (isSearchResult ? 0 : 1));
            ((BuyViewAdapter.ItemsViewHolder) holder).name.setText(i.getName());
            ((BuyViewAdapter.ItemsViewHolder) holder).price.setText("$" + i.getPrice());
            ((BuyViewAdapter.ItemsViewHolder) holder).date.setText(i.getPostedTimeStamp().toString());

            //Extra zero if the price doesn't have one
            if (((BuyViewAdapter.ItemsViewHolder) holder).price.getText().toString().split("\\.")[1].length() == 1) {
                ((BuyViewAdapter.ItemsViewHolder) holder).price.setText("$" + i.getPrice() + "0");
            }

            SingleShotLocationProvider.requestSingleUpdate(
                    mContext,
                    new SingleShotLocationProvider.LocationCallback() {
                        @Override
                        public void onNewLocationAvailable(Location location) {
                            DecimalFormat df = new DecimalFormat("###.##");
                            ((BuyViewAdapter.ItemsViewHolder) holder).pickupLocation.setText(
                                    i.getPickupLocation().getProvider() + ": " +
                                            df.format(location.distanceTo(i.getPickupLocation()) * 0.000621371)
                                            + " miles away"
                            );
                        }
                    });
//            ((BuyViewAdapter.ItemsViewHolder)holder).pickupLocation.setText(i.getPickupLocation().getProvider());

            if (i.getStringsOfBitmapofPicuresOfItem().isEmpty()) {
                final String docId = this.itemsOnMarketIds.get(position - (isSearchResult ? 0 : 1));

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

                if (i.getStringsOfBitmapofPicuresOfItem().size() != 0) {
                    byte[] encodeByte = Base64.decode(i.getStringsOfBitmapofPicuresOfItem().get(0), Base64.DEFAULT);
                    Bitmap b = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                    Matrix matrix = new Matrix();

                    matrix.postRotate(90);

                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, b.getWidth(), b.getHeight(), true);

                    Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                    ((BuyViewAdapter.ItemsViewHolder) holder).image.setImageBitmap(rotatedBitmap);
                }
            }
        } else {
            if (((SliderViewHolder) holder).isInitialized) return;
            SliderLayout mDemoSlider = ((SliderViewHolder) holder).mDemoSlider;
            HashMap<String, Bitmap> file_maps = new HashMap<>();
            HashMap<String, Item> item_map = new HashMap<>();

            List<Item> carouselItems = getRandomSubList(itemsOnMarket, 4);
            for (int j = 0; j < carouselItems.size(); j++) {
                Item item = carouselItems.get(j);
                if (item.getStringsOfBitmapofPicuresOfItem().size() != 0) {
                    byte[] encodeByte = Base64.decode(item.getStringsOfBitmapofPicuresOfItem().get(0), Base64.DEFAULT);
                    Bitmap b = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);

                    Matrix matrix = new Matrix();

                    matrix.postRotate(90);

                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, b.getWidth(), b.getHeight(), true);

                    Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

                    file_maps.put(item.getName(), rotatedBitmap);
                    item_map.put(item.getName(), item);
                } else {
                    file_maps.put(item.getName(), null);
                }
            }

            for (String name : file_maps.keySet()) {
                TextSliderView textSliderView = new TextSliderView(mContext);
                // initialize a SliderLayout
                textSliderView
                        .description(name)
                        .image(file_maps.get(name))
                        .setScaleType(BaseSliderView.ScaleType.Fit)
                        .setOnSliderClickListener(((SliderViewHolder) holder));

                //add your extra information
                textSliderView.bundle(new Bundle());
                textSliderView.getBundle()
                        .putString("extra", name);

                Item referencedItem = item_map.get(name);

                textSliderView.getBundle()
                        .putString("extra", name);
                textSliderView.getBundle()
                        .putParcelable("item", referencedItem);


                mDemoSlider.addSlider(textSliderView);
            }
            mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Tablet);
            mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
            mDemoSlider.setCustomAnimation(new DescriptionAnimation());
            mDemoSlider.startAutoCycle(5000, 5000, true);
            mDemoSlider.addOnPageChangeListener(((SliderViewHolder) holder));
//            mDemoSlider.stopAutoCycle();

            ((SliderViewHolder) holder).isInitialized = true;
        }
    }

    @Override
    public int getItemCount() {
        return itemsOnMarket.size() + (isSearchResult ? 0 : 1);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position, Item item);
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
            if (mClickListener != null) mClickListener.onItemClick(view,
                    getAdapterPosition(),
                    itemsOnMarket.get(getAdapterPosition() - (isSearchResult ? 0 : 1)));
        }
    }

    public class SliderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {
        private SliderLayout mDemoSlider;
        private boolean isInitialized = false;

        SliderViewHolder(View v) {
            super(v);
            mDemoSlider = v.findViewById(R.id.slider);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

        }

        @Override
        public void onSliderClick(BaseSliderView slider) {
            mClickListener.onItemClick(null,
                    getAdapterPosition(),
                    (Item) slider.getBundle().get("item"));
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}
