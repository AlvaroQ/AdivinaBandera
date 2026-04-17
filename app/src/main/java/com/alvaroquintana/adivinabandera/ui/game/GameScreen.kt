package com.alvaroquintana.adivinabandera.ui.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.components.AnswerOptionCard
import com.alvaroquintana.adivinabandera.ui.components.AnswerState
import com.alvaroquintana.adivinabandera.ui.components.GameStatusRow
import com.alvaroquintana.adivinabandera.ui.components.LoadingState
import com.alvaroquintana.adivinabandera.ui.components.OptionGrid
import com.alvaroquintana.adivinabandera.ui.components.QuestionCard
import com.alvaroquintana.adivinabandera.ui.composables.FlagImage
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.LocalGameColors
import com.alvaroquintana.adivinabandera.ui.theme.getBackgroundGradient
import com.alvaroquintana.adivinabandera.utils.Constants.TOTAL_COUNTRIES
import com.alvaroquintana.domain.Country
import com.alvaroquintana.domain.GameMode
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

private fun alpha2ToDisplayName(alpha2Code: String): String {
    if (alpha2Code.isBlank()) return alpha2Code
    val name = Locale.Builder().setRegion(alpha2Code).build().displayCountry
    return name.ifBlank { alpha2Code }
}

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    stage: Int,
    lives: Int = 3,
    points: Int = 0,
    onAnswerSelected: (selectedIndex: Int, correctAnswer: String, options: List<String>) -> Unit
) {
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val flagBase64 by viewModel.question.collectAsStateWithLifecycle()
    val countryName by viewModel.countryName.collectAsStateWithLifecycle()
    val currencyQuestion by viewModel.currencyQuestion.collectAsStateWithLifecycle()
    val populationPair by viewModel.populationPair.collectAsStateWithLifecycle()
    val currentMixType by viewModel.currentMixType.collectAsStateWithLifecycle()
    val mixQuestionText by viewModel.mixQuestionText.collectAsStateWithLifecycle()

    val isCapitalMode = viewModel.gameMode == GameMode.CapitalByFlag || viewModel.gameMode == GameMode.CapitalByCountry
    val isCountryNameMode = viewModel.gameMode == GameMode.CapitalByCountry
    val isCurrencyMode = viewModel.gameMode == GameMode.CurrencyDetective
    val isPopulationMode = viewModel.gameMode == GameMode.PopulationChallenge
    val isWorldMixMode = viewModel.gameMode == GameMode.WorldMix

    // WorldMix capital sub-types use capital city text as options (not alpha2codes)
    val isMixCapitalType = isWorldMixMode && (
        currentMixType == MixQuestionType.FLAG_TO_CAPITAL ||
        currentMixType == MixQuestionType.COUNTRY_TO_CAPITAL
    )
    // WorldMix flag sub-types show the flag image as question
    val isMixFlagType = isWorldMixMode && (
        currentMixType == MixQuestionType.FLAG_TO_COUNTRY ||
        currentMixType == MixQuestionType.FLAG_TO_CAPITAL
    )

    val isLoading = progress is GameViewModel.UiModel.Loading &&
            (progress as GameViewModel.UiModel.Loading).show

    var rawOptions by remember { mutableStateOf(listOf("", "", "", "")) }
    var displayOptions by remember { mutableStateOf(listOf("", "", "", "")) }

    var buttonsVisible by remember { mutableStateOf(false) }
    var buttonsEnabled by remember { mutableStateOf(true) }
    val answerStates = remember {
        mutableStateListOf(
            AnswerState.NEUTRAL,
            AnswerState.NEUTRAL,
            AnswerState.NEUTRAL,
            AnswerState.NEUTRAL
        )
    }

    LaunchedEffect(Unit) {
        viewModel.responseOptions.collect { optionList ->
            rawOptions = optionList.toList()
            // Re-read mix type at collection time (StateFlow.value is always current)
            val mixType = viewModel.currentMixType.value
            val isMixCapitalNow = isWorldMixMode && (
                mixType == MixQuestionType.FLAG_TO_CAPITAL ||
                mixType == MixQuestionType.COUNTRY_TO_CAPITAL
            )
            displayOptions = if (isCapitalMode || isMixCapitalNow) {
                // Capital modes and WorldMix capital types: options are capital city names, display as-is
                optionList.toList()
            } else {
                // Classic, CurrencyDetective, and WorldMix non-capital types use alpha2codes → country names
                // Population mode also uses alpha2codes but renders differently
                optionList.map { alpha2ToDisplayName(it) }
            }
            // Reset states to match new options size (supports both 2 and 4 options)
            while (answerStates.size < optionList.size) answerStates.add(AnswerState.NEUTRAL)
            while (answerStates.size > optionList.size) answerStates.removeLastOrNull()
            for (i in answerStates.indices) {
                answerStates[i] = AnswerState.NEUTRAL
            }
            buttonsEnabled = true

            if (stage == 1) {
                buttonsVisible = true
            } else {
                buttonsVisible = false
                delay(50)
                buttonsVisible = true
            }
        }
    }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            buttonsEnabled = false
            for (i in answerStates.indices) {
                answerStates[i] = AnswerState.NEUTRAL
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(getBackgroundGradient())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        GameStatusRow(
            stageLabel = "$stage/$TOTAL_COUNTRIES",
            score = points
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isPopulationMode) {
            // Population Challenge: full-screen binary comparison layout
            PopulationChallengeContent(
                pair = populationPair,
                answerStates = answerStates,
                buttonsEnabled = buttonsEnabled,
                buttonsVisible = buttonsVisible,
                isLoading = isLoading,
                stage = stage,
                onTap = { selectedIndex ->
                    if (buttonsEnabled && !isLoading) {
                        buttonsEnabled = false
                        val correctAnswer = viewModel.getCorrectAnswer()
                        // For population mode, displayOptions contains country names by alpha2code
                        // but feedback uses rawOptions (alpha2codes) directly
                        val correctDisplay = alpha2ToDisplayName(correctAnswer)
                        applyAnswerFeedbackStates(
                            answerStates,
                            displayOptions,
                            correctDisplay,
                            selectedIndex
                        )
                        onAnswerSelected(selectedIndex, correctAnswer, rawOptions)
                    }
                }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    LoadingState()
                }

                AnimatedContent(
                    targetState = stage,
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                    },
                    label = "questionTransition"
                ) { _ ->
                when {
                    isCountryNameMode -> {
                        if (countryName.isNotEmpty() && !isLoading) TextQuestion(countryName, 32.sp)
                    }
                    isCurrencyMode -> {
                        if (currencyQuestion.isNotEmpty() && !isLoading) TextQuestion(currencyQuestion, 26.sp)
                    }
                    isWorldMixMode && !isMixFlagType -> {
                        // WorldMix text-based types: COUNTRY_TO_CAPITAL, CURRENCY_TO_COUNTRY,
                        // DEMONYM_TO_COUNTRY, LANGUAGE_TO_COUNTRY, CALLING_CODE_TO_COUNTRY, NEIGHBOR_CHALLENGE
                        if (mixQuestionText.isNotEmpty() && !isLoading) TextQuestion(mixQuestionText, 24.sp)
                    }
                    else -> {
                        // All flag-based modes (Classic, CapitalByFlag, MixFlagType)
                        if (flagBase64.isNotEmpty() && !isLoading) {
                            QuestionCard(
                                flagBase64 = flagBase64,
                                questionNumber = stage,
                                totalQuestions = TOTAL_COUNTRIES,
                                modifier = Modifier.fillMaxSize(),
                                imageContentScale = ContentScale.Fit
                            )
                        }
                    }
                }
                } // AnimatedContent
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.game_title),
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            OptionGrid(
                options = displayOptions,
                modifier = Modifier.fillMaxWidth()
            ) { index, option, cardModifier ->
                var buttonVisible by remember(stage) { mutableStateOf(false) }

                LaunchedEffect(stage, buttonsVisible) {
                    buttonVisible = false
                    if (buttonsVisible && !isLoading) {
                        delay(index * 80L)
                        buttonVisible = true
                    }
                }

                AnimatedVisibility(
                    visible = buttonVisible,
                    enter = fadeIn(animationSpec = tween(200)) + slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = spring(
                            dampingRatio = 0.6f,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
                    modifier = cardModifier
                ) {
                    AnswerOptionCard(
                        text = option,
                        state = answerStates[index],
                        enabled = buttonsEnabled,
                        modifier = Modifier,
                        onClick = {
                            if (buttonsEnabled) {
                                buttonsEnabled = false
                                val correctAnswer = viewModel.getCorrectAnswer()
                                val correctDisplay = if (isCapitalMode || isMixCapitalType) {
                                    correctAnswer
                                } else {
                                    alpha2ToDisplayName(correctAnswer)
                                }
                                applyAnswerFeedbackStates(
                                    answerStates,
                                    displayOptions,
                                    correctDisplay,
                                    index
                                )
                                onAnswerSelected(index, correctAnswer, rawOptions)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TextQuestion(text: String, fontSize: TextUnit = 26.sp) {
    Text(
        text = text,
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Bold,
        fontSize = fontSize,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

@Composable
private fun PopulationChallengeContent(
    pair: Pair<Country, Country>?,
    answerStates: List<AnswerState>,
    buttonsEnabled: Boolean,
    buttonsVisible: Boolean,
    isLoading: Boolean,
    stage: Int,
    onTap: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading || pair == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                LoadingState()
            }
        } else {
            Text(
                text = stringResource(R.string.population_challenge_question),
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )

            AnimatedVisibility(
                visible = buttonsVisible,
                enter = if (stage <= 1) {
                    fadeIn(animationSpec = tween(100))
                } else {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(200)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PopulationCountryCard(
                        country = pair.first,
                        state = if (answerStates.size > 0) answerStates[0] else AnswerState.NEUTRAL,
                        enabled = buttonsEnabled,
                        modifier = Modifier.weight(1f),
                        onClick = { onTap(0) }
                    )
                    PopulationCountryCard(
                        country = pair.second,
                        state = if (answerStates.size > 1) answerStates[1] else AnswerState.NEUTRAL,
                        enabled = buttonsEnabled,
                        modifier = Modifier.weight(1f),
                        onClick = { onTap(1) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PopulationCountryCard(
    country: Country,
    state: AnswerState,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val gameColors = LocalGameColors.current

    val containerColor = when (state) {
        AnswerState.NEUTRAL, AnswerState.SELECTED -> MaterialTheme.colorScheme.surface
        AnswerState.CORRECT -> gameColors.correctContainer
        AnswerState.WRONG -> gameColors.wrongContainer
    }
    val borderColor = when (state) {
        AnswerState.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        AnswerState.SELECTED -> MaterialTheme.colorScheme.primary
        AnswerState.CORRECT -> gameColors.correctAnswer
        AnswerState.WRONG -> gameColors.wrongAnswer
    }
    val textColor = when (state) {
        AnswerState.NEUTRAL, AnswerState.SELECTED -> MaterialTheme.colorScheme.onSurface
        AnswerState.CORRECT -> gameColors.onCorrectContainer
        AnswerState.WRONG -> gameColors.onWrongContainer
    }

    val animatedContainerColor by animateColorAsState(
        targetValue = containerColor,
        animationSpec = tween(300),
        label = "popContainerColor"
    )
    val animatedBorderColor by animateColorAsState(
        targetValue = borderColor,
        animationSpec = tween(300),
        label = "popBorderColor"
    )
    val animatedTextColor by animateColorAsState(
        targetValue = textColor,
        animationSpec = tween(300),
        label = "popTextColor"
    )

    val countryDisplayName = alpha2ToDisplayName(country.alpha2Code)
    val populationText = if (state == AnswerState.CORRECT || state == AnswerState.WRONG) {
        val fmt = NumberFormat.getNumberInstance(Locale.getDefault())
        fmt.format(country.population) + " hab."
    } else {
        "???"
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .border(1.dp, animatedBorderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = animatedContainerColor,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                FlagImage(
                    base64 = country.icon,
                    contentDescription = countryDisplayName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Text(
                text = countryDisplayName,
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = animatedTextColor,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Text(
                text = populationText,
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = animatedTextColor.copy(alpha = if (state == AnswerState.NEUTRAL) 0.6f else 1f),
                textAlign = TextAlign.Center
            )
        }
    }
}
