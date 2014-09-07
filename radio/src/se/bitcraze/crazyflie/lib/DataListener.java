package se.bitcraze.crazyflie.lib;

import se.bitcraze.crazyflie.lib.crtp.ConsolePacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.Port;
import se.bitcraze.crazyflie.lib.crtp.log.LogConfigReply;
import se.bitcraze.crazyflie.lib.crtp.log.LogData;
import se.bitcraze.crazyflie.lib.crtp.toc.TocInfoReply;
import se.bitcraze.crazyflie.lib.crtp.toc.TocItem;

/**
 * Interface for receiving notifications about data received from the Crazyflie
 * on a {@link Link}.
 */
public interface DataListener {


    /**
     * Called when a console packet has been received
     *
     * @param packet the received console packet
     */
    public void consolePacketReceived(ConsolePacket packet);

    /**
     * Called when a log block reply packet has been received
     *
     * @param packet the received log block reply packet
     */
    public void logConfigReplyReceived(LogConfigReply packet);

    /**
     * Called when a log data packet has been received
     *
     * @param packet the received log data packet
     */
    public void logDataReceived(LogData packet);

    /**
     * Called when a TOC item has been received
     *
     * @param source the link which received the packet
     * @param port the port on which the packet has been received
     * @param item the received item
     */
    public void tocItemReceived(Link source, Port port, TocItem item);

    /**
     * Called when a TOC info has been received
     *
     * @param source the link which received the packet
     * @param port the port on which the packet has been received
     * @param item the received info
     */
    public void tocInfoReceived(Link source, Port port, TocInfoReply info);

    /**
     * Called when a parameter data packet has been received
     *
     * @param packet the received parameter data packet
     */
    public void parameterDataReceived(CrtpPacket packet);

}
