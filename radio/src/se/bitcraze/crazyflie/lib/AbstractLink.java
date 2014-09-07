/**
 *
 */

package se.bitcraze.crazyflie.lib;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.crtp.ConsolePacket;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.Port;
import se.bitcraze.crazyflie.lib.crtp.log.LogConfigReply;
import se.bitcraze.crazyflie.lib.crtp.log.LogData;
import se.bitcraze.crazyflie.lib.crtp.toc.TocInfoReply;
import se.bitcraze.crazyflie.lib.crtp.toc.TocInfoRequest;
import se.bitcraze.crazyflie.lib.crtp.toc.TocItem;
import se.bitcraze.crazyflie.lib.crtp.toc.TocItemRequest;

/**
 * This class provides a skeletal implementation of the {@link Link} interface
 * to minimize the effort required to implement the interface.
 */
public abstract class AbstractLink implements Link {

    final Logger mLogger = LoggerFactory.getLogger("AbstractLink");

    private List<ConnectionListener> mConnectionListeners;
    private List<DataListener> mDataListeners;

    /**
     * Create a new abstract link.
     */
    public AbstractLink() {
        this.mConnectionListeners = Collections.synchronizedList(new LinkedList<ConnectionListener>());
        this.mDataListeners = Collections.synchronizedList(new LinkedList<DataListener>());
    }

    /*
     * (non-Javadoc)
     * @see
     * se.bitcraze.crazyflielib.Link#addConnectionListener(se.bitcraze.crazyflielib
     * .ConnectionListener)
     */
    @Override
    public void addConnectionListener(ConnectionListener l) {
        this.mConnectionListeners.add(l);
    }

    /*
     * (non-Javadoc)
     * @see se.bitcraze.crazyflielib.Link#removeConnectionListener(se.bitcraze.
     * crazyflielib.ConnectionListener)
     */
    @Override
    public void removeConnectionListener(ConnectionListener l) {
        this.mConnectionListeners.remove(l);
    }

    /*
     * (non-Javadoc)
     * @see
     * se.bitcraze.crazyflielib.Link#addDataListener(se.bitcraze.crazyflielib
     * .DataListener)
     */
    @Override
    public void addDataListener(DataListener l) {
        this.mDataListeners.add(l);
    }

    /*
     * (non-Javadoc)
     * @see
     * se.bitcraze.crazyflielib.Link#removeDataListener(se.bitcraze.crazyflielib
     * .DataListener)
     */
    @Override
    public void removeDataListener(DataListener l) {
        this.mDataListeners.remove(l);
    }

    /**
     * Handle the response from the Crazyflie. Parses the CRTP packet and informs
     * registered listeners.
     *
     * @param payload the data received from the Crazyflie. Must not include any
     *            headers or other attachments added by the link.
     */
    protected void handleResponse(CrtpPacket responsePacket) {
        Port port = responsePacket.getHeader().getPort();
        switch (port) {
        case CONSOLE:
            notifyConsolePacketReceived(ConsolePacket.parse(responsePacket.getPayload()));
            break;
        case LOG:
            if (responsePacket.getHeader().getChannel() == 0) { // TOC
                handleTocPacket(port, responsePacket.getPayload());
            } else if (responsePacket.getHeader().getChannel() == 1) { // Log settings access
                notifyLogConfigReplyReceived(LogConfigReply.parse(responsePacket.getPayload()));
            } else if (responsePacket.getHeader().getChannel() == 2) { // Log data
                notifyLogDataReceived(LogData.parse(responsePacket.getPayload()));
            }
            break;
        case PARAMETERS:
            if (responsePacket.getHeader().getChannel() == 0) { // TOC
                handleTocPacket(port, responsePacket.getPayload());
            } else if (responsePacket.getHeader().getChannel() == 1 ||   // Parameter read
                        responsePacket.getHeader().getChannel() == 2) {   // Parameter write
                notifyParameterDataReceived(responsePacket);
            }
            break;
        default:
            mLogger.warn("Unknown type of packet: port: " + responsePacket.getHeader().getPort() + " channel: " + responsePacket.getHeader().getChannel());
            break;
        }
    }

    /**
     * Handle a response which contains a TOC packet. Parses the TOC packet and informs registered listeners.
     *
     * @param link the link where the packet was received
     * @param payload the data of the packet without the header
     */
    protected void handleTocPacket(Port port, final byte[] payload) {
        switch (payload[0]) {
            case TocInfoRequest.COMMAND_IDENTIFIER:
                notifyTocInfoReceived(port, TocInfoReply.parse(payload));
                break;
            case TocItemRequest.COMMAND_IDENTIFIER:
                notifyTocItemReceived(port, TocItem.parse(port, payload));
                break;
            default:
                mLogger.warn("unknown logging TOC packet, command = " + payload[0]);
                break;
        }
    }

    /**
     * Notify all registered listeners about an initiated connection.
     */
    protected void notifyConnectionInitiated() {
        synchronized (this.mConnectionListeners) {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.connectionInitiated(this);
            }
        }
    }

    /**
     * Notify all registered listeners about a setup connection.
     */
    protected void notifyConnectionSetupFinished() {
        synchronized (this.mConnectionListeners) {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.connectionSetupFinished(this);
            }
        }
    }

    /**
     * Notify all registered listeners about a disconnect.
     */
    protected void notifyDisconnected() {
        synchronized (this.mConnectionListeners) {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.disconnected(this);
            }
        }
    }

    /**
     * Notify all registered listeners about a lost connection.
     */
    protected void notifyConnectionLost() {
        synchronized (this.mConnectionListeners) {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.connectionLost(this);
            }
        }
    }

    /**
     * Notify all registered listeners about a failed connection attempt.
     */
    protected void notifyConnectionFailed() {
        synchronized (this.mConnectionListeners) {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.connectionFailed(this);
            }
        }
    }

    /**
     * Notify all registered listeners about the link status.
     *
     * @param quality quality of the link (0 = connection lost, 100 = good)
     * @see ConnectionListener#linkQualityUpdate(Link, int)
     */
    protected void notifyLinkQuality(int quality) {
        synchronized (this.mConnectionListeners) {
            for (ConnectionListener cl : this.mConnectionListeners) {
                cl.linkQualityUpdate(this, quality);
            }
        }
    }

    /**
     * Notify all registered listeners about a received console packet.
     *
     * @param packet the received console packet
     * @see DataListener#consolePacketReceived(ConsolePacket)
     */
    protected void notifyConsolePacketReceived(ConsolePacket packet) {
        mLogger.debug("received " + packet);
        synchronized (this.mDataListeners) {
            for (DataListener dl : this.mDataListeners) {
                dl.consolePacketReceived(packet);
            }
        }
    }

    /**
     * Notify all registered listeners about a received TOC info.
     *
     * @param port the port where the info was received
     * @param info the received TOC info
     * @see DataListener#tocInfoReceived(Link, Port, TocInfoReply)
     */
    protected void notifyTocInfoReceived(Port port, TocInfoReply info) {
        synchronized (this.mConnectionListeners) {
            for (DataListener dl : this.mDataListeners) {
                dl.tocInfoReceived(this, port, info);
            }
        }
    }

    /**
     * Notify all registered listeners about a received TOC item.
     *
     * @param port the port where the item was received
     * @param tocItem the received TOC item
     * @see DataListener#tocItemReceived(Link, Port, TocItem)
     */
    protected void notifyTocItemReceived(Port port, TocItem tocItem) {
        synchronized (this.mConnectionListeners) {
            for (DataListener dl : this.mDataListeners) {
                dl.tocItemReceived(this, port, tocItem);
            }
        }
    }

    /**
     * Notify all registered listeners about a received log config reply.
     *
     * @param logConfigReply the received log block reply
     * @see DataListener#logConfigReplyReceived(LogConfigReply)
     */
    protected void notifyLogConfigReplyReceived(LogConfigReply logConfigReply) {
        synchronized (this.mConnectionListeners) {
            for (DataListener dl : this.mDataListeners) {
                dl.logConfigReplyReceived(logConfigReply);
            }
        }
    }

    /**
     * Notify all registered listeners about a received log data packet.
     *
     * @param logData the received log data packet
     * @see DataListener#logDataReceived(LogData)
     */
    protected void notifyLogDataReceived(LogData logData) {
        synchronized (this.mConnectionListeners) {
            for (DataListener dl : this.mDataListeners) {
                dl.logDataReceived(logData);
            }
        }
    }

    /**
     * Notify all registered listeners about a received parameter data packet.
     *
     * @param logData the received log data packet
     * @see DataListener#logDataReceived(LogData)
     */
    protected void notifyParameterDataReceived(CrtpPacket packet) {
        synchronized (this.mConnectionListeners) {
            for (DataListener dl : this.mDataListeners) {
                dl.parameterDataReceived(packet);
            }
        }
    }
}
