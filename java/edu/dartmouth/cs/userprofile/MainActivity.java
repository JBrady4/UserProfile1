package edu.dartmouth.cs.userprofile;

import android.os.Environment;
import android.provider.MediaStore;
import android.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;
import android.app.FragmentManager;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import java.io.*;


import com.soundcloud.android.crop.Crop;


public class MainActivity extends AppCompatActivity  implements PasswordDialog.PasswordDialogListener {


    //Dialog fragment for verifying user password
    private PasswordDialog authd;
    private String authdPass = "";
    private boolean okClicked;

    public void onDialogPositiveClick(DialogFragment dialog){
        Log.d("MAIN", "Ok clicked");
        authdPass = authd.getPass().toString();
        Log.d("hi", "After ok clicked, authd pass: " + authdPass);
        okClicked = authd.getOkClicked();
    }

    public void onDialogNegativeClick(DialogFragment dialog){
        Log.d("MAIN", "Cancel clicked");
    }


    public static String SHARED = "shared_preferences";

    //variables for managing storage of EditText contents
    //technique to manage storage adapted from:
    // http://www.cs.dartmouth.edu/~sergey/cs65/examples/StorageOptions/MainActivity.java
    private EditText charName;
    private EditText fullName;
    private EditText passWord;


    //variables for managing image capture
    // technique for managing the image capture adapted from:
    // http://www.cs.dartmouth.edu/~sergey/cs65/examples/SimpleCam/MainActivity.java
    private ImageView profPic;
    private Bitmap bitmap;
    private final String profPicFile = "profPicFile";
    private boolean clearProfPic = false;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //loading saved data into editText fields
        SharedPreferences sp = getSharedPreferences(SHARED, 0);

        charName = (EditText) findViewById(R.id.charName);
        charName.setText(sp.getString("characterN", ""));

        fullName = (EditText) findViewById(R.id.fullName);
        fullName.setText(sp.getString("fullN", ""));

        passWord = (EditText) findViewById(R.id.passWord);
        passWord.setText(sp.getString("passwrd", ""));



//        Log.d("clearProfPicOnCreate", "onCreate: " + String.valueOf(clearProfPic));

        //retrieving the profile picture in a bitmap if we have one saved
        profPic = (ImageView) findViewById(R.id.profilePicture);
        try{
            FileInputStream fis = openFileInput(profPicFile);
            bitmap = BitmapFactory.decodeStream(fis);
            fis.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        //checking to see if our bitmap isn't empty, and the user hasn't chosen to clear the picture within the state
        if(bitmap != null ){
            profPic.setImageBitmap(bitmap);
        }
        //if it is empty or the user has chosen to clear, give the user a default profile picture
        else{
            profPic.setImageResource(R.mipmap.ic_launcher);
        }


        //creating TextChangedListeners for any text changed in the EditText boxes
        //used to change the "I have an account already" button to a "clear" button
        final Button accountOrClearButton = (Button) findViewById(R.id.accountOrClear);
        final EditText charNameTextBox = (EditText) findViewById(R.id.charName);
        final EditText fullNameTextBox = (EditText) findViewById(R.id.fullName);
        final EditText passWordTextBox = (EditText) findViewById(R.id.passWord);

        createTextChangedListener(charNameTextBox, accountOrClearButton);
        createTextChangedListener(fullNameTextBox, accountOrClearButton);
        createTextChangedListener(passWordTextBox, accountOrClearButton);

        //creating a OnKeyListener to make sure the user confirms their password if they press enter on the keyboard
        passWordTextBox.setOnKeyListener( new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent keyEvent ){
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //if they press enter, show a dialog that tells the user to confirm their password
                    showPasswordDialog(findViewById(R.id.layout));
                    return true;
                }
                return false;
            }
        });
    }

    /*
    *   function for creating an instance of the dialog fragment that makes the user confirm their
    *   entered password
     */
    public void showPasswordDialog(View view) {
        // Create an instance of the dialog fragment and show it
        authd = new PasswordDialog();
        authd.show(getFragmentManager(), "PasswordDialogFragment");
    }


    /*
     *   helper function that changes the appearance of the clear/I don't have an account button.
     *   used in onCreate().
     *
     *   idea to use TextChangedListener adapted from:
     *   https://stackoverflow.com/questions/33797431/how-to-determine-if-someone-is-typing-on-edittext
     */
    protected void createTextChangedListener (final EditText text, final Button button){
        text.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                button.setText("Clear All");
            }
        });
    }

    /*
    *   function for creating an intent to start up the camera when the user chooses to change their profile picture
    */
    protected void onProfilePictureClicked(View v){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePictureIntent, 1);
        }
    }

    @Override
    /*
    *   saves the state of all variables the user has entered
     */
    protected void onSaveInstanceState (Bundle state){
        super.onSaveInstanceState(state);
        state.putParcelable("IMG", bitmap);
        state.putBoolean("BOOL", clearProfPic);
        state.putBoolean("BOOL2", okClicked);
        if(!authdPass.equals("")){
            state.putString("PASS", authdPass);
        }
        else{
            state.putString("PASS", "");
        }

    }

    @Override
     /*
    *   restores the state of all variables the user has entered
     */
    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        bitmap = savedState.getParcelable("IMG");
        clearProfPic = savedState.getBoolean("BOOL");
        authdPass = savedState.getString("PASS");
        okClicked = savedState.getBoolean("BOOL2");

        Log.d("here", "onRestoreInstanceState: value of clearProfPic: " + String.valueOf(clearProfPic));

        //checking to see if we have a picture saved in storage for handling the savedInstanceState

        if (bitmap != null && clearProfPic != true ) {
            profPic.setImageBitmap(bitmap);
        }

        //if there's no image in storage, maintain the default profile picture through changed instance states
        else{
            profPic.setImageResource(R.mipmap.ic_launcher);
        }

    }

    @Override
    /*
     *  function for making the captured image on the camera the user's profile picture
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 1 && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            profPic.setImageBitmap(bitmap);
            clearProfPic = false;


        }
    }

    /*
    *   function for saving all entered data
    * */
    protected void onSaveClicked(View v){
        Log.d("bool", "authdPass: " + String.valueOf(authdPass));
        Log.d("bool2", "okClicked: " + String.valueOf(okClicked));
        if( authdPass != null && okClicked == true) {

//            Log.d("passwrd", "passwrd: " + passWord.getText());
//            Log.d("dialogpass", "dialogPassword: " + authd.getPass());
//            Log.d("hi", String.valueOf(passWord.getText().toString().equals(authdPass)));

            if(passWord.getText().toString().equals(authdPass)) {

                //saving the EditText content
                SharedPreferences sp = getSharedPreferences(SHARED, 0);
                SharedPreferences.Editor editor = sp.edit();

                editor.putString("characterN", charName.getText().toString());
                editor.putString("fullN", fullName.getText().toString());
                editor.putString("passwrd", passWord.getText().toString());
                editor.putBoolean("clearProfPic", clearProfPic);

                editor.commit();


                //if we cleared the picture and then pressed save, we want to delete the file that stores our profile picture
                if (clearProfPic == true) {
                    Log.d("here", "onSaveClicked: here!");
                }
                //check if the the imageView is the default profile picture so that we don't compress an empty bitmap
                else if (profPic.getDrawable().getConstantState() != getResources().getDrawable(R.mipmap.ic_launcher).getConstantState()) {
                    //saving the profile picture to internal storage
                    FileOutputStream fos = null;

                    try {
                        fos = openFileOutput(profPicFile, Context.MODE_PRIVATE);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                        fos.close();

                    } catch (FileNotFoundException e) {
                        Log.d("fileNotFound", "File not found: " + e.getMessage());
                    } catch (IOException e) {
                        Log.d("errorAccessingFile", "Error accessing file: " + e.getMessage());
                    }
                } else {

                }
                Toast.makeText(getApplicationContext(), "User information has been saved!",
                        Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getApplicationContext(), "The confirmation password you entered does not match your original password",
                        Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Your password has not been confirmed. " +
                            "In the re-entry popup, press 'Ok' to confirm it",
                    Toast.LENGTH_LONG).show();
        }
    }


    /*
    *      function that handles when clear is clicked. clears all entries in the
    *      EditText fields and the user's profile picture
    *
     */
    protected void onClearClicked(View v){

        Button accountOrClearButton = (Button) findViewById(R.id.accountOrClear);


        if( !accountOrClearButton.getText().toString().equals("I already have an account") ){
            charName.setText("");
            fullName.setText("");
            passWord.setText("");

            authdPass="";

            clearProfPic = true;



            String dir = getFilesDir().getAbsolutePath();
            File file = new File(dir, profPicFile);
            if(file.exists()) {
                boolean delete = file.delete();
                Log.d("exists", "file deleted? -> " + String.valueOf(delete));
            }
            else {
                Log.d("dne", "file does not exist");
            }

            //user cleared profile picture so give the user a default profile picture
            profPic.setImageResource(R.mipmap.ic_launcher);

        }




    }


}
