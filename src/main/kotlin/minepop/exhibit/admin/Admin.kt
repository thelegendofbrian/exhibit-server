package minepop.exhibit.admin

import minepop.exhibit.Crypto

object Admin {

    private val adminDAO = AdminDAO()

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            println("Missing action argument (user, upgrade)")
            return
        }
        when (args[0].toLowerCase()) {
            "user" -> {
                if (args.size < 2) {
                    println("Missing user sub-action argument (create, delete, enable, disable, recover)")
                    return
                }
                when (args[1].toLowerCase()) {
                    "create" -> {
                        createUser(args)
                    }
                    "delete" -> {
                        deleteUser(args)
                    }
                    "enable" -> {
                        disenableUser(args, true)
                    }
                    "disable" -> {
                        disenableUser(args, false)
                    }
                    "recover" -> {
                        recoverUser(args)
                    }
                    else -> {
                        println("Unrecognized user sub-action " + args[1])
                    }
                }
            }
            "upgrade" -> {
                if (args.size < 2) {
                    println("Missing upgrade sub-action argument (statsStateLastCheckin)")
                    return
                }
                when (args[1].toLowerCase()) {
                    "statsstatelastcheckin" -> {
                        upgradeStatsStateLastCheckin()
                    }
                    else -> {
                        println("Unrecognized upgrade sub-action " + args[1])
                    }
                }
            }
            else -> {
                println("Unrecognized action " + args[0])
            }
        }
    }

    private fun upgradeStatsStateLastCheckin() {
        adminDAO.upgradeStatsStateLastCheckin()
    }

    private fun deleteUser(args: Array<String>) {
        if (args.size < 3) {
            println("Missing delete user arguments (UserName)")
            return
        }
        val userName = args[2]
        adminDAO.deleteUser(userName)
    }

    private fun recoverUser(args: Array<String>) {
        if (args.size < 4) {
            println("Missing recover user arguments (UserName, Password)")
            return
        }
        val userName = args[2]
        val password = args[3]

        val salt = Crypto.nextSalt(32)
        val hash = Crypto.hash(password.toCharArray(), salt)

        adminDAO.updateUserCredentials(userName, salt, hash)
    }

    private fun disenableUser(args: Array<String>, enable: Boolean) {
        if (args.size < 3) {
            println("Missing dis/enable user arguments (UserName)");
            return
        }

        val userName = args[2]

        adminDAO.updateUserFailedLogins(userName, if (enable) 0 else 127)
    }

    private fun createUser(args: Array<String>) {
        if (args.size < 4) {
            println("Missing create user arguments (UserName, Password)")
            return
        }
        val userName = args[2]
        val password = args[3]

        val salt = Crypto.nextSalt(32)
        val hash = Crypto.hash(password.toCharArray(), salt)
        adminDAO.createUser(userName, salt, hash)
    }
}
