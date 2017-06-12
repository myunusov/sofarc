package org.maxur.sofarc

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 *
 * This class supports greeting people by name.
 *
 * @property name The name of the person to be greeted.
 */
class Greeter(val name: String) {

    /**
     * Prints the greeting to the standard output.
     */
    fun greet() {
        println("Hello $name!")
    }
}

/**
 * Main method
 * @param args Application parameters
 */
fun main(args: Array<String>) {
    Greeter(args[0]).greet()
}
