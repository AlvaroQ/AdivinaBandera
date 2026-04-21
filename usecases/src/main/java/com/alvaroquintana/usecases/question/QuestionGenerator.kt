package com.alvaroquintana.usecases.question

interface QuestionGenerator {
    /**
     * Returns a fresh question or null when the generator cannot satisfy the
     * constraints in [context] (e.g. not enough countries with the required field).
     */
    suspend fun generate(context: GenerationContext): GeneratedQuestion?
}
