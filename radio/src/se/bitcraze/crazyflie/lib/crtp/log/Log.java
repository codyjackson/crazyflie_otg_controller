package se.bitcraze.crazyflie.lib.crtp.log;

import java.nio.ByteBuffer;
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
 * Logging
 */
public class Log {

    final Logger mLogger = LoggerFactory.getLogger("Log");

    public static final Map<Integer, VariableType> VARIABLE_TYPE_MAP;

    static {
        VARIABLE_TYPE_MAP = new HashMap<Integer, VariableType>(8);
        VARIABLE_TYPE_MAP.put(1, VariableType.UINT8_T);
        VARIABLE_TYPE_MAP.put(2, VariableType.UINT16_T);
        VARIABLE_TYPE_MAP.put(3, VariableType.UINT32_T);
        VARIABLE_TYPE_MAP.put(4, VariableType.INT8_T);
        VARIABLE_TYPE_MAP.put(5, VariableType.INT16_T);
        VARIABLE_TYPE_MAP.put(6, VariableType.INT32_T);
        VARIABLE_TYPE_MAP.put(7, VariableType.FLOAT32);
        VARIABLE_TYPE_MAP.put(8, VariableType.FLOAT16);
    }

    private Link mLink;
    private TableOfContents mToc;

    private Map<Integer, LogConfig> mLogConfigs = new HashMap<Integer, LogConfig>();

    public Log(Link l, TableOfContents toc) {
        this.mLink = l;
        this.mToc = toc;

        if(toc.getPort() != Port.LOG){
            throw new IllegalArgumentException("Logging TOC must use logging port.");
        }

        this.mLink.removeDataListener(this.mDataListener); // remove first to avoid multiple registrations
        this.mLink.addDataListener(this.mDataListener);
    }

    public Map<Integer, LogConfig> getLogConfigs(){
        return this.mLogConfigs;
    }

    /**
     * Add log configuration with default period of 100 ms
     *
     * @param logConfigName
     * @param fullVariableNames
     * @return log config ID
     */
    public int addLogConfiguration(String... fullVariableNames){
        return addLogConfiguration(100, fullVariableNames);
    }

    /**
     * Add a log configuration to the Crazyflie
     *
     * TODO: add append mode
     *
     * @param logConfigName
     * @param periodInMs
     * @param fullVariableNames
     * @return log config ID
     */
    public int addLogConfiguration(int periodInMs, String... fullVariableNames){
        int logConfigId = this.mLogConfigs.size();
        LogConfig logConfig = createLogConfig(logConfigId, periodInMs, fullVariableNames);
        if(mLink != null){
            if(!logConfig.isAdded()){
                mLogger.debug("Adding log config ID: " + logConfig.getId());
                ByteBuffer bb = ByteBuffer.allocate(31);
                //Add command
                bb.put(LogConfigRequest.CREATE_BLOCK);
                bb.put((byte) logConfig.getId());

                //Add log variables
                for(LogVariable lv : logConfig.getLogVariables()){
                    mLogger.debug("Adding " + lv);
                    bb.put(new byte[]{(byte) lv.getVariableType().ordinal(), (byte) lv.getVariableId()});
                }
                mLink.send(new LogConfigRequest(bb.array()));
            }else{
                mLogger.debug("Not adding log config ID: " + logConfig.getId() + ", because it is already added.");
            }
        }
        mLogConfigs.put(logConfig.getId(), logConfig);
        return logConfig.getId();
    }

    private LogConfig createLogConfig(int logConfigId, int periodInMs, String... fullVariableNames){
        LogConfig logConfig = new LogConfig(logConfigId, periodInMs);
        for(String fvn : fullVariableNames){
            TocItem item = mToc.getItem(fvn);
            if(item == null){
                mLogger.warn("No TOC item found for FVN: " + fvn);
            }else{
                logConfig.addLogVariable(fvn, item.getType(), item.getId());
            }
        }
        return logConfig;
    }

    /**
     * Delete a log configuration from the Crazyflie
     *
     * @param blockId
     */
    public void deleteLogConfiguration(int blockId){
        if(mLink != null){
            LogConfig logConfig = this.mLogConfigs.get(blockId);
            if(logConfig == null){
                mLogger.warn("Log config ID: " + blockId + " not found.");
                return;
            }
            if(logConfig.isAdded()){
                mLogger.debug("Deleting log config ID: " + logConfig.getId());
                mLink.send(new LogConfigRequest(LogConfigRequest.DELETE_BLOCK, (byte) logConfig.getId()));
            }else{
                mLogger.debug("Not deleting log config ID: " + logConfig.getId() + ", because it has not been added.");
            }
        }
        this.mLogConfigs.remove(blockId);
    }

    /**
     * Start logging a log configuration
     *
     * @param blockId
     */
    public void startLogConfiguration(int blockId){
        if(mLink != null){
            LogConfig logConfig = this.mLogConfigs.get(blockId);
            if(logConfig == null){
                mLogger.warn("Log config ID: " + blockId + " not found.");
                return;
            }
            mLogger.debug("Starting log config ID: " + logConfig.getId());
            mLink.send(new LogConfigRequest(LogConfigRequest.START_BLOCK, (byte) logConfig.getId(), (byte) logConfig.getPeriod()));
        }
    }

    /**
     * Stop logging a log configuration
     *
     * @param blockId
     */
    public void stopLogConfiguration(int blockId){
        if(mLink != null){
            LogConfig logConfig = this.mLogConfigs.get(blockId);
            if(logConfig == null){
                mLogger.warn("Log config ID: " + blockId + " not found.");
                return;
            }
            if(logConfig.isAdded() && logConfig.isStarted()){
                mLogger.debug("Stopping log config ID: " + logConfig.getId());
                mLink.send(new LogConfigRequest(LogConfigRequest.STOP_BLOCK, (byte) logConfig.getId()));
            }else{
                mLogger.debug("Not stopping log config ID: " + logConfig.getId() + ", because it is not added or started yet.");
            }
        }
    }

    /**
     * Reset all log configurations
     */
    public void resetLogConfigurations(){
        mLink.send(new LogConfigRequest(LogConfigRequest.RESET));
        this.mLogConfigs.clear();
    }

    /**
     * Parse log variables
     *
     * @param logData
     * @return
     */
    public Map<String, Number> parseLogVariables(LogData logData) {
        ByteBuffer logVariables = ByteBuffer.wrap(logData.getLogVariables()).order(CrtpPacket.BYTE_ORDER);
        Map<String, Number> logDataMap = new ConcurrentHashMap<String, Number>();
        LogConfig logConfig = getLogConfigs().get((int) logData.getBlockId());
        if(logConfig != null){
            for(LogVariable lv : logConfig.getLogVariables()){
                logDataMap.put(lv.getFullVariableName(), lv.getVariableType().parse(logVariables));
            }
        }
        return logDataMap;
    }

    //DataListener for Response
    private final DataListener mDataListener = new DataAdapter() {

        @Override
        public void logConfigReplyReceived(LogConfigReply packet) {
            mLogger.info("LogConfigReply received: " + packet);
            //TODO: deal with errors
            LogConfig logConfig = mLogConfigs.get((int) packet.getBlockId());
            if(logConfig != null) {
                switch (packet.getCommand()) {
                case LogConfigRequest.CREATE_BLOCK:
                    logConfig.setAdded(true);
                    break;
                case LogConfigRequest.DELETE_BLOCK:
                    logConfig.setAdded(false);
                    break;
                case LogConfigRequest.START_BLOCK:
                    logConfig.setStarted(true);
                    break;
                case LogConfigRequest.STOP_BLOCK:
                    logConfig.setStarted(false);
                    break;
                default:
                    break;
                }
            }
        }

        @Override
        public void logDataReceived(LogData packet) {
            mLogger.info("LogData received: " + packet);
        }

    };

}
