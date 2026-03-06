Kotlin Assignments Repository

Overview

This repository contains solutions to two Kotlin programming assignments.
Each assignment is organized in its own branch to keep the project clean and easy to navigate.

The branches included in this repository are:

- grade-calculator → Assignment 1: Kotlin Grade Calculator
- kotlin-collection-exercises → Assignment 2: Kotlin Higher-Order Functions and Collection Processing Exercises

Using separate branches allows each assignment to be reviewed independently without interfering with other project files.

---

Repository Structure

Main Repository

- README.md
- Branch: grade-calculator
- Branch: kotlin-collection-exercises

To view the assignments, switch branches using the branch selector on GitHub.

---

Assignment 1: Grade Calculator

Branch: grade-calculator

Description

This assignment implements a Kotlin grade calculator based on a predefined grading system.

The program accepts a numerical score and determines the corresponding letter grade using conditional logic.

Grading System

Score Range| Grade
85 – 100| A
70 – 84| B
50 – 69| C
30 – 49| D
0 – 29| F

How the Program Works

1. The program accepts a student's score.
2. It evaluates the score using conditional logic.
3. The score is compared against the grading ranges.
4. The corresponding letter grade is returned.
5. The result is printed to the console.

Example:

Score: 78
Output: Grade B

The implementation also demonstrates Kotlin features such as:

- concise function syntax
- higher-order functions
- lambdas
- clean conditional evaluation

---

Running the Grade Calculator

Option 1: Kotlin Playground

The program can be executed online using the Kotlin compiler:

https://play.kotlinlang.org/

Steps:

1. Copy the code from the repository.
2. Paste it into Kotlin Playground.
3. Click Run to execute the program.

---

Option 2: Run Locally

Requirements:

- Kotlin installed
- or an IDE such as IntelliJ IDEA or Android Studio

Steps:

1. Clone the repository
2. Switch to the grade-calculator branch

git clone <repository-url>
git checkout grade-calculator

3. Run the Kotlin file containing the program.

---

Assignment 2: Kotlin Collection Exercises

Branch: kotlin-collection-exercises

Description

This assignment demonstrates the use of higher-order functions and collection transformations in Kotlin.

Three exercises were implemented:

Exercise 1 – Higher-Order Function

A function "processList" was implemented that:

- Accepts a list of integers
- Accepts a predicate lambda "(Int) -> Boolean"
- Returns a new list containing only the elements that satisfy the predicate

Example:

Input:

[1, 2, 3, 4, 5, 6]

Predicate:

it % 2 == 0

Output:

[2, 4, 6]

---

Exercise 2 – Transforming Collections

A list of words is transformed into a map where:

- Key = word
- Value = word length

Then only entries with length greater than 4 are printed.

Example Output:

apple has length 5
banana has length 6
elephant has length 7

---

Exercise 3 – Complex Data Processing

A "Person" data class was created containing:

- name
- age

The program:

1. Filters people whose names start with A or B
2. Extracts their ages
3. Calculates the average age
4. Prints the result rounded to one decimal place

This exercise demonstrates the use of:

- "filter"
- "map"
- "average"
- functional collection processing

---

Running the Collection Exercises

The exercises were executed using Kotlin Playground:

https://play.kotlinlang.org/

They can also be compiled locally using any Kotlin IDE.

---

Author

Emmanuel Obasi Okorie

Kotlin Programming Assignments
