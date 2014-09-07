package se.bitcraze.crazyflie.lib.crtp.toc;

import java.nio.ByteBuffer;

import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;

/**
 * Reply for a {@link TocInfoRequest}.
 */
public class TocInfoReply {

    private final byte mNumberOfVariables;
    private final int mChecksum;
    private final int mMaxNoOfLogConfigs;
    private final int mMaxNoOfLogVariables;

    protected TocInfoReply(byte numberOfVariables, int checksum, int maxNoOfLogConfigs, int maxNoOfLogVariables) {
        this.mNumberOfVariables = numberOfVariables;
        this.mChecksum = checksum;
        this.mMaxNoOfLogConfigs = maxNoOfLogConfigs;
        this.mMaxNoOfLogVariables = maxNoOfLogVariables;
    }

    /**
     * Parse the reply from raw data.
     *
     * @param data the raw data containing the reply.
     * @return the parsed reply
     */
    public static TocInfoReply parse(byte[] data) {
        if (data[0] != TocInfoRequest.COMMAND_IDENTIFIER) {
            throw new IllegalArgumentException("data doesn't contain a reply for a TOC info");
        }
        final ByteBuffer buffer = ByteBuffer.wrap(data, 2, data.length - 2).order(CrtpPacket.BYTE_ORDER);
        return new TocInfoReply(data[1], buffer.getInt(), data[6], data[7]);
    }

    public byte getNumberOfVariables() {
        return mNumberOfVariables;
    }

    public int getChecksum() {
        return mChecksum;
    }

    public int getMaxNoOfLogConfigs() {
        return mMaxNoOfLogConfigs;
    }

    public int getMaxNoOfLogVariables() {
        return mMaxNoOfLogVariables;
    }

    public String toString(){
        return "TocInfo: NoOfVariables: " + this.getNumberOfVariables() + ", Checksum: " + this.getChecksum();
    }
}
