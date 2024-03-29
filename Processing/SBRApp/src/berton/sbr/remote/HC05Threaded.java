package berton.sbr.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.Connector;

/**
 * <h4>Public variables:</h4>
 * <ul style="list-style-type:disc;">
 * <li> <code> public static String mac</code>: the mac address of the target HC05 </li>
 * <li> <code> public static String hcName</code>: the visible name of the target HC05. If the mac is not set the name will be used instead </li>
 * <li> <code> public static String url</code>: url for the bluetooth services. Leave default if not sure. </li>
 * <li> <code> public staitc char END_CH</code>: end character for both transmission and reception of strings with the target HC05 </li>
 * </ul>
 * 
 * How to use: after creating the object just use {@code hc05.start()}. Then you can use {@code void connect()/disconnect()} for
 * obvious reasons, {@code boolean onConnect()/onDisconnect()} to execute actions on {@code connection/disconnection}.
 * Receive strings with {@code String getStringFromHC05()}, send with {@code void sendString(String s)}. Quit using {@code void disableThread()}.
 * 
 */
public class HC05Threaded extends Thread {
    public static String mac = "00211303D07A";
    public static String hcName = "HC.*";
    public static String url = "btspp://" + mac + ":1;authenticate=false;encrypt=false;master=false";
    public static char END_CH = ';';

    private volatile String recvString;
    private volatile String bufferString;
    private volatile boolean scanFinished;
    private volatile static RemoteDevice device;
    private volatile StreamConnection s;
    private volatile OutputStream os;
    private volatile InputStream is;
    private volatile boolean toConnect;
    private volatile boolean toDisconnect;
    private volatile boolean onConnect;
    private volatile boolean quit;

    private void setup() {
        recvString = "";
        bufferString = "";
        toConnect = false;
        toDisconnect = false;
        onConnect = false;
        quit = false;
        device = null;
        s = null;
        is = null;
        os = null;
        scanFinished = false;
    }

    public HC05Threaded() {
        setup();
    }

    public void connect() {
        toConnect = true;
    }

    public void disconnect() {
        toDisconnect = true;
    }

    /**
     * Return the last received string from the HC05.
     * <pre> {@code 
     * public String getStringFromHC05() {
     *    String s = recvString.toString();
     *    recvString = "";
     *    return s;
     * }
     * </pre>
     * @return the last string received, if any.
     */
    public String getStringFromHC05() {
        String s = recvString.toString();
        recvString = "";
        return s;
    }

    public void sendString(String text) {
        if (isConnected() && text != null) {
            // System.out.println("Sending " + text);
            try {
                os.write(text.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Device: " + device + "; text: " + text);
        }
    }

    public boolean isConnected() {
        return device != null && os != null && is != null;
    }

    public void run() {
        System.out.println("Started hc05 thread");
        while (!quit) {

            if (toConnect) {
                System.out.println("Connecting...");
                scanFinished = false;
                try {
                    LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC,
                            new DiscoveryListener() {
                                @Override
                                public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                                    try {
                                        if (mac == null) {
                                            String name = btDevice.getFriendlyName(false);
                                            System.out.format("%s (%s)\n", name, btDevice.getBluetoothAddress());
                                            if (name.matches(hcName)) {
                                                System.out.println("Device found by name.");
                                                device = btDevice;
                                                scanFinished = true;
                                                return;
                                            }
                                        } else if (btDevice.getBluetoothAddress().equals(mac)) {
                                            System.out.println("Device found by mac.");
                                            device = btDevice;
                                            scanFinished = true;
                                            return;
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void inquiryCompleted(int discType) {
                                    scanFinished = true;
                                }

                                @Override
                                public void serviceSearchCompleted(int transID, int respCode) {
                                }

                                @Override
                                public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                                }
                            });
                } catch (BluetoothStateException e) {
                    e.printStackTrace();
                }
                while (!scanFinished)
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                if (device != null) {
                    try {
                        s = (StreamConnection) Connector.open(url);
                        is = s.openInputStream();
                        os = s.openOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Connection successful");
                    onConnect = true;
                } else {
                    System.out.println("Connection unsuccesful/Device not found. Retry.");
                }

                toConnect = false;
            }

            if (toDisconnect) {
                if (isConnected()) {
                    try {
                        s.close();
                        os.close();
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                setup();
                System.out.println("Disconnected");
            }

            if (isConnected()) {
                try {
                    if (is.available() > 0) {
                        char ch = '\0';
                        bufferString = "";
                        // read the first recognizable string
                        while (ch != END_CH) {
                            ch = (char) is.read();
                            if (ch == '\n' || ch == '\r')
                                ch = '\0';
                            bufferString += ch;
                        }

                        recvString = bufferString.toString();
                        // empty buffer
                        while (is.available() > 0)
                            is.read();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Stopped hc05 thread");
    }

    public boolean onConnect() {
        if (onConnect) {
            onConnect = false;
            return true;
        }
        return false;
    }

    public void enableThread() {
        quit = false;
    }

    public void disableThread() {
        quit = true;
    }
}