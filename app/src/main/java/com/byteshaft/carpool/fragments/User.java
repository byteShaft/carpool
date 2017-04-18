package com.byteshaft.carpool.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.byteshaft.carpool.utils.AppGlobals;
import com.byteshaft.carpool.utils.Helpers;
import com.byteshaft.carpool.IntroScreen;
import com.byteshaft.carpool.R;
import com.byteshaft.carpool.utils.RotateUtil;
import com.byteshaft.carpool.gettersetter.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by s9iper1 on 4/16/17.
 */

public class User extends Fragment implements View.OnClickListener {

    private View mBaseView;
    private CircleImageView profilePicture;
    private EditText name;
    private EditText userName;
    private EditText phoneNumber;
    private AppCompatButton saveButton;
    private static final int STORAGE_PERMISSION = 2;
    private static final int REQUEST_CAMERA = 1;
    private static final int SELECT_FILE = 2;
    private File destination;
    private Uri selectedImageUri;
    private static String imageUrl = "";
    private DatabaseReference ref;
    private AppCompatButton logout;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private AppCompatButton userType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.user_details, container, false);
        ref = FirebaseDatabase.getInstance().
                getReferenceFromUrl("https://carpool-ec8c1.firebaseio.com/");
        profilePicture = (CircleImageView) mBaseView.findViewById(R.id.profile_picture);
        name = (EditText) mBaseView.findViewById(R.id.name);
        userName = (EditText) mBaseView.findViewById(R.id.username);
        phoneNumber = (EditText) mBaseView.findViewById(R.id.phone_number);
        saveButton = (AppCompatButton) mBaseView.findViewById(R.id.save_info);
        logout = (AppCompatButton) mBaseView.findViewById(R.id.logout);
        userType = (AppCompatButton) mBaseView.findViewById(R.id.user_type);
        userType.setText(AppGlobals.getStringFromSP(AppGlobals.KEY_USER_TYPE));
        logout.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        name.setText(AppGlobals.getStringFromSP(AppGlobals.KEY_NAME));
        userName.setText(AppGlobals.getStringFromSP(AppGlobals.KEY_USERNAME));
        phoneNumber.setText(AppGlobals.getStringFromSP(AppGlobals.KEY_PHONE_NUMBER));
        profilePicture.setOnClickListener(this);
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION);
        } else {
            setImage();
        }
        return mBaseView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setImage();
                }
        }
    }

    private void setImage() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        File file = new File(AppGlobals.getStringFromSP(AppGlobals.KEY_LOCAL_IMAGE_URI));
        Log.i("TAG", "url " + user.getPhotoUrl());
        Log.i("TAG", "exist "+ String.valueOf(file.exists()));
        byte[] decodedByteArray = Base64.decode(AppGlobals.getStringFromSP(AppGlobals.KEY_ENCODED_IMAGE), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            profilePicture.setImageBitmap(bitmap);
        } else {
            if (decodedByte != null) {
                profilePicture.setImageBitmap(decodedByte);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_picture:
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION);
                } else {
                    selectImage();
                }
                break;
            case R.id.save_info:
                String name = this.name.getText().toString().trim();
                String user = userName.getText().toString().trim();
                String phone = phoneNumber.getText().toString();


                if (TextUtils.isEmpty(name)) {
                    Helpers.showSnackBar(getView(), "please enter your name");
                    return;
                }

                if (TextUtils.isEmpty(user)) {
                    Helpers.showSnackBar(getView(), "please enter your username");
                    return;
                }

                if (TextUtils.isEmpty(phone)) {
                    Helpers.showSnackBar(getView(), "please enter phone Number");
                    return;
                }
                showProgressDialog(getActivity(), "updating");
                saveInfo(imageUrl);
                break;
            case R.id.logout:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogTheme);
                alertDialogBuilder.setTitle("Confirmation");
                alertDialogBuilder.setMessage("Do you really want to logout?")
                        .setCancelable(false).setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                auth = FirebaseAuth.getInstance();
                                auth.signOut();
                                AppGlobals.clearSettings();
                                startActivity(new Intent(getActivity().getApplicationContext(), IntroScreen.class));
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
                break;
        }
    }

    private void saveInfo(String url) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String imageEncoded = null;
        if (url != null && !url.trim().isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(url);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final UserDetails userDetails = new UserDetails();
        userDetails.setUserName(userName.getText().toString());
        userDetails.setPhoneNumber(phoneNumber.getText().toString());
        userDetails.setName(name.getText().toString());
        userDetails.setUserType(AppGlobals.getStringFromSP(AppGlobals.KEY_USER_TYPE));
        if (url != null) {
            userDetails.setLocalImageUri(imageUrl);
            userDetails.setPhoto(imageEncoded);
        }
        ref.child("users").child(user.getUid()).setValue(userDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dismissProgressDialog();
                Log.e("ADDED USER", String.valueOf(task.isSuccessful()));
                Helpers.showSnackBar(getView(), "Profile Updated!");
                if (task.isSuccessful()) {
                    AppGlobals.saveStringToSP(AppGlobals.KEY_USERNAME, userName.getText().toString());
                    AppGlobals.saveStringToSP(AppGlobals.KEY_PHONE_NUMBER, phoneNumber.getText().toString());
                    AppGlobals.saveStringToSP(AppGlobals.KEY_NAME, name.getText().toString());
                    if (imageUrl != null) {
                        AppGlobals.saveStringToSP(AppGlobals.KEY_ENCODED_IMAGE, userDetails.getPhoto());
                        AppGlobals.saveStringToSP(AppGlobals.KEY_LOCAL_IMAGE_URI, imageUrl);

                    }

                }
            }
        });

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name.getText().toString())
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                        }
                    }
                });
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
//                else if (items[item].equals("Remove photo")) {
//                    mProfilePicture.setImageDrawable(null);
//                }

            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");
                imageUrl = destination.getAbsolutePath();
                FileOutputStream fileOutputStream;
                try {
                    destination.createNewFile();
                    fileOutputStream = new FileOutputStream(destination);
                    fileOutputStream.write(bytes.toByteArray());
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bitmap orientedBitmap = RotateUtil.rotateBitmap(destination.getAbsolutePath(),
                        Helpers.getBitMapOfProfilePic(destination.getAbsolutePath()));
                profilePicture.setImageBitmap(orientedBitmap);
            } else if (requestCode == SELECT_FILE) {
                selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        selectedImageUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                String selectedImagePath = cursor.getString(column_index);
                Bitmap orientedBitmap = RotateUtil.rotateBitmap(selectedImagePath,
                        Helpers.getBitMapOfProfilePic(selectedImagePath));
                profilePicture.setImageBitmap(orientedBitmap);
                imageUrl = String.valueOf(selectedImagePath);
            }
        }
    }

    public void showProgressDialog(Activity activity, String message) {
        progressDialog = new ProgressDialog(activity, R.style.AppCompatAlertDialogStyle);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

    }
}
