package se.bitcraze.crazyflie.lib;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.CrazyradioLink.ConnectionData;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;

public class DebugLink extends AbstractLink {

    final Logger logger = LoggerFactory.getLogger("DebugLink");
    private Thread mDebugLinkThread;

    private final BlockingDeque<CrtpPacket> mSendQueue;

    public DebugLink() {
        this.mSendQueue = new LinkedBlockingDeque<CrtpPacket>();
    }


    @Override
    public void connect(ConnectionData connectionData) {
        logger.debug("DebugLink start()");
        notifyConnectionInitiated();

        mDebugLinkThread = new Thread(debugRunnable);
        mDebugLinkThread.start();
    }

    @Override
    public void disconnect() {
        logger.debug("DebugLink stop()");
        if (mDebugLinkThread != null) {
            mDebugLinkThread.interrupt();
            mDebugLinkThread = null;
        }

        notifyDisconnected();
    }

    @Override
    public boolean isConnected() {
        return mDebugLinkThread != null;
    }

    @Override
    public void send(CrtpPacket p) {
        this.mSendQueue.addLast(p);
    }


    private final Runnable debugRunnable = new Runnable() {
        @Override
        public void run() {
            notifyConnectionSetupFinished();

            while(true){
                try{
                    //Take packet from the queue
                    CrtpPacket p = mSendQueue.pollFirst(5, TimeUnit.MILLISECONDS);
                    if (p == null) { // if no packet was available in the send queue
                        p = CrtpPacket.NULL_PACKET;
                    }

                    notifyLinkQuality(100);

                    //TODO: Fake stuff
                    //TODO: Print Packets to console
                    //TODO: CrtpPacket.toString() -> übersichtliche Darstellung des Paketinhalts
                    //TODO: implement missing methods (eg scan interfaces)

                } catch(InterruptedException e) {
                    break;
                }
            }
        }
    };

}
