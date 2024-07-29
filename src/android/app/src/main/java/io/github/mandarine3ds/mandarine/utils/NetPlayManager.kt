package org.citra.citra_emu.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.mandarine3ds.mandarine.NativeLibrary
import io.github.mandarine3ds.mandarine.R

object NetPlayManager {
    fun showCreateRoomDialog(activity: Activity) {
        val dialog = MaterialAlertDialogBuilder(activity)
            .setCancelable(true)
            .setView(R.layout.dialog_multiplayer_room)
            .show()

        val textTitle = dialog.findViewById<TextView>(R.id.text_title)
        textTitle?.setText(R.string.multiplayer_create_room)

        val ipAddressView = dialog.findViewById<EditText>(R.id.ip_address)
        ipAddressView?.setText(getIpAddressByWifi(activity))

        val ipPortView = dialog.findViewById<EditText>(R.id.ip_port)
        ipPortView?.setText(getRoomPort(activity))

        val usernameView = dialog.findViewById<EditText>(R.id.username)
        usernameView?.setText(getUsername(activity))

        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)
        btnConfirm?.setOnClickListener {
            val ipAddress = ipAddressView?.text.toString()
            val username = usernameView?.text.toString()
            val portStr = ipPortView?.text.toString()
            val port = try {
                portStr.toInt()
            } catch (e: Exception) {
                Toast.makeText(activity, R.string.multiplayer_port_invalid, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (ipAddress.length < 7 || username.length < 5) {
                Toast.makeText(activity, R.string.multiplayer_input_invalid, Toast.LENGTH_LONG).show()
            } else if (netPlayCreateRoom(ipAddress, port, username) == 0) {
                setUsername(activity, username)
                setRoomPort(activity, portStr)
                Toast.makeText(activity, R.string.multiplayer_create_room_success, Toast.LENGTH_LONG).show()
                dialog.dismiss()
            } else {
                Toast.makeText(activity, R.string.multiplayer_create_room_failed, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun showJoinRoomDialog(activity: Activity) {
        val dialog = AlertDialog.Builder(activity)
            .setCancelable(true)
            .setView(R.layout.dialog_multiplayer_room)
            .show()

        val textTitle = dialog.findViewById<TextView>(R.id.text_title)
        textTitle?.setText(R.string.multiplayer_join_room)

        val ipAddressView = dialog.findViewById<EditText>(R.id.ip_address)
        ipAddressView?.setText(getRoomAddress(activity))

        val ipPortView = dialog.findViewById<EditText>(R.id.ip_port)
        ipPortView?.setText(getRoomPort(activity))

        val usernameView = dialog.findViewById<EditText>(R.id.username)
        usernameView?.setText(getUsername(activity))

        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)
        btnConfirm?.setOnClickListener {
            val ipAddress = ipAddressView?.text.toString()
            val username = usernameView?.text.toString()
            val portStr = ipPortView?.text.toString()
            val port = try {
                portStr.toInt()
            } catch (e: Exception) {
                Toast.makeText(activity, R.string.multiplayer_port_invalid, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (ipAddress.length < 7 || username.length < 5) {
                Toast.makeText(activity, R.string.multiplayer_input_invalid, Toast.LENGTH_LONG).show()
            } else if (netPlayJoinRoom(ipAddress, port, username) == 0) {
                setRoomAddress(activity, ipAddress)
                setUsername(activity, username)
                setRoomPort(activity, portStr)
                Toast.makeText(activity, R.string.multiplayer_join_room_success, Toast.LENGTH_LONG).show()
                dialog.dismiss()
            } else {
                Toast.makeText(activity, R.string.multiplayer_join_room_failed, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun getUsername(activity: Activity): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val name = "Citra${(Math.random() * 100).toInt()}"
        return prefs.getString("NetPlayUsername", name) ?: name
    }

    private fun setUsername(activity: Activity, name: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        prefs.edit().putString("NetPlayUsername", name).apply()
    }

    private fun getRoomAddress(activity: Activity): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val address = getIpAddressByWifi(activity)
        return prefs.getString("NetPlayRoomAddress", address) ?: address
    }

    private fun setRoomAddress(activity: Activity, address: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        prefs.edit().putString("NetPlayRoomAddress", address).apply()
    }

    private fun getRoomPort(activity: Activity): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        return prefs.getString("NetPlayRoomPort", "24872") ?: "24872"
    }

    private fun setRoomPort(activity: Activity, port: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        prefs.edit().putString("NetPlayRoomPort", port).apply()
    }

    private external fun netPlayCreateRoom(ipAddress: String, port: Int, username: String): Int

    private external fun netPlayJoinRoom(ipAddress: String, port: Int, username: String): Int

    external fun netPlayRoomInfo(): Array<String>

    external fun netPlayIsJoined(): Boolean

    external fun netPlayIsHostedRoom(): Boolean

    external fun netPlaySendMessage(msg: String)

    external fun netPlayKickUser(username: String)

    external fun netPlayLeaveRoom()

    external fun netPlayGetConsoleId(): String

    fun addNetPlayMessage(type: Int, msg: String) {
        val activity1 = NativeLibrary.sEmulationActivity.get()
        val activity2 = NativeLibrary.sMainActivity.get()

        when {
            activity1 != null -> {
                activity1.runOnUiThread { activity1.addNetPlayMessage(formatNetPlayStatus(activity1, type, msg)) }
            }
            activity2 != null -> {
                activity2.runOnUiThread { activity2.addNetPlayMessage(formatNetPlayStatus(activity2, type, msg)) }
            }
        }
    }

    private fun formatNetPlayStatus(context: Context, type: Int, msg: String): String {
        return when (type) {
            NetPlayStatus.NETWORK_ERROR -> context.getString(R.string.multiplayer_network_error)
            NetPlayStatus.LOST_CONNECTION -> context.getString(R.string.multiplayer_lost_connection)
            NetPlayStatus.NAME_COLLISION -> context.getString(R.string.multiplayer_name_collision)
            NetPlayStatus.MAC_COLLISION -> context.getString(R.string.multiplayer_mac_collision)
            NetPlayStatus.CONSOLE_ID_COLLISION -> context.getString(R.string.multiplayer_console_id_collision)
            NetPlayStatus.WRONG_VERSION -> context.getString(R.string.multiplayer_wrong_version)
            NetPlayStatus.WRONG_PASSWORD -> context.getString(R.string.multiplayer_wrong_password)
            NetPlayStatus.COULD_NOT_CONNECT -> context.getString(R.string.multiplayer_could_not_connect)
            NetPlayStatus.ROOM_IS_FULL -> context.getString(R.string.multiplayer_room_is_full)
            NetPlayStatus.HOST_BANNED -> context.getString(R.string.multiplayer_host_banned)
            NetPlayStatus.PERMISSION_DENIED -> context.getString(R.string.multiplayer_permission_denied)
            NetPlayStatus.NO_SUCH_USER -> context.getString(R.string.multiplayer_no_such_user)
            NetPlayStatus.ALREADY_IN_ROOM -> context.getString(R.string.multiplayer_already_in_room)
            NetPlayStatus.CREATE_ROOM_ERROR -> context.getString(R.string.multiplayer_create_room_error)
            NetPlayStatus.HOST_KICKED -> context.getString(R.string.multiplayer_host_kicked)
            NetPlayStatus.UNKNOWN_ERROR -> context.getString(R.string.multiplayer_unknown_error)
            NetPlayStatus.ROOM_UNINITIALIZED -> context.getString(R.string.multiplayer_room_uninitialized)
            NetPlayStatus.ROOM_IDLE -> context.getString(R.string.multiplayer_room_idle)
            NetPlayStatus.ROOM_JOINING -> context.getString(R.string.multiplayer_room_joining)
            NetPlayStatus.ROOM_JOINED -> context.getString(R.string.multiplayer_room_joined)
            NetPlayStatus.ROOM_MODERATOR -> context.getString(R.string.multiplayer_room_moderator)
            NetPlayStatus.MEMBER_JOIN -> context.getString(R.string.multiplayer_member_join, msg)
            NetPlayStatus.MEMBER_LEAVE -> context.getString(R.string.multiplayer_member_leave, msg)
            NetPlayStatus.MEMBER_KICKED -> context.getString(R.string.multiplayer_member_kicked, msg)
            NetPlayStatus.MEMBER_BANNED -> context.getString(R.string.multiplayer_member_banned, msg)
            NetPlayStatus.ADDRESS_UNBANNED -> context.getString(R.string.multiplayer_address_unbanned)
            NetPlayStatus.CHAT_MESSAGE -> msg
            else -> ""
        }
    }

    @Suppress("DEPRECATION")
    private fun getIpAddressByWifi(activity: Activity): String {
        var ipAddress = 0
        val wifiManager = activity.getSystemService(WifiManager::class.java)
        val wifiInfo = wifiManager.connectionInfo
        if (wifiInfo != null) {
            ipAddress = wifiInfo.ipAddress
        }

        if (ipAddress == 0) {
            val dhcpInfo = wifiManager.dhcpInfo
            if (dhcpInfo != null) {
                ipAddress = dhcpInfo.ipAddress
            }
        }

        return if (ipAddress == 0) {
            "192.168.0.1"
        } else {
            Formatter.formatIpAddress(ipAddress)
        }
    }

    object NetPlayStatus {
        const val NO_ERROR = 0
        const val NETWORK_ERROR = 1
        const val LOST_CONNECTION = 2
        const val NAME_COLLISION = 3
        const val MAC_COLLISION = 4
        const val CONSOLE_ID_COLLISION = 5
        const val WRONG_VERSION = 6
        const val WRONG_PASSWORD = 7
        const val COULD_NOT_CONNECT = 8
        const val ROOM_IS_FULL = 9
        const val HOST_BANNED = 10
        const val PERMISSION_DENIED = 11
        const val NO_SUCH_USER = 12
        const val ALREADY_IN_ROOM = 13
        const val CREATE_ROOM_ERROR = 14
        const val HOST_KICKED = 15
        const val UNKNOWN_ERROR = 16
        const val ROOM_UNINITIALIZED = 17
        const val ROOM_IDLE = 18
        const val ROOM_JOINING = 19
        const val ROOM_JOINED = 20
        const val ROOM_MODERATOR = 21
        const val MEMBER_JOIN = 22
        const val MEMBER_LEAVE = 23
        const val MEMBER_KICKED = 24
        const val MEMBER_BANNED = 25
        const val ADDRESS_UNBANNED = 26
        const val CHAT_MESSAGE = 27
    }
}
