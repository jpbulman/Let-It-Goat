package com.example.letitgoat.ui.home.buy_recycler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

//import com.daimajia.slider.library.Animations.DescriptionAnimation;
//import com.daimajia.slider.library.SliderLayout;
//import com.daimajia.slider.library.SliderTypes.BaseSliderView;
//import com.daimajia.slider.library.SliderTypes.TextSliderView;
//import com.daimajia.slider.library.Tricks.ViewPagerEx;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.example.letitgoat.R;
import com.example.letitgoat.db_models.Item;
import com.example.letitgoat.db_models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private String title;

    BuyViewAdapter(Context mContext, String title) {
        this.mContext = mContext;
        this.title = title;

        this.db = FirebaseFirestore.getInstance();
        this.itemsOnMarket = new ArrayList<>();

        if (title == "All") {
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

                                    String category = null;
                                    if (doc.get("category") != null) {
                                        category = doc.get("category").toString();
                                    }

                                    Item i = new Item(
                                            doc.get("name").toString(),
                                            Double.valueOf(doc.get("price").toString()),
                                            u,
                                            doc.get("description").toString(),
                                            d,
                                            (List<String>)doc.get("stringsOfBitmapofPicuresOfItem"),
                                            category
                                    );
                                    itemsOnMarket.add(i);
                                    notifyDataSetChanged();
                                }
                            } else {
                                System.out.println("Could not get the user's items for selling from the DB");
                            }
                        }
                    });
        } else {
            db.collection("Items")
                    .whereEqualTo("category", title)
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

                                    String category = null;
                                    if (doc.get("category") != null) {
                                        category = doc.get("category").toString();
                                    }

                                    Item i = new Item(
                                            doc.get("name").toString(),
                                            Double.valueOf(doc.get("price").toString()),
                                            u,
                                            doc.get("description").toString(),
                                            d,
                                            (List<String>)doc.get("stringsOfBitmapofPicuresOfItem"),
                                            category
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

    }

    // Provide a reference to the views for each data single_buy
    // Complex data items may need more than one view per single_buy, and
    // you provide access to all the views for a data single_buy in a view holder
    public class ItemsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView image;
        private TextView name;
        private TextView price;
        private TextView date;

        ItemsViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.itemImage);
            name = v.findViewById(R.id.name);
            price = v.findViewById(R.id.price);
            date = v.findViewById(R.id.date);
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
        private boolean isInitialized;

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
            Toast.makeText(mContext,slider.getBundle().get("extra") + "",Toast.LENGTH_SHORT).show();
            mClickListener.onItemClick(null,
                    getAdapterPosition(),
                    null);
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (this.itemsOnMarket.size() == 0) return;
        if (holder instanceof ItemsViewHolder) {
            Item i = this.itemsOnMarket.get(position - 1);
            ((BuyViewAdapter.ItemsViewHolder)holder).name.setText(i.getName());
            ((BuyViewAdapter.ItemsViewHolder)holder).price.setText("$" + i.getPrice());
            ((BuyViewAdapter.ItemsViewHolder)holder).date.setText(i.getPostedTimeStamp().toString());
            if (i.getStringsOfBitmapofPicuresOfItem().size() != 0) {
                byte[] encodeByte = Base64.decode(i.getStringsOfBitmapofPicuresOfItem().get(0), Base64.DEFAULT);
                Bitmap b = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);

                Matrix matrix = new Matrix();

                matrix.postRotate(90);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, b.getWidth(), b.getHeight(), true);

                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

                ((BuyViewAdapter.ItemsViewHolder)holder).image.setImageBitmap(rotatedBitmap);
            }

        } else {
            if (((SliderViewHolder)holder).isInitialized) return;
            SliderLayout mDemoSlider = ((SliderViewHolder)holder).mDemoSlider;
            HashMap<String,Bitmap> file_maps = new HashMap<>();
            for (int i = 0; i < this.itemsOnMarket.size() / 2 + 1; i++) {
                Item item = this.itemsOnMarket.get(i);
                if (item.getStringsOfBitmapofPicuresOfItem().size() != 0) {
                    byte[] encodeByte = Base64.decode(item.getStringsOfBitmapofPicuresOfItem().get(0), Base64.DEFAULT);
                    Bitmap b = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);

                    Matrix matrix = new Matrix();

                    matrix.postRotate(90);

                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, b.getWidth(), b.getHeight(), true);

                    Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

                    file_maps.put(item.getName(), rotatedBitmap);
                } else {
                    file_maps.put(item.getName(), null);
                }
            }

            for(String name : file_maps.keySet()){
                TextSliderView textSliderView = new TextSliderView(mContext);
                // initialize a SliderLayout
                textSliderView
                        .description(name)
                        .image(file_maps.get(name))
                        .setScaleType(BaseSliderView.ScaleType.Fit)
                        .setOnSliderClickListener(((SliderViewHolder)holder));

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
            mDemoSlider.addOnPageChangeListener(((SliderViewHolder)holder));
//            mDemoSlider.stopAutoCycle();

            ((SliderViewHolder) holder).isInitialized = true;
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

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
