object AssertionsHelper {

    inline fun <reified T: Exception> assertThrows(executable: ()-> Unit): Exception {
        try {
            executable()
        } catch (ex: Exception) {
            if (ex is T) {
                return ex
            }
        }
        throw RuntimeException("Expected exception wasn't thrown")
    }
}