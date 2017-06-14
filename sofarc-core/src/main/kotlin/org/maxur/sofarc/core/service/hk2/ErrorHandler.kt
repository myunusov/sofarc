@file:Suppress("unused")

package org.maxur.sofarc.core.service.hk2

import org.glassfish.hk2.api.ErrorInformation
import org.glassfish.hk2.api.ErrorService
import org.jvnet.hk2.annotations.Service
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * Application Configurations
 *
 * @author myunusov
 * @version 1.0
 * @since <pre>01.09.2015</pre>
 */
@Service
open class ErrorHandler: ErrorService {

    companion object {
        val log: Logger = LoggerFactory.getLogger(ErrorHandler::class.java)
    }

    override fun onFailure(errorInformation: ErrorInformation) {
        if (log.isDebugEnabled) {
            log.debug("Bean initialization error: ", errorInformation.associatedException.message)
        } else {
            log.error(errorInformation.associatedException.message)
        }
    }

}
