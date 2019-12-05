package com.example.letitgoat;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

import com.example.letitgoat.ui.home.SliderFragment;
import com.example.letitgoat.ui.home.buy_recycler.BuyRecyclerFragment;

public class TmpMainActivity extends AppCompatActivity
        implements SliderFragment.OnFragmentInteractionListener,
        BuyRecyclerFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmp_main);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_buy_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            SliderFragment firstFragment = SliderFragment.newInstance("1", "2");
            BuyRecyclerFragment secondFragment = BuyRecyclerFragment.newInstance("1");

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());
            secondFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_buy_container, firstFragment).commit();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container2, secondFragment).commit();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
