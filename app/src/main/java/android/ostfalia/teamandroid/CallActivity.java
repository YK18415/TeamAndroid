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
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.Duration;

public class CallActivity extends AppCompatActivity {

    private static String TAG = "CallActivity";
    protected static final int REQUEST_CAPTURE_PICTURE = 1;
    private static final int PERMISSION_REQUEST_CODE = 1;

    private boolean downloading = false;

    SharedPreferences savedData;

    // Layout components for both:
    ImageView imageView;
    ImageButton btnCamera;
    String currentPhotoPath;
    File photoFile;
    Bitmap bitmap;
    String imageFileName;
    Task<Uri> firebaseUri;
    boolean progressbarVisible;
    ImageButton imageButtonSync;

    // Layout components for Betreuer:
    ImageButton imageButtonAccept;
    ImageButton imageButtonDecline;

    // Layout components for Betreuter:
    TextView textViewDecision;

    Boolean isPictureTaken;
    /*//Storage:
    SharedPreferences settings;*/

    // Firebase - CloudStorage:
    private StorageReference mStorageRef;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setPictureTakenInsteadRole();
        progressbarVisible=false;
        handleLayout();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        signInAnonymously();
        savedData = getApplicationContext().getSharedPreferences("betreuapp", MODE_PRIVATE);

        // Firebase - CloudStorage:
        mStorageRef = FirebaseStorage.getInstance().getReference();
        //settings = getApplicationContext().getSharedPreferences("emailmessagedetails", MODE_PRIVATE); // For reading.
        /*Boolean wasPaused = getIntent().getExtras().getBoolean("IS_PAUSED");

        if(isCallActive(this) && wasPaused) {
            String bitmapStr = settings.getString("imagePreferance", "");
            imageView.setImageBitmap(decodeBase64(bitmapStr));
        }*/

        if(MainActivity.role==Role.BETREUTER){
            String phoneNumber =savedData.getString("PHONE_NUMBER", "");
            if(phoneNumber!=null) {
                StorageReference imageRef = mStorageRef.child("images/" + PhoneCallReceiver.formatPhoneNumber(phoneNumber) + ".jpg");
                imageRef.delete();
                StorageReference answerRef = mStorageRef.child("documents/" + PhoneCallReceiver.formatPhoneNumber(phoneNumber) + ".txt");
                answerRef.delete();
            }
        }

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
        }
    }

    /**
     * set boolean isPictureTaken instead of the role
     */
    private void setPictureTakenInsteadRole() {
        switch(MainActivity.role) {
            case BETREUER:  this.isPictureTaken = true;     break;
            case BETREUTER: this.isPictureTaken = false;    break;
        }
    }

    /**
     * Sets the layout depending on the role of the user and sets the clickListener for the Betreuer (Accept/Decline-Button).
     */
    private void handleLayout() {
       /* Bundle bundle = getIntent().getExtras();
        String role = bundle.get("role").toString();*/
        SharedPreferences settings = getSharedPreferences("betreuapp", MODE_PRIVATE); // For reading.;
        String role = settings.getString("role","");


        switch (role) {
            case "Betreuer":
                MainActivity.role = Role.BETREUER;
                setContentView(R.layout.activity_call_betreuer);
                imageButtonSync = findViewById(R.id.imageButtonSyncBetreuer);
                imageButtonAccept = findViewById(R.id.imageButtonAccept);
                imageButtonDecline = findViewById(R.id.imageButtonDecline);

                // ClickListener:
                imageButtonAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "Kein Bild vorhanden", Toast.LENGTH_SHORT).show();
                    }
                });

                imageButtonDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "Kein Bild vorhanden", Toast.LENGTH_SHORT).show();
                    }
                });

                imageButtonSync.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!downloading) {
                            downloadPhotoFromFirebase();
                        }
                    }
                });
                break;
            case "Betreuter":
                MainActivity.role = Role.BETREUTER;
                setContentView(R.layout.activity_call_betreuter);
                /*btnCamera.setImageResource(R.drawable.ic_sharp_photo_camera_24px);
                btnCamera.setVisibility(View.GONE);*/
                imageButtonSync = findViewById(R.id.imageButtonSyncBetreuter);
                textViewDecision = findViewById(R.id.textViewDecision);

                imageButtonSync.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!downloading) {
                            downloadAndReadFileFromFirebase();
                        }
                    }
                });
                break;
        }
    }

    private File createTextFile(String message) {
        File path = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(path, "answer.txt");


        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  file;
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
                sendFileToFirebase(file, false);
            }
        }
    }

    private void sendFileToFirebase(File file, final boolean isText) {
        Uri fileUri = Uri.fromFile(file);
        StorageReference imageRef;

        if(isText) {
            imageRef = mStorageRef.child("documents/" + PhoneCallReceiver.formatPhoneNumber(PhoneCallReceiver.partnerNumber) + ".txt");
        } else {
            imageRef = mStorageRef.child("images/" + PhoneCallReceiver.formatPhoneNumber(savedData.getString("PHONE_NUMBER", "")) + ".jpg");
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressbarVisible=false;
        imageRef.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                        progressDialog.dismiss();
                        Toast.makeText(CallActivity.this, isText?"Antwort erfolgreich hochgeladen.":"Foto erfolgreich hochgeladen.", Toast.LENGTH_LONG).show();
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
        if(!progressbarVisible && progress>1.0) {
            progressbarVisible = true;
            progressDialog.setTitle("Hochladen zum Firebase-Storage");
            progressDialog.show();
        }
        progressDialog.setMessage(message + ((int) progress) + "%...");
        if (progress >= 99.9) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void updateProgress2(FileDownloadTask.TaskSnapshot taskSnapshot, ProgressDialog progressDialog, String message) {
        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
        progressDialog.setMessage(message + ((int) progress) + "%");
        if (progress >= 99.9) {
            try {
                Thread.sleep(1000);
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
                }
                return;
            }
        }
    }

    /**
     * Download a photo from Firebase and show a progressbar for the percentage of the downloaded file.
     */
    private void downloadPhotoFromFirebase() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        String fileName = "images/" + PhoneCallReceiver.partnerNumber + ".jpg";

        final StorageReference imageRef = mStorageRef.child(fileName);
        //final ProgressDialog progressDialog = new ProgressDialog(this);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Bild herunterladen von Firebase");
        progressDialog.show();

        try {
            final File localFile = File.createTempFile("images", "jgp");
            downloading=true;
            imageRef.getFile(localFile).addOnSuccessListener(
                    new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(CallActivity.this, "Foto erfolgreich heruntergeladen", Toast.LENGTH_LONG).show();
                            bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            imageView.setImageBitmap(bitmap);
                            //progressDialog.dismiss();
                            downloading=false;
                            //progressbarVisible=false;
                            //progressDialog.cancel();
                            progressDialog.dismiss();
                            imageRef.delete();
                            setAcceptAndDeclineOnClickListener();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CallActivity.this,"Download Fehlgeschlagen: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    //progressDialog.dismiss();
                    downloading=false;
                    progressDialog.dismiss();
                    //progressbarVisible=false;
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    updateProgress2(taskSnapshot, progressDialog, "Heruntergeladen zu ");
                }
            });
        } catch (Exception e) {
            Toast.makeText(CallActivity.this,"Failed to create temp file: " + e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
            downloading=false;
            progressDialog.dismiss();
            //progressbarVisible=false;
        }
        //progressDialog.show();
    }

    private void setAcceptAndDeclineOnClickListener(){

        imageButtonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CallActivity.this, "Akzeptiert.", Toast.LENGTH_SHORT).show();
                File textFile = createTextFile("yes");

                sendFileToFirebase(textFile, true);
            }
        });

        imageButtonDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CallActivity.this, "Nicht akzeptiert.", Toast.LENGTH_SHORT).show();
                File textFile = createTextFile("no");
                sendFileToFirebase(textFile, true);
            }
        });
    }

    /**
     * Download a textfile from Firebase, which contains the answer of the Betreuer according a sent picture.
     */
    private void downloadAndReadFileFromFirebase() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        String fileName = "documents/" + PhoneCallReceiver.formatPhoneNumber(savedData.getString("PHONE_NUMBER", "")) + ".txt";

        final StorageReference answerRef = mStorageRef.child(fileName);


        try {
            final File localFile = File.createTempFile(PhoneCallReceiver.formatPhoneNumber(savedData.getString("PHONE_NUMBER", "")), "txt");
            downloading=true;
            answerRef.getFile(localFile).addOnSuccessListener(
                    new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            answerRef.delete();
                            downloading=false;
                            progressbarVisible=false;
                            Toast.makeText(CallActivity.this, "Textdatei erfolgreich heruntergeladen", Toast.LENGTH_LONG).show();
                            String text = "";

                            try {
                                text = getStringFromFile(localFile.getAbsolutePath());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(CallActivity.this, text, Toast.LENGTH_LONG).show();
                            if(text.contains("yes")) {
                                textViewDecision.setText("Ja");
                            } else if(text.contains("no")) {
                                textViewDecision.setText("Nein");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    downloading=false;
                    progressbarVisible=false;
                    Toast.makeText(CallActivity.this,"Download Fehlgeschlagen: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            downloading=false;
            progressbarVisible=false;
            Toast.makeText(CallActivity.this,"Failed to download text-file: " + e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
        }
    }

    /**
     *
     * @param filePath String. The path from Firebase.
     * @return returns the content of the text-file.
     */
    public static String getStringFromFile (String filePath) {
        File fl = new File(filePath);
        String ret = null;
        try(FileInputStream fin = new FileInputStream(fl)) {
            ret = convertStreamToString(fin);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     *
     * @param is InputStream (from the textfile from Firebase)
     * @return returns the content of the textfile
     * @throws Exception
     */
    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
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
            Toast.makeText(CallActivity.this, "FATAL ERROR: Verbindung zum Telephony-Subsystem ist fehlgeschlagen.", Toast.LENGTH_LONG).show();
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
        //downloading = false;
        //startDownloadThread();
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


    @Override
    protected void onPause() {
        super.onPause();
        //isActivityActive = false;
    }
        /*SharedPreferences.Editor editor = settings.edit(); // For writing.

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
