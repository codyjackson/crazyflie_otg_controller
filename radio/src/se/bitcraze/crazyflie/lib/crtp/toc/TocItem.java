
package se.bitcraze.crazyflie.lib.crtp.toc;


import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import se.bitcraze.crazyflie.lib.crtp.Port;
import se.bitcraze.crazyflie.lib.crtp.VariableType;
import se.bitcraze.crazyflie.lib.crtp.log.Log;
import se.bitcraze.crazyflie.lib.crtp.para.Param;

/**
 * An item of the TOC.
 */
public class TocItem implements Serializable {

    private static final long serialVersionUID = 2L;

    private static final byte TOC_ITEM_TYPE_MASK = 0x1F;
    private static final byte TOC_ITEM_RO_FLAG = (1<<6); // flag for read only

    private final byte mId;
    private final VariableType mType;
    private final String mGroupName;
    private final String mVariableName;
    private final boolean mReadOnly;

    public TocItem(byte id, VariableType type, String groupName, String variableName, boolean readOnly) {
        this.mId = id;
        this.mType = type;
        this.mGroupName = groupName;
        this.mVariableName = variableName;
        this.mReadOnly = readOnly;
    }

    /**
     * Parse the item from raw data.
     *
     * @param port the port where the item was received
     * @param data the raw data containing the item.
     * @return the parsed item
     */
    public static TocItem parse(Port port, byte[] data) {
        if (data[0] != TocItemRequest.COMMAND_IDENTIFIER) {
            throw new IllegalArgumentException("data doesn't contain a reply for a TOC item request");
        }

        int offset = 3;
        int byteCount;
        // search end of first null terminated string
        for (byteCount = 0; byteCount + offset < data.length && data[byteCount + offset] != 0; byteCount++) {
        }
        final String groupName = new String(data, offset, byteCount, Charset.forName("US-ASCII"));
        // offset of second null terminated string is last character of first + 1
        offset = offset + byteCount + 1;
        // search end of second null terminated string
        for (byteCount = 0; byteCount + offset < data.length && data[byteCount + offset] != 0; byteCount++) {
        }
        final String variableName = new String(data, offset, byteCount, Charset.forName("US-ASCII"));

        final boolean readOnly = ((data[2] & TOC_ITEM_RO_FLAG) != 0) || port == Port.LOG; //Logging TOC items are always read-only

        Map<Integer, VariableType> typeMap = new HashMap<Integer, VariableType>();
        //choose variable type map depending on port
        if(port == Port.LOG){
            typeMap = Log.VARIABLE_TYPE_MAP;
        }else if(port == Port.PARAMETERS){
            typeMap = Param.VARIABLE_TYPE_MAP;
        }else{
            //FIXME: handle this case
        }

        return new TocItem(data[1], typeMap.get(data[2] & TOC_ITEM_TYPE_MASK), groupName, variableName, readOnly);
    }

    public boolean isReadOnly() {
        return mReadOnly;
    }

    public byte getId() {
        return mId;
    }

    public VariableType getType() {
        return mType;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public String getVariableName() {
        return mVariableName;
    }

    public String getFullVariableName(){
        return getGroupName()+"."+getVariableName();
    }

    //TODO: add port?
    public String toString(){
        return "TocItem: " + this.getGroupName() + "." + this.getVariableName() +
                " (" + this.getId() + "," + this.getType() + (this.isReadOnly() ? ",ro" : ",rw") + ")";
    }
}
