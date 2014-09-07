
package se.bitcraze.crazyflie.lib.crtp.toc;

import java.nio.ByteBuffer;

import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.Port;

/**
 * Request a summary about the TOC which includes CRC and item count of the TOC.
 */
public class TocInfoRequest extends CrtpPacket {

    public static final byte COMMAND_IDENTIFIER = (byte) 0x01;

    /**
     * Create a new request.
     * 
     * @param port the port where the request should be made
     */
    public TocInfoRequest(Port port) {
        super(0, port);
    }

    @Override
    protected void serializeData(ByteBuffer buffer) {
        buffer.put(COMMAND_IDENTIFIER);
    }

    @Override
    protected int getDataByteCount() {
        return 1;
    }

    @Override
    public String toString() {
        return "TOCInfoRequest: Port: " + this.getHeader().getPort() + " Channel: " + this.getHeader().getChannel();
    }

}
