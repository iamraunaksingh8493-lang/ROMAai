package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class SubjectType(
    val title: String,
    val iconEmoji: String,
    val color: Color,
    val tagline: String,
    val samplePrompts: List<String>
) {
    MATHS(
        title = "Maths",
        iconEmoji = "🔢",
        color = SubjectMath,
        tagline = "Algebra, Calculus, Geometry & Logic",
        samplePrompts = listOf(
            "Solve the quadratic equation 2x² - 5x + 2 = 0 step by step.",
            "Explain the Pythagorean theorem with an intuitive real-world example.",
            "How does compound interest differ from simple interest?",
            "What is the derivative of sin(x) * cos(x)?"
        )
    ),
    CHEMISTRY(
        title = "Chemistry",
        iconEmoji = "🧪",
        color = SubjectChemistry,
        tagline = "Reactions, Bonding, Elements & Lab Concepts",
        samplePrompts = listOf(
            "Why is water called the universal solvent and why does ice float?",
            "Explain the difference between ionic and covalent bonding.",
            "How do catalysts speed up a chemical reaction?",
            "What makes noble gases chemically inert?"
        )
    ),
    HISTORY(
        title = "History",
        iconEmoji = "📜",
        color = SubjectHistory,
        tagline = "Civilizations, Epochs, Revolutions & Timelines",
        samplePrompts = listOf(
            "What were the primary economic and social causes of the French Revolution?",
            "Explain the key factors that launched the Industrial Revolution.",
            "Who was Emperor Ashoka and what was the historical significance of the Kalinga War?",
            "How did the Silk Road transform ancient global trade?"
        )
    ),
    CIVICS(
        title = "Civics",
        iconEmoji = "🏛️",
        color = SubjectCivics,
        tagline = "Governance, Rights, Democracy & Constitutions",
        samplePrompts = listOf(
            "Explain the separation of powers between Legislature, Executive, and Judiciary.",
            "What is the fundamental difference between human rights and legal duties?",
            "How does a bill become a law in a parliamentary democracy?",
            "What role does the UN Security Council play in international peacekeeping?"
        )
    ),
    BIOLOGY(
        title = "Biology",
        iconEmoji = "🧬",
        color = SubjectBiology,
        tagline = "Cell Anatomy, Genetics, Physiology & Ecology",
        samplePrompts = listOf(
            "Explain photosynthesis: light-dependent vs light-independent reactions.",
            "How does DNA replication ensure genetic fidelity?",
            "What is the structure and function of the mitochondria in human cells?",
            "How does the human immune system generate antibody memory against pathogens?"
        )
    ),
    GRAMMAR(
        title = "Grammar",
        iconEmoji = "✍️",
        color = SubjectGrammar,
        tagline = "Syntax, Tenses, Vocabulary & Style",
        samplePrompts = listOf(
            "Explain the grammatical difference between 'affect' and 'effect' with examples.",
            "How and when should I use semicolons instead of commas?",
            "What is the difference between active and passive voice?",
            "Give me 5 sophisticated alternatives for 'very good' and 'very bad'."
        )
    ),
    GEOGRAPHY(
        title = "Geography",
        iconEmoji = "🌍",
        color = SubjectGeography,
        tagline = "Landforms, Climate, Tectonic Plates & Oceans",
        samplePrompts = listOf(
            "Why do tectonic plates shift and cause earthquakes and tsunamis?",
            "Explain the difference between El Niño and La Niña climate phenomena.",
            "What are the distinct structural layers of the Earth's atmosphere?",
            "Why do deserts form around 30 degrees latitude?"
        )
    ),
    GENERAL(
        title = "General",
        iconEmoji = "⚡",
        color = RomaCyan,
        tagline = "All-Round AI Voice Assistant",
        samplePrompts = listOf(
            "Introduce yourself and tell me what subjects you can help me master.",
            "How do quantum computers differ from classical computers?",
            "Give me a quick 3-minute study plan to prepare for exams."
        )
    )
}
