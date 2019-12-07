package com.example.letitgoat.ui.sell.sell_recycler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.letitgoat.db_models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letitgoat.MainActivity;
import com.example.letitgoat.R;
import com.example.letitgoat.db_models.Item;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
        this.mContext = mContext;
        this.db = FirebaseFirestore.getInstance();
        this.usersItemsOnMarket = new ArrayList<>();

        db.collection("Items")
                .whereEqualTo("user", MainActivity.Companion.getUser())
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
                                Log.d("check_sell_item", doc.get("name").toString());
                                Item i = new Item(
                                        doc.get("name").toString(),
                                        Double.valueOf(doc.get("price").toString()),
                                        u,
                                        doc.get("description").toString(),
                                        d,
                                        (List<String>)doc.get("stringsOfBitmapofPicuresOfItem")
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
                    usersItemsOnMarket.get(getAdapterPosition()));
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item i = this.usersItemsOnMarket.get(position);
        ((ItemsViewHolder)holder).name.setText(i.getName());
        ((ItemsViewHolder)holder).price.setText("$" + i.getPrice());

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
        void onItemClick(View view, int position, Item item);
    }
}
