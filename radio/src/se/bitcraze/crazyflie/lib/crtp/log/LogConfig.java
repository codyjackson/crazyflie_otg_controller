package se.bitcraze.crazyflie.lib.crtp.log;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.crtp.VariableType;

/**
 * Represents one log configuration that contains different
 * {@link LogVariable}s.
 *
 * TODO: deal with maximum number of variables per log config
 *
 */
public class LogConfig {

    final Logger mLogger = LoggerFactory.getLogger("LogConfig");

    private int mId;
    private int mPeriodInMs;
    private List<LogVariable> logVariables = new ArrayList<LogVariable>();
    private boolean mAdded = false;
    private boolean mStarted = false;

    public LogConfig(int id, int periodInMs) {
        this.mId = id;
        this.mPeriodInMs = periodInMs;
    }

    /**
     * Add a log variable to the log configuration
     *
     * @param fullVariableName
     */
    public void addLogVariable(String fullVariableName, VariableType type, int logVariableId){
        logVariables.add(new LogVariable(fullVariableName, type, logVariableId));
    }

    /**
     * Returns log variable of the log configuration
     *
     * @return list of log variables
     */
    public List<LogVariable> getLogVariables(){
        return this.logVariables;
    }

    /**
     * Returns the ID of the log configuration
     *
     * @return the ID of the log configuration
     */
    public int getId() {
        return mId;
    }

    /**
     * Returns the period in milliseconds
     *
     * @return the period in milliseconds
     */
    public int getPeriodInMs() {
        return mPeriodInMs;
    }

    /**
     * Sets the period in milliseconds
     *
     * @param periodInMs
     */
    public void setPeriodInMs(int periodInMs) {
        this.mPeriodInMs = periodInMs;
    }

    public int getPeriod() {
        return getPeriodInMs() / 10;
    }

    public boolean isAdded(){
        return this.mAdded;
    }

    public void setAdded(boolean added){
        this.mAdded = added;
    }

    public boolean isStarted() {
        return mStarted;
    }

    public void setStarted(boolean mStarted) {
        this.mStarted = mStarted;
    }

}
