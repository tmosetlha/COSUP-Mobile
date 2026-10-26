package com.example.cosupv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject

/** Loads data from the website, and falls back to the built-in copy if the website cannot be reached. */
object Repo {

    private fun str(o: JSONObject, key: String): String = if (o.isNull(key)) "" else o.optString(key)

    private fun stem(path: String): String = path.substringAfterLast('/').substringBeforeLast('.')

    private val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    private fun niceDate(iso: String): String {
        return try {
            val y = iso.substring(0, 4)
            val m = iso.substring(5, 7).toInt()
            val d = iso.substring(8, 10).toInt()
            "$d ${months[m - 1]} $y"
        } catch (e: Exception) {
            ""
        }
    }

    fun loadServices(cb: (List<ServiceItem>) -> Unit) {
        Api.get("/api/services") { text ->
            val list = try {
                val a = JSONArray(text ?: "[]")
                (0 until a.length()).map { i ->
                    val o = a.getJSONObject(i)
                    ServiceItem(str(o, "serviceName"), str(o, "serviceDescription"), "svc_" + stem(str(o, "iconPath")))
                }
            } catch (e: Exception) {
                emptyList()
            }
            cb(if (list.isEmpty()) SampleData.services else list)
        }
    }

    fun loadSites(cb: (List<SiteItem>) -> Unit) {
        Api.get("/api/sites") { text ->
            val list = try {
                val a = JSONArray(text ?: "[]")
                (0 until a.length()).map { i ->
                    val o = a.getJSONObject(i)
                    val sv = o.optJSONArray("services")
                    SiteItem(
                        o.optInt("siteId"), str(o, "siteName"), str(o, "address"),
                        o.optDouble("latitude"), o.optDouble("longitude"),
                        str(o, "operatingHours"), str(o, "phone"), o.optBoolean("isMainCentre"),
                        if (sv == null) emptyList() else (0 until sv.length()).map { sv.getString(it) }
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
            cb(if (list.isEmpty()) SampleData.sites else list)
        }
    }

    fun loadNews(cb: (List<NewsStory>) -> Unit) {
        Api.get("/api/news") { text ->
            val list = try {
                val a = JSONArray(text ?: "[]")
                (0 until a.length()).map { i ->
                    val o = a.getJSONObject(i)
                    NewsStory(
                        str(o, "title"), str(o, "author"), niceDate(str(o, "publishedAt")), str(o, "sourceUrl"),
                        str(o, "body"), str(o, "category"), stem(str(o, "coverImg"))
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
            cb(if (list.isEmpty()) SampleData.allNews else list)
        }
    }
}

/** Fills one service card (same card as the Home screen). */
fun bindServiceCard(inflater: LayoutInflater, parent: ViewGroup, s: ServiceItem) {
    val card = inflater.inflate(R.layout.item_service, parent, false)
    card.findViewById<TextView>(R.id.svcName).text = s.name
    card.findViewById<TextView>(R.id.svcDesc).text = s.description
    val icon = card.findViewById<ImageView>(R.id.svcIcon)
    icon.setDrawableByName(s.icon)
    card.findViewById<View>(R.id.svcIconWrap).visibility = icon.visibility
    parent.addView(card)
}
