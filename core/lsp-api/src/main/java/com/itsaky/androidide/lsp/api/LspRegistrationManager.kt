/*
 *  This file is part of AndroidIDE.
 *
 *  AndroidIDE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  AndroidIDE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with AndroidIDE.  If not, see <https://www.gnu.org/licenses/>.
 */
/*
 *  @author android_zero
 */
package com.itsaky.androidide.lsp.api

import com.itsaky.androidide.lsp.models.Registration
import com.itsaky.androidide.lsp.models.Unregistration
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class LspRegistrationManager {
    private val log = LoggerFactory.getLogger(LspRegistrationManager::class.java)
    
    private val activeRegistrations = ConcurrentHashMap<String, MutableMap<String, Registration>>()

    fun register(registration: Registration) {
        log.info("LSP Dynamic Register: ${registration.method} (ID: ${registration.id})")
        activeRegistrations.getOrPut(registration.method) { ConcurrentHashMap() }[registration.id] = registration
    }

    fun unregister(unreg: Unregistration) {
        log.info("LSP Dynamic Unregister: ${unreg.method} (ID: ${unreg.id})")
        activeRegistrations[unreg.method]?.remove(unreg.id)
    }

    fun isSupported(method: String): Boolean {
        return activeRegistrations[method]?.isNotEmpty() ?: false
    }
}