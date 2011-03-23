package com.peruzzo.torpedeira;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ServerActivity extends Activity {
	
	private TextView serverStatus;
	
	private ListView listView;
	
	public static String SERVER_IP;
	
	public static int SERVER_PORT = 2180;
	
	private Handler handler = new Handler();
	
	private ServerSocket serverSocket;
		
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.server);
        
        serverStatus = (TextView) findViewById(R.id.server_status);
        //serverStatus.setBackgroundColor(Color.BLUE);
       
        listView = (ListView) findViewById(R.id.lista);
        listView.setBackgroundColor(Color.WHITE);
        listView.setAdapter(new TwoLayoutsArrayAdapter(this));
        
        SERVER_IP = getLocalIpAddress();
               
        Thread fst = new Thread(new ServerThread());
        fst.start();
       
        for (int i = 0; i < 15; i++) {	
        	int status = i == 5 || i == 12 ? Activity.RESULT_CANCELED : Activity.RESULT_OK;
        	Mensagem mensagem = new Mensagem(status, "99999999", "mensagem " + i);        	
	        addMensagem(mensagem);
        }
    }
    
    
    public class ServerThread implements Runnable {

        public void run() {
            try {
                if (SERVER_IP != null) {                 
               
                serverSocket = new ServerSocket(SERVER_PORT);
               
                    handler.post(new Runnable() {                        
                        public void run() {
                            serverStatus.setText("no IP: " + SERVER_IP + " Porta: " + SERVER_PORT);
                        }
                    });
                   
                    while (true) {
                   
                        Socket client = serverSocket.accept();
                        handler.post(new Runnable() {                            
                            public void run() {
                                serverStatus.setText("Conectado.");
                            }
                        });

                        try {
                            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                            String line = null;
                            while ((line = in.readLine()) != null) {
                                final String[] valores = line.split(";;");                               
                                handler.post(new Runnable() {                                    
                                    public void run() {                                    	
                                    	serverStatus.setText("Enviando para: " + valores[0] + " - " + valores[1]);
                                    }
                                });
                                handler.post(new Runnable() {                                    
                                    public void run() {
                                        sendSMS(valores[0], valores[1]);
                                    }
                                });
                            }
                        } catch (Exception e) {
                        final Exception exe = e;
                            handler.post(new Runnable() {                                
                                public void run() {
                                    serverStatus.setText("Opa. Conexão o interrompida. Por favor reconnect seu celular." + exe.getMessage());
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                } else {
                    handler.post(new Runnable() {                        
                        public void run() {
                            serverStatus.setText("Não foi possível detectar uma conexão.");
                        }
                    });
                }
            } catch (Exception e) {
                handler.post(new Runnable() {                    
                    public void run() {
                        serverStatus.setText("Erro");
                    }
                });
                e.printStackTrace();
            }
        }
    }
    
    private String getLocalIpAddress() {
    	try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if(!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}					
				}
				
			}
			
		} catch (SocketException e) {
			handler.post(new Runnable() {				
				public void run() {
					serverStatus.setText("Não foi possível encontrar um IP válido na rede.");
				}
			});
		}
		return null;
    }
    
    private void sendSMS(final String numero, final String mensagem) {       
        final String SENT = "SMS Enviado";
        final String DELIVERED = "SMS entregue";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        // Registra o evento de envio de SMS
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), SENT, Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Ocorreu um erro", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "Sem serviço", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));
        
        // Registra o evento de entrega do SMS
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:                    	
                        Toast.makeText(getBaseContext(), DELIVERED, Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:                    	
                        Toast.makeText(getBaseContext(), "SMS não entregue.", Toast.LENGTH_SHORT).show();
                        break;                       
                }
                addMensagem(new Mensagem(getResultCode(), numero, mensagem));
            }
        }, new IntentFilter(DELIVERED));       

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(numero, null, mensagem, sentPI, deliveredPI);       
    }
   
    private void addMensagem(Mensagem mensagem) {     
        ((TwoLayoutsArrayAdapter)this.listView.getAdapter()).add(mensagem);   
    }
   
    public void selfDestruct(View view) {
    	onStop();
    	finish();
    }

    @Override
    protected void onStop() {       
        try {            
             serverSocket.close();
        } catch (IOException e) {
             e.printStackTrace();
        }
        super.onStop();    	
    }
    
}