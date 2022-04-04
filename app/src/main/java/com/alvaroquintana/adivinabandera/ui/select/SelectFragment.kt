package com.alvaroquintana.adivinabandera.ui.select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.common.startActivity
import com.alvaroquintana.adivinabandera.databinding.SelectFragmentBinding
import androidx.lifecycle.Observer
import com.alvaroquintana.adivinabandera.ui.game.GameActivity
import com.alvaroquintana.adivinabandera.ui.info.InfoActivity
import com.alvaroquintana.adivinabandera.ui.settings.SettingsActivity
import com.alvaroquintana.adivinabandera.utils.setSafeOnClickListener
import org.koin.android.scope.lifecycleScope
import org.koin.android.viewmodel.scope.viewModel

class SelectFragment : Fragment() {
    private lateinit var binding: SelectFragmentBinding
    private val selectViewModel: SelectViewModel by lifecycleScope.viewModel(this)

    companion object {
        fun newInstance() = SelectFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SelectFragmentBinding.inflate(inflater)
        val root = binding.root

        val btnSubmit: Button = root.findViewById(R.id.btnStart)
        btnSubmit.setSafeOnClickListener { selectViewModel.navigateToGame() }

        val btnLearn: Button = root.findViewById(R.id.btnLearn)
        btnLearn.setSafeOnClickListener { selectViewModel.navigateToLearn() }

        val btnSettings: Button = root.findViewById(R.id.btnSettings)
        btnSettings.setSafeOnClickListener { selectViewModel.navigateToSettings() }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectViewModel.navigation.observe(viewLifecycleOwner, Observer(::navigate))
    }

    private fun navigate(navigation: SelectViewModel.Navigation?) {
        when (navigation) {
            SelectViewModel.Navigation.Game -> { activity?.startActivity<GameActivity> {} }
            SelectViewModel.Navigation.Info -> { activity?.startActivity<InfoActivity> {} }
            SelectViewModel.Navigation.Settings -> { activity?.startActivity<SettingsActivity> {} }
        }
    }
}
