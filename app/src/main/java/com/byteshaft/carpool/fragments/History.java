package com.byteshaft.carpool.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.byteshaft.carpool.R;
import com.byteshaft.carpool.gettersetter.RequestDetails;
import com.byteshaft.carpool.utils.AppGlobals;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by s9iper1 on 4/16/17.
 */

public class History extends Fragment {

    private View mBaseView;
    private DatabaseReference ref;
    private ListView historyListView;
    private Adapter historyAdapter;
    private ArrayList<RequestDetails> historyArrayList;
    private int randomInt;
    private FirebaseUser firebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.history, container, false);
        historyArrayList = new ArrayList<>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        historyListView = (ListView) mBaseView.findViewById(R.id.my_history_list);

        historyAdapter = new Adapter(getActivity().getApplicationContext(), historyArrayList);
        historyListView.setAdapter(historyAdapter);
        return mBaseView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getHistoryRequests();
    }

    private void getHistoryRequests() {
        ref = FirebaseDatabase.getInstance().
                getReferenceFromUrl("https://carpool-ec8c1.firebaseio.com/")
                .child("users").child(firebaseUser.getUid()).child(AppGlobals.MY_REQUEST);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //Getting the data from snapshot
                historyArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RequestDetails request = ds.getValue(RequestDetails.class);
                    Log.i("TAG", "request" + request.getState());
                    if (request != null && request.getState().equals(AppGlobals.STATE_HISTORY)) {
                        historyArrayList.add(request);
                        historyAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TAG", databaseError.getMessage());

            }
        });
    }

    private class Adapter extends ArrayAdapter<RequestDetails> {

        private ViewHolder viewHolder;
        private ArrayList<RequestDetails> arrayList;

        public Adapter(@NonNull Context context, ArrayList<RequestDetails> arrayList) {
            super(context, R.layout.delegate_request);
            this.arrayList = arrayList;
        }


        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.delegate_request,
                        parent, false);
                viewHolder = new ViewHolder();
                viewHolder.circleImageView = (CircleImageView) convertView.findViewById(R.id.pic);
                viewHolder.username = (TextView) convertView.findViewById(R.id.username);
                viewHolder.fromLocation = (TextView) convertView.findViewById(R.id.from_location);
                viewHolder.toLocation = (TextView) convertView.findViewById(R.id.to_location);
                viewHolder.time = (TextView) convertView.findViewById(R.id.time);
                viewHolder.button = (AppCompatButton) convertView.findViewById(R.id.process);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final RequestDetails requestDetails = arrayList.get(position);
            if (requestDetails.getEncodedImage() != null && !requestDetails.getEncodedImage().trim().isEmpty()) {
                byte[] decodedByteArray = Base64.decode(requestDetails.getEncodedImage(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
                if (decodedByte != null) {
                    viewHolder.circleImageView.setImageBitmap(decodedByte);
                }
            }
            viewHolder.button.setVisibility(View.GONE);
            viewHolder.username.setText(requestDetails.getUserName());
            viewHolder.fromLocation.setText(requestDetails.getFromLocation());
            viewHolder.toLocation.setText(requestDetails.getToLocation());
            viewHolder.time.setText(requestDetails.getTime());
            viewHolder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AppGlobals.getStringFromSP(AppGlobals.KEY_USER_TYPE).equals(AppGlobals.DRIVER)) {

                    } else {
                        Log.i("TAG", "click");

                    }
                }
            });
            return convertView;
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }
    }

    private class ViewHolder {
        CircleImageView circleImageView;
        TextView username;
        TextView fromLocation;
        TextView toLocation;
        TextView time;
        AppCompatButton button;

    }
}
