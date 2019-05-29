package android.ostfalia.teamandroid;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CallActivity extends AppCompatActivity {

    private static String TAG = "CallActivity";
    protected static final int REQUEST_CAPTURE_PICTURE = 1;
    private static final int PERMISSION_REQUEST_CODE = 1;


    SharedPreferences savedData;


    // Layout components for both:
    ImageView imageView;
    ImageButton btnCamera;
    String currentPhotoPath;
    File photoFile;
    Bitmap bitmap;
    String imageFileName;
    String fileUrl;
    Task<Uri> firebaseUri;
    ProgressBar progressBar;

    // Layout components for Betreuer:
    ImageButton imageButtonAccept;
    ImageButton imageButtonDecline;

    Boolean isPictureTaken = false;
    /*//Storage:
    SharedPreferences settings;*/

    // Firebase - CloudStorage:
    private StorageReference mStorageRef;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayout();
        signInAnonymously();
        //todo refactor name of the sharedpreferencesy
        savedData = getApplicationContext().getSharedPreferences("contactList", MODE_PRIVATE);

        // Firebase - CloudStorage:
        mStorageRef = FirebaseStorage.getInstance().getReference();
        //settings = getApplicationContext().getSharedPreferences("emailmessagedetails", MODE_PRIVATE); // For reading.
        /*Boolean wasPaused = getIntent().getExtras().getBoolean("IS_PAUSED");

        if(isCallActive(this) && wasPaused) {
            String bitmapStr = settings.getString("imagePreferance", "");
            imageView.setImageBitmap(decodeBase64(bitmapStr));
        }*/

        // Layout components for both:
        btnCamera = findViewById(R.id.btnCamera);
        ImageButton btnCallEnd = findViewById(R.id.btnCallEnd);
        imageView = findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.ic_sharp_photo_camera_24px);
        btnCamera.setImageResource(R.drawable.ic_sharp_photo_camera_24px);
        btnCamera.setVisibility(View.GONE);

        // Layout component for Betreuter:
        TextView textViewDecision = findViewById(R.id.textViewDecision);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeAPicture();
            }
        });

        setImageViewClickListener();

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

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.actionbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
                return;
            }
            List<SubscriptionInfo> _sb = SubscriptionManager.from(getApplicationContext()).getActiveSubscriptionInfoList();
            for (int i = 0; i < _sb.size(); i++) {
                SubscriptionInfo info = _sb.get(i);
                System.out.println("Mobile_number " + info.getNumber());
                Log.d(TAG, "Mobile_number " + info.getNumber());
            }
        }
    }

    /**
     * Sets the layout depending on the role of the user and sets the clickListener for the Betreuer (Accept/Decline-Button).
     */
    private void setLayout() {
       /* Bundle bundle = getIntent().getExtras();
        String role = bundle.get("role").toString();*/
        switch (MainActivity.role) {
            case BETREUER:
                setContentView(R.layout.activity_call_betreuer);
                imageButtonAccept = findViewById(R.id.imageButtonAccept);
                imageButtonDecline = findViewById(R.id.imageButtonDecline);
                // ClickListener:
                imageButtonAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(CallActivity.this, "Akzeptiert.", Toast.LENGTH_SHORT).show();
                        downloadPhotoFromFirebase();
                        // TODO
                    }
                });

                imageButtonDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(CallActivity.this, "Nicht akzeptiert.", Toast.LENGTH_SHORT).show();
                        // TODO
                    }
                });
                break;
            case BETREUTER:
                setContentView(R.layout.activity_call_betreuter);
                break;
        }
    }

    /**
     * If the user taps the imageView, then the picture is shown in a popup (Except the first tab - then the user can take a photo).
     */
    private void showPictureInPopup() {
        if (imageView.getDrawable() != null) {
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

    /**
     * The (system) camera-app is opening, so the user can take a photo.
     * Also a File with this taken photo will be created with the filepath.
     */
    private void takeAPicture() {
        Intent intentTakePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intentTakePicture.resolveActivity(getPackageManager()) != null) {
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
        if (requestCode == REQUEST_CAPTURE_PICTURE && resultCode == RESULT_OK) {
            File file = new File(photoFile.toString());

            if (file.exists()) {
                bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                imageView.setImageBitmap(bitmap);
                isPictureTaken = true;
                btnCamera.setVisibility(View.VISIBLE);
                sendPhotoToFirebase(file);
            }
        }
    }

    private void sendPhotoToFirebase(File file) {
        Uri fileUri = Uri.fromFile(file);

        /*if (CallReceiver.INCOMING_NUMBER == null) {
            System.out.println("Keine Nummer gespeichert!");
        }

        StorageReference riversRef = mStorageRef.child("images/" + PhoneCallReceiver.INCOMING_NUMBER + ".jpg");
        */


        StorageReference riversRef = mStorageRef.child("images/" + savedData.getString("PHONE_NUMBER", "") + ".jpg");

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Hochladen zum Firebase-Storage");
        progressDialog.show();

        riversRef.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                        progressDialog.dismiss();
                        Toast.makeText(CallActivity.this, "Foto erfolgreich hochgeladen.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(CallActivity.this, R.string.callActivity_Firebase_Exception_Upload_Toast, Toast.LENGTH_LONG).show();
                        exception.printStackTrace();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                updateProgress(taskSnapshot, progressDialog, "Hochgeladen zu ");
            }
        });


    }

    // TODO: Generisch.
    public void updateProgress(UploadTask.TaskSnapshot taskSnapshot, ProgressDialog progressDialog, String message) {
        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
        progressDialog.setMessage(message + ((int) progress) + "%...");
        if (progress == 100) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateProgress2(FileDownloadTask.TaskSnapshot taskSnapshot, ProgressDialog progressDialog, String message) {
        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
        progressDialog.setMessage(message + ((int) progress) + "%...");
        if (progress == 100) {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    List<SubscriptionInfo> _sb = SubscriptionManager.from(getApplicationContext()).getActiveSubscriptionInfoList();
                    for (int i = 1; i < _sb.size(); i++) {
                        SubscriptionInfo info = _sb.get(i);
                        System.out.println("Mobile_number " + info.getNumber());
                        Log.d(TAG, "Mobile_number " + info.getNumber());
                    }
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    private void downloadPhotoFromFirebase() {

       /* TelephonyManager phoneManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //todo testen, ob es crasht, wenn der user nicht die permission gibt
        //Wenn die eigene Telefonnummer nicht auf der SIM karte gespeichert ist, dann funktioniert das vielleicht nicht
        String fileName = "images/" + phoneManager.getLine1Number() + ".jpg";*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);

                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                System.out.println("############################################# returned");
                return;
            }
            List<SubscriptionInfo> subscription = SubscriptionManager.from(getApplicationContext()).getActiveSubscriptionInfoList();
            for (int i = 0; i < subscription.size(); i++) {
                SubscriptionInfo info = subscription.get(i);
                Log.d(TAG, "number " + info.getNumber());
                Log.d(TAG, "network name : " + info.getCarrierName());
                Log.d(TAG, "country iso " + info.getCountryIso());
            }
        }
        TelephonyManager phoneManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String fileName = "images/" + PhoneCallReceiver.INCOMING_NUMBER + ".jpg";
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++" + fileName);

        final StorageReference riversRef = mStorageRef.child(fileName);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Herunterladen vom Firebase-Storage");
        progressDialog.show();

        try {
            final File localFile = File.createTempFile("images", "jgp");

            riversRef.getFile(localFile).addOnSuccessListener(
                    new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(CallActivity.this, "Foto erfolgreich heruntergeladen", Toast.LENGTH_LONG).show();
                            bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            imageView.setImageBitmap(bitmap);
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CallActivity.this,"Download failed: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    updateProgress2(taskSnapshot, progressDialog, "Heruntergeladen zu ");
                }
            });
        } catch (Exception e) {
            Toast.makeText(CallActivity.this,"Failed to create temp file: " + e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Create the image-file and the path to the local storage from the image/picture.
     * @return Image-/Picture-File
     * @throws IOException
     */
    // Code by google from 'https://developer.android.com/training/camera/photobasics':
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFileName = imageFileName + ".jpg";
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    /**
     * Ends the current active call if the user tabs the endCall-Button.
     */
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

    /**
     *
     * @param context
     * @return true if a call is active (current in use), otherwise false.
     */
    // Code by Mauricio Manoel and slfan from StackOverflow:
    public static boolean isCallActive(Context context){
        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(manager.getMode() == AudioManager.MODE_IN_CALL || manager.getMode() == AudioManager.MODE_IN_COMMUNICATION){
            return true;
        }
        return false;
    }

    /**
     * set the onClickListener for the imageView depending on the 'first-photo-take'.
     * First tab: The user can take a photo.
     * Otherwise: A popup with the current picture is shown.
     */
    private void setImageViewClickListener() {
        if(isPictureTaken) {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPictureInPopup();
                }
            });
        } else {
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takeAPicture();
                }
            });
        }
    }

    /**
     * Sign in to Firebase (anonymously).
     */
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
    protected void onResume() {
        super.onResume();
        setImageViewClickListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
        } else {
            signInAnonymously();
        }
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

    /**
     * Close call after touching Back-Button
     */
    @Override
    public void onBackPressed() {
        endCall();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
