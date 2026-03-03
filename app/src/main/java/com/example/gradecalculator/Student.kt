package com.example.gradecalculator

import java.util.Locale

/**
 * Data class representing a Student's academic record.
 * Demonstrates Object-Oriented principles and Kotlin features.
 */
data class Student(
    val name: String,
    val score: Double,
    val grade: String
) {
    /**
     * Function 1: Validation
     * Checks if the student data is valid.
     */
    fun isValid(): Boolean {
        return name.isNotBlank() && score in 0.0..100.0
    }

    /**
     * Function 2: Formatting
     * Returns a professionally formatted string for display.
     */
    fun getFormattedDetails(): String {
        val capitalizedName = name.split(" ").joinToString(" ") { it.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
        } }
        return "$capitalizedName: ${score.toInt()}% ($grade)"
    }
}
