package android.ostfalia.teamandroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CallActivity extends AppCompatActivity {

    private static String TAG = "CallActivity";
    protected static final int REQUEST_CAPTURE_PICTURE = 1;

    // Layout components for both:
    ImageView imageView;

    String currentPhotoPath;
    File photoFile;
    Bitmap bitmap;

    /*//Storage:
    SharedPreferences settings;*/

    // Firebase - CloudStorage:
    private StorageReference mStorageRef;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
        } else {
            signInAnonymously();
        }
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new  OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
               Toast.makeText(CallActivity.this, "Eingeloggt. Juhuu.", Toast.LENGTH_LONG).show();
            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "signInAnonymously:FAILURE", exception);
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase - CloudStorage:
        mStorageRef = FirebaseStorage.getInstance().getReference();



        setLayout();
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        //settings = getApplicationContext().getSharedPreferences("emailmessagedetails", MODE_PRIVATE); // For reading.
        /*Boolean wasPaused = getIntent().getExtras().getBoolean("IS_PAUSED");

        if(isCallActive(this) && wasPaused) {
            String bitmapStr = settings.getString("imagePreferance", "");
            imageView.setImageBitmap(decodeBase64(bitmapStr));
        }*/

        // Layout components for both:
        Button btnCamera = findViewById(R.id.btnCamera);
        ImageButton btnCallEnd = findViewById(R.id.btnCallEnd);
        imageView = findViewById(R.id.imageView);
        // Layout components for Betreuer:
        ImageButton imageButtonAccept = findViewById(R.id.imageButtonAccept);
        ImageButton imageButtonDecline =  findViewById(R.id.imageButtonDecline);
        // Layout component for Betreuter:
        TextView textViewDecision = findViewById(R.id.textViewDecision);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeAPicture();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPictureInPopup();
            }
        });

        btnCallEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCallActive(getApplicationContext())) {
                    endCall();
                } else {
                    Toast.makeText(CallActivity.this, "Kein Anruf ist aktiv.", Toast.LENGTH_LONG).show();
                }
            }
        });

       /* imageButtonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });*/

       /* imageButtonDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });*/
    }

    private void setLayout() {
        Bundle bundle = getIntent().getExtras();
        String role = bundle.get("role").toString();
        switch (role) {
            case "Betreuer":
                setContentView(R.layout.activity_call_betreuer);
                break;
            case "Betreuter":
                setContentView(R.layout.activity_call_betreuter);
                break;
        }
    }

    private void showPictureInPopup() {
        if(imageView.getDrawable() != null) {
            AlertDialog dialog;
            AlertDialog.Builder imageDialog = new AlertDialog.Builder(CallActivity.this);
            imageDialog.setTitle("Bild vergrößert");
            LayoutInflater inflater = CallActivity.this.getLayoutInflater(); // Takes the xml-file and builds the View-Object from it. It is neccessary, because I have a custom-layout for the image.
            View view = inflater.inflate(R.layout.image_popup, null);
            ImageView imageViewPopup = view.findViewById(R.id.imageViewPopup);
            //Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            imageViewPopup.setImageBitmap(bitmap);

            imageDialog.setView(view);
            imageDialog.setNegativeButton("Zurück", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = imageDialog.create();
            dialog.show();
        } else {
            Toast.makeText(this, "Sie haben kein Bild aufgenommen.", Toast.LENGTH_LONG).show();
        }

    }

    private void takeAPicture() {
        Intent intentTakePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intentTakePicture.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
               photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Es konnte kein einzigartiger Speicherpfad für das Foto erstellt werden.", Toast.LENGTH_LONG).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                intentTakePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intentTakePicture, REQUEST_CAPTURE_PICTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CAPTURE_PICTURE && resultCode == RESULT_OK) {
            File file = new File(photoFile.toString());

            if(file.exists()) {
                bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                imageView.setImageBitmap(bitmap);
                sendPhotoToFirebase(file);
            }
        }
    }

    private void sendPhotoToFirebase(File file) {
        Uri fileUri = Uri.fromFile(file);
        StorageReference riversRef = mStorageRef.child("images/test.jpg");


        riversRef.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content

                        Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();

                        //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        exception.printStackTrace();
                    }
                });
    }

    // Code by google from 'https://developer.android.com/training/camera/photobasics':
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    private void endCall() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

            Method methods = telephonyManager.getClass().getDeclaredMethod("getITelephony");
            methods.setAccessible(true);
            Object iTelephony = methods.invoke(telephonyManager);

            ITelephony telephony = (ITelephony) iTelephony;
            telephony.endCall();

        } catch (Exception e) {
            Toast.makeText(CallActivity.this, "FATALER ERROR: Verbindung zum Telephony-Subsystem ist fehlgeschlagen.", Toast.LENGTH_LONG).show();
        }
    }

    // Code by Mauricio Manoel and slfan from StackOverflow:
    public static boolean isCallActive(Context context){
        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(manager.getMode() == AudioManager.MODE_IN_CALL || manager.getMode() == AudioManager.MODE_IN_COMMUNICATION){
            return true;
        }
        return false;
    }

   /* @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = settings.edit(); // For writing.

        // Store the data:
        editor.putBoolean("IS_CALL_ACTIVE", isCallActive(this));
        editor.putString("CURRENT_PHOTO_PATH", currentPhotoPath);
        // Store image:
        editor.putString("imagePreferance", encodeTobase64(bitmap));

        editor.commit();
    }

    public static String encodeTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        return imageEncoded;
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }*/
}
