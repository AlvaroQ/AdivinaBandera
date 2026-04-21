package com.alvaroquintana.usecases.question.generators

import com.alvaroquintana.usecases.GetSubdivisionsForCountry
import com.alvaroquintana.usecases.question.GeneratedQuestion
import com.alvaroquintana.usecases.question.GenerationContext
import com.alvaroquintana.usecases.question.QuestionGenerator
import com.alvaroquintana.usecases.question.internal.RandomUtils

class SubdivisionQuestionGenerator(
    private val getSubdivisionsForCountry: GetSubdivisionsForCountry
) : QuestionGenerator {

    override suspend fun generate(context: GenerationContext): GeneratedQuestion? {
        val alpha2 = context.subdivisionAlpha2 ?: return null
        val pool = getSubdivisionsForCountry.invoke(alpha2)
        if (pool.size < 4) return null

        val available = pool.filter { it.id !in context.seenSubdivisionIds }
        val correct = (if (available.isNotEmpty()) available else pool).random()
        val wrongPool = pool.filter { it.id != correct.id }.shuffled().take(3)

        val correctPos = RandomUtils.randomWithExclusion(0, 3)
        val remainingPositions = (0..3).filter { it != correctPos }
        val options = MutableList(4) { "" }
        options[correctPos] = correct.name
        wrongPool.forEachIndexed { idx, sub -> options[remainingPositions[idx]] = sub.name }

        return GeneratedQuestion(
            options = options,
            correctAnswer = correct.name,
            flagIcon = correct.flagUrl,
            seenSubdivisionId = correct.id
        )
    }
}
