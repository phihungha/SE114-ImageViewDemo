package com.example.imageviewdemo;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (!result)
                        Toast.makeText(MainActivity.this,
                                "You won't be able to load photos from external files",
                                Toast.LENGTH_SHORT).show();
                }
            });

    ActivityResultLauncher<String> pickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null)
                        loadImageFromUri(result);
                }
            });

    ImageView imageView;
    Bitmap currentBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

        imageView = findViewById(R.id.image_view);
        Button pickFromGalleryBtn = findViewById(R.id.pick_from_gallery_button);
        Button saveGalleryBtn = findViewById(R.id.save_gallery_button);
        Button loadFromFileBtn = findViewById(R.id.load_from_file_button);
        Button saveFileBtn = findViewById(R.id.save_file_button);

        pickFromGalleryBtn.setOnClickListener(view -> pickerLauncher.launch("image/*"));
        loadFromFileBtn.setOnClickListener(view -> loadImageFromFile());
        saveGalleryBtn.setOnClickListener(view -> saveImageToGallery());
        saveFileBtn.setOnClickListener(view -> saveImageToFile());
    }

    private void loadImageFromFile() {
        String path = Environment.getExternalStorageDirectory() + "/Download/test_image1.png";
        Bitmap loadedBitmap = BitmapFactory.decodeFile(path);
        if (loadedBitmap != null) {
            currentBitmap = loadedBitmap;
            imageView.setImageBitmap(currentBitmap);
        } else
            Toast.makeText(this, "Failed to load bitmap from file!", Toast.LENGTH_SHORT).show();
    }

    private void loadImageFromUri(Uri uri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(uri);
            currentBitmap = BitmapFactory.decodeStream(imageStream);
            imageView.setImageBitmap(currentBitmap);
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "Failed to load bitmap from gallery", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToFile() {
        String savePath = Environment.getExternalStorageDirectory() + "/Download/test_image2.png";
        try (final FileOutputStream stream = new FileOutputStream(savePath)) {
            if (currentBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
                Toast.makeText(this, "Saved bitmap into file!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Failed to save bitmap into file!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save bitmap into file!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToGallery() {
        if (currentBitmap != null) {
            final ContentValues values = new ContentValues();
            long name = System.currentTimeMillis();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, name + ".png");
            Uri imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri newImageUri = getContentResolver().insert(imageCollection, values);

            if (newImageUri != null) {
                try (final OutputStream stream = getContentResolver().openOutputStream(newImageUri)) {
                    if (currentBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
                        Toast.makeText(this, "Saved bitmap into gallery!", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "Failed to save bitmap into gallery!", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(this, "Failed to save bitmap into gallery!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}