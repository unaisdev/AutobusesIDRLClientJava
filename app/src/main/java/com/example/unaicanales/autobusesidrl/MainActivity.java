package com.example.unaicanales.autobusesidrl;

import android.Manifest;
import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.w3c.dom.Text;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity: ";

    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_LATLONG = "latLong";

    private GoogleMap mMap;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mGoogleSignInAccount;
    private GoogleApiClient mGoogleApiClient;

    private TextView txtViewEmail;
    private TextView txtViewPass;
    private TextView txtConect;
    private EditText eTUsername;
    private EditText eTPassword;
    private SignInButton bLoginGoogle;
    private Button bLogin;
    private ProgressBar sPCargando;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Parada");

    private static final int REQUEST_SIGN_IN = 9001;
    private static final int REQUEST_LOCATION_FINE = 1;
    private static final int REQUEST_LOCATION_COARSE = 2;

    @Override
    public void onStart() {
        super.onStart();

        //Si no tiene los permisos necesarios para el acceso a la localizacion, los pedimos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_FINE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_COARSE);
        }

        //Leer qué idioma estaba seleccionado
        Locale localization = Locale.getDefault();
        Configuration config = new Configuration();
        config.locale = localization;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        //Si tenemos un usuario ya logueado, lo guardamos

    }

    //AÑADIMOS EL SUPRESSLINT PORQUE SIEMPRE COMPROBAREMOS ANTES SI TENEMOS PERMISOS
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION_FINE:
                if (permissions.length == 1 &&
                        permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                        grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "SI NO LO HABILITAS....... FINE", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_LOCATION_COARSE:
                if (permissions.length == 1 &&
                        permissions[0] == Manifest.permission.ACCESS_COARSE_LOCATION &&
                        grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "SI NO LO HABILITAS....... COARSE", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Nos suscribimos a el Topic de poner en marcha un bus
        FirebaseMessaging.getInstance().subscribeToTopic("Marcha")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Se ha suscrito a Marcha";
                        if (!task.isSuccessful()) {
                            msg = "ERROR suscribiendo";
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
//To do//
                            return;
                        }

// Get the Instance ID token//
                        String token = task.getResult().getToken();
                        String msg = getString(R.string.fcm_token, token);
                        Log.e("EYYYYYYYYYYYYYYy", msg);

                    }
                });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
        mAuth = FirebaseAuth.getInstance();

        //todos los elementos de la vista
        txtConect = (TextView)findViewById(R.id.txtConect);
        txtViewEmail = (TextView)findViewById(R.id.icEmail);
        txtViewPass = (TextView)findViewById(R.id.icPass);
        sPCargando = (ProgressBar)findViewById(R.id.progressBar);
        eTUsername = (EditText) findViewById(R.id.eTUsername);
        eTPassword = (EditText) findViewById(R.id.eTPassword);
        bLoginGoogle = (SignInButton) findViewById(R.id.bLoginGoogle);
        bLogin = (Button) findViewById(R.id.bLogin);

        sPCargando.setVisibility(View.GONE);

        bLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conectarse();
            }
        });

    }

    private boolean validarLogin() {
        boolean correcto = true;

        String email = eTUsername.getText().toString();
        if (TextUtils.isEmpty(email)) {
            eTUsername.setError("Required.");
            correcto = false;
        } else {
            eTUsername.setError(null);
        }

        String password = eTPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            eTPassword.setError("Required.");
            correcto = false;
        } else {
            eTPassword.setError(null);
        }

        return correcto;
    }

    private void conectarse(){

        while(!validarLogin()){
            return;
        }

        mAuth.signInWithEmailAndPassword(eTUsername.getText().toString(), eTPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            abrirMapas();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private void abrirMapas(){
        Intent abrirMaps = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(abrirMaps);
        //Hacemos que si vuelve atras desde el mapa, la aplicación se cierre
        finish();

    }

    private void signIn() {
        if(currentUser == null){
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, REQUEST_SIGN_IN);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, data.toString());
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQUEST_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            // Google Sign In was successful, authenticate with Firebase
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                // Signed in successfully, show authenticated UI.
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getIdToken());

        txtConect.setText(R.string.conectando);
        txtViewEmail.setVisibility(View.GONE);
        txtViewPass.setVisibility(View.GONE);
        eTUsername.setVisibility(View.GONE);
        eTPassword.setVisibility(View.GONE);
        bLoginGoogle.setVisibility(View.GONE);
        bLogin.setVisibility(View.GONE);
        sPCargando.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential( acct.getIdToken(), acct.getIdToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWith.Credential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            abrirMapas();
                        } else {
                            Log.d(TAG, "NOOOOOOOOOOOOOOOOOOO");
                        }

                    }
                });
    }
}
