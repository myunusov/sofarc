package org.maxur.sofarc.core.service

import org.jvnet.hk2.annotations.Contract

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Contract
interface MicroService {

    /**
     * start
     */
    fun start()

    /**
     * stop
     */
    fun stop()

    val name: String
}
