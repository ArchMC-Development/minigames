package gg.tropic.practice.application

import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class LogRedirector {
    private val originalOut = System.out
    private val originalErr = System.err

    fun startLogging(logFileName: String = "app.log") {
        try {
            // Create file output stream
            val fileOutputStream = FileOutputStream(logFileName, true) // append mode
            val printStream = PrintStream(fileOutputStream)

            // Create a custom PrintStream that writes to both file and console
            val dualPrintStream = object : PrintStream(fileOutputStream) {
                override fun println(x: String?) {
                    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                    val logEntry = "[$timestamp] $x"

                    // Write to file
                    super.println(logEntry)
                    super.flush()

                    // Also write to original console
                    originalOut.println(logEntry)
                }

                override fun print(x: String?) {
                    super.print(x)
                    super.flush()
                    originalOut.print(x)
                }
            }

            // Redirect System.out and System.err
            System.setOut(dualPrintStream)
            System.setErr(dualPrintStream)

            println("Logging started to $logFileName")

        } catch (e: FileNotFoundException) {
            println("Error: Could not create log file: ${e.message}")
        }
    }

    fun stopLogging() {
        System.setOut(originalOut)
        System.setErr(originalErr)
        println("Logging stopped")
    }
}
