package com.example.beautyhub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.beautyhub.utils.RegistrationManager;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class RegistrationActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText, nicknameEditText, dobEditText;
    private RadioGroup roleRadioGroup;
    private AutoCompleteTextView genderAutoComplete;
    private int selectedAge = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nicknameEditText = findViewById(R.id.et_nickname);
        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_password);
        dobEditText = findViewById(R.id.et_dob);
        roleRadioGroup = findViewById(R.id.rg_role);
        genderAutoComplete = findViewById(R.id.actv_gender);
        Button registerButton = findViewById(R.id.btn_register);
        Button backToLogin = findViewById(R.id.btn_back_to_login);

        // הגדרת בחירת תאריך לידה
        dobEditText.setOnClickListener(v -> showMaterialDatePicker());

        // הגדרת האפשרויות לתפריט המגדר
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, genders);
        genderAutoComplete.setAdapter(adapter);

        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String pass = passwordEditText.getText().toString().trim();
            String name = nicknameEditText.getText().toString().trim();
            String dob = dobEditText.getText().toString().trim();
            String gender = genderAutoComplete.getText().toString().trim();

            boolean isGuide = roleRadioGroup.getCheckedRadioButtonId() == R.id.rb_guide;

            if (email.isEmpty() || pass.isEmpty() || name.isEmpty() || dob.isEmpty() || gender.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            new RegistrationManager(this).startRegistration(email, pass, name, selectedAge, gender, 0, isGuide, null,
                    (success, message) -> {
                        if (success) {
                            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, WelcomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        backToLogin.setOnClickListener(v -> finish());
    }

    private void showMaterialDatePicker() {
        // הגדרת הגבלות (לא לבחור תאריך עתידי)
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(DateValidatorPointBackward.now());

        // קביעת תאריך התחלתי (לפני 18 שנה)
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.YEAR, -18);
        long openAt = calendar.getTimeInMillis();
        constraintsBuilder.setOpenAt(openAt);

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date of Birth")
                .setSelection(openAt)
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar selectedCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            selectedCal.setTimeInMillis(selection);
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            dobEditText.setText(sdf.format(selectedCal.getTime()));
            
            selectedAge = calculateAge(
                    selectedCal.get(Calendar.YEAR),
                    selectedCal.get(Calendar.MONTH),
                    selectedCal.get(Calendar.DAY_OF_MONTH)
            );
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private int calculateAge(int year, int month, int day) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        dob.set(year, month, day);
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }
}