package com.example.gkmoney;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

public class InstagramPostActivity extends AppCompatActivity {

    private LinearLayout wordContainer;
    private Button addWordBtn, generateBtn, saveBtn, shareBtn;
    private ImageView generatedImage;
    private Spinner templateSpinner;

    private final int MAX_WORDS = 5;
    private ArrayList<View> wordViews = new ArrayList<>();
    private Bitmap generatedBitmap;

    private final int REQ_WRITE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instagram_generator);

        wordContainer = findViewById(R.id.wordContainer);
        addWordBtn = findViewById(R.id.addWordBtn);
        generateBtn = findViewById(R.id.generateBtn);
        saveBtn = findViewById(R.id.saveBtn);
        shareBtn = findViewById(R.id.shareBtn);
        generatedImage = findViewById(R.id.generatedImage);
        templateSpinner = findViewById(R.id.templateSpinner);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                getResources().getStringArray(R.array.template_array)
        );

        templateSpinner.setAdapter(adapter);

        // Add a default input block
        addWordInputBlock();

        addWordBtn.setOnClickListener(v -> {
            if (wordViews.size() < MAX_WORDS) {
                addWordInputBlock();
            } else {
                Toast.makeText(this, "Maximum 5 words allowed", Toast.LENGTH_SHORT).show();
            }
        });

        generateBtn.setOnClickListener(v -> generatePostImage());

        saveBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // ask permission for older devices
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_WRITE);
                    return;
                }
            }
            saveImageToGallery();
        });

        shareBtn.setOnClickListener(v -> {
            if (generatedBitmap == null) {
                Toast.makeText(this, "Generate image first", Toast.LENGTH_SHORT).show();
                return;
            }
            shareToInstagram();
        });
    }

    private void addWordInputBlock() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.word_input_block, wordContainer, false);

        int index = wordViews.size() + 1;
        TextView tvIndex = view.findViewById(R.id.tvIndex);
        tvIndex.setText(index + ".");

        ImageButton removeBtn = view.findViewById(R.id.removeBtn);
        removeBtn.setOnClickListener(v -> {
            wordContainer.removeView(view);
            wordViews.remove(view);
            reorderIndices();
        });

        wordViews.add(view);
        wordContainer.addView(view);
    }

    private void reorderIndices() {
        for (int i = 0; i < wordViews.size(); i++) {
            View v = wordViews.get(i);
            TextView tv = v.findViewById(R.id.tvIndex);
            tv.setText((i + 1) + ".");
        }
    }

    private int getSelectedTemplateRes() {
        int pos = templateSpinner.getSelectedItemPosition();
        switch (pos) {
            case 0: return R.drawable.post_bg_gradient_dark;
            case 1: return R.drawable.post_bg_gradient_light;
            case 2: return R.drawable.post_bg_solid_dark;
            default: return R.drawable.post_bg_gradient_dark;
        }
    }

    private void generatePostImage() {
        // Instagram typical post size
        final int width = 1080;
        final int height = 1350;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // draw selected background
        Drawable bg = ContextCompat.getDrawable(this, getSelectedTemplateRes());
        if (bg != null) {
            bg.setBounds(0, 0, width, height);
            bg.draw(canvas);
        } else {
            canvas.drawColor(Color.BLACK);
        }

        // Title paint
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.WHITE);
        titlePaint.setTextSize(72f);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        Typeface titleTypeface = ResourcesCompat.getFont(this, R.font.montserrat_bold);
        if (titleTypeface != null) titlePaint.setTypeface(titleTypeface);

        canvas.drawText("Today's Vocabulary", width / 2f, 110f, titlePaint);

        // base y position
        int y = 170;
        int cardMargin = 48;
        int cardHeight = 220;
        int available = Math.min(wordViews.size(), MAX_WORDS);

        Paint cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // semi transparent card for contrast depending on template
        if (getSelectedTemplateRes() == R.drawable.post_bg_gradient_light) {
            cardPaint.setColor(Color.argb(200, 255, 255, 255));
        } else {
            cardPaint.setColor(Color.argb(200, 0, 0, 0));
        }

        // text paints
        Paint engPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        engPaint.setColor(getSelectedTemplateRes() == R.drawable.post_bg_gradient_light ? Color.BLACK : Color.WHITE);
        engPaint.setTextSize(48f);
        Typeface bold = ResourcesCompat.getFont(this, R.font.montserrat_bold);
        if (bold != null) engPaint.setTypeface(bold);

        Paint pronPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pronPaint.setColor(getSelectedTemplateRes() == R.drawable.post_bg_gradient_light ? Color.DKGRAY : Color.LTGRAY);
        pronPaint.setTextSize(36f);
        Typeface reg = ResourcesCompat.getFont(this, R.font.montserrat_regular);
        if (reg != null) pronPaint.setTypeface(reg);

        Paint meanPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        meanPaint.setColor(getSelectedTemplateRes() == R.drawable.post_bg_gradient_light ? Color.DKGRAY : Color.LTGRAY);
        meanPaint.setTextSize(36f);
        if (reg != null) meanPaint.setTypeface(reg);

        // adjust spacing dynamically depending on number of items
        int totalCards = available;
        int spacePer = (height - y - 80) / Math.max(totalCards, 1);

        int currentY = y;

        for (int i = 0; i < totalCards; i++) {
            View v = wordViews.get(i);

            EditText etEng = v.findViewById(R.id.etEnglish);
            EditText etPron = v.findViewById(R.id.etTamilPronounce);
            EditText etMean = v.findViewById(R.id.etTamilMeaning);

            String english = nonNull(etEng.getText().toString());
            String pron = nonNull(etPron.getText().toString());
            String mean = nonNull(etMean.getText().toString());

            int left = cardMargin;
            int right = width - cardMargin;
            int top = currentY;
            int bottom = currentY + Math.min(cardHeight, spacePer - 20);

            // draw rounded card with shadow
            RectF rect = new RectF(left, top, right, bottom);
            Paint shadow = new Paint(Paint.ANTI_ALIAS_FLAG);
            shadow.setColor(Color.BLACK);
            shadow.setAlpha(40);
            canvas.drawRoundRect(new RectF(left + 6, top + 8, right + 6, bottom + 8), 30, 30, shadow);

            // card
            canvas.drawRoundRect(rect, 30, 30, cardPaint);

            float textX = left + 36f;
            float textBase = top + 60f;

            // English word - a little larger
            canvas.drawText(english, textX, textBase, engPaint);

            // Tamil pronunciation - next line
            canvas.drawText(pron, textX, textBase + 52f, pronPaint);

            // Tamil meaning - next line, wrap if needed
            drawMultilineText(mean, meanPaint, textX, textBase + 110f, right - left - 72, canvas);

            currentY += spacePer;
        }

        generatedBitmap = bitmap;
        generatedImage.setImageBitmap(bitmap);
        // scroll to preview
        generatedImage.post(() -> generatedImage.getParent().requestChildFocus(generatedImage, generatedImage));
    }

    private void drawMultilineText(String text, Paint paint, float x, float y, int maxWidth, Canvas canvas) {
        if (text == null) return;
        // basic word-wrapping
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        float lineHeight = paint.getTextSize() + 8;
        for (String w : words) {
            String candidate = line.length() == 0 ? w : line + " " + w;
            if (paint.measureText(candidate) > maxWidth) {
                // draw current line
                canvas.drawText(line.toString(), x, y, paint);
                line = new StringBuilder(w);
                y += lineHeight;
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0) {
            canvas.drawText(line.toString(), x, y, paint);
        }
    }

    private String nonNull(String s) {
        if (s == null) return "";
        return s.trim();
    }

    private void saveImageToGallery() {
        if (generatedBitmap == null) {
            Toast.makeText(this, "Generate image first", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "vocab_post_" + System.currentTimeMillis() + ".png");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/VocabPosts");
                values.put(MediaStore.Images.Media.IS_PENDING, true);

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new Exception("Failed to create new MediaStore record.");

                try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                    generatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                }

                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, false);
                getContentResolver().update(uri, values, null, null);
                Toast.makeText(this, "Saved to Pictures/VocabPosts", Toast.LENGTH_SHORT).show();

            } else {
                // older devices: save to external storage Pictures
                String filename = "vocab_post_" + System.currentTimeMillis() + ".png";
                File pictures = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES);
                File appDir = new File(pictures, "VocabPosts");
                if (!appDir.exists()) appDir.mkdirs();
                File file = new File(appDir, filename);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    generatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                }
                // trigger gallery update
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                Toast.makeText(this, "Saved to Pictures/VocabPosts", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void shareToInstagram() {
        try {
            File file = new File(getExternalCacheDir(), "insta_share.png");
            try (FileOutputStream out = new FileOutputStream(file)) {
                generatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/*");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Prefer Instagram if installed
            Intent instagramIntent = new Intent(share);
            instagramIntent.setPackage("com.instagram.android");

            if (instagramIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(instagramIntent);
            } else {
                // fallback chooser
                startActivity(Intent.createChooser(share, "Share Image"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Share failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // permission result callback
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQ_WRITE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImageToGallery();
            } else {
                Toast.makeText(this, "Permission required to save image", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}

