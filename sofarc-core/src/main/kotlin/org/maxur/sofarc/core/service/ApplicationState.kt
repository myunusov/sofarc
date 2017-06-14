package org.maxur.sofarc.core.service

enum class ApplicationState {

    /**
     * Stop application
     */
    STOP,

    /**
     * Restart application
     */
    RESTART;

    companion object {

        fun from(value: String): ApplicationState{
            val case = value.toUpperCase()
            if (case in ApplicationState::class.java.getEnumConstants().map { e -> e.name }) {
                return ApplicationState.valueOf(case)
            } else {
                throw IllegalArgumentException("The '$value' is not acceptable Application State")
            }
        }
    }
    
}