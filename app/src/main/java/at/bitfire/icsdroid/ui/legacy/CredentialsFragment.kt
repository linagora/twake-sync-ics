/***************************************************************************************************
 * Copyright © All Contributors. See LICENSE and AUTHORS in the root directory for details.
 **************************************************************************************************/

package at.bitfire.icsdroid.ui.legacy

import androidx.fragment.app.Fragment

class CredentialsFragment: Fragment() {

    /*val model by activityViewModels<CredentialsModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, inState: Bundle?): View {
        val binding = CredentialsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.model = model

        model.requiresAuth.observe(viewLifecycleOwner) { requiresAuth ->
            binding.inputs.visibility = if (requiresAuth) View.VISIBLE else View.GONE
        }

        return binding.root
    }

    class CredentialsModel : ViewModel() {
        var originalRequiresAuth: Boolean? = null
        var originalUsername: String? = null
        var originalPassword: String? = null

        val requiresAuth = MutableLiveData<Boolean>()
        val username = MutableLiveData<String>()
        val password = MutableLiveData<String>()

        init {
            requiresAuth.value = false
        }

        fun dirty() = requiresAuth.value != originalRequiresAuth ||
                username.value != originalUsername ||
                password.value != originalPassword
    }*/

}
