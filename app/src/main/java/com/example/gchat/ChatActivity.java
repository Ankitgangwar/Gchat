package com.example.gchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.UserData;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.gchat.adapters.AdapterChat;
import com.example.gchat.models.ModelChat;
import com.example.gchat.models.ModelUser;
import com.example.gchat.notifications.APIService;
import com.example.gchat.notifications.Client;
import com.example.gchat.notifications.Data;
import com.example.gchat.notifications.Response;
import com.example.gchat.notifications.Sender;
import com.example.gchat.notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.zip.Inflater;

import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv,userstatusTv;
    EditText messageET;
    ImageButton sendbtn;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDbReference;
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    String hisUid,myUid,hisimage;
    APIService apiService;
    boolean notify=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        androidx.appcompat.widget.Toolbar toolbar=findViewById(R.id.toolbaar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView=findViewById(R.id.chat_recyler);
        profileIv=findViewById(R.id.profiletool);
        nameTv=findViewById(R.id.nametool);
        userstatusTv=findViewById(R.id.statustool);
        messageET=findViewById(R.id.message);
        sendbtn=findViewById(R.id.sendbtn);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //create api service
        apiService= Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        Intent intent=getIntent();
        hisUid=intent.getStringExtra("hisUid");

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        userDbReference=firebaseDatabase.getReference("Users");

        final Query userQuery= userDbReference.orderByChild("uid").equalTo(hisUid);

        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String name=""+ds.child("name").getValue();
                     hisimage=""+ds.child("image").getValue();
                     String typingstatus=""+ds.child("typingTo").getValue();
                     if (typingstatus.equals(myUid)){
                         userstatusTv.setText("Typing...");
                     }else{

                         String onlinestatus=""+ds.child("onlineStatus").getValue();
                         if(onlinestatus.equals("Online"))
                         {
                             userstatusTv.setText(onlinestatus);
                         }else {
                             Calendar cal=Calendar.getInstance(Locale.ENGLISH);
                             cal.setTimeInMillis(Long.parseLong(onlinestatus));
                             String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
                             userstatusTv.setText("Last seen: "+dateTime);
                         }
                     }
                    //Toast.makeText(ChatActivity.this, name, Toast.LENGTH_SHORT).show();

                    nameTv.setText(name);
                    try {
                        Picasso.get().load(hisimage).placeholder(R.drawable.ic_default_image).into(profileIv);
                    }catch (Exception e){
                        //Picasso.get().load(R.drawable.ic_default_image).into(profileIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify=true;
                String message=messageET.getText().toString().trim();

                if(TextUtils.isEmpty(message)){
                    Toast.makeText(ChatActivity.this, "cannot send, message is empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    sendMessage(message);
                }
                messageET.setText("");
            }
        });
        //
        messageET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length()==0){
                    checkTypingStatus("noOne");
                }
                else checkTypingStatus(hisUid);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        readMessages();
        seenMessage();
    }

    private void seenMessage() {
        userRefForSeen=FirebaseDatabase.getInstance().getReference("Chats");
        seenListener=userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelChat chat=ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUid)&& chat.getSender().equals(hisUid)){
                        HashMap<String,Object> hasSeenHashmap=new HashMap<>();
                        hasSeenHashmap.put("mstatus",true);
                        ds.getRef().updateChildren(hasSeenHashmap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {
        chatList=new ArrayList<>();
        DatabaseReference dbRfe=FirebaseDatabase.getInstance().getReference("Chats");
        dbRfe.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               chatList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelChat chat=ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)||
                            chat.getReceiver().equals(hisUid)&&chat.getSender().equals(myUid)){
                        chatList.add(chat);
                    }
                    adapterChat=new AdapterChat(ChatActivity.this,chatList,hisimage);
                    adapterChat.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterChat);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(final String message)  {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();

        String timeStamp= String.valueOf(System.currentTimeMillis());
        HashMap<String ,Object> hashMap=new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("receiver",hisUid);
        hashMap.put("timestamp",timeStamp);
        hashMap.put("mstatus",false);
        hashMap.put("message",message);
        databaseReference.child("Chats").push().setValue(hashMap);


        DatabaseReference database=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUser user=dataSnapshot.getValue(ModelUser.class);
                if (notify){
                    senNotification(hisUid,user.getName(),message);
                }
                notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void senNotification(final String hisUid, final String name, final String message) {

        DatabaseReference allTokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    Token token=ds.getValue(Token.class);
                    Data data=new Data(myUid,name+":"+message,"New Message",hisUid,R.drawable.ic_default_image);
                    Sender sender=new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(ChatActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null){
            myUid=user.getUid();
        }
        else{
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbref=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("onlineStatus",status);
        dbref.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing){
        DatabaseReference dbref=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("typingTo",typing);
        dbref.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("Online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timeStamp= String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timeStamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("Online");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

}
