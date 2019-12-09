package com.example.letitgoat.ui.home.buy_recycler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.example.letitgoat.MainActivity;
import com.example.letitgoat.R;
import com.example.letitgoat.SingleShotLocationProvider;
import com.example.letitgoat.WPILocationHelper;
import com.example.letitgoat.db_models.Item;
import com.example.letitgoat.db_models.User;
import com.example.letitgoat.ui.sell.sell_recycler.SellViewAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
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

class BuyViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ItemClickListener mClickListener;
    private Context mContext;
    private List<Item> itemsOnMarket;
    private FirebaseFirestore db;

    BuyViewAdapter(Context mContext) {
        this.mContext = mContext;

        this.db = FirebaseFirestore.getInstance();
        this.itemsOnMarket = new ArrayList<>();

        db.collection("Items")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                Map<String, Object> doc = document.getData();
                                HashMap<String, Object> hash = (HashMap<String, Object>) doc.get("user");
                                User u = new User(hash.get("email").toString(), hash.get("name").toString(), hash.get("profilePicture").toString());
                                System.out.println(doc.get("postedTimeStamp").toString());
                                Date d = ((Timestamp)doc.get("postedTimeStamp")).toDate();
                                Log.d("check_buy_item", doc.get("name").toString());
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
                                        (List<String>)doc.get("stringsOfBitmapofPicuresOfItem"),
                                        l
                                );
                                itemsOnMarket.add(i);
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
            if (mClickListener != null) mClickListener.onItemClick(view,
                    getAdapterPosition(),
                    itemsOnMarket.get(getAdapterPosition() - 1));
        }
    }

    public class SliderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {
        private SliderLayout mDemoSlider;
        SliderViewHolder(View v) {
            super(v);
            mDemoSlider = v.findViewById(R.id.slider);


            HashMap<String,String> url_maps = new HashMap<String, String>();
            url_maps.put("Hannibal", "http://static2.hypable.com/wp-content/uploads/2013/12/hannibal-season-2-release-date.jpg");
            url_maps.put("Big Bang Theory", "http://tvfiles.alphacoders.com/100/hdclearart-10.png");
            url_maps.put("House of Cards", "http://cdn3.nflximg.net/images/3093/2043093.jpg");
            url_maps.put("Game of Thrones", "http://images.boomsbeat.com/data/images/full/19640/game-of-thrones-season-4-jpg.jpg");

            HashMap<String,Integer> file_maps = new HashMap<String, Integer>();
            file_maps.put("Nintendo Switch", R.drawable.foo);
            file_maps.put("Couch", R.drawable.couch);
            file_maps.put("CC meal swipe", R.drawable.cc);

            for(String name : file_maps.keySet()){
                TextSliderView textSliderView = new TextSliderView(mContext);
                // initialize a SliderLayout
                textSliderView
                        .description(name)
                        .image(file_maps.get(name))
                        .setScaleType(BaseSliderView.ScaleType.Fit)
                        .setOnSliderClickListener(this);

                //add your extra information
                textSliderView.bundle(new Bundle());
                textSliderView.getBundle()
                        .putString("extra",name);

                mDemoSlider.addSlider(textSliderView);
            }
            mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Tablet);
            mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
            mDemoSlider.setCustomAnimation(new DescriptionAnimation());
            mDemoSlider.startAutoCycle(5000, 5000, true);
            mDemoSlider.addOnPageChangeListener(this);
            mDemoSlider.stopAutoCycle();

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(view,
                        getAdapterPosition(),
                        itemsOnMarket.get(getAdapterPosition()));
            }
        }

        @Override
        public void onSliderClick(BaseSliderView slider) {
            Toast.makeText(mContext,slider.getBundle().get("extra") + "",Toast.LENGTH_SHORT).show();
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
        if (viewType == 0) {
            view = inflater.inflate(R.layout.slider, parent, false);
            viewHolder = new SliderViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.single_buy, parent, false);
            viewHolder = new ItemsViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ItemsViewHolder) {
            final Item i = this.itemsOnMarket.get(position - 1);
            ((BuyViewAdapter.ItemsViewHolder)holder).name.setText(i.getName());
            ((BuyViewAdapter.ItemsViewHolder)holder).price.setText("$" + i.getPrice());
            ((BuyViewAdapter.ItemsViewHolder)holder).date.setText(i.getPostedTimeStamp().toString());

            //Extra zero if the price doesn't have one
            if(((BuyViewAdapter.ItemsViewHolder)holder).price.getText().toString().split("\\.")[1].length() == 1){
                ((BuyViewAdapter.ItemsViewHolder)holder).price.setText("$" + i.getPrice() + "0");
            }

            SingleShotLocationProvider.requestSingleUpdate(
                    mContext,
                    new SingleShotLocationProvider.LocationCallback() {
                        @Override public void onNewLocationAvailable(Location location) {
                            DecimalFormat df = new DecimalFormat("###.##");
                            System.out.println(i.getName());
                            ((BuyViewAdapter.ItemsViewHolder)holder).pickupLocation.setText(
                                    i.getPickupLocation().getProvider() + ": " +
                                            df.format(location.distanceTo(i.getPickupLocation()) * 0.000621371)
                                            + " miles away"
                            );
                        }
                    });
//            ((BuyViewAdapter.ItemsViewHolder)holder).pickupLocation.setText(i.getPickupLocation().getProvider());

            if(i.getStringsOfBitmapofPicuresOfItem().size() != 0) {

                byte[] encodeByte = Base64.decode(i.getStringsOfBitmapofPicuresOfItem().get(0), Base64.DEFAULT);
                Bitmap b = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);

                Matrix matrix = new Matrix();

                matrix.postRotate(90);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, b.getWidth(), b.getHeight(), true);

                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                ((BuyViewAdapter.ItemsViewHolder)holder).image.setImageBitmap(rotatedBitmap);
            }

        }
    }

    @Override
    public int getItemCount() {
        return itemsOnMarket.size() + 1;
    }


    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position, Item item);
    }
}
