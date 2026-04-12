package com.example.beautyhub.utils;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;

public class UserImageSelector {

    private AppCompatActivity activity;
    private ImageView imageView;
    private Uri imageUri;
    private Bitmap imageBitmap;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private OnImageSelectedListener listener;
    private static final String TAG = "UserImageSelector";

    public interface OnImageSelectedListener {
        void onImageSelected();
    }

    public UserImageSelector(AppCompatActivity activity, ImageView imageView){
        this.activity = activity;
        this.imageView = imageView;
        this.imageUri = null;
        this.imageBitmap = null;
        initResultLaunchers();
    }

    public void setOnImageSelectedListener(OnImageSelectedListener listener) {
        this.listener = listener;
    }

    public void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(activity)
                .setTitle("Select Profile Picture")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermissionAndOpen();
                    } else {
                        openImagePicker();
                    }
                })
                .show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        try {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(cameraIntent);
        } catch (Exception e) {
            Log.e(TAG, "openCamera: failed to launch camera", e);
            Toast.makeText(activity, "Could not open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImagePicker() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void initResultLaunchers()
    {
        pickMedia = activity.registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                this.imageUri = uri;
                this.imageBitmap = null;
                imageView.setImageURI(uri);
                if (listener != null) listener.onImageSelected();
            }
        });

        cameraLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        if (bitmap != null) {
                            this.imageUri = null;
                            imageView.setImageBitmap(bitmap);
                            this.imageBitmap = bitmap;
                            if (listener != null) listener.onImageSelected();
                        }
                    }
                }
        );

        requestPermissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(activity, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public File createImageFile()
    {
        if(imageUri != null) return ImageFileCreator.createTempFileFromUri(imageUri, activity);
        else if(imageBitmap != null) return ImageFileCreator.createTempFileFromBitmap(imageBitmap, activity);
        return null;
    }
}