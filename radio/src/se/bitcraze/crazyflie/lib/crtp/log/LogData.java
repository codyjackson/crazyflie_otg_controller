package se.bitcraze.crazyflie.lib.crtp.log;

import java.nio.ByteBuffer;

import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;

public class LogData {

    private byte mBlockId;
    private int mTimestamp;
    private byte[] mLogVariables;


    public LogData(byte blockId, int timestamp, byte[] logVariables) {
        this.mBlockId = blockId;
        this.mTimestamp = timestamp;
        this.mLogVariables = logVariables;
    }

    public static LogData parse(byte[] payload){
        byte[] logVariables = new byte[27];
        System.arraycopy(payload, 4, logVariables, 0, 27);
        return new LogData(payload[0], parseTimestamp(payload[1], payload[2], payload[3]), logVariables);
    }

    public byte getBlockId() {
        return this.mBlockId;
    }

    public int getTimestamp() {
        return this.mTimestamp;
    }

    public byte[] getLogVariables() {
        return this.mLogVariables;
    }

//    timestamps = struct.unpack("<BBB", packet.data[1:4])
//    timestamp = (timestamps[0] | timestamps[1] << 8 | timestamps[2] << 16)
    private static int parseTimestamp(byte data1, byte data2, byte data3) {
        //allocate 4 bytes for an int
        ByteBuffer buffer = ByteBuffer.allocate(4).order(CrtpPacket.BYTE_ORDER);
        buffer.put(data1);
        buffer.put(data2);
        buffer.put(data3);
        buffer.rewind();
        return buffer.getInt();
    }

    public String toString(){
        return "LogData: BlockId: " + this.getBlockId() + " Timestamp: " + this.getTimestamp();
    }

}
