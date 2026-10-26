package com.example.cosupv2

data class ServiceItem(val name: String, val description: String, val icon: String)

data class NewsStory(
    val title: String,
    val source: String,
    val date: String,
    val url: String,
    val body: String = "",
    val category: String = "",
    val cover: String = ""
)

data class SiteItem(
    val id: Int,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val hours: String,
    val phone: String,
    val isMain: Boolean,
    val services: List<String>
)

/** Built-in copy of the website data. Used when the website cannot be reached. */
object SampleData {
    val services = listOf(
        ServiceItem("Opioid Agonist Therapy", "Supervised medication-assisted treatment to reduce cravings and withdrawal.", "svc_oat"),
        ServiceItem("Needle & Syringe Programme", "Clean injecting equipment, safe disposal and harm-reduction advice.", "svc_nsp"),
        ServiceItem("Psychosocial Support", "Individual and group counselling with trained community caseworkers.", "svc_psychosocial"),
        ServiceItem("Vocational Training", "Skills development and job-readiness support.", "svc_vocational"),
        ServiceItem("HIV & HCV Prevention", "Testing, linkage to care and prevention education.", "svc_hiv"),
        ServiceItem("Community Integration", "Peer support and reintegration activities.", "svc_community")
    )

    val allNews = listOf(
        NewsStory(
            "Here's how to make drug addiction a health issue, not a criminal one", "Bhekisisa", "13 Apr 2026",
            "https://bhekisisa.org/health-beat-shorts/2026-04-13-heres-how-to-make-drug-addiction-a-health-issue-not-a-criminal-one/",
            "Why drug addiction should be treated as a health issue rather than a crime, with COSUP's harm reduction clinic at Mamelodi Hospital as an example.",
            "Policy", "news1"
        ),
        NewsStory(
            "COSUP in South Africa: A model for domestic harm reduction funding", "Harm Reduction International", "2 Jan 2025",
            "https://hri.global/publications/cosup-in-south-africa-a-model-for-domestic-harm-reduction-funding/",
            "Harm Reduction International profiles COSUP as a model of city-funded harm reduction, delivering around half of South Africa's harm reduction services.",
            "Funding", "news2"
        ),
        NewsStory(
            "Tshwane's new substance abuse marshalls to wage war against drugs", "Pretoria News", "11 May 2023",
            "https://m.motoring.co.za/pretoria-news/news/tshwanes-new-substance-abuse-marshalls-to-wage-war-against-drugs-f453b0c2-f58d-4cca-9c89-513e41f98ffc",
            "The University of Pretoria trained 100 community patrollers in Akasia as drug and substance abuse marshals, as part of COSUP's work with the City of Tshwane.",
            "Training", "news3"
        ),
        NewsStory(
            "Tshwane mayor Randall Williams warns Eersterust youth about dangers linked to drugs", "Pretoria News", "1 Dec 2022",
            "https://m.motoring.co.za/pretoria-news/news/tshwane-mayor-randall-williams-warns-eersterust-youth-about-dangers-linked-to-drugs-30b50f27-e4ac-4107-ad46-a2ba150d1ff7",
            "The mayor visits the Eersterust Community Health Centre, a COSUP site, during a youth drug-awareness campaign in Eersterust.",
            "Community", "news4"
        ),
        NewsStory(
            "Fighting drug use with humanity and science", "University of Pretoria", "29 May 2018",
            "https://www.up.ac.za/news/fighting-drug-use-humanity-and-science",
            "How COSUP treats harmful drug use as a health issue instead of a crime, using harm reduction, needle and syringe services, opioid therapy and peer support.",
            "Harm reduction", "news5"
        ),
        NewsStory(
            "Department of Family Medicine brings hope to drug users", "University of Pretoria", "19 Feb 2018",
            "https://www.up.ac.za/news/department-of-family-medicine-brings-hope-drug-users",
            "The University of Pretoria and the City of Tshwane share early results from COSUP, which began in August 2016 and had enrolled over 2 500 people by early 2018.",
            "Results", "news6"
        )
    )

    /** First three stories, shown on the Home screen. */
    val news: List<NewsStory> get() = allNews.take(3)

    private fun servicesFor(id: Int, main: Boolean): List<String> {
        val n = services.map { it.name }
        if (main) return n
        val out = mutableListOf(n[0], n[2])
        if (id % 2 == 0) out.add(n[1])
        if (id % 4 == 0) out.add(n[3])
        if (id % 3 == 0) out.add(n[4])
        if (id % 5 == 0) out.add(n[5])
        return out
    }

    private fun site(id: Int, name: String, addr: String, lat: Double, lng: Double, hours: String, phone: String) =
        SiteItem(id, name, addr, lat, lng, hours, phone, id == 1, servicesFor(id, id == 1))

    val sites: List<SiteItem> = listOf(
        site(1, "COSUP Sediba Hope - Bosman St", "173 Bosman Street, Pretoria CBD", -25.745, 28.188, "Mon-Fri 08:00-16:00", "082 858 4304"),
        site(2, "COSUP Bronkhorstspruit", "Zithobeni Clinic, Bronkhorstspruit", -25.799, 28.742, "Tue & Thur 09:30-14:00", "076 560 1389"),
        site(3, "COSUP Hammanskraal", "Mandisa Shiceka Clinic, Old Warmbaths Road", -25.449, 28.273, "Mon & Fri 08:00-16:00", "066 300 8338"),
        site(4, "COSUP Daspoort Poli Clinic", "Cnr Market & Camp Street, Pretoria West", -25.735, 28.156, "Mon-Fri 08:00-16:00", "082 857 0922"),
        site(5, "COSUP Attridgeville Clinic", "1A Mareka Street, Attridgeville", -25.78, 28.096, "Mon-Fri 08:00-16:00", "082 858 2450"),
        site(6, "COSUP Laudium Community", "405 Bengal Street, Laudium", -25.795, 28.074, "Mon-Fri 08:00-16:00", "081 725 2462"),
        site(7, "COSUP Olievenhoutbosch", "Cnr Imbovane & Imbongolo Street", -25.91, 28.085, "Mon-Fri 08:00-16:00", "066 472 5740"),
        site(8, "COSUP Winterveldt", "1/4 Cemetery Road, Slovo Gardens", -25.51, 28.07, "Mon-Fri 08:00-16:00", "071 573 5883"),
        site(9, "COSUP Mamelodi Lusaka", "Cnr Millenyane & Ratshwene Street", -25.7, 28.37, "Mon-Fri 08:00-16:00", "082 858 2553"),
        site(10, "COSUP Mamelodi Ikageng", "21882 Molokoloko Circle, Mamelodi", -25.715, 28.39, "Mon-Fri 08:00-16:00", "076 560 1389"),
        site(11, "COSUP Mamelodi Regional Hospital", "Cnr Serapeng & Tsarnaya Road, Mamelodi", -25.73, 28.41, "Mon-Fri 08:00-16:00", "066 472 5580"),
        site(12, "COSUP Eersterust CHC", "Cnr PS Fourie Drive & Hans Coverdale Road", -25.72, 28.29, "Mon-Fri 08:00-16:00", "082 941 6038"),
        site(13, "COSUP Soshanguve Block V", "Elim Tabernacle Church, Inkanyezi Street", -25.55, 28.1, "Mon-Thur 08:00-16:00", "066 300 8338"),
        site(14, "COSUP Soshanguve Block K POPUP", "Cnr Aubrey Matlala & Tlou Street", -25.57, 28.08, "Mon-Fri 08:00-16:00", "076 587 6770"),
        site(15, "COSUP Soshanguve M17", "Dream Team Foundation, M17 Road, Soshanguve South", -25.59, 28.065, "Mon-Fri 08:00-16:00", "082 858 2563"),
        site(16, "COSUP Garankuwa", "Reatlegile Centre, Stand 05030", -25.6, 28.02, "Mon-Fri 08:00-16:00", "079 321 4152")
    )
}
