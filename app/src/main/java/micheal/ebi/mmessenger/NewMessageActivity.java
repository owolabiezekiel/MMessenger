package micheal.ebi.mmessenger;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

public class NewMessageActivity extends AppCompatActivity {

    private static final int SEND_SMS_PERMISSIONS_REQUEST = 1;
    private static final int PICK_CONTACT = 2;
    private SmsManager smsManager = SmsManager.getDefault();
    private EditText messageBody;
    private EditText messageAddress;
    private Button sendButton;
    private Button selectContactButton;
    private String phoneNumberFromContacts;
    private String phoneNum;
    private Boolean gotContacts = false;
    private ListView allMessages;
    private ArrayAdapter arrayAdapter;
    ArrayList<String> messagesGrouped = new ArrayList<>();
    private ProgressBar spinner;
    ProgressDialog loading = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);
        spinner=(ProgressBar)findViewById(R.id.progressBar);
        spinner.setVisibility(View.GONE);
        messageBody = (EditText) findViewById(R.id.message_body);
        messageAddress = (EditText) findViewById(R.id.phone_number);
        phoneNum = messageAddress.getText().toString();
        sendButton = (Button) findViewById(R.id.send_message);
        selectContactButton = (Button) findViewById(R.id.select_contact);
        allMessages = (ListView) findViewById(R.id.same_messages_list_view);


        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messagesGrouped);
        allMessages.setAdapter(arrayAdapter);


        Intent populate = getIntent();
        Bundle bundle = populate.getExtras();
        if (bundle != null){
            messagesGrouped = (ArrayList<String>) bundle.get("all_messages");
            String contactPerson = (String) bundle.get("message_title");
            messageAddress.setText(contactPerson);
            //allMessagesTextView.setText("");
            gotContacts = true;
            phoneNumberFromContacts = (String) bundle.get("phone_number");
            phoneNum = phoneNumberFromContacts;

            arrayAdapter.clear();
            for (int i = 0; i < messagesGrouped.size(); i++){
                if(i != 0){
                    arrayAdapter.add(messagesGrouped.get(i));
                }

            }
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sms = messageBody.getText().toString();
                if(!gotContacts)
                    phoneNum = messageAddress.getText().toString();
                if(TextUtils.isEmpty(sms) || TextUtils.isEmpty(phoneNum)){
                    Toast.makeText(NewMessageActivity.this, "Make sure Phone Number and Message Body is not Empty", Toast.LENGTH_LONG).show();
                } else {
                    if (gotContacts == true){
                        Toast.makeText(NewMessageActivity.this, "got number from contacts: " + phoneNumberFromContacts, Toast.LENGTH_LONG).show();
                        onSendClick(phoneNumberFromContacts, sms);
                    } else {
                        phoneNum = messageAddress.getText().toString();
                        Toast.makeText(NewMessageActivity.this, "got number manually: " + phoneNum, Toast.LENGTH_LONG).show();
                        onSendClick(phoneNum, sms);
                    }
                    Toast.makeText(NewMessageActivity.this, "Message Sent", Toast.LENGTH_LONG).show();
                }
            }
        });

        selectContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT);
            }
        });

    }


   public void onSendClick(String number, String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(NewMessageActivity.this, "No permission", Toast.LENGTH_LONG).show();
        } else {
            spinner.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Sending message", Toast.LENGTH_LONG).show();
                String sent = "SMS_SENT";
                PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                        new Intent(sent), 0);

                //---when the SMS has been sent---
                registerReceiver(new BroadcastReceiver(){
                    @Override
                    public void onReceive(Context arg0, Intent arg1) {
                        if(getResultCode() == Activity.RESULT_OK)
                        {
                            Toast.makeText(getBaseContext(), "SMS sent",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(NewMessageActivity.this, MainActivity.class);
                            spinner.setVisibility(View.GONE);
                            Toast.makeText(NewMessageActivity.this, "Message sent", Toast.LENGTH_LONG).show();
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(getBaseContext(), "SMS could not sent",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new IntentFilter(sent));
            smsManager.sendTextMessage(number, null, message, sentPI, null);
            Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        if (requestCode == SEND_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Send SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Send SMS permission denied", Toast.LENGTH_SHORT).show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK){
                    Uri contactData = data.getData();
                    Cursor c = managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()){
                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        if (hasPhone.equalsIgnoreCase("1")){
                            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                            phones.moveToFirst();
                            String cNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String cName = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            messageAddress.setText(cName);
                            phoneNumberFromContacts = cNumber;
                            Toast.makeText(NewMessageActivity.this, "Phone number selected is " + phoneNumberFromContacts, Toast.LENGTH_LONG).show();
                            gotContacts = true;
                            phoneNum = phoneNumberFromContacts;
                        }
                    }
                }
        }
    }

}
