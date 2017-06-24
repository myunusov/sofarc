package org.maxur.sofarc.core.service.eservice

/**
 * This class represents Embedded to micro-service Service
 * with start and stop functions
 */
interface EmbeddedService {

    /**
     * Start Web server.
     */
    fun start()

    /**
     * Stop Web server.
     */
    fun stop()
    
}