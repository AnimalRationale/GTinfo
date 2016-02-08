package pl.appnode.gtinfo;

/**
 * Defines structure of items holding game servers information.
 */

class GameServerItem {

    static final int DATASET_VERSION = 1;

    /** Game server IP address and port, should be in address:port format */
    String mId;

    /** User given short name fo game server */
    String mName;

    /** Server rating */
    String mRating;
}
