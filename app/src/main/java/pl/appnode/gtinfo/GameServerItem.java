package pl.appnode.gtinfo;

/**
 * Defines structure of every game server object
 */

public class GameServerItem {

    public String mId; // game server IP address and port
    public String mName; // user given game server short name
    public boolean mAlive = true; // indicator of game server item state, true if normal, false when mark as deleted
}
