package se.bitcraze.crazyflie.lib.crtp.log;

import se.bitcraze.crazyflie.lib.crtp.VariableType;

/**
 * A LogVariable is an element of a LogConfig.
 *
 * Note: for now only the so called "TOC_TYPE" is supported, which refers to
 * variables that are predefined in the Crazyflie's TOC. Variables pointing
 * to memory addresses (MEM_TYPE) will be added at a later point.
 *
 */
public class LogVariable {

    private String mFullVariableName;
    private VariableType mVariableType;
    private int mId;

    public LogVariable(String fullVariableName, VariableType type, int id) {
        this.mFullVariableName = fullVariableName;
        this.mVariableType = type;
        this.mId = id;
    }

    public String getFullVariableName() {
        return mFullVariableName;
    }

    public VariableType getVariableType() {
        return mVariableType;
    }

    public int getVariableId() {
        return mId;
    }

    public String toString(){
        return "log variable: FullVariableName: " + this.mFullVariableName + ", VariableType: " + this.mVariableType + ", ID: " + this.mId;
    }
}
