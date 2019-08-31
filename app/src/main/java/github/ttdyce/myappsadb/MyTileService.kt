package github.ttdyce.myappsadb

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import java.io.DataOutputStream
import java.io.IOException


class MyTileService : TileService() {
    val KEY_PORT_OPENED = "portOpened";
    val TAG = "myTileService"
    var portOpened = false;
    val STOPCMD = arrayOf("stop adbd")
    val STARTCMD = arrayOf("setprop service.adb.tcp.port 5555", "stop adbd", "start adbd")


    override fun onClick() {
        super.onClick()
        Log.d(TAG, "onClick")

        // Called when the user click the tile
        if (portOpened == false) {
            portOpened = true
            Log.d(TAG, "port opened")
            runCommand(STARTCMD)
        } else {
            portOpened = false
            Log.d(TAG, "port closed")
            runCommand(STOPCMD)
        }
        getPrefs(baseContext).edit().putBoolean(KEY_PORT_OPENED, portOpened).apply()

    }

    override fun onTileRemoved() {
        super.onTileRemoved()

        Log.d(TAG, "removed")
    }

    override fun onStartListening() {
        super.onStartListening()

        portOpened = getPrefs(baseContext).getBoolean(KEY_PORT_OPENED, portOpened)
        // Called when the Tile becomes visible
        if(portOpened)
            qsTile.state = Tile.STATE_ACTIVE
        else
            qsTile.state = Tile.STATE_INACTIVE

        qsTile.updateTile()

    }

    fun runCommand(cmds : Array<String>) {

        try {
            val su = Runtime.getRuntime().exec("su")
            val outputStream = DataOutputStream(su.outputStream)

            for (str:String in cmds){
                //outputStream.writeBytes("setprop service.adb.tcp.port 5555\nstop adbd\nstart adbd\n")
                outputStream.writeBytes(str + "\n")
                outputStream.flush()
            }

            outputStream.writeBytes("exit\n")
            outputStream.flush()
            su.waitFor()
        } catch (e: IOException) {
            throw Exception(e)
        } catch (e: InterruptedException) {
            throw Exception(e)
        }
        finally{
            if(portOpened){
                qsTile.state = Tile.STATE_ACTIVE
                Log.d(TAG, "qsTile.state: " + qsTile.state)
            }else{
                qsTile.state = Tile.STATE_INACTIVE
            }

            qsTile.updateTile()
        }

    }

    fun getPrefs(c: Context) : SharedPreferences{
        return PreferenceManager.getDefaultSharedPreferences(c)
    }
}