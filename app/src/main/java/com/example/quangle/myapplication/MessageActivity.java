package com.example.quangle.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.scaledrone.lib.HistoryRoomListener;
import com.scaledrone.lib.Listener;
import com.scaledrone.lib.Room;
import com.scaledrone.lib.RoomListener;
import com.scaledrone.lib.Scaledrone;
import com.scaledrone.lib.SubscribeOptions;

import java.util.Random;

public class MessageActivity extends AppCompatActivity implements RoomListener {

        private String channelID = "PEZK7EebF9AOmP1H";
        private String roomName = "observable-room1";
        private EditText editText;
        private Scaledrone scaledrone;
        private MessageAdapter messageAdapter;
        private ListView messagesView;
        private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        private String uid = user.getUid();


        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.messenger);
            // This is where we write the mesage adCv5bIJMG
            editText = (EditText) findViewById(R.id.editText);
            messageAdapter = new MessageAdapter(this);
            messagesView = (ListView) findViewById(R.id.messages_view);
            messagesView.setAdapter(messageAdapter);
            User data = new User(" ","grey");
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                roomName = "observable-" +extras.getString("EXTRA_SESSION_ID");
                //The key argument here must match that used in the other activity

            }
            scaledrone = new Scaledrone(channelID, data);

            scaledrone.connect(new Listener() {
                @Override
                public void onOpen() {
                    Room room = scaledrone.subscribe(roomName, new RoomListener() {
                        @Override
                        public void onOpen(Room room) {

                        }

                        @Override
                        public void onOpenFailure(Room room, Exception ex) {

                        }

                        @Override
                        public void onMessage(Room room, com.scaledrone.lib.Message receivedMessage) {
                            final ObjectMapper mapper = new ObjectMapper();
//                            try {
                                // member.clientData is a MemberData object, let's parse it as such
//                                final User data = mapper.treeToValue(receivedMessage.getMember().getClientData(), User.class);
                                final User data = new User(" ", "grey");
                                String m = receivedMessage.getData().asText();
                                String []receive = m.split(":");
                                // if the clientID of the message sender is the same as our's it was sent by us

                                boolean belongsToCurrentUser = receivedMessage.getClientID().equals(scaledrone.getClientID());
                                // since the message body is a simple string in our case we can use json.asText() to parse it as such
                                // if it was instead an object we could use a similar pattern to data parsing
                                System.out.println(scaledrone.getClientID());

                                final Message message = new Message(receive[1], data, belongsToCurrentUser);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageAdapter.add(message);
                                        // scroll the ListView to the last added element
                                        messagesView.setSelection(messagesView.getCount() - 1);
                                    }
                                });
                            /*} catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }*/
                        }
                        // implement the default RoomListener methods here
                    }, new SubscribeOptions(5)); // ask for 50 messages from the history

                    room.listenToHistoryEvents(new HistoryRoomListener() {
                        @Override
                        public void onHistoryMessage(Room room, com.scaledrone.lib.Message receivedMessage) {
                            //final ObjectMapper mapper = new ObjectMapper();
                            //try {
                                // member.clientData is a MemberData object, let's parse it as such
                                //final User data = mapper.treeToValue(receivedMessage.getMember().getClientData(), User.class);
                                String m = receivedMessage.getData().asText();
                                System.out.println(m);
                                String []receive = m.split(":");
                                System.out.println(receive.length);
                                final User data = new User(" ", "grey");
                                // if the clientID of the message sender is the same as our's it was sent by us
                                boolean belongsToCurrentUser = receive[0].equals(uid);
                                // since the message body is a simple string in our case we can use json.asText() to parse it as such
                                // if it was instead an object we could use a similar pattern to data parsing
                                final Message message = new Message(receive[1], data, belongsToCurrentUser);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messageAdapter.add(message);
                                        // scroll the ListView to the last added element
                                        messagesView.setSelection(messagesView.getCount() - 1);
                                    }
                                });
                            /*} catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }*/
                            System.out.println("Received a message from the past " +receivedMessage.getClientID() +receivedMessage.getData().asText());
                        }

                    });
                    System.out.println("Scaledrone connection open");
                    // Since the MainActivity itself already implement RoomListener we can pass it as a target
                    //scaledrone.subscribe(roomName, MessageActivity.this);
                }

                @Override
                public void onOpenFailure(Exception ex) {
                    System.err.println(ex);
                }

                @Override
                public void onFailure(Exception ex) {
                    System.err.println(ex);
                }

                @Override
                public void onClosed(String reason) {
                    System.err.println(reason);
                }
            });
        }

        // Connect to Scaledrone room
        public void onOpen(Room room){
            System.out.println("Connected to room");
        }

        public void onOpenFailure(Room room, Exception ex){
            System.err.println(ex);
        }

        //@Override
        public void onMessage(Room room, com.scaledrone.lib.Message receivedMessage) {
            // To transform the raw JsonNode into a POJO we can use an ObjectMapper
            final ObjectMapper mapper = new ObjectMapper();
            try {
                // member.clientData is a MemberData object, let's parse it as such
                final User data = mapper.treeToValue(receivedMessage.getMember().getClientData(), User.class);
                // if the clientID of the message sender is the same as our's it was sent by us
                boolean belongsToCurrentUser = receivedMessage.getClientID().equals(scaledrone.getClientID());
                // since the message body is a simple string in our case we can use json.asText() to parse it as such
                // if it was instead an object we could use a similar pattern to data parsing
                final Message message = new Message(receivedMessage.getData().asText(), data, belongsToCurrentUser);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messageAdapter.add(message);
                        // scroll the ListView to the last added element
                        messagesView.setSelection(messagesView.getCount() - 1);
                    }
                });
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(View view) {
            String message = editText.getText().toString();
            String m = uid + ":" + message;
            if (message.length() > 0) {
                scaledrone.publish(roomName, m);
                editText.getText().clear();
            }
        }



}
