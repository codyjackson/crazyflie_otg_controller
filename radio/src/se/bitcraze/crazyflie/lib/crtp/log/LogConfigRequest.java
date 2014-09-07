package se.bitcraze.crazyflie.lib.crtp.log;

import java.nio.ByteBuffer;

import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.Port;

public class LogConfigRequest extends CrtpPacket {

    //TODO: convert to enum?
    public static final byte CREATE_BLOCK = (byte) 0x00;
    public static final byte APPEND_BLOCK = (byte) 0x01;
    public static final byte DELETE_BLOCK = (byte) 0x02;
    public static final byte START_BLOCK = (byte) 0x03;
    public static final byte STOP_BLOCK = (byte) 0x04;
    public static final byte RESET = (byte) 0x05;

    private byte[] byteArray;

    public LogConfigRequest(byte... command) {
        super(1, Port.LOG);
        this.byteArray = command;
    }

    @Override
    protected void serializeData(ByteBuffer buffer) {
        buffer.put(byteArray);
    }

    @Override
    protected int getDataByteCount() {
        return byteArray.length;
    }
}
