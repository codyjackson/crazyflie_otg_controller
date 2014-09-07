
package se.bitcraze.crazyflie.lib.crtp.toc;

import java.nio.ByteBuffer;

import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.Port;

/**
 * Packet for requesting a TOC item.
 */
public class TocItemRequest extends CrtpPacket {

    public static final byte COMMAND_IDENTIFIER = (byte) 0x00;

    private final Byte mIndex;

    /**
     * Request the first TOC item.
     *
     * @param port the port where the request should be made
     */
    public TocItemRequest(Port port) {
        this(port, null);
    }

    /**
     * Request a specific TOC item.
     *
     * @param port the port where the request should be made
     * @param index the index of the requested item.
     */
    public TocItemRequest(Port port, Byte index) {
        super(0, port);
        this.mIndex = index;
    }

    @Override
    protected void serializeData(ByteBuffer buffer) {
        buffer.put(COMMAND_IDENTIFIER);
        if (mIndex != null) {
            buffer.put(mIndex);
        }
    }

    @Override
    protected int getDataByteCount() {
        return (mIndex == null ? 1 : 2);
    }

    public String toString(){
        return "TocItemRequest: Port: " + this.getHeader().getPort() + " Channel: " + this.getHeader().getChannel() +
                (this.mIndex != null ? " Index: " + this.mIndex : "");
    }
}
