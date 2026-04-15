package com.example.beautyhub;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.beautyhub.utils.GeminiManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PersonalInfoActivity extends AppCompatActivity {

    private Spinner spinnerSkin, spinnerEyes, spinnerEyeShape, spinnerFace, spinnerLips, spinnerHair, spinnerEyebrows;
    private MaterialButton btnSave, btnBack, btnUploadPhoto;
    private ProgressBar analysisProgress;
    private FirebaseFirestore db;
    private String userId;
    private Uri currentPhotoUri;

    private static final String TAG = "PersonalInfoActivity";

    // Launcher for taking a picture
    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                if (result && currentPhotoUri != null) {
                    try {
                        analyzeImage(currentPhotoUri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
    );

    // Launcher for picking an image from gallery
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        analyzeImage(uri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
    );

    // Launcher for camera permission request
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    launchCamera();
                } else {
                    Toast.makeText(this, "Camera permission is required to take photos.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        initViews();
        setupAllSpinners();
        loadUserData();

        btnSave.setOnClickListener(v -> saveUserData());
        btnBack.setOnClickListener(v -> finish());
        btnUploadPhoto.setOnClickListener(v -> showImageSourceDialog());
    }

    private void initViews() {
        spinnerSkin = findViewById(R.id.spinner_skin_tone);
        spinnerEyes = findViewById(R.id.spinner_eye_color);
        spinnerEyeShape = findViewById(R.id.spinner_eye_shape);
        spinnerFace = findViewById(R.id.spinner_face_shape);
        spinnerEyebrows = findViewById(R.id.spinner_eyebrows_shape);
        spinnerLips = findViewById(R.id.spinner_lips_size);
        spinnerHair = findViewById(R.id.spinner_hair_color);
        btnSave = findViewById(R.id.btn_save_info);
        btnBack = findViewById(R.id.btn_goback);
        btnUploadPhoto = findViewById(R.id.btn_upload_photo);
        analysisProgress = findViewById(R.id.analysis_progress);
    }

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a Photo");
        builder.setItems(new CharSequence[]{"Take a Photo", "Choose from Gallery"}, (dialog, which) -> {
            if (which == 0) { // Take a Photo
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    launchCamera();
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                }
            } else { // Choose from Gallery
                galleryLauncher.launch("image/*");
            }
        });
        builder.show();
    }

    private void launchCamera() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoFile != null) {
            currentPhotoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", photoFile);
            takePictureLauncher.launch(currentPhotoUri);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
    
    private void setupAllSpinners() {
        String[] skin = {"Fair", "Medium", "Olive", "Deep"};
        String[] eyes = {"Brown", "Blue", "Green", "Hazel", "Other"};
        String[] eyeShape = {"Almond", "Round", "Hooded", "Monolid","Upturned","Downturned"};
        String[] faceShape = {"Oval", "Round", "Square", "Heart"};
        String[] eyebrowShape = {"Straight", "Arched", "Rounded", "S-Shaped", "Upward"};
        String[] lips = {"Thin", "Natural", "Full"};
        String[] hair = {"Blonde", "Brown", "Black", "Grey", "Other"};

        loadSpinner(spinnerSkin, skin, "Select Skin Tone");
        loadSpinner(spinnerEyes, eyes, "Select Eye Color");
        loadSpinner(spinnerEyeShape, eyeShape, "Select Eye Shape");
        loadSpinner(spinnerFace, faceShape, "Select Face Shape");
        loadSpinner(spinnerEyebrows, eyebrowShape, "Select Eyebrows Shape");
        loadSpinner(spinnerLips, lips, "Select Lips Size");
        loadSpinner(spinnerHair, hair, "Select Hair Color");
    }

    private void loadSpinner(Spinner spinner, String[] data, String prompt) {
        if (spinner == null) return;
        List<String> list = new ArrayList<>();
        list.add(prompt);
        list.addAll(Arrays.asList(data));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void analyzeImage(Uri imageUri) throws IOException {
        analysisProgress.setVisibility(View.VISIBLE);
        btnUploadPhoto.setEnabled(false);
        Toast.makeText(this, "Analyzing features...", Toast.LENGTH_SHORT).show();

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        getAiFaceAnalysis(bitmap);
    }

    private void saveUserData() {
        if (userId == null) return;

        Map<String, Object> userData = new HashMap<>();
        userData.put("skinTone", getSelectedValue(spinnerSkin));
        userData.put("eyeColor", getSelectedValue(spinnerEyes));
        userData.put("eyeShape", getSelectedValue(spinnerEyeShape));
        userData.put("faceShape", getSelectedValue(spinnerFace));
        userData.put("eyebrowsShape", getSelectedValue(spinnerEyebrows));
        userData.put("lipsSize", getSelectedValue(spinnerLips));
        userData.put("hairColor", getSelectedValue(spinnerHair));

        db.collection("users").document(userId).update(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    db.collection("users").document(userId).set(userData, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(err -> Toast.makeText(this, "Error saving info", Toast.LENGTH_SHORT).show());
                });
    }

    private String getSelectedValue(Spinner spinner) {
        if (spinner == null || spinner.getSelectedItemPosition() <= 0) return null;
        return spinner.getSelectedItem().toString();
    }

    private void loadUserData() {
        if (userId == null) return;

        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                if (documentSnapshot.contains("skinTone")) setSpinnerSelection(spinnerSkin, documentSnapshot.getString("skinTone"));
                if (documentSnapshot.contains("eyeColor")) setSpinnerSelection(spinnerEyes, documentSnapshot.getString("eyeColor"));
                if (documentSnapshot.contains("eyeShape")) setSpinnerSelection(spinnerEyeShape, documentSnapshot.getString("eyeShape"));
                if (documentSnapshot.contains("faceShape")) setSpinnerSelection(spinnerFace, documentSnapshot.getString("faceShape"));
                if (documentSnapshot.contains("eyebrowsShape")) setSpinnerSelection(spinnerEyebrows, documentSnapshot.getString("eyebrowsShape"));
                if (documentSnapshot.contains("lipsSize")) setSpinnerSelection(spinnerLips, documentSnapshot.getString("lipsSize"));
                if (documentSnapshot.contains("hairColor")) setSpinnerSelection(spinnerHair, documentSnapshot.getString("hairColor"));

                if (documentSnapshot.getData().size() > 0) btnSave.setText("UPDATE INFORMATION");
            }
        });
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null || spinner == null) return;
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }

    public void getAiFaceAnalysis(Bitmap faceImage) {
        String prompt = getAiFaceAnalyzerPrompt();

        GeminiManager gemini = GeminiManager.getInstance();
        gemini.sendImageAndText(faceImage, prompt, this, new GeminiManager.GeminiCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: result: " + result);
                analysisProgress.setVisibility(View.GONE);
                btnUploadPhoto.setEnabled(true);

                try {
                    // Clean result string in case it contains markdown backticks
                    String jsonString = result.trim();
                    if (jsonString.startsWith("```json")) {
                        jsonString = jsonString.substring(7, jsonString.length() - 3).trim();
                    } else if (jsonString.startsWith("```")) {
                        jsonString = jsonString.substring(3, jsonString.length() - 3).trim();
                    }

                    JSONObject jsonObject = new JSONObject(jsonString);

                    if (jsonObject.has("skin")) setSpinnerSelection(spinnerSkin, jsonObject.getString("skin"));
                    if (jsonObject.has("eyes")) setSpinnerSelection(spinnerEyes, jsonObject.getString("eyes"));
                    if (jsonObject.has("eyeShape")) setSpinnerSelection(spinnerEyeShape, jsonObject.getString("eyeShape"));
                    if (jsonObject.has("faceShape")) setSpinnerSelection(spinnerFace, jsonObject.getString("faceShape"));
                    if (jsonObject.has("eyebrowShape")) setSpinnerSelection(spinnerEyebrows, jsonObject.getString("eyebrowShape"));
                    if (jsonObject.has("lipsSize")) setSpinnerSelection(spinnerLips, jsonObject.getString("lipsSize"));
                    if (jsonObject.has("hairColor")) setSpinnerSelection(spinnerHair, jsonObject.getString("hairColor"));



                    Toast.makeText(PersonalInfoActivity.this, "Analysis complete! Features updated.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing JSON", e);
                    Toast.makeText(PersonalInfoActivity.this, "Analysis succeeded but failed to parse result.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Throwable error) {
                Log.d(TAG, "onError: " + error.getMessage());
                analysisProgress.setVisibility(View.GONE);
                btnUploadPhoto.setEnabled(true);
                Toast.makeText(PersonalInfoActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getAiFaceAnalyzerPrompt() {
        return "Analyze the provided facial image for beauty and makeup profiling. Your response must be strictly in JSON format only, with no additional text or explanations.\n" +
                "\n" +
                "For each field, you MUST choose a value ONLY from the provided lists below. If you cannot determine a feature with high confidence, use \"NA\" as the value.\n" +
                "\n" +
                "Allowed Values:\n" +
                "\n" +
                "skin: [\"Fair\", \"Medium\", \"Olive\", \"Deep\"]\n" +
                "\n" +
                "eyes: [\"Brown\", \"Blue\", \"Green\", \"Hazel\"]\n" +
                "\n" +
                "eyeShape: [\"Almond\", \"Round\", \"Hooded\"]\n" +
                "\n" +
                "faceShape: [\"Oval\", \"Round\", \"Heart\"]\n" +
                "\n" +
                "eyebrowShape: [\"Arched\", \"Straight\", \"Rounded\"]\n" +
                "\n" +
                "lipsSize: [\"Thin\", \"Natural\", \"Full\"]\n" +
                "\n" +
                "hairColor: [\"Blonde\", \"Brown\", \"Grey\",\"Other\",\"Black\"]\n" +
                "\n" +
                "JSON Output Format:\n" +
                "{\n" +
                "\"skin\": \"value\",\n" +
                "\"eyes\": \"value\",\n" +
                "\"eyeShape\": \"value\",\n" +
                "\"faceShape\": \"value\",\n" +
                "\"eyebrowShape\": \"value\"\n" +
                "\"lipsSize\": \"value\"\n" +
                "\"hairColor\": \"value\"\n" +
                "}";
    }
}
