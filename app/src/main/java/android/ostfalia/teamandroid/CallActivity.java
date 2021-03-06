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
import android.support.v7.widget.Toolbar;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The CallActivity. This class handles all operations during a phone call.
 * Examples: endCall, camera-app-function, connection to Firebase-Storage etc.
 */
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
        savedData = getApplicationContext().getSharedPreferences(getString(R.string.SharedPreferencesName), MODE_PRIVATE);

        // Firebase - CloudStorage:
        mStorageRef = FirebaseStorage.getInstance().getReference();

        deleteFilesFromFirebase();

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
                    Toast.makeText(CallActivity.this, getString(R.string.callActivity_isCallActivive_No_Toast), Toast.LENGTH_LONG).show();
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
     * If a phonecall was started, the files (images, textfiles) were deleted from Firebase.
     */
    private void deleteFilesFromFirebase() {
        if(MainActivity.role==Role.BETREUTER){
            String phoneNumber =savedData.getString(getString(R.string.SharedPreferences_Phonenumber), "");
            if(phoneNumber!=null) {
                StorageReference imageRef = mStorageRef.child(getString(R.string.callActivity_Firebase_ImagesPath) + PhoneCallReceiver.formatPhoneNumber(phoneNumber) + getString(R.string.callActivity_fileExtension_JPG));
                imageRef.delete();
                StorageReference answerRef = mStorageRef.child(getString(R.string.callActivity_Firebase_DocumentsPath) + PhoneCallReceiver.formatPhoneNumber(phoneNumber) + getString(R.string.callActivity_fileExtension_TXT));
                answerRef.delete();
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
        SharedPreferences settings = getSharedPreferences(getString(R.string.SharedPreferencesName), MODE_PRIVATE); // For reading.;
        String role = settings.getString(getString(R.string.SharedPreferences_Key_RoleUser),"");

        switch (role) {
            case MainActivity.BETREUER:
                MainActivity.role = Role.BETREUER;
                setContentView(R.layout.activity_call_betreuer);
                imageButtonSync = findViewById(R.id.imageButtonSyncBetreuer);
                imageButtonAccept = findViewById(R.id.imageButtonAccept);
                imageButtonDecline = findViewById(R.id.imageButtonDecline);

                // ClickListener:
                imageButtonAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), getString(R.string.callActivity_NoImage_Toast), Toast.LENGTH_SHORT).show();
                    }
                });

                imageButtonDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), getString(R.string.callActivity_NoImage_Toast), Toast.LENGTH_SHORT).show();
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
            case MainActivity.BETREUTER:
                MainActivity.role = Role.BETREUTER;
                setContentView(R.layout.activity_call_betreuter);
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

    /**
     * When the Betreuer sends the answer to the Betreuter, a textfile will be created for Firebase. In this file is the answer 'yes' or 'no'.
     * @param message The answer from the Betreuer to the Betreuter ('yes' or 'no').
     * @return The textfile, which will be sended to Firebase.
     */
    private File createTextFile(String message) {
        File path = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(path, getString(R.string.callActivity_Textfile_Name));


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
            imageDialog.setTitle(getString(R.string.callActivity_showPictureInPopup_Headline));
            LayoutInflater inflater = CallActivity.this.getLayoutInflater(); // Takes the xml-file and builds the View-Object from it. It is neccessary, because I have a custom-layout for the image.
            View view = inflater.inflate(R.layout.image_popup, null);
            ImageView imageViewPopup = view.findViewById(R.id.imageViewPopup);
            imageViewPopup.setImageBitmap(bitmap);

            imageDialog.setView(view);
            imageDialog.setNegativeButton(getString(R.string.imageDialog_NegativeButton_Back), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = imageDialog.create();
            dialog.show();
        } else {
            Toast.makeText(this, getString(R.string.callActivity_showPictureInPopup_NoImage_Toast), Toast.LENGTH_LONG).show();
        }

    }

    /**
     * The (system) camera-app is opening, so the user can take a photo.
     * Also a file with this taken photo will be created with the filepath.
     */
    private void takeAPicture() {
        Intent intentTakePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intentTakePicture.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, getString(R.string.callActivity_createImageFile_Exception_Toast), Toast.LENGTH_LONG).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, getString(R.string.callActivity_FileProviderUri_Authority), photoFile);
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

    /**
     * Sends the file (image or textfile) to the Firebase-Storage.
     * @param file the file which should be send to the Firebase-Storage.
     * @param isText true, if the file is a textfile, otherwise false.
     */
    private void sendFileToFirebase(File file, final boolean isText) {
        Uri fileUri = Uri.fromFile(file);
        StorageReference imageRef;

        if(isText) {
            imageRef = mStorageRef.child(getString(R.string.callActivity_Firebase_DocumentsPath) + PhoneCallReceiver.formatPhoneNumber(PhoneCallReceiver.partnerNumber) + getString(R.string.callActivity_fileExtension_TXT));
        } else {
            imageRef = mStorageRef.child(getText(R.string.callActivity_Firebase_ImagesPath) + PhoneCallReceiver.formatPhoneNumber(savedData.getString(getString(R.string.SharedPreferences_Phonenumber), "")) + getString(R.string.callActivity_fileExtension_JPG));
        }
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.callActivity_ProgressDialog_Upload_Headline));
        progressDialog.show();

        progressbarVisible=false;
        imageRef.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                        progressDialog.dismiss();
                        Toast.makeText(CallActivity.this, isText?getString(R.string.callActivity_Firebase_UploadAnswerCorrectly_Toast):getString(R.string.callActivity_Firebase_UploadPhotoCorrectly_Toast), Toast.LENGTH_LONG).show();
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
                updateProgressUpload(taskSnapshot, progressDialog, getString(R.string.callActivity_Firebase_UploadUpdateProgress));
            }
        });
    }

    /**
     * During upload a file (image or textfile) to the Firebase-Storage, a progressdialog is shown. In this method the calculation is made.
     * @param taskSnapshot the instance of an UploadTask.TaskSnapshot.
     * @param progressDialog the progressdialog, which is updated.
     * @param message the message, which is shown in the progressdialog.
     */
    private void updateProgressUpload(UploadTask.TaskSnapshot taskSnapshot, ProgressDialog progressDialog, String message) {
        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
        updateProgressDialog(progress, progressDialog, message);
    }

    /**
     * During upload a file (image or textfile) to the Firebase-Storage, a progressdialog is shown. In this method the calculation is made.
     * @param taskSnapshot the instance of an FileDownloadTask.TaskSnapshot.
     * @param progressDialog the progressdialog, which is updated.
     * @param message the message, which is shown in the progressdialog.
     */
    private void updateProgressDownload(FileDownloadTask.TaskSnapshot taskSnapshot, ProgressDialog progressDialog, String message) {
        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
        updateProgressDialog(progress, progressDialog, message);
    }

    /**
     * Updates the progressdialog and shows it.
     * @param progress the upload-/downloadprogress of the file (the percentage).
     * @param progressDialog the progressdialog, which is updated.
     * @param message the message, which is shown in the progressdialog.
     */
    private void updateProgressDialog(double progress, ProgressDialog progressDialog, String message){
        if(!progressbarVisible && progress>1.0) {
            progressbarVisible = true;
            progressDialog.show();
        }
        progressDialog.setMessage(message + " " + ((int) progress) + getString(R.string.callActivity_Firebase_ProgressPercentage));
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
     * Downloads a photo from Firebase and shows a progressbar for the percentage of the downloaded file.
     */
    private void downloadPhotoFromFirebase() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        String fileName = getString(R.string.callActivity_Firebase_ImagesPath) + PhoneCallReceiver.partnerNumber + getString(R.string.callActivity_fileExtension_JPG);

        final StorageReference imageRef = mStorageRef.child(fileName);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.callActivity_ProgressDialog_Download_Headline));


        try {
            final File localFile = File.createTempFile(getString(R.string.callActivity_Firebase_CreateTempFilePrefix), getString(R.string.callActivity_fileExtension_TXT_WithoutPoint));
            downloading = true;
            imageRef.getFile(localFile).addOnSuccessListener(
                    new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(CallActivity.this, getString(R.string.callActivity_Firebase_DownloadPhoto_Success_Toast), Toast.LENGTH_LONG).show();
                            bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            imageView.setImageBitmap(bitmap);
                            downloading = false;
                            progressDialog.dismiss();
                            imageRef.delete();
                            setAcceptAndDeclineOnClickListener();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CallActivity.this, getString(R.string.callActivity_Firebase_Download_Exception), Toast.LENGTH_LONG).show();
                    //progressDialog.dismiss();
                    downloading = false;
                    progressDialog.dismiss();
                    progressbarVisible = false;
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    updateProgressDownload(taskSnapshot, progressDialog, getString(R.string.callActivity_Firebase_DownloadPhoto_UpdateProgress));
                }
            });
        } catch (IOException e) {
            Toast.makeText(CallActivity.this, getString(R.string.callActivity_Firebase_DownloadPhoto_CreateTempFile_Exception), Toast.LENGTH_LONG).show();
            downloading = false;
            progressDialog.dismiss();
            progressbarVisible = false;
        }
    }

    /**
     * handles the clickListener for the buttons 'Accept' and 'Decline'. If the  user taps on one of them, an answer (textfile) will be send to the Firebase-Storage.
     */
    private void setAcceptAndDeclineOnClickListener(){

        imageButtonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CallActivity.this, getString(R.string.callActivity_Accept_Toast), Toast.LENGTH_SHORT).show();
                File textFile = createTextFile(getString(R.string.callActivity_Accept_TextfileContent));

                sendFileToFirebase(textFile, true);
            }
        });

        imageButtonDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CallActivity.this, getString(R.string.callActivity_Decline_Toast), Toast.LENGTH_SHORT).show();
                File textFile = createTextFile(getString(R.string.callActivity_Decline_TextfileContent));
                sendFileToFirebase(textFile, true);
            }
        });
    }

    /**
     * Download a textfile from Firebase, which contains the answer of the Betreuer according a sent picture.
     * Besides, the answer is shown in a textView in the layout.
     */
    private void downloadAndReadFileFromFirebase() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        String fileName = getString(R.string.callActivity_Firebase_DocumentsPath) + PhoneCallReceiver.formatPhoneNumber(savedData.getString(getString(R.string.SharedPreferences_Phonenumber), "")) + getString(R.string.callActivity_fileExtension_TXT);

        final StorageReference answerRef = mStorageRef.child(fileName);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.callActivity_ProgressDialog_Download_Headline));

        try {
            final File localFile = File.createTempFile(PhoneCallReceiver.formatPhoneNumber(savedData.getString(getString(R.string.SharedPreferences_Phonenumber), "")), getString(R.string.callActivity_fileExtension_TXT_WithoutPoint));
            downloading = true;
            answerRef.getFile(localFile).addOnSuccessListener(
                    new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            answerRef.delete();
                            downloading = false;
                            progressbarVisible = false;
                            Toast.makeText(CallActivity.this, getString(R.string.callActivity_DownloadTextfile_Succeed_Toast), Toast.LENGTH_LONG).show();
                            String text = "";

                            try {
                                text = getStringFromFile(localFile.getAbsolutePath());
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(CallActivity.this, getString(R.string.callActivity_DownloadTextfile_Exception), Toast.LENGTH_LONG).show();
                            }
                            Toast.makeText(CallActivity.this, text, Toast.LENGTH_LONG).show();
                            if (text.contains(getString(R.string.callActivity_Accept_TextfileContent))) {
                                textViewDecision.setText(getString(R.string.callActivity_Accept_TextView));
                            } else if (text.contains(getString(R.string.callActivity_Decline_TextfileContent))) {
                                textViewDecision.setText(getString(R.string.callActivity_Decline_TextView));
                            }
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    downloading = false;
                    progressbarVisible = false;
                    Toast.makeText(CallActivity.this, getString(R.string.callActivity_Firebase_Download_Exception), Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    updateProgressDownload(taskSnapshot, progressDialog, getString(R.string.callActivity_Firebase_DownloadTextfile_UpdateProgress));
                }
            });
        } catch (IOException e) {
            downloading = false;
            progressbarVisible = false;
            Toast.makeText(CallActivity.this, getString(R.string.callActivity_Firebase_DownloadTextfile_Exception) + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     *
     * @param filePath String. The path from the Firebase-Photo.
     * @return returns the content of the text-file.
     */
    public static String getStringFromFile (String filePath) {
        File file = new File(filePath);
        String answer = "";
        try (FileInputStream fin = new FileInputStream(file)) {
            answer = convertStreamToString(fin);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return answer;
    }

    /**
     *
     * @param inputStream InputStream (from the textfile from Firebase).
     * @return returns the content of the textfile.
     * @throws Exception
     */
    public static String convertStreamToString(InputStream inputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }


    /**
     * Creates the image-file and the path to the local storage from the image/picture.
     * @return returns image-/picture-File.
     * @throws IOException
     */
    // Code by google from 'https://developer.android.com/training/camera/photobasics':
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat(getString(R.string.callActivity_CreateImage_SimpleDateFormat)).format(new Date());
        imageFileName = getString(R.string.callActivity_fileExtension_JPEG_) + timeStamp + getString(R.string.callActivity_Underscore);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                getString(R.string.callActivity_fileExtension_JPG),         /* suffix */
                storageDir      /* directory */
        );

        imageFileName = imageFileName + getString(R.string.callActivity_fileExtension_JPG);
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

        } catch (IllegalAccessException e) {
            Toast.makeText(CallActivity.this, getString(R.string.callActivity_endCall_IllegalAccessException_Toast), Toast.LENGTH_LONG).show();
            Log.e(TAG, getString(R.string.callActivity_endCall_IllegalAccessException_Log));
        } catch (IllegalArgumentException e) {
            Toast.makeText(CallActivity.this, getString(R.string.callActivity_endCall_IllegalArgumentException_Toast), Toast.LENGTH_LONG).show();
            Log.e(TAG, getString(R.string.callActivity_endCall_IllegalArgumentException_Log));
        } catch (NoSuchMethodException e) {
            Toast.makeText(CallActivity.this, getString(R.string.callActivity_endCall_NoSuchMethodException_Toast), Toast.LENGTH_LONG).show();
            Log.e(TAG, getString(R.string.callActivity_endCall_NoSuchMethodException_Log));
        } catch (SecurityException e) {
            Toast.makeText(CallActivity.this, getString(R.string.callActivity_endCall_SecurityException_Toast), Toast.LENGTH_LONG).show();
            Log.e(TAG, getString(R.string.callActivity_endCall_SecurityException_Log));
        } catch (InvocationTargetException e) {
            Toast.makeText(CallActivity.this, getString(R.string.callActivity_endCall_InvocationTargetException_Toast), Toast.LENGTH_LONG).show();
            Log.e(TAG, getString(R.string.callActivity_endCall_InvocationTargetException_Log));
        }
    }

    /**
     *
     * @param context the context of this activity.
     * @return true, if a call is active (current in use), otherwise false.
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
     * Sets the onClickListener for the imageView depending on the 'first-photo-take'.
     * First tab: The user can take a photo.
     * Otherwise: A popup with the current picture is shown.
     * Notice: The Beutreuer cannot take a photo. They can only see the photo in the popup.
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
                        Log.e(TAG, getString(R.string.callActivity_SignInAnonymouslyException_Log), exception);
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
        if(user != null) {
        } else {
            signInAnonymously();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        endCall();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
