package com.example.satyam.opustry;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.score.rahasak.utils.OpusDecoder;
import com.score.rahasak.utils.OpusEncoder;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CallActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int MY_PERMISSIONS_REQUEST_SAVE_RECORDING = 2;
    private static final int MY_PERMISSIONS_REQUEST_PLAY_RECORDING = 3;
    private static final String TAG = "CallActivity";
    private static final int CALL_PORT = 8080;
    private static final short VOICE_DATA = 1, CALL_ABORT = 2, RINGING = 3;

    private DatagramSocket socket;
    private String remoteAddress;
    private byte[] key;
    private InetAddress ip;
    private String path_dec = "/sdcard/opus_dec.pcm";
    private String path_enc = "/sdcard/opus_enc.pcm";

    private Button acceptBtn, endBtn, mplayRecorded;
    private TextView callStatus;
    private SenderThread mSenderThread;
    private ReceiverThread mReceiverThread;
    private boolean CALLING = false, INCOMING = false, CALL_ESTABLISHED = false;
    private FileOutputStream os = null, os2 = null;
    private boolean callRecording = false;
    private Handler mHandler = new Handler();

    //Audio Track Config
    // Sample rate must be one supported by Opus.
    static final int SAMPLE_RATE = 8000;

    // Number of samples per frame is not arbitrary,
    // it must match one of the predefined values, specified in the standard.
    static final int FRAME_SIZE = 160;

    // 1 or 2
    static final int NUM_CHANNELS = 1;
    static int MIN_BUF_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE,
            NUM_CHANNELS == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.ENCODING_PCM_16BIT);
    private boolean mFocusDuringOnPause;
    private boolean windowAttached;
    private AdvancedEncryptionStandard advancedEncryptionStandard;

    private class Packet {
        private byte[] data;
        private short type;
        private int length;

        public Packet(byte[] data, short type) {
            this.data = data;
            this.length = data.length;
            this.type = type;
        }

        public Packet(byte[] data, int length, short type) {
            this.data = data;
            this.length = length;
            this.type = type;
        }

        public Packet(short tag) {
            this.length = 0;
            this.type = tag;
        }

        public byte[] getData() {
            return data;
        }

        public short getType() {
            return type;
        }

        public int getLength() {
            return length;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowParams();
        initPeerInfo();
        //configForTesting();
        setContentView(R.layout.activity_call);
        initView();
        Log.d("LifeCycle : ", "Oncreate");
        try {
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress("0.0.0.0", CALL_PORT));
        } catch (SocketException e) {
            e.printStackTrace();
            finish();
        }
        CALLING = true;
        if (!INCOMING) {
            setupOutgoingView();
            callPeer();
        }
        else
        {
            setupIncomingView();
        }
        start();
        //startCallRecording();
    }

    @Override
    public void onAttachedToWindow() {
        Log.d("LifeCycle : ", "onAttachedToWindow");
        super.onAttachedToWindow();
        windowAttached = true;
        //setWindowParams();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("LifeCycle : ", "onResumeCalled");
    }

    private void setWindowParams() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private void initPeerInfo() {
        try {
            key = getIntent().getExtras().getByteArray("key");
            remoteAddress = getIntent().getExtras().getString("peer");
            ip = InetAddress.getByName(remoteAddress);
            INCOMING = getIntent().getBooleanExtra("incoming", false);
            advancedEncryptionStandard = new AdvancedEncryptionStandard(key);
            String str = new String(advancedEncryptionStandard.decrypt(advancedEncryptionStandard.encrypt("satyam".getBytes())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configForTesting() {
        remoteAddress = "0.0.0.0";
        try {
            ip = InetAddress.getByName(remoteAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void callPeer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(ip, CallService.CONTROL_PORT), 5000);
                    OutputStream sendStream = socket.getOutputStream();
                    String random = RSAEncryption.getSaltString(16);
                    //String msgSnd = "Please Come In";
                    //byte[] sndBytes = msgSnd.getBytes();
                    //TOdo send myEmail
                    //todo encrypt key with my private key
                    //todo encrypt encrypted key with his public key

                    packAndWriteData(sendStream, key);//Sent AES key
                    InputStream inputStream = socket.getInputStream();
                    byte[] rcvd = readPackedData(inputStream);
                    sendStream.close();
                    inputStream.close();
                    Log.v(TAG, new String(rcvd));

                } catch (IOException e) {
                    e.printStackTrace();
                    finish();
                }
            }
        }).start();
    }

    public static byte[] readPackedData(InputStream inputStream) throws IOException {
        byte[] messageByte = new byte[1024];
        DataInputStream in = new DataInputStream(inputStream);
        messageByte[0] = in.readByte();
        messageByte[1] = in.readByte();
        ByteBuffer byteBuffer = ByteBuffer.wrap(messageByte, 0, 2);
        int bytesToRead = byteBuffer.getShort();
        Log.v("Reading Packed Data", "About to read " + bytesToRead + " octets");
        byte[] data = new byte[bytesToRead];
        in.readFully(data);
        return data;
    }

    public static void packAndWriteData(OutputStream sendStream, byte[] sndBytes) throws IOException {
        if (sndBytes.length > Short.MAX_VALUE - 2)
            throw new UnsupportedOperationException();
        sendStream.write(ByteBuffer.allocate(2).putShort((short) sndBytes.length).array());
        sendStream.write(sndBytes);
        Log.v("Packed TCP", sndBytes.length + "bytes Data written");
    }

    private void initView() {
        acceptBtn = findViewById(R.id.accept_btn);
        endBtn = findViewById(R.id.end_button);
        mplayRecorded = findViewById(R.id.play_button);
        callStatus = findViewById(R.id.call_status);
        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CALL_ESTABLISHED = true;
                Log.v(TAG, " Call Accepted");
                acceptBtn.setVisibility(View.GONE);
            }
        });
        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                terminateCall();
            }
        });
        mplayRecorded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playRecordedVoice();
            }
        });
    }

    private void setupIncomingView() {
        callStatus.setText(remoteAddress + " Calling");
    }

    private void setupOutgoingView() {
        acceptBtn.setVisibility(View.GONE);
        callStatus.setText("Calling " + remoteAddress);
    }

    private void startCallRecording() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_SAVE_RECORDING);
                callRecording = false;
                return;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
        }
        callRecording = true;
        try {
            os = new FileOutputStream(path_dec, false);
            os2 = new FileOutputStream(path_enc, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        CALLING = true;
        //acceptBtn.setEnabled(false);
        endBtn.setEnabled(true);
        mSenderThread = new SenderThread();
        mSenderThread.start();
        mReceiverThread = new ReceiverThread();
        mReceiverThread.start();
    }

    private void end_call() {

        CALLING = false;
        CALL_ESTABLISHED = false;
        try {
            mSenderThread.interrupt();
            mReceiverThread.interrupt();
            mSenderThread.join();
            mReceiverThread.join();
            socket.close();
        } catch (InterruptedException e) {
            Log.w(TAG, "Interrupted waiting for thread to finish");
        }
        endBtn.setEnabled(false);
    }

    @Override
    protected void onPause() {
        mFocusDuringOnPause = hasWindowFocus();
        Log.d("LifeCycle : ", "OnPause with windowFocussed = " + mFocusDuringOnPause);
        super.onPause();
        if (CALLING) {
            //end_call();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    start();
                }
            }
            case MY_PERMISSIONS_REQUEST_SAVE_RECORDING: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCallRecording();
                }
            }
            case MY_PERMISSIONS_REQUEST_PLAY_RECORDING: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    playRecordedVoice();
                }
            }
        }
    }

    private void playRecordedVoice() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_PLAY_RECORDING);
                return;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
        }
        int minBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                NUM_CHANNELS == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        final AudioTrack track = new AudioTrack(AudioManager.STREAM_SYSTEM,
                SAMPLE_RATE,
                NUM_CHANNELS == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufSize,
                AudioTrack.MODE_STREAM);
        track.play();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileInputStream is = new FileInputStream(path_dec);
                    byte[] buff = new byte[1024];
                    int read = 0;
                    while ((read = is.read(buff, 0, buff.length)) > 0) {
                        track.write(buff, 0, read);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private class ReceiverThread extends Thread {
        @Override
        public void run() {

            Log.d(TAG, "Receiver Thread Started");
            // init audio track
            AudioTrack track = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                    SAMPLE_RATE,
                    NUM_CHANNELS == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    MIN_BUF_SIZE,
                    AudioTrack.MODE_STREAM);

            track.play();

            // init opus decoder
            OpusDecoder decoder = new OpusDecoder();
            decoder.init(SAMPLE_RATE, NUM_CHANNELS);
            byte[] outBuf = new byte[FRAME_SIZE * NUM_CHANNELS * 2];

            //TODO
            // try
            //  Keep on receiving calling signal untill CALL_ESTABLISHED = true
            //  if caller cancels the call then Terminate the call and finish Activity
            while (!Thread.interrupted() && !CALL_ESTABLISHED && CALLING) {
                // Encoder must be fed entire frames.
                try {
                    socket.setSoTimeout(5000);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
                byte[] encBuf = new byte[1024];
                Packet dataPacket = null;
                try {
                    dataPacket = receivePacket(encBuf);
                    if (dataPacket.type == VOICE_DATA) {
                        //  if VOICE_DATA or CALL_ACCEPTED Packet received set CALL_ESTABLISHED = true to exit this loop
                        CALL_ESTABLISHED = true;
                    } else if (dataPacket.type == CALL_ABORT) {
                        terminateCall();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    //  Terminate call if no data received from peer for a period of 3-5 seconds as it signifies cancellation of call
                    terminateCall();
                }
            }

            try {
                while (!mReceiverThread.isInterrupted() && CALLING && CALL_ESTABLISHED) {
                    // Encoder must be fed entire frames.
                    byte[] encBuf = new byte[1024];
                    try {
                        final Packet dataPacket = receivePacket(encBuf);
                        if (dataPacket.type == VOICE_DATA) {
                            int decoded = decoder.decode(dataPacket.getData(), outBuf, FRAME_SIZE);
                            Log.v(TAG, "Decoded back " + decoded * NUM_CHANNELS * 2 + " bytes");
                            if (callRecording) {
                                try {
                                    os2.write(outBuf);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            track.write(outBuf, 0, decoded * NUM_CHANNELS * 2);
                        } else {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    CallActivity.this.handleControlInfo(dataPacket);
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        //  Terminate call if no data received from peer for a period of 3-5 seconds as it signifies Connection lost
                        terminateCall();
                    }
                }
            } finally {
                track.stop();
                track.release();
            }
        }
    }

    private void terminateCall() {
        Log.d(TAG, "Terminating call");
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendPacket(new Packet(CALL_ABORT));
                sendPacket(new Packet(CALL_ABORT));
            }
        }).start();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Termination posted to main thread");
                CallActivity.this.end_call();
                finish();
            }
        });
    }

    private void handleControlInfo(Packet dataPacket) {
        switch (dataPacket.getType()) {
            case CALL_ABORT:
                terminateCall();
                break;
        }
    }

    private Packet receivePacket(byte[] buff) throws IOException {
        DatagramPacket dp = new DatagramPacket(buff, buff.length);
        socket.receive(dp);
        byte[] encdata = Arrays.copyOf(buff, dp.getLength());
        byte[] decdata = null;
        try {
            decdata = advancedEncryptionStandard.decrypt(encdata);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] data = Arrays.copyOf(decdata, decdata.length - 2);
        short type = ByteBuffer.wrap(decdata, decdata.length - 2, 2).getShort();
        Log.v(TAG, dp.getLength() - 2 + " bytes of Data Received of type "+type);
        return new Packet(data, type);

    }

    private class SenderThread extends Thread {


        @Override
        public void run() {
            //Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);

            // init opus encoder
            Log.d(TAG, "Sender Thread Started");
            OpusEncoder encoder = new OpusEncoder();
            encoder.init(SAMPLE_RATE, NUM_CHANNELS, OpusEncoder.OPUS_APPLICATION_VOIP);

            // initialize audio recorder
            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    NUM_CHANNELS == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    MIN_BUF_SIZE);

            //TODO While CALL_ESTABLISHED is false keep on sending RINGING signal at regular intervals to indicate that callee is online

            while (!CALL_ESTABLISHED && !interrupted() && CALLING) {
                sendPacket(new Packet(RINGING));
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    terminateCall();
                }
            }

            // When CALL IS ESTABLISHED Start recording and sending VOICE_DATA
            recorder.startRecording();

            byte[] inBuf = new byte[FRAME_SIZE * NUM_CHANNELS * 2];
            byte[] encBuf = new byte[1024];

            try {
                while (!Thread.interrupted() && CALL_ESTABLISHED) {
                    // Encoder must be fed entire frames.
                    int to_read = inBuf.length;
                    int offset = 0;
                    while (to_read > 0) {
                        int read = recorder.read(inBuf, offset, to_read);
                        if (read < 0) {
                            throw new RuntimeException("recorder.read() returned error " + read);
                        }
                        to_read -= read;
                        offset += read;
                    }
                    int encoded = encoder.encode(inBuf, FRAME_SIZE, encBuf);
                    Log.v(TAG, "Encoded " + inBuf.length + " bytes of audio into " + encoded + " bytes");
                    sendPacket(new Packet(encBuf, encoded, VOICE_DATA));
                    if (callRecording) {
                        try {
                            os.write(inBuf);
                            //os2.write(encBuf2);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } finally {
                recorder.stop();
                recorder.release();
            }
        }
    }

    private void sendPacket(Packet dataPacket) {
        byte[] dataBuff = dataPacket.getData();
        int length = dataPacket.getLength();
        short tag = dataPacket.getType();
        if (dataBuff == null)
            dataBuff = new byte[2];
        byte[] encodedParam = ByteBuffer.allocate(2).putShort(tag).array();
        dataBuff[length] = encodedParam[0];
        dataBuff[length + 1] = encodedParam[1];
        byte[] data = Arrays.copyOf(dataBuff,length+2);
        try {
            byte[] encrypted = advancedEncryptionStandard.encrypt(data);
            DatagramPacket dp = new DatagramPacket(encrypted, encrypted.length, ip, CALL_PORT);
            Log.v(TAG, "Sending " + length + " bytes of Data of type " + tag + " to " + remoteAddress);
            socket.send(dp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        Log.d("LifeCycle : ", "OnStop");
        if (mFocusDuringOnPause)
            end_call();
        super.onStop();
    }

    byte[] ShortToByte_ByteBuffer_Method(short[] input) {
        int index;
        int iterations = input.length;

        ByteBuffer bb = ByteBuffer.allocate(input.length * 2);

        for (index = 0; index != iterations; ++index) {
            bb.putShort(input[index]);
        }

        return bb.array();
    }
}