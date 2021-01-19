/*
 * Copyright (c) 2020 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.features.call.transfer

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import im.vector.app.core.extensions.toMvRxBundle
import im.vector.app.core.platform.Restorable
import im.vector.app.features.call.dialpad.DialPadFragment
import im.vector.app.features.settings.VectorLocale
import im.vector.app.features.userdirectory.UserListFragment
import im.vector.app.features.userdirectory.UserListFragmentArgs

class CallTransferPagerAdapter(
        private val fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity), Restorable{

    val userListFragment: UserListFragment?
        get() = findFragmentAtPosition(0) as? UserListFragment
    val dialPadFragment: DialPadFragment?
        get() = findFragmentAtPosition(1) as? DialPadFragment

    var onDialPadOkClicked: ((String) -> Unit)? = null

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        val fragment: Fragment
        if (position == 0) {
            fragment = fragmentActivity.supportFragmentManager.fragmentFactory.instantiate(fragmentActivity.classLoader, UserListFragment::class.java.name)
            fragment.arguments = UserListFragmentArgs(
                    title = "",
                    menuResId = -1,
                    singleSelection = true,
                    showInviteActions = false,
                    showToolbar = false,
                    showContactBookAction = false
            ).toMvRxBundle()
        } else {
            fragment = fragmentActivity.supportFragmentManager.fragmentFactory.instantiate(fragmentActivity.classLoader, DialPadFragment::class.java.name)
            (fragment as DialPadFragment).apply {
                arguments = Bundle().apply {
                    putBoolean(DialPadFragment.EXTRA_ENABLE_DELETE, true)
                    putBoolean(DialPadFragment.EXTRA_ENABLE_OK, true)
                    putString(DialPadFragment.EXTRA_REGION_CODE, VectorLocale.applicationLocale.country)
                }
                applyCallback()
            }
        }
        return fragment
    }

    private fun findFragmentAtPosition(position: Int): Fragment? {
        return fragmentActivity.supportFragmentManager.findFragmentByTag("f$position")
    }

    override fun onSaveInstanceState(outState: Bundle) = Unit

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        dialPadFragment?.applyCallback()
    }

    private fun DialPadFragment.applyCallback(): DialPadFragment{
        callback = object : DialPadFragment.Callback {
            override fun onOkClicked(formatted: String?, raw: String?) {
                if (raw.isNullOrEmpty()) return
                onDialPadOkClicked?.invoke(raw)
            }
        }
        return this
    }


}
