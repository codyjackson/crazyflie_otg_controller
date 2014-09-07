package se.bitcraze.crazyflie.lib.crtp.para;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.DataAdapter;
import se.bitcraze.crazyflie.lib.DataListener;
import se.bitcraze.crazyflie.lib.Link;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.Port;
import se.bitcraze.crazyflie.lib.crtp.VariableType;
import se.bitcraze.crazyflie.lib.crtp.toc.TableOfContents;
import se.bitcraze.crazyflie.lib.crtp.toc.TocItem;

/**
 * Utility for working with the parameters port.
 */
public class Param {

    final Logger mLogger = LoggerFactory.getLogger("Param");

    public static final Map<Integer, VariableType> VARIABLE_TYPE_MAP;

    // TODO: add missing types
    static {
        VARIABLE_TYPE_MAP = new HashMap<Integer, VariableType>(8);
        VARIABLE_TYPE_MAP.put(8, VariableType.UINT8_T);
        VARIABLE_TYPE_MAP.put(9, VariableType.UINT16_T);
        VARIABLE_TYPE_MAP.put(10, VariableType.UINT32_T);
        VARIABLE_TYPE_MAP.put(0, VariableType.INT8_T);
        VARIABLE_TYPE_MAP.put(1, VariableType.INT16_T);
        VARIABLE_TYPE_MAP.put(2, VariableType.INT32_T);
        VARIABLE_TYPE_MAP.put(6, VariableType.FLOAT32);
        VARIABLE_TYPE_MAP.put(5, VariableType.FLOAT16);
    }

    private final int PARAM_READ_CHANNEL = 1;
    private final int PARAM_WRITE_CHANNEL = 2;

    private Link mLink;
    private TableOfContents mToc;

    private Map<String, Number> mParameterDataMap = new ConcurrentHashMap<String, Number>();

    public Param(Link l, TableOfContents toc) {
        this.mLink = l;
        this.mToc = toc;

        if(toc.getPort() != Port.PARAMETERS){
            throw new IllegalArgumentException("Logging TOC must use logging port.");
        }

        this.mLink.removeDataListener(this.mDataListener); // remove first to avoid multiple registrations
        this.mLink.addDataListener(this.mDataListener);
    }

    public Map<String, Number> getParameterDataMap(){
        return this.mParameterDataMap;
    }

    public void readValue(String fullVariableName) {
        final TocItem item = mToc.getItem(fullVariableName);
        if(item == null){
            mLogger.error("TOC Item with full variable name '" + fullVariableName + "' not found.");
            return;
        }
        mLogger.debug("Requesting update for parameter '" + fullVariableName + "'");
        mLink.send(new ParamReadRequest(item.getId()));
    }

    public void setValue(String fullVariableName, final int value){
        final TocItem item = mToc.getItem(fullVariableName);
        if(item == null){
            mLogger.error("TOC Item with full variable name '" + fullVariableName + "' not found.");
            return;
        }else if(item.isReadOnly()){
            mLogger.info("Cannot set value. TOC Item with full variable name '" + fullVariableName + "' is read-only.");
            return;
        }
        mLogger.debug("Setting parameter '" + fullVariableName + "', param ID: " + item.getId() + ", type: " + item.getType() + ", value: " + value);
        mLink.send(new ParamWriteRequest(item.getId(), item.getType().ordinal(), value));
    }

    //Parameter data
    private void parseParameterData(byte[] payload) {
        byte id = payload[0];
        ByteBuffer parameterVariables = ByteBuffer.wrap(payload, 1, payload.length - 1).order(ByteOrder.LITTLE_ENDIAN);
        TocItem tocItem = mToc.getItemById(id);
        if(tocItem != null){
            Number parsedValue = tocItem.getType().parse(parameterVariables);
            mParameterDataMap.put(tocItem.getFullVariableName(), tocItem.getType().parse(parameterVariables));
            mLogger.debug("Parsed parameter: ID: " + id + ", FVN: " + tocItem.getFullVariableName() + ", Value: " + parsedValue);
        }else{
            mLogger.debug("No TOC item found for ID: " + id);
        }
    }

    //DataListener for Response
    private final DataListener mDataListener = new DataAdapter() {

        @Override
        public void parameterDataReceived(CrtpPacket packet) {
            int channel = packet.getHeader().getChannel();
            mLogger.info("Parameter data received, channel: " + (channel == PARAM_READ_CHANNEL ? "read" : "write"));
            parseParameterData(packet.getPayload());
        }

    };

    private class ParamReadRequest extends CrtpPacket{

        private int mParamId;

        public ParamReadRequest(int paramId) {
            super(PARAM_READ_CHANNEL, Port.PARAMETERS);
            this.mParamId = paramId;
        }

        @Override
        protected void serializeData(ByteBuffer buffer) {
            buffer.put((byte) mParamId);
        }

        @Override
        protected int getDataByteCount() {
            return 1;
        }

    };

    private class ParamWriteRequest extends CrtpPacket{

        private int mParamId;
        private int mTypeId;
        private Number mValue;  //TODO: is Number the right class to use here?

        public ParamWriteRequest(int paramId, int typeId, Number value) {
            super(PARAM_WRITE_CHANNEL, Port.PARAMETERS);
            this.mParamId = paramId;
            this.mTypeId = typeId;
            this.mValue = value;
        }

        @Override
        protected void serializeData(ByteBuffer buffer) {
            buffer.put((byte) mParamId);
            //TODO: Parse value according to type?
            buffer.put(mValue.byteValue());
        }

        @Override
        protected int getDataByteCount() {
            return 2;
        }

    };

}
