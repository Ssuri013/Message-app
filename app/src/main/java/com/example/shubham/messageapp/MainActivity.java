package com.example.shubham.messageapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG=MainActivity.class.getSimpleName();

    private Button mSendButton;
    private EditText mMessegeContent;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabase;
    private String mUsername;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final int RC_SIGN_IN = 123;
    private MessageAdapter mMessagesAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUsername = "Anonymous";
        mSendButton = (Button) findViewById(R.id.sendButton);
        mMessegeContent = (EditText) findViewById(R.id.messageEditText);

        //firebase related
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesDatabase = mFirebaseDatabase.getReference().child("messages");
        mFirebaseAuth = FirebaseAuth.getInstance();

        //listview
        ListView listview = (ListView) findViewById(R.id.messageListView);
        List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        mMessagesAdapter = new MessageAdapter(this, R.layout.item_message, friendlyMessages);
        listview.setAdapter(mMessagesAdapter);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendlyMessage fm =new FriendlyMessage(mMessegeContent.getText().toString(),mUsername,null);
                mMessagesDatabase.push().setValue(fm);
                mMessegeContent.setText("");
            }
        });



        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialize(user.getDisplayName());
                    Log.d(LOG_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    onSignedOutCleanup();
                    Log.d(LOG_TAG, "onAuthStateChanged:signed_out");
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }

            }
        };

//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Log.d(LOG_TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
//
//                        // If sign in fails, display a message to the user. If sign in succeeds
//                        // the auth state listener will be notified and logic to handle the
//                        // signed in user can be handled in the listener.
//                        if (!task.isSuccessful()) {
//                            Toast.makeText(EmailPasswordActivity.this, R.string.auth_failed,
//                                    Toast.LENGTH_SHORT).show();
//                        }
//
//                        // ...
//                    }
//                });
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
                mMessagesAdapter.clear();
                detachDatabaseReadListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
       // inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    void onSignedInInitialize(String user){
        mUsername = user;
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                FriendlyMessage fr1 = dataSnapshot.getValue(FriendlyMessage.class);
                mMessagesAdapter.add(fr1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mMessagesDatabase.addChildEventListener(mChildEventListener);
    }

        private void onSignedOutCleanup() {
                mUsername = "ANONYMOUS";
                mMessagesAdapter.clear();
                detachDatabaseReadListener();
            }

                private void attachDatabaseReadListener() {
                if (mChildEventListener == null) {
                        mChildEventListener = new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                                        mMessagesAdapter.add(friendlyMessage);
                                    }

                                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                                public void onCancelled(DatabaseError databaseError) {}
                            };
                        mMessagesDatabase.addChildEventListener(mChildEventListener);
                    }
            }

                private void detachDatabaseReadListener() {
                if (mChildEventListener != null) {
                        mMessagesDatabase.removeEventListener(mChildEventListener);
                        mChildEventListener = null;
                    }
            }

}
