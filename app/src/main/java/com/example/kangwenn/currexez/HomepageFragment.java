package com.example.kangwenn.currexez;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.kangwenn.currexez.Entity.Purchase;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomepageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomepageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomepageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ImageButton mRate, mCalculator, mPurchase, mHotel, mHistory, mProfile, mFlight, mSell;
    TextView currencyScroll;
    ArrayAdapter<String> adapter;
    ListView homeListView;
    String todaydate;
    Date dateToday;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private AdView mAdView;
    private FirebaseAnalytics mFirebaseAnalytics;

    public HomepageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomepageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomepageFragment newInstance(String param1, String param2) {
        HomepageFragment fragment = new HomepageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_homepage, container, false);
        mAdView = v.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("E4F7CE788C71C5DC44498B08E010C9AB").build();
        mAdView.loadAd(adRequest);
        mRate = v.findViewById(R.id.imageButtonRate);
        mCalculator = v.findViewById(R.id.imageButtonCalculator);
        mPurchase = v.findViewById(R.id.imageButtonPurchase);
        mHotel = v.findViewById(R.id.imageButtonHotel);
        mHistory= v.findViewById(R.id.imageButtonHistory);
        mProfile= v.findViewById(R.id.imageButtonProfile);
        mFlight = v.findViewById(R.id.imageButtonFlight);
        mSell = v.findViewById(R.id.imageButtonSell);
        homeListView = v.findViewById(R.id.homeListView);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this.getContext());
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("com.example.kangwenn.RATES", Context.MODE_PRIVATE);
        final String nationality = sharedPref.getString("nationality", null);
        TextView textViewPurchase = v.findViewById(R.id.textViewPurchaseCurrency), textViewSell = v.findViewById(R.id.textViewSellCurrency);
        try {
            if (!nationality.equals("Malaysia")) {
                textViewPurchase.setText(R.string.purchase_myr);
                textViewSell.setText(R.string.sell_myr);
            }
        } catch (NullPointerException e) {
            Log.d("tag", e.getMessage());
        }
        mRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), CurrencyRates.class);
                startActivity(i);
            }
        });
        mCalculator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), CurrencyCalculator.class);
                startActivity(i);
            }
        });
        mPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nationality.equals("Malaysia")) {
                    Intent i = new Intent(getContext(), PurchaseCurrency.class);
                    startActivity(i);
                } else {
                    Intent i = new Intent(getContext(), PurchaseRinggit.class);
                    startActivity(i);
                }
            }
        });
        mHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.agoda.com";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                mFirebaseAnalytics.logEvent("click_hotel", null);
                startActivity(i);
            }
        });
        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), PurchaseHistory2.class);
                startActivity(i);
            }
        });
        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), UserProfile.class);
                startActivity(i);
            }
        });
        mFlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://www.google.com/flights/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                mFirebaseAnalytics.logEvent("click_flight", null);
                startActivity(i);
            }
        });
        mSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nationality.equals("Malaysia")) {
                    Intent i = new Intent(getContext(), SellCurrency.class);
                    startActivity(i);
                } else {
                    Intent i = new Intent(getContext(), SellRinggit.class);
                    startActivity(i);
                }
            }
        });
        populateListView();
        return v;
    }

    //Need check for the date update
    protected void populateListView() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("PurchaseHistory");
        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userID = mCurrentUser.getUid();
        DatabaseReference currentUser = databaseReference.child(userID);
        final ArrayList<String> arrayList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.US);
        todaydate = sdf.format(new Date());

        try {
            dateToday = new SimpleDateFormat("dd/MM/yy").parse(todaydate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    for (DataSnapshot ds1 : ds.getChildren()) {
                        Purchase purchase = ds1.getValue(Purchase.class);
                        String date = ds1.getKey().substring(0, 4) + "/" + ds1.getKey().substring(4, 6) + "/" + ds1.getKey().substring(6, 8);
                        //String month = ds.getKey().toString().substring(4,5);
                        //String date = ds.getKey().toString().substring(6,7);
                        String time = ds1.getKey().substring(9, 11) + ":" + ds1.getKey().substring(11, 13) + ":" + ds1.getKey().substring(12, 14);
                        String values = "Currency : " + purchase.getCurrency()
                                + "\n" + ds.getKey() + " Amount : " + round(purchase.getAmount(), 2)
                                + "\nCollection Date : " + purchase.getCollectionDate()
                                + "\nCollection Location: " + purchase.getCollectionLocation();

                        Date collectionDate = dateToday; // to prevent using null to initialize the date variable
                        try {
                            collectionDate = new SimpleDateFormat("dd/MM/yy").parse(purchase.getCollectionDate());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (dateToday.before(collectionDate) || dateToday.equals(collectionDate)) {
                            arrayList.add(values);
                        }

                    }
                }
                adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
                homeListView.setAdapter(adapter);
                homeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent(getContext(), SuccessPaymentActivity.class);
                        String string = arrayList.get(i);
                        intent.putExtra("purchase", string);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
