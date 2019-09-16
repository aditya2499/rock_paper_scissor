package com.example.android.rock_paper_scissor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate.Status;
import com.google.android.gms.nearby.connection.Strategy;


import static java.nio.charset.StandardCharsets.UTF_8;

public class MainActivity extends AppCompatActivity {


    private static final String Tag="rock_paper_scissor";

    private static final Strategy STRATEGY = Strategy.P2P_STAR;

    private enum GameChoice {
        ROCK,
        PAPER,
        SCISSORS;

        boolean beats(GameChoice other) {
            return (this == GameChoice.ROCK && other == GameChoice.SCISSORS)
                    || (this == GameChoice.SCISSORS && other == GameChoice.PAPER)
                    || (this == GameChoice.PAPER && other == GameChoice.ROCK);
        }
    }

    private final String codeName="Abc";
    private ConnectionClient connectionclient;
    private String opponentName;
    private String opponentEndpointID;
    private int opponentscore;
    private GameChoice opponentChoice;
    private int myScore;
    private GameChoice myChoice;
    private Button findOpponentButton;
    private Button rockButton;
    private Button paperButton;
    private Button scissorButton;
    private Button disconnectButton;
    private TextView statusText;
    private TextView scoreText;
    private TextView opponentText;
    private TextView myName;

    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    opponentChoice = GameChoice.valueOf(new String(payload.asBytes(), UTF_8));
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    if (update.getStatus() == Status.SUCCESS && myChoice != null && opponentChoice != null) {
                        finishRound();
                    }
                }
            };

    private void startAdvertising() {
        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
        connectionclient
                .startAdvertising(
                        codeName, getPackageName(), connectionLifecycleCallback, advertisingOptions)

                ;
    }

    private void startDiscovery() {
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        connectionclient
                .startDiscovery(getPackageName(), endpointDiscoveryCallback, discoveryOptions)
                ;
    }

    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                    // An endpoint was found. We request a connection to it.
                    connectionclient
                            .requestConnection(codeName, endpointId, connectionLifecycleCallback);
                }
            };

    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    // Automatically accept the connection on both sides.
                    connectionclient.acceptConnection(endpointId, payloadCallback);
                    opponentName= connectionInfo.getEndpointName();

                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            connectionclient.stopAdvertising();
                            connectionclient.stopDiscovering();
                            opponentEndpointID=endpointid;
                            setOpponentName(opponentName);
                            setStatusText(getString(R.string.status_connected));
                            setButtonState(true);


                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // The connection was rejected by one or both sides.
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // The connection broke before it was able to be accepted.
                            break;
                        default:
                            // Unknown status code
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                    resetGame();
                }
            };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findOpponentButton =findViewById(R.id.find_opponent);
        disconnectButton= findViewById(R.id.disconnect);
        rockButton = findViewById(R.id.rock);
        paperButton= findViewById(R.id.paper);
        scissorButton = findViewById(R.id.scissor);

        opponentText = findViewById(R.id.opponentname);
        statusText = findViewById(R.id.status);
        scoreText = findViewById(R.id.score);
        myName = findViewById(R.id.name);
        myName.setText("yours Name is" + codeName );

        connectionclient =Nearby.getConnectionsClient(this);
        resetGame();
    }
@Override
protected void onStart(){
        super.onStart();

}

  @Override
    protected void onStop()
  {
     connectionclient.stopAllEndpoints();

     super.onStop();
  }

  public void findOpponent(View view)
  {
      startAdvertising();
      startDiscovery();
      findOpponentButton.setEnabled(false);
  }

  public void disconnect(View view)
  {
      connectionclient.disconnectFromEndPoint(oppponentEndpointId);
  }

  public void makemove(View view){
        if(view.getId()== R.id.rock)
                sendGameChoice(GameChoice.ROCK);
      if(view.getId()== R.id.paper)
          sendGameChoice(GameChoice.PAPER);
      if(view.getId()== R.id.scissor)
          sendGameChoice(GameChoice.SCISSORS);

  }

  private void sendGameChoice(GameChoice choice){
        myChoice= choice;
        connectionclient.sendPayload(opponentEndpointID,Payload.fromBytes(choice.name().getBytes(UTF_8)));
        setGameChoiceEnabled(false);
  }

  private void setButtonState(boolean connect){
        findOpponentButton.setEnabled(true);
        findOpponentButton.setVisibility(connect ? View.GONE : View.VISIBLE);
        disconnectButton.setVisibility(connect ? View.VISIBLE : View.GONE);
  }
  private void setGameChoiceEnabled(boolean enabled){
        rockButton.setEnabled(enabled);
        paperButton.setEnabled(enabled);
        scissorButton.setEnabled(enabled);
  }
   private void setOpponentName(String opponentName){
   opponentText.setText("opponentname: "+ opponentName);

   }

   private void updateScore(int myScore, int opponentscore)
   {
       scoreText.setText(myScore +" : "+ opponentscore);

   }

   private void finishRound(){
        if(myChoice.beats(opponentChoice))
        { setStatusText(myName+"wins");
          myScore++;}

        else if(myChoice==opponentChoice)
        setStatusText("you choose same choice");

        else {
            setStatusText(opponentName+"wins");
            opponentscore++;
        }

        myChoice= null;
        opponentChoice=null;
        updateScore(myScore , opponentscore);
        setGameChoiceEnabled(true);
   }

   private void setStatusText(String text){
     statusText.setText(text);

   }

   private void resetGame()
   {
       opponentEndpointID=null;
       opponentName=null;
       opponentChoice=null;
       opponentscore=0;
       myChoice=null;
       myScore=0;

       setOpponentName("none yet");;
       setStatusText("Disconnected");
       updateScore(myScore,opponentscore);
       setButtonState(false);
   }

}
