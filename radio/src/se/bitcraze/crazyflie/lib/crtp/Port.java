package se.bitcraze.crazyflie.lib.crtp;

/**
 * Available ports for communicating with subsystems.
 */
public enum Port {
    CONSOLE(0),
    PARAMETERS(2),
    COMMANDER(3),
    LOG(5),
    DEBUG(14),
    LINK_LAYER(15),
    UNKNOWN(-1); //FIXME

    private final byte mNumber;

    private Port(int number) {
        if (number > 15) {
            throw new IllegalArgumentException("port number too large");
        }
        this.mNumber = (byte) number;
    }

    /**
     * Get the number associated with this port.
     * 
     * @return the number of the port
     */
    public byte getNumber() {
        return mNumber;
    }

    /**
     * Get the port with a specific number.
     * 
     * @param number
     *            the number of the port.
     * @return the port or <code>null</code> if no port with the specified number exists.
     */
    public static Port getByNumber(byte number) {
        for (Port p : Port.values()) {
            if (p.getNumber() == number) {
                return p;
            }
        }
        return Port.UNKNOWN;
    }
}
