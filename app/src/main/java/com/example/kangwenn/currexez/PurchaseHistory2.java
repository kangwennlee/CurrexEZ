package com.example.kangwenn.currexez;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.kangwenn.currexez.Entity.Purchase;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PurchaseHistory2 extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private FirebaseAnalytics mFirebaseAnalytics;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_history2);
        setTitle("My Purchase");
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAnalytics.logEvent("click_history", null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_purchase_history2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        FirebaseUser mCurrentUser;
        String userID;
        String todaydate;
        private DatabaseReference databaseReference, currentUser;
        private ArrayList<String> arrayListPast, arrayListUpcoming;
        private ArrayAdapter<String> adapterPast, adapterUpcoming;
        private ListView listView;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        //round the double value
        public static double round(double value, int places) {
            if (places < 0) throw new IllegalArgumentException();

            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_purchase_history2, container, false);
            listView = rootView.findViewById(R.id.purchaseHistoryListView);
            databaseReference = FirebaseDatabase.getInstance().getReference().child("PurchaseHistory");
            mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
            userID = mCurrentUser.getUid();
            currentUser = databaseReference.child(userID);
            arrayListPast = new ArrayList<>();
            arrayListUpcoming = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.US);
            todaydate = sdf.format(new Date());
            currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Purchase purchase = ds.getValue(Purchase.class);
                        String date = ds.getKey().substring(0, 4) + "/" + ds.getKey().substring(4, 6) + "/" + ds.getKey().substring(6, 8);
                        //String month = ds.getKey().toString().substring(4,5);
                        //String date = ds.getKey().toString().substring(6,7);
                        String time = ds.getKey().substring(9, 11) + ":" + ds.getKey().substring(11, 13) + ":" + ds.getKey().substring(12, 14);
                        String values = "Currency : " + purchase.getCurrency()
                                + "\nPuchase Amount : " + round(purchase.getAmount(), 2)
                                + "\nPuchase Amount In MYR : " + round(purchase.getAmountInRM(), 2)
                                + "\nPayment Method : " + purchase.getPayMethod()
                                + "\nPurchase Date : " + date + " " + time
                                + "\nCollection Date : " + purchase.getCollectionDate()
                                + "\nCollection Location: " + purchase.getCollectionLocation();
                        if (todaydate.compareTo(purchase.getCollectionDate()) > 0) {
                            arrayListPast.add(values);
                        } else {
                            arrayListUpcoming.add(values);
                        }
                    }
                    adapterPast = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arrayListPast);
                    adapterUpcoming = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, arrayListUpcoming);
                    if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                        listView.setAdapter(adapterUpcoming);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Intent intent = new Intent(getContext(), SuccessPaymentActivity.class);
                                String string = arrayListUpcoming.get(i);
                                intent.putExtra("purchase", string);
                                startActivity(intent);
                            }
                        });
                    } else {
                        listView.setAdapter(adapterPast);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
    }
}
