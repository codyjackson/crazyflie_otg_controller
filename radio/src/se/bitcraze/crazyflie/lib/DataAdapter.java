package se.bitcraze.crazyflie.lib;

import se.bitcraze.crazyflie.lib.crtp.ConsolePacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.Port;
import se.bitcraze.crazyflie.lib.crtp.log.LogConfigReply;
import se.bitcraze.crazyflie.lib.crtp.log.LogData;
import se.bitcraze.crazyflie.lib.crtp.toc.TocInfoReply;
import se.bitcraze.crazyflie.lib.crtp.toc.TocItem;

/**
 * An abstract adapter class for receiving data events. The methods in
 * this class are empty. This class exists as convenience for creating listener
 * objects.
 */
public abstract class DataAdapter implements DataListener {

    /* (non-Javadoc)
     * @see se.bitcraze.crazyflie.lib.DataListener#consolePacketReceived(se.bitcraze.crazyflie.lib.crtp.ConsolePacket)
     */
    public void consolePacketReceived(ConsolePacket packet){
    }

    /* (non-Javadoc)
     * @see se.bitcraze.crazyflie.lib.DataListener#logConfigReplyReceived(se.bitcraze.crazyflie.lib.crtp.log.LogConfigReply)
     */
    public void logConfigReplyReceived(LogConfigReply packet){
    }

    /* (non-Javadoc)
     * @see se.bitcraze.crazyflie.lib.DataListener#logDataReceived(se.bitcraze.crazyflie.lib.crtp.log.LogData)
     */
    public void logDataReceived(LogData packet){
    }

    /* (non-Javadoc)
     * @see se.bitcraze.crazyflie.lib.DataListener#tocItemReceived(se.bitcraze.crazyflie.lib.Link, se.bitcraze.crazyflie.lib.crtp.Port, se.bitcraze.crazyflie.lib.crtp.toc.TocItem)
     */
    public void tocItemReceived(Link source, Port port, TocItem item){
    }

    /* (non-Javadoc)
     * @see se.bitcraze.crazyflie.lib.DataListener#tocInfoReceived(se.bitcraze.crazyflie.lib.Link, se.bitcraze.crazyflie.lib.crtp.Port, se.bitcraze.crazyflie.lib.crtp.toc.TocInfoReply)
     */
    public void tocInfoReceived(Link source, Port port, TocInfoReply info){
    }

    /* (non-Javadoc)
     * @see se.bitcraze.crazyflie.lib.DataListener#parameterDataReceived(se.bitcraze.crazyflie.lib.crtp.CrtpPacket)
     */
    public void parameterDataReceived(CrtpPacket packet) {
    }
}
