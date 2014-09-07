
package se.bitcraze.crazyflie.lib.crtp.toc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.bitcraze.crazyflie.lib.DataAdapter;
import se.bitcraze.crazyflie.lib.DataListener;
import se.bitcraze.crazyflie.lib.Link;
import se.bitcraze.crazyflie.lib.crtp.Port;

/**
 * Table of contents of variables available on the Crazyflie.
 */
public class TableOfContents {

    final Logger mLogger = LoggerFactory.getLogger("TableOfContents");

    private Map<String, Map<String, TocItem>> mItems;
    private int mChecksum;

    private Port mPort;

    private int mNumberOfVariables;

    //only used by logging TOC
    private int mMaxNoOfLogConfigs;
    private int mMaxNoOfLogVariables;

    protected boolean allTocItemRequestSent = false;

    /**
     * Create a new (empty) table of contents.
     */
    public TableOfContents(Port port) {
        this.mItems = new HashMap<String, Map<String, TocItem>>();
        this.mChecksum = -1;
        this.mPort = port;
    }

    /**
     * Create a new table of contents and retrieve information from the Crazyflie.
     *
     * @param l the link to use for loading the TOC.
     */
    public TableOfContents(Link l, Port port) {
        this(port);
        verify(l);
    }

    public boolean isTocFetchFinished(Link link){
        if(allTocItemRequestSent){
            if(mNumberOfVariables > 0 && getFullVariableNames().length == mNumberOfVariables){
                mLogger.info("TOC (" + mPort + ") fetching done.");
                return true;
            }else{
                mLogger.info("Need to verify TOC (" + mPort + ").");
                //verify TOC
                for(int i = 0; i < mNumberOfVariables;i++){
                    if(getItemById(i) == null){
                        mLogger.info("Resending TocItemRequest for ID: " + i);
                        link.send(new TocItemRequest(mPort, (byte) i));
                    }
                }
            }
        }
        return false;
    }

    public Port getPort(){
        return this.mPort;
    }

    /**
     * Verify validity of all TOCs against the Crazyflie and and update them if
     * necessary.
     *
     * @param l the link to use for verification.
     * @param forceUpdate if <code>true</code> the TOCs will be updated
     *            regardless of the validity of cached checksum
     */
    public void verify(Link l, boolean forceUpdate) {
        // register as listener in order to receive the reply and request TOC info on the link
        l.removeDataListener(this.mDataListener); // remove first to avoid multiple registrations
        l.addDataListener(this.mDataListener);

        // delete checksum to force update
        if (forceUpdate) {
            mChecksum = -1;
        }
        l.send(new TocInfoRequest(mPort));
    }

    /**
     * Verify validity of all TOCs against the Crazyflie and and update them if necessary.
     *
     * @param l the link to use for verification.
     */
    public void verify(Link l) {
        verify(l, false);
    }

    /**
     * Get the checksum of the local TOC.
     *
     * @return the checksum of the TOC or <code>null</code> if no checksum is stored
     */
    public int getChecksum() {
        return mChecksum;
    }

    /**
     * Load previously saved TOC from a file.
     *
     * @param filename the name of the file containing the TOC
     * @return the loaded TOC
     * @throws IOException if an I/O error occurs when loading the TOC
     */
    @SuppressWarnings("unchecked")
    public static TableOfContents load(String filename) throws IOException {
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new FileInputStream(filename));
            final TableOfContents toc = new TableOfContents((Port) in.readObject());
            toc.mChecksum =  (Integer) in.readObject();
            toc.mItems = (Map<String, Map<String, TocItem>>) in.readObject();
            return toc;
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Save the current TOC to a file.
     *
     * @param filename the name of the file
     * @throws IOException if an I/O error occurs when saving the TOC
     */
    public void save(String filename) throws IOException {
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(filename));
            out.writeObject(mPort);
            out.writeObject(mChecksum);
            out.writeObject(mItems);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Get the names of the groups of all available items.
     *
     * @return available group names
     */
    public String[] getGroupNames() {
        if(mItems != null) {
            return mItems.keySet().toArray(new String[0]);
        } else {
            return new String[0]; // no groups for this port
        }
    }

    /**
     * Get the names of all variables in a specific group.
     *
     * @param groupName name of the group containing the desired variables
     * @return available variable names in the specified group
     */
    public String[] getVariableNames(String groupName) {
        if(mItems != null) {
            final Map<String, TocItem> variableNameMap = mItems.get(groupName);
            if(variableNameMap != null) {
                return variableNameMap.keySet().toArray(new String[0]);
            } else {
                return new String[0]; // no variables in this group
            }
        } else {
            return new String[0]; // no groups for this port
        }
    }

    /**
     * Get the full names of all available items.
     * The full name has the format 'group.variable'.
     *
     * @return full names of all available variables
     */
    public String[] getFullVariableNames() {
        final List<String> nameList = new ArrayList<String>();

        if(mItems != null) {
            for(Entry<String, Map<String, TocItem>> groupEntry : mItems.entrySet()) {
                for(String variableName : groupEntry.getValue().keySet()) {
                    nameList.add(groupEntry.getKey() + "." + variableName);
                }
            }
        }

        String[] fvnArray = nameList.toArray(new String[nameList.size()]);
        Arrays.sort(fvnArray);
        return fvnArray;
    }

    public Map<String, Map<String, TocItem>> getItems(){
        return this.mItems;
    }

    /**
     * Get an item from the TOC.
     *
     * @param fullVariableName the full name of the variable
     * @return the item or <code>null</code> if no item with the
     *      specified name exists
     */
    public TocItem getItem(String fullVariableName) {
        final String[] parts = fullVariableName.split("\\.", 2);
        if(parts.length != 2) {
            throw new IllegalArgumentException("invalid name, valid names have the format 'group.variable'");
        } else {
            return getItem(parts[0], parts[1]);
        }
    }

    /**
     * Get an item from the TOC.
     *
     * @param groupName the group name of the item
     * @param variableName the variable name of the item
     * @return the item or <code>null</code> if no item with the specified name exists
     */
    public TocItem getItem(String groupName, String variableName) {
        if(mItems != null || mItems.isEmpty()) {
            final Map<String, TocItem> variableNameMap = mItems.get(groupName);
            if(variableNameMap != null) {
                return variableNameMap.get(variableName);
            }
        }
        mLogger.debug("getItem...mItems is either null or empty");
        return null; // default if the item was not found
    }

    /**
     * Get an item from the TOC by it's ID
     *
     * @param id the ID of the item
     * @return the item or <code>null</code> if no item with the specified name exists
     */
    public TocItem getItemById(int id){
        if(mItems != null || mItems.isEmpty()) {
            for(String group : mItems.keySet()){
                for(String name : mItems.get(group).keySet()){
                    if(mItems.get(group).get(name).getId() == (byte) id){
                        return mItems.get(group).get(name);
                    }
                }
            }
            mLogger.debug("TocItem with ID " + id + " not found.");
            return null;
        }else{
            mLogger.debug("getItemById...mItems is either null or empty");
            return null; // default if the item was not found
        }
    }

    public int getNoOfVariables() {
        return mNumberOfVariables;
    }

    public int getMaxNoOfLogConfigs() {
        return mMaxNoOfLogConfigs;
    }

    public int getMaxNoOfLogVariables() {
        return mMaxNoOfLogVariables;
    }

    /**
     * Data listener for receiving TOC info and items. Items are stored and
     * remaining items are requested via the link if possible.
     */
    private final DataListener mDataListener = new DataAdapter() {

        @Override
        public void tocItemReceived(Link source, Port port, TocItem item) {
            mLogger.debug("received " + item + " (" + port + ")");
            if(port == mPort) {
                if (mItems == null) { // first item in this port, create new map
                    mItems = new HashMap<String, Map<String, TocItem>>();
                }

                // search map for the group
                Map<String, TocItem> variableNameMap = mItems.get(item.getGroupName());
                if (variableNameMap == null) { // first item in this group, create new map for the group
                    variableNameMap = new HashMap<String, TocItem>();
                    mItems.put(item.getGroupName(), variableNameMap);
                }

                // store item
                variableNameMap.put(item.getVariableName(), item);
            }
        }

        @Override
        public void tocInfoReceived(Link source, Port port, TocInfoReply info) {
            mLogger.debug("received " + info + " (" + port + ")");
            if (port == mPort) {
                mLogger.debug("TOC (" + port + ") contains " + info.getNumberOfVariables() + " items");
                // if checksums don't match request: clear the local TOC and request the first item
                if (getChecksum() == -1 || getChecksum() != info.getChecksum()) {
                    mLogger.info("TOC checksum doesn't match; local " + getChecksum() + ", remote " + info.getChecksum());
                    if (mItems != null) {
                        mItems.clear();
                    }
                    mChecksum = info.getChecksum();
                    mNumberOfVariables = info.getNumberOfVariables();
                    allTocItemRequestSent  = false;
                    for (byte i = 0; i < info.getNumberOfVariables(); i++) {
                        source.send(new TocItemRequest(port, i));
                    }
                    allTocItemRequestSent = true;
                    if(port == Port.LOG){
                        mMaxNoOfLogConfigs = info.getMaxNoOfLogConfigs();
                        mMaxNoOfLogVariables = info.getMaxNoOfLogVariables();
                    }
                } else {
                    mLogger.info("TOC checksum matches.");
                }
            }
        }

    };

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mChecksum;
        result = prime * result + ((mItems == null) ? 0 : mItems.hashCode());
        result = prime * result + ((mPort == null) ? 0 : mPort.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TableOfContents)) {
            return false;
        }
        TableOfContents other = (TableOfContents) obj;
        if (mChecksum != other.mChecksum) {
            return false;
        }
        if (mItems == null) {
            if (other.mItems != null) {
                return false;
            }
        } else if (!mItems.equals(other.mItems)) {
            return false;
        }
        if (mPort != other.mPort) {
            return false;
        }
        return true;
    }


}
