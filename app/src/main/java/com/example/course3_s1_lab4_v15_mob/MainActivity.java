package com.example.course3_s1_lab4_v15_mob;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.course3_s1_lab4_v15_mob.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private final String FILE_NAME = "users.txt";
    private static final int COLOR_SUCCESS = Color.parseColor("#4CAF50");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        copyAssetsFileIfNeeded();

        binding.btnRegister.setOnClickListener(v -> registerUser());
        binding.btnLogin.setOnClickListener(v -> loginUser());
    }

    // Папка assets є read-only. Копіюємо початковий файл у внутрішню пам'ять (filesDir),
    // щоб мати можливість дописувати нових користувачів.
    private void copyAssetsFileIfNeeded() {
        File file = getUsersFile();
        if (!file.exists()) {
            try (InputStream is = getAssets().open(FILE_NAME);
                 OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            } catch (IOException e) {
                android.util.Log.e("MainActivity", "Помилка копіювання файлу-шаблону з assets", e);
            }
        }
    }

    private void registerUser() {
        String[] creds = getValidCredentialsOrNull();
        if (creds == null) return;

        if (isUserExists(creds[0])) {
            showStatus("Користувач з таким логіном вже існує!", Color.RED);
            return;
        }

        try (FileWriter fw = new FileWriter(getUsersFile(), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.println(creds[0] + "," + creds[1]);
            showStatus("Користувача успішно зареєстровано", COLOR_SUCCESS);

            binding.etLogin.setText("");
            binding.etPassword.setText("");
        } catch (IOException e) {
            android.util.Log.e("MainActivity", "Помилка запису нового користувача у локальний файл", e);
            showStatus("Помилка збереження даних", Color.RED);
        }
    }

    private boolean isUserExists(String login) {
        try (BufferedReader br = new BufferedReader(new FileReader(getUsersFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 1 && parts[0].trim().equals(login)) {
                    return true;
                }
            }
        } catch (IOException e) {
            android.util.Log.e("MainActivity", "Помилка читання файлу при перевірці логіна", e);
        }
        return false;
    }

    private void loginUser() {
        String[] creds = getValidCredentialsOrNull();
        if (creds == null) return;

        boolean isFound = false;
        try (BufferedReader br = new BufferedReader(new FileReader(getUsersFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].equals(creds[0]) && parts[1].equals(creds[1])) {
                    isFound = true;
                    break;
                }
            }
        } catch (IOException e) {
            android.util.Log.e("MainActivity", "Помилка читання файлу користувачів при спробі авторизації", e);
        }

        if (isFound) {
            showStatus("Успішний вхід користувача: " + creds[0], COLOR_SUCCESS);
        } else {
            showStatus("Невірний логін або пароль!", Color.RED);
        }
    }

    private String[] getValidCredentialsOrNull() {
        String login = String.valueOf(binding.etLogin.getText()).trim();
        String password = String.valueOf(binding.etPassword.getText()).trim();

        if (login.isEmpty() || password.isEmpty()) {
            showStatus("Заповніть всі поля!", Color.RED);
            return null;
        }
        return new String[]{login, password};
    }

    private File getUsersFile() {
        return new File(getFilesDir(), FILE_NAME);
    }

    private void showStatus(String message, int color) {
        binding.tvStatusMessage.setText(message);
        binding.tvStatusMessage.setTextColor(color);
    }
}