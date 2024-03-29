package berton.sbr.remote.testers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

public class BTTester {
    private boolean scanFinished = false;
    private RemoteDevice hc05device;
    private static final String HC05MAC = "00211303D07A"; // set to null to enable discovery through name
    String macToConnectTo = HC05MAC;
    // set to null if not sure about the url
    String urlToConnectTo = "btspp://" + macToConnectTo + ":1;authenticate=false;encrypt=false;master=false";

    public static void main(String[] args) {
        try {
            new BTTester().go();
        } catch (Exception ex) {
            //Logger.getLogger(BTTester.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex);
        }
    }

    private void go() throws Exception {
        //scan for all devices:
        scanFinished = false;
        LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, new DiscoveryListener() {
            @Override
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                try {
                    String name = btDevice.getFriendlyName(false);
                    System.out.format("%s (%s)\n", name, btDevice.getBluetoothAddress());
                    // if (name.matches("HC.*")) {
                    if (macToConnectTo == null ? name.matches("HC.*")
                            : btDevice.getBluetoothAddress().equals(macToConnectTo)) {
                        hc05device = btDevice;
                        System.out.println("Connected");
                        scanFinished = true;
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
        while (!scanFinished) {
            //this is easier to understand (for me) as the thread stuff examples from bluecove
            Thread.sleep(500);
        }

        if (urlToConnectTo == null) { // only if we are not sure about the url
            //search for services:
            UUID uuid = new UUID(0x1101); //scan for btspp://... services (as HC-05 offers it)
            UUID[] searchUuidSet = new UUID[] { uuid };
            int[] attrIDs = new int[] { 0x0100 // service nameew BTTester().
            };
            scanFinished = false;
            System.out.println("Scanning services...");
            LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, hc05device,
                    new DiscoveryListener() {
                        @Override
                        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                        }

                        @Override
                        public void inquiryCompleted(int discType) {
                        }

                        @Override
                        public void serviceSearchCompleted(int transID, int respCode) {
                            scanFinished = true;
                        }

                        @Override
                        public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                            for (int i = 0; i < servRecord.length; i++) {
                                urlToConnectTo = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT,
                                        false);
                                if (urlToConnectTo != null) {
                                    break; //take the first one
                                }
                            }
                        }
                    });

            while (!scanFinished) {
                Thread.sleep(500);
            }
        }
        System.out.println(hc05device.getBluetoothAddress());
        System.out.println(urlToConnectTo);

        //if you know your hc05Url this is all you need:
        StreamConnection streamConnection = (StreamConnection) Connector.open(urlToConnectTo);
        OutputStream os = streamConnection.openOutputStream();
        InputStream is = streamConnection.openInputStream();

        // os.write("1".getBytes()); //just send '1' to the device
        long time = System.currentTimeMillis();
        String recv = "";
        char ch = '\0';
        while (System.currentTimeMillis() < time + 5000) {
            if (is.available() > 0) {
                ch = (char) is.read();
                if (ch == '\n')
                    ch = '\0';
                recv += ch;
            }
            if (ch == ';') {
                System.out.print("\r" + recv);
                recv = "";
                ch = '\0';
            }
        }
        os.close();
        is.close();
    }
}