package org.maxur.sofarc.core.service

import org.jvnet.hk2.annotations.Contract

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>12.06.2017</pre>
 */
@Contract
interface MicroService {

    val name: String

    /**
     * on start event
     */
    fun onStart()

    /**
     * on stop event
     */
    fun onStop()

    /**
     * on error event
     *
     * @param exception The exception associated with this failure
     */
    fun onError(exception:Exception)
}
