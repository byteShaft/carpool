package com.byteshaft.carpool.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;


public class Home extends Fragment {

    private View mBaseView;
    private FloatingActionButton floatingActionButton;
    private DatabaseReference ref;
    private ListView myHomeList;
    private Adapter homeAdapter;
    private ArrayList<RequestDetails> homeArrayList;
    private int randomInt;
    private FirebaseUser firebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.home, container, false);
        homeArrayList = new ArrayList<>();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myHomeList = (ListView) mBaseView.findViewById(R.id.my_home_list);
        floatingActionButton = (FloatingActionButton) mBaseView.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewDialog filterDialog = new ViewDialog(getActivity());
                filterDialog.show();
            }
        });
        if (AppGlobals.getStringFromSP(AppGlobals.KEY_USER_TYPE).equals(AppGlobals.DRIVER)) {
            floatingActionButton.setVisibility(View.GONE);
        }
        homeAdapter = new Adapter(getActivity().getApplicationContext(), homeArrayList);
        myHomeList.setAdapter(homeAdapter);
        return mBaseView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i("TAG", "USER" + AppGlobals.getStringFromSP(AppGlobals.KEY_USER_TYPE));
        if (AppGlobals.getStringFromSP(AppGlobals.KEY_USER_TYPE).equals(AppGlobals.USER)) {
            getHomeRequests(AppGlobals.MY_REQUEST);
        } else if (AppGlobals.getStringFromSP(AppGlobals.KEY_USER_TYPE).equals(AppGlobals.DRIVER)){
            getHomeRequests(AppGlobals.REQUEST);
        }
    }


    private void getHomeRequests(String request) {
        if (AppGlobals.getStringFromSP(AppGlobals.KEY_USER_TYPE).equals(AppGlobals.USER)) {
            ref = FirebaseDatabase.getInstance().
                    getReferenceFromUrl("https://carpool-ec8c1.firebaseio.com/")
                    .child("users").child(firebaseUser.getUid()).child(request);
        } else if (AppGlobals.getStringFromSP(AppGlobals.KEY_USER_TYPE).equals(AppGlobals.DRIVER)) {
            ref = FirebaseDatabase.getInstance().
                    getReferenceFromUrl("https://carpool-ec8c1.firebaseio.com/")
                    .child("users").child(request);
        }
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //Getting the data from snapshot
                homeArrayList.clear();
                Log.i("TAG", "request called");
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RequestDetails request = ds.getValue(RequestDetails.class);
                    Log.i("TAG", "request" + request.getState());
                    if (request != null && request.getState().equals(AppGlobals.STATE_HOME)) {
                        homeArrayList.add(request);
                        homeAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TAG", databaseError.getMessage());

            }
        });
    }

    public class ViewDialog extends Dialog {

        public ViewDialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.custom_dialog);
            setCancelable(false);
            CircleImageView circleImageView = (CircleImageView) findViewById(R.id.pic);
            byte[] decodedByteArray = Base64.decode(AppGlobals.getStringFromSP(AppGlobals.KEY_ENCODED_IMAGE), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
            if (decodedByte != null) {
                circleImageView.setImageBitmap(decodedByte);
            }
            final TextView userName = (TextView) findViewById(R.id.username);
            userName.setText(AppGlobals.getStringFromSP(AppGlobals.KEY_USERNAME));

            final TextView fromLocation = (TextView) findViewById(R.id.from_location);

            final TextView toLocation = (TextView) findViewById(R.id.to_location);

            final TextView time = (TextView) findViewById(R.id.time);

            final EditText phoneNumber = (EditText) findViewById(R.id.phone_number);

            AppCompatButton dialogButton = (AppCompatButton) findViewById(R.id.cancel);
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

            AppCompatButton process = (AppCompatButton) findViewById(R.id.process);
            process.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    String user = userName.getText().toString();
                    String from = fromLocation.getText().toString();
                    String to = toLocation.getText().toString();
                    String currentTime = time.getText().toString();
                    String number = phoneNumber.getText().toString();
                    if (user.trim().isEmpty() || from.trim().isEmpty() || to.trim().isEmpty() ||
                            currentTime.trim().isEmpty() || number.trim().isEmpty()) {
                        Helpers.showSnackBar(getView(), "All fields are required");
                        return;
                    }
                    randomInt = randInt();
                    sendRequest(from, to, currentTime, number);
                    dismiss();


                }
            });
        }
    }

    private void sendRequest(String fromLocation, String toLocation, String time, String number) {
        AppGlobals.showProgressDialog(getActivity(), "sending request...");
        ref = FirebaseDatabase.getInstance().
                getReferenceFromUrl("https://carpool-ec8c1.firebaseio.com/");
        final RequestDetails requestDetails = new RequestDetails();
        requestDetails.setServerId(String.valueOf(randomInt));
        requestDetails.setPhoneNumber(number);
        requestDetails.setUserName(AppGlobals.getStringFromSP(AppGlobals.KEY_USERNAME));
        requestDetails.setFromLocation(fromLocation);
        requestDetails.setToLocation(toLocation);
        requestDetails.setTime(time);
        requestDetails.setState(AppGlobals.STATE_HOME);
        requestDetails.setSenderId(firebaseUser.getUid());
        requestDetails.setEncodedImage(AppGlobals.getStringFromSP(AppGlobals.KEY_ENCODED_IMAGE));
        ref.child("users").child(AppGlobals.REQUEST).child(String.valueOf(randomInt))
                .setValue(requestDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.e("Request", String.valueOf(task.isSuccessful()));
                if (task.isSuccessful()) {
                    ref.child("users").child(firebaseUser.getUid()).child(AppGlobals.MY_REQUEST).child(String.valueOf(randomInt)).setValue(requestDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            AppGlobals.dismissProgressDialog();
                            Log.e("Request", String.valueOf(task.isSuccessful()));
                            if (task.isSuccessful()) {
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

    public static int randInt() {

        // NOTE: This will (intentionally) not run as written so that folks
        // copy-pasting have to think about how to initialize their
        // Random instance.  Initialization of the Random instance is outside
        // the main scope of the question, but some decent options are to have
        // a field that is initialized once and then re-used as needed or to
        // use ThreadLocalRandom (if using at least Java 1.7).
        Random rn = new Random();
        int range = 999999999 - 10 + 1;
        int randomNum =  rn.nextInt(range) + 10;

        return randomNum;
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
                viewHolder.phoneNumber = (TextView) convertView.findViewById(R.id.phone_number);
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
            if (AppGlobals.getStringFromSP(AppGlobals.KEY_USER_TYPE).equals(AppGlobals.DRIVER)) {
                viewHolder.button.setBackground(getResources().getDrawable(R.mipmap.tick));
            } else {
                viewHolder.button.setBackground(getResources().getDrawable(R.mipmap.cross));
            }
            if (AppGlobals.getStringFromSP(AppGlobals.KEY_USER_TYPE).equals(AppGlobals.DRIVER)) {
                viewHolder.phoneNumber.setText(requestDetails.getPhoneNumber());
            } else {
                viewHolder.phoneNumber.setVisibility(View.GONE);
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
                        alertDialogBuilder.setTitle("Accept Event");
                        alertDialogBuilder.setMessage("Do you want to accept event?").setCancelable(false).setPositiveButton("Accept",
                                new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                updateState(requestDetails);
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
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogTheme);
                        alertDialogBuilder.setTitle("Delete Event");
                        alertDialogBuilder.setMessage("Do you want to Delete this event?").setCancelable(false).setPositiveButton("yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        deleteRequest(requestDetails);
                                    }
                                });
                        alertDialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
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
        private TextView phoneNumber;
    }

    private void updateState(final RequestDetails requestInfo) {
        AppGlobals.showProgressDialog(getActivity(), "updating request...");
        ref = FirebaseDatabase.getInstance().
                getReferenceFromUrl("https://carpool-ec8c1.firebaseio.com/");
        final RequestDetails requestDetails = new RequestDetails();
        requestDetails.setPhoneNumber(requestInfo.getPhoneNumber());
        requestDetails.setServerId(requestInfo.getServerId());
        requestDetails.setSenderId(requestInfo.getSenderId());
        requestDetails.setUserName(requestInfo.getUserName());
        requestDetails.setFromLocation(requestInfo.getFromLocation());
        requestDetails.setToLocation(requestInfo.getToLocation());
        requestDetails.setTime(requestInfo.getTime());
        requestDetails.setState(AppGlobals.STATE_ACTIVITY);
        requestDetails.setEncodedImage(requestInfo.getEncodedImage());
        ref.child("users").child(AppGlobals.REQUEST).child(requestInfo.getServerId())
                .setValue(requestDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    AppGlobals.dismissProgressDialog();
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
                                        Log.e("Request", String.valueOf(task.isSuccessful()));
                                        if (task.isSuccessful()) {
                                            new android.os.Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (getActivity() != null) {
                                                        homeAdapter = new Adapter(getActivity().getApplicationContext(),
                                                                homeArrayList);
                                                        myHomeList.setAdapter(homeAdapter);
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

    private void deleteRequest(final RequestDetails requestDetails) {
        AppGlobals.showProgressDialog(getActivity(), "Deleting ...");
        ref = FirebaseDatabase.getInstance().
                getReferenceFromUrl("https://carpool-ec8c1.firebaseio.com/")
                .child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(AppGlobals.MY_REQUEST);
        Query applesQuery = ref.orderByChild("serverId").equalTo(requestDetails.getServerId());
        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("TAG", "data change");
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue();
                }
                ref = FirebaseDatabase.getInstance().
                        getReferenceFromUrl("https://carpool-ec8c1.firebaseio.com/")
                        .child("users").child(AppGlobals.REQUEST);
                Query applesQuery = ref.orderByChild("serverId").equalTo(requestDetails.getServerId());
                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i("TAG", "data change");
                        AppGlobals.dismissProgressDialog();
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            appleSnapshot.getRef().removeValue();
                        }
                        homeAdapter = new Adapter(getActivity().getApplicationContext(), homeArrayList);
                        myHomeList.setAdapter(homeAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        AppGlobals.dismissProgressDialog();
                        Log.e("TAG", "onCancelled", databaseError.toException());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                AppGlobals.dismissProgressDialog();
                Log.e("TAG", "onCancelled", databaseError.toException());
            }
        });
    }
}


