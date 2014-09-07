package se.bitcraze.crazyflie.lib.crtp.log;

public class LogConfigReply {

    private byte mCommand;
    private byte mBlockId;
    private byte mReturnCode;

    public LogConfigReply(byte command, byte blockId, byte returnCode){
        this.mCommand = command;
        this.mBlockId = blockId;
        this.mReturnCode = returnCode;
    }

    public static LogConfigReply parse(byte[] data){
        return new LogConfigReply(data[0], data[1], data[2]);
    }

    public int getCommand(){
        return (int) this.mCommand;
    }

    public String getCommandAsString(){
        switch (this.mCommand) {
        case LogConfigRequest.CREATE_BLOCK:
            return "Create";
        case LogConfigRequest.APPEND_BLOCK:
            return "Append";
        case LogConfigRequest.DELETE_BLOCK:
            return "Delete";
        case LogConfigRequest.START_BLOCK:
            return "Start";
        case LogConfigRequest.STOP_BLOCK:
            return "Stop";
        case LogConfigRequest.RESET:
            return "Reset";
        default:
            return "Unknown";
        }
    }

    public byte getBlockId(){
        return this.mBlockId;
    }

    public ReturnCode getReturnCode(){
        return ReturnCode.getByNumber(this.mReturnCode);
    }

    public String toString(){
        return "LogConfigReply: Command: " + getCommandAsString() + (getCommand() != LogConfigRequest.RESET ? " BlockId: " + getBlockId() : "") + " ReturnCode: " + getReturnCode();
    }


    /**
     * Available return codes
     */
    public enum ReturnCode {
        NO_ERROR(0, "No Error"),
        ENOENT(2, "Block or variable not found"),
        E2BIG(7, "Log block is too long"),
        ENOEXEC(8, "Unknown command received"),
        ENOMEM(12, "No memory to allocate log block or log item");


        private final byte mNumber;
        private String mDescription;

        private ReturnCode(int number, String desc) {
            this.mNumber = (byte) number;
            this.mDescription = desc;
        }

        /**
         * Get the number associated with this port.
         *
         * @return the number of the return code
         */
        public byte getNumber() {
            return mNumber;
        }

        /**
         * Get the description associated with this return code.
         *
         * @return the description of the return code
         */
        public String getDescription() {
            return mDescription;
        }

        /**
         * Get the return code with a specific number.
         *
         * @param number the number of the return code
         * @return the return code or <code>null</code> if no return code with the specified number exists.
         */
        public static ReturnCode getByNumber(byte number) {
            for (ReturnCode p : ReturnCode.values()) {
                if (p.getNumber() == number) {
                    return p;
                }
            }
            return null;
        }
    }
}
