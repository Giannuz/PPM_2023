package com.example.myapplication.fragment;

import static android.content.ContentValues.TAG;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.models.FirebaseWrapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;


public class QrCodeFragment extends Fragment {

    ImageView iv_qr;
    Bitmap bitmap;
    TextView usruID;
    String userUID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View externalView = inflater.inflate(R.layout.fragment_qr_code, container, false);

        iv_qr = externalView.findViewById(R.id.iv_qr);

        generateQR();

        // Save to gallery button
        Button button = externalView.findViewById(R.id.saveingallerybutton);

        button.setOnClickListener(view -> {

            String path = saveToInternalStorage(bitmap);

            getResources().getString(R.string.imgSavedto);

            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(getContext(), getResources().getString(R.string.imgSavedto) + " " + path, duration);
            toast.show();


        });

        Button buttonshare = externalView.findViewById(R.id.qrsharebutton);

        buttonshare.setOnClickListener(view -> {

            Uri uri = saveImagetocache(bitmap);
            shareImageUri(uri);

        });

        // USER UID ################################################################################

        usruID = externalView.findViewById(R.id.usrUID);
        usruID.setText(userUID);

        usruID.setOnClickListener(v -> {

            getContext();
            ClipboardManager clipboardManager = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("UID", usruID.getText().toString());
            clipboardManager.setPrimaryClip(clip);

            CharSequence text = getResources().getString(R.string.uidcopiedtoast);
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getContext(), text, duration);
            toast.show();

        });

        // Inflate the layout for this fragment
        return externalView;

    }

    private void generateQR(){

        FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
        userUID = auth.getUid();

        MultiFormatWriter writer = new MultiFormatWriter();

        try {

            BitMatrix matrix = writer.encode(userUID, BarcodeFormat.QR_CODE, 600, 600);

            BarcodeEncoder encoder = new BarcodeEncoder();
            bitmap = encoder.createBitmap(matrix);
            iv_qr.setImageBitmap(bitmap);
            iv_qr.setForegroundGravity(Gravity.CENTER);

        } catch (WriterException e) {
            throw new RuntimeException(e);
        }

    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        // path of the pictures directories
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        // Create imageDir
        String fileName = "profile"+ System.currentTimeMillis() / 1000L + ".jpg";
        File mypath=new File(directory,fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    /**
     * Saves the image as PNG to the app's cache directory.
     * @param image Bitmap to save.
     * @return Uri of the saved file or null
     */
    private Uri saveImagetocache(Bitmap image) {
        //TODO - Should be processed in another thread
        File imagesFolder = new File(requireContext().getCacheDir(), "images");
        Uri uri = null;
        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(requireContext(), "com.mydomain.fileprovider", file);

        } catch (IOException e) {
            Log.d(TAG, "IOException while trying to write file for sharing: " + e.getMessage());
        }
        return uri;
    }

    private void shareImageUri(Uri uri){
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/png");
        startActivity(intent);
    }


}