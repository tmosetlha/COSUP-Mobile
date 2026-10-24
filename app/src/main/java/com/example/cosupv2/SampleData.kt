package com.example.cosupv2

data class ServiceItem(val name: String, val description: String, val icon: String)

data class NewsStory(val title: String, val source: String, val date: String, val url: String)

/** Temporary content. The next steps replace this with live data from the COSUP website database. */
object SampleData {
    val services = listOf(
        ServiceItem("Opioid Agonist Therapy", "Supervised medication-assisted treatment to reduce cravings and withdrawal.", "svc_oat"),
        ServiceItem("Needle & Syringe Programme", "Clean injecting equipment, safe disposal and harm-reduction advice.", "svc_nsp"),
        ServiceItem("Psychosocial Support", "Individual and group counselling with trained community caseworkers.", "svc_psychosocial"),
        ServiceItem("Vocational Training", "Skills development and job-readiness support.", "svc_vocational"),
        ServiceItem("HIV & HCV Prevention", "Testing, linkage to care and prevention education.", "svc_hiv"),
        ServiceItem("Community Integration", "Peer support and reintegration activities.", "svc_community")
    )

    val news = listOf(
        NewsStory(
            "Here's how to make drug addiction a health issue, not a criminal one",
            "Bhekisisa", "13 Apr 2026",
            "https://bhekisisa.org/health-beat-shorts/2026-04-13-heres-how-to-make-drug-addiction-a-health-issue-not-a-criminal-one/"
        ),
        NewsStory(
            "COSUP in South Africa: A model for domestic harm reduction funding",
            "Harm Reduction International", "2 Jan 2025",
            "https://hri.global/publications/cosup-in-south-africa-a-model-for-domestic-harm-reduction-funding/"
        ),
        NewsStory(
            "Tshwane's new substance abuse marshalls to wage war against drugs",
            "Pretoria News", "11 May 2023",
            "https://m.motoring.co.za/pretoria-news/news/tshwanes-new-substance-abuse-marshalls-to-wage-war-against-drugs-f453b0c2-f58d-4cca-9c89-513e41f98ffc"
        )
    )
}
