object UserManager {
    val userList = mutableListOf<User>()

    fun registerUser(name: String, email: String, password: String): Boolean {
        //Already registered
        if (userList.any { it.email == email })
            return false
        userList.add(User(name, email, password))
        return true
    }
    fun isValidUser(email: String, password: String): Boolean {
        return userList.any { it.email == email && it.password == password }

    }
}