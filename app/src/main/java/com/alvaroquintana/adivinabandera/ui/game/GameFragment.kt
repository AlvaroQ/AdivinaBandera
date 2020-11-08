package com.alvaroquintana.adivinabandera.ui.game

import android.content.Context
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.common.startActivity
import com.alvaroquintana.adivinabandera.common.traslationAnimation
import com.alvaroquintana.adivinabandera.common.traslationAnimationFadeIn
import com.alvaroquintana.adivinabandera.databinding.GameFragmentBinding
import com.alvaroquintana.adivinabandera.ui.result.ResultActivity
import com.alvaroquintana.adivinabandera.utils.Constants.POINTS
import com.alvaroquintana.adivinabandera.utils.Constants.TOTAL_BREED
import com.alvaroquintana.adivinabandera.utils.glideLoadBase64
import com.alvaroquintana.adivinabandera.utils.glideLoadingGif
import com.alvaroquintana.adivinabandera.utils.setSafeOnClickListener
import kotlinx.coroutines.*
import org.koin.android.scope.lifecycleScope
import org.koin.android.viewmodel.scope.viewModel
import java.util.*
import java.util.concurrent.TimeUnit


class GameFragment : Fragment() {
    private val gameViewModel: GameViewModel by lifecycleScope.viewModel(this)
    private lateinit var binding: GameFragmentBinding

    lateinit var imageLoading: ImageView
    lateinit var imageQuiz: ImageView
    lateinit var btnOptionOne: TextView
    lateinit var btnOptionTwo: TextView
    lateinit var btnOptionThree: TextView
    lateinit var btnOptionFour: TextView

    private var life: Int = 2
    private var stage: Int = 1
    private var points: Int = 0

    companion object {
        fun newInstance() = GameFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = GameFragmentBinding.inflate(inflater)
        val root = binding.root

        imageLoading = root.findViewById(R.id.imagenLoading)
        imageQuiz = root.findViewById(R.id.imageQuiz)
        btnOptionOne = root.findViewById(R.id.btnOptionOne)
        btnOptionTwo = root.findViewById(R.id.btnOptionTwo)
        btnOptionThree = root.findViewById(R.id.btnOptionThree)
        btnOptionFour = root.findViewById(R.id.btnOptionFour)

        btnOptionOne.setSafeOnClickListener {
            btnOptionOne.isSelected = !btnOptionOne.isSelected
            checkResponse()
        }

        btnOptionTwo.setSafeOnClickListener {
            btnOptionTwo.isSelected = !btnOptionTwo.isSelected
            checkResponse()
        }

        btnOptionThree.setSafeOnClickListener {
            btnOptionThree.isSelected = !btnOptionThree.isSelected
            checkResponse()
        }

        btnOptionFour.setSafeOnClickListener {
            btnOptionFour.isSelected = !btnOptionFour.isSelected
            checkResponse()
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameViewModel.navigation.observe(viewLifecycleOwner, Observer(::navigate))
        gameViewModel.progress.observe(viewLifecycleOwner, Observer(::updateProgress))
        gameViewModel.question.observe(viewLifecycleOwner, Observer(::drawQuestionImage))
        gameViewModel.responseOptions.observe(viewLifecycleOwner, Observer(::drawOptionsResponse))
    }

    private fun navigate(navigation: GameViewModel.Navigation?) {
        when (navigation) {
            is GameViewModel.Navigation.Result -> {
                activity?.startActivity<ResultActivity> { putExtra(POINTS, points) }
            }
        }
    }

    private fun updateProgress(model: GameViewModel.UiModel?) {
        if (model is GameViewModel.UiModel.Loading && model.show) {
            glideLoadingGif(activity as GameActivity, imageLoading)
            imageLoading.visibility = View.VISIBLE

            btnOptionOne.isSelected = false
            btnOptionTwo.isSelected = false
            btnOptionThree.isSelected = false
            btnOptionFour.isSelected = false

            enableBtn(false)
        } else {
            imageLoading.visibility = View.GONE

            btnOptionOne.background = context?.getDrawable(R.drawable.button)
            btnOptionTwo.background = context?.getDrawable(R.drawable.button)
            btnOptionThree.background = context?.getDrawable(R.drawable.button)
            btnOptionFour.background = context?.getDrawable(R.drawable.button)

            enableBtn(true)
            (activity as GameActivity).writeStage(stage)
        }
    }

    private fun drawQuestionImage(icon: String) {
        glideLoadBase64(activity as GameActivity, icon, imageQuiz)
    }

    private fun drawOptionsResponse(optionsListByPos: MutableList<String>) {
        var delay = 150L
        if(stage == 1) {
            delay = 0L
            binding.containerButtons.traslationAnimationFadeIn()
        }
        else binding.containerButtons.traslationAnimation()

        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.MILLISECONDS.toMillis(delay))
            withContext(Dispatchers.Main) {
                btnOptionOne.text = Locale(getString(R.string.locale), optionsListByPos[0]).displayCountry
                btnOptionTwo.text = Locale(getString(R.string.locale), optionsListByPos[1]).displayCountry
                btnOptionThree.text = Locale(getString(R.string.locale), optionsListByPos[2]).displayCountry
                btnOptionFour.text = Locale(getString(R.string.locale), optionsListByPos[3]).displayCountry
            }
        }
    }

    private fun checkResponse() {
        enableBtn(false)
        stage += 1

        val loc = Locale(getString(R.string.locale), gameViewModel.getCode2CountryCorrect()!!)
        drawCorrectResponse(loc.displayCountry)
        nextScreen()
    }

    private fun deleteLife() {
        life--
        (activity as GameActivity).writeDeleteLife(life)
    }

    private fun drawCorrectResponse(dogNameCorrect: String) {
        when {
            btnOptionOne.text == dogNameCorrect -> {
                btnOptionOne.background =  ContextCompat.getDrawable(requireContext(), R.drawable.btn_correct)
                when {
                    btnOptionOne.isSelected -> {
                        MediaPlayer.create(context, R.raw.success).start()
                        points += 1
                    }
                    btnOptionTwo.isSelected -> {
                        MediaPlayer.create(context, R.raw.fail).start()
                        deleteLife()
                        btnOptionTwo.background =  ContextCompat.getDrawable(requireContext(), R.drawable.btn_wrong)
                    }
                    btnOptionThree.isSelected -> {
                        MediaPlayer.create(context, R.raw.fail).start()
                        deleteLife()
                        btnOptionThree.background =  ContextCompat.getDrawable(requireContext(), R.drawable.btn_wrong)
                    }
                    btnOptionFour.isSelected -> {
                        MediaPlayer.create(context, R.raw.fail).start()
                        deleteLife()
                        btnOptionFour.background =  ContextCompat.getDrawable(requireContext(), R.drawable.btn_wrong)
                    }
                    else -> {
                        MediaPlayer.create(context, R.raw.fail).start()
                        deleteLife()
                    }
                }
            }
            btnOptionTwo.text == dogNameCorrect -> {
                btnOptionTwo.background =  ContextCompat.getDrawable(requireContext(), R.drawable.btn_correct)
                when {
                    btnOptionOne.isSelected -> {
                        MediaPlayer.create(context, R.raw.fail).start()
                        deleteLife()
                        btnOptionOne.background =  ContextCompat.getDrawable(requireContext(), R.drawable.btn_wrong)
                    }
                    btnOptionTwo.isSelected -> {
                        MediaPlayer.create(context, R.raw.success).start()
                        points += 1
                    }
                    btnOptionThree.isSelected -> {
                        MediaPlayer.create(context, R.raw.fail).start()
                        deleteLife()
                        btnOptionThree.background =  ContextCompat.getDrawable(requireContext(), R.drawable.btn_wrong)
                    }
                    btnOptionFour.isSelected -> {
                        MediaPlayer.create(context, R.raw.fail).start()
                        deleteLife()
                        btnOptionFour.background =  ContextCompat.getDrawable(requireContext(), R.drawable.btn_wrong)
                    }
                    else -> {
                        MediaPlayer.create(context, R.raw.fail).start()
                        deleteLife()
                    }
                }
            }
            btnOptionThree.text == dogNameCorrect -> {
                btnOptionThree.background =  ContextCompat.getDrawable(requireContext(), R.drawable.btn_correct)
                when {
                    btnOptionOne.isSelected -> {
                        MediaPlayer.create(context, R.raw.fail).start()
                        deleteLife()
                        btnOptionOne.background =  ContextCompat.getDrawable(requireContext(), R.drawable.btn_wrong)
                    }
                    btnOptionTwo.isSelected -> {
                        MediaPlayer.create(context, R.raw.fail).start()
                        deleteLife()
                        btnOptionTwo.background =  ContextCompat.getDrawable(requireContext(), R.drawable.btn_wrong)
                    }
                    btnOptionThree.isSelected -> {
                        MediaPlayer.create(context, R.raw.success).start()
                        points += 1
                    }
                    btnOptionFour.isSelected -> {
                        MediaPlayer.create(context, R.raw.fail).start()
                        deleteLife()
                        btnOptionFour.background =  ContextCompat.getDrawable(requireContext(), R.drawable.btn_wrong)
                    }
                    else -> {
                        MediaPlayer.create(context, R.raw.fail).start()
                        deleteLife()
                    }
                }
            }
            btnOptionFour.text == dogNameCorrect -> {
                btnOptionFour.background =  ContextCompat.getDrawable(requireContext(), R.drawable.btn_correct)
                when {
                    btnOptionOne.isSelected -> {
                        MediaPlayer.create(context, R.raw.fail).start()
                        deleteLife()
                        btnOptionOne.background =  ContextCompat.getDrawable(requireContext(), R.drawable.btn_wrong)
                    }
                    btnOptionTwo.isSelected -> {
                        MediaPlayer.create(context, R.raw.fail).start()
                        deleteLife()
                        btnOptionTwo.background =  ContextCompat.getDrawable(requireContext(), R.drawable.btn_wrong)
                    }
                    btnOptionThree.isSelected -> {
                        MediaPlayer.create(context, R.raw.fail).start()
                        deleteLife()
                        btnOptionThree.background =  ContextCompat.getDrawable(requireContext(), R.drawable.btn_wrong)
                    }
                    btnOptionFour.isSelected -> {
                        MediaPlayer.create(context, R.raw.success).start()
                        points += 1
                    }
                    else -> {
                        MediaPlayer.create(context, R.raw.fail).start()
                        deleteLife()
                    }
                }
            }
        }
    }

    private fun enableBtn(isEnable: Boolean) {
        btnOptionOne.isClickable = isEnable
        btnOptionTwo.isClickable = isEnable
        btnOptionThree.isClickable = isEnable
        btnOptionFour.isClickable = isEnable
    }

    private fun nextScreen() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(TimeUnit.MILLISECONDS.toMillis(1000))
            withContext(Dispatchers.Main) {
                if(stage > TOTAL_BREED || life < 1) gameViewModel.navigateToResult(points.toString())
                else gameViewModel.generateNewStage()
            }
        }
    }
}
