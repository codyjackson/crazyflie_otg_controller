package se.bitcraze.crazyflie.lib.crtp;

import java.nio.ByteBuffer;

/**
 * Data type of variables used when exchanging data with the Crazyflie.
 */
public enum VariableType {
    UINT8_T,
    UINT16_T,
    UINT32_T,
    INT8_T,
    INT16_T,
    INT32_T,
    FLOAT32,
    FLOAT16;

    /**
     * Parse one variable of the given type.
     *
     * @param buffer the buffer to read raw data from
     * @return the parsed variable
     */
    public Number parse(ByteBuffer buffer) {
        ByteBuffer tempBuffer = ByteBuffer.allocate(4).order(CrtpPacket.BYTE_ORDER);
        tempBuffer.put(buffer.get());
        tempBuffer.put(buffer.get());
        tempBuffer.rewind();
        switch (this) {
            case FLOAT16:
                return parseHalfPrecisionFloat(tempBuffer);
            case FLOAT32:
                return tempBuffer.getFloat();
            case INT16_T:
                return tempBuffer.getShort();
            case INT32_T:
                return tempBuffer.getInt();
            case INT8_T:
                return tempBuffer.get();
            case UINT16_T:
                return ((int) tempBuffer.getShort()) & 0xffff;
            case UINT32_T:
                return ((long) tempBuffer.getInt()) & 0xffffffffL;
            case UINT8_T:
                return ((short) tempBuffer.get()) & 0xff;
        }
        throw new RuntimeException("parsing of " + this.name() + " not implemented");
    }

    /**
     * Read a half precision float from a buffer. As there is no
     * primitive type in Java for storing this, the result is stored
     * in a 'float' (single precision) without any loss of accuracy.
     * @param buffer the buffer to read raw data from
     * @return the parsed number
     */
    private static Float parseHalfPrecisionFloat(ByteBuffer buffer) {
        final short halfRawData = buffer.getShort();

        final boolean positive = (halfRawData & (1<<15)) == 0; // sign bit
        final int rawExponent = ((halfRawData >> 10) & 0x1f); // exponent as stored in the raw data
        final int exponent = rawExponent - 15; // exponent corrected with bias
        final int fraction = halfRawData & 0x3ff;

        if(rawExponent == 0x1f) { // max possible exponent -> special values
            if(fraction == 0) {
                return (positive ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY);
            } else {
                return Float.NaN;
            }
        } else { // build single precision float
            int singlePrecisionRawExponent;
            int singlePrecisionFraction;
            if(rawExponent == 0) { // denormal number
                // convert to normalized
                int e = -1; // The following loop figures out how much extra to adjust the exponent
                singlePrecisionFraction = fraction;
                do {
                    e++;
                    singlePrecisionFraction <<= 1;
                } while( (singlePrecisionFraction & 0x400) == 0 ); //shift until leading bit overflows into exponent bit
                singlePrecisionRawExponent = - 15 + 127 - e;
                singlePrecisionFraction = (singlePrecisionFraction & 0x3ff) << 13;
            } else {
                singlePrecisionRawExponent = exponent + 127;
                singlePrecisionFraction = fraction << 13; // extend fraction to larger format
            }

            final int singleRawData =
                    (positive ? 0 : 1 << 31) // sign bit
                    | (singlePrecisionRawExponent << 23) // shift exponent to correct position
                    | singlePrecisionFraction;

            return Float.intBitsToFloat(singleRawData);
        }
    }
}
