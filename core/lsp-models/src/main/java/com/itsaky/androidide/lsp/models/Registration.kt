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
package com.itsaky.androidide.lsp.models

/**
 * 动态能力注册参数 (client/registerCapability)
 */
data class RegistrationParams(
    val registrations: List<Registration>
)

data class Registration(
    val id: String,         
    val method: String,     
    val registerOptions: Any? = null 
)

data class UnregistrationParams(
    val unregisterations: List<Unregistration>
)

data class Unregistration(
    val id: String,
    val method: String
)