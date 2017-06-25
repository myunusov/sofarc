package org.maxur.sofarc.core.service.embedded

/**
 * This class represents Embedded to micro-service Service
 * with start and stop functions
 */
abstract class EmbeddedService {

    /**
     * Start Web server.
     */
    abstract fun start()

    /**
     * Stop Web server.
     */
    abstract fun stop()


}