package com.byteshaft.carpool.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import com.byteshaft.carpool.utils.Helpers;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class ActivityFragment extends Fragment {

    private View mBaseView;
    private DatabaseReference ref;
    private ListView activityListView;
    private Adapter activityAdapter;
    private ArrayList<RequestDetails> activityArrayList;
    private int randomInt;
    private FirebaseUser firebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.activity, container, false);
        activityArrayList = new ArrayList<>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        activityListView = (ListView) mBaseView.findViewById(R.id.my_activity_list);

        activityAdapter = new Adapter(getActivity().getApplicationContext(), activityArrayList);
        activityListView.setAdapter(activityAdapter);
        return mBaseView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivityRequests();
    }

    private void getActivityRequests() {
        ref = FirebaseDatabase.getInstance().
                getReferenceFromUrl("https://carpool-ec8c1.firebaseio.com/")
                .child("users").child(firebaseUser.getUid()).child(AppGlobals.MY_REQUEST);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //Getting the data from snapshot
                activityArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RequestDetails request = ds.getValue(RequestDetails.class);
                    Log.i("TAG", "request" + request.getState());
                    if (request != null && request.getState().equals(AppGlobals.STATE_ACTIVITY)) {
                        activityArrayList.add(request);
                        activityAdapter.notifyDataSetChanged();
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
            ViewGroup.LayoutParams lp = viewHolder.button.getLayoutParams();
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            if (AppGlobals.getStringFromSP(AppGlobals.KEY_USER_TYPE).equals(AppGlobals.DRIVER)) {
                viewHolder.button.setText("Complete");
                viewHolder.button.setLayoutParams(lp);
            } else {
                viewHolder.button.setVisibility(View.GONE);
            }
            viewHolder.username.setText(requestDetails.getUserName());
            viewHolder.fromLocation.setText(requestDetails.getFromLocation());
            viewHolder.toLocation.setText(requestDetails.getToLocation());
            viewHolder.time.setText(requestDetails.getTime());
            viewHolder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AppGlobals.getStringFromSP(AppGlobals.KEY_USER_TYPE).equals(AppGlobals.DRIVER)) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogTheme);
                        alertDialogBuilder.setTitle("Complete Event");
                        alertDialogBuilder.setMessage("Do you want to mark event as complete?").setCancelable(false).setPositiveButton("Accept",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        completeEvent(requestDetails);
                                    }
                                });
                        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
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

    private void completeEvent(final RequestDetails requestInfo) {
        AppGlobals.showProgressDialog(getActivity(), "updating request...");
        ref = FirebaseDatabase.getInstance().
                getReferenceFromUrl("https://carpool-ec8c1.firebaseio.com/");
        final RequestDetails requestDetails = new RequestDetails();
        requestDetails.setEncodedImage(requestInfo.getEncodedImage());
        requestDetails.setServerId(requestInfo.getServerId());
        requestDetails.setSenderId(requestInfo.getSenderId());
        requestDetails.setUserName(requestInfo.getUserName());
        requestDetails.setFromLocation(requestInfo.getFromLocation());
        requestDetails.setToLocation(requestInfo.getToLocation());
        requestDetails.setTime(requestInfo.getTime());
        requestDetails.setState(AppGlobals.STATE_HISTORY);
        requestDetails.setEncodedImage(requestInfo.getEncodedImage());
        ref.child("users").child(AppGlobals.REQUEST).child(requestInfo.getServerId())
                .setValue(requestDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    ref.child("users").child(requestInfo.getSenderId()).child(AppGlobals.MY_REQUEST).child(requestInfo.getServerId())
                            .setValue(requestDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ref.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .child(AppGlobals.MY_REQUEST).child(requestInfo.getServerId())
                                        .setValue(requestDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        AppGlobals.dismissProgressDialog();
                                        Log.e("Request", String.valueOf(task.isSuccessful()));
                                        if (task.isSuccessful()) {
                                            new android.os.Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (getActivity() != null) {
                                                        activityAdapter = new Adapter(getActivity().getApplicationContext(),
                                                                activityArrayList);
                                                        activityListView.setAdapter(activityAdapter);
                                                    }
                                                }
                                            }, 500);
                                        } else {
                                            Helpers.showSnackBar(getView(), task.getException().getMessage());
                                        }
                                    }
                                });
                            } else {
                                Helpers.showSnackBar(getView(), task.getException().getMessage());
                            }
                        }
                    });
                } else {
                    AppGlobals.dismissProgressDialog();
                    Helpers.showSnackBar(getView(), task.getException().getMessage());
                }
            }
        });

    }
}
