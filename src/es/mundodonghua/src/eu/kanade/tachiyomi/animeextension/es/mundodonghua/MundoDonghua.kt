package com.lagradost.cloudstream3.movieproviders

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addExtractorLink
import com.lagradost.cloudstream3.utils.AppUtils.toJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import org.jsoup.Jsoup

class MundoDonghuaProvider : MainAPI() {
    override var mainUrl = "https://www.mundodonghua.com"
    override var name = "MundoDonghua"
    override val hasMainPage = true
    override var lang = "es"
    override val supportedTypes = setOf(TvType.Anime)

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val items = mutableListOf<HomePageList>()

        val doc = app.get(mainUrl).document
        val animeList = doc.select("div.anime-card")

        val data = animeList.mapNotNull {
            val title = it.select("h3").text()
            val href = it.select("a").attr("href")
            val poster = it.select("img").attr("src")
            newAnimeSearchResponse(title, href, TvType.Anime) {
                this.posterUrl = poster
            }
        }

        items.add(HomePageList("Últimos Donghua", data))
        return HomePageResponse(items)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val doc = app.get(url).document
        return doc.select("div.anime-card").mapNotNull {
            val title = it.select("h3").text()
            val href = it.select("a").attr("href")
            val poster = it.select("img").attr("src")
            newAnimeSearchResponse(title, href, TvType.Anime) {
                this.posterUrl = poster
            }
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        val title = doc.select("h1").text()
        val poster = doc.select("div.thumb img").attr("src")
        val episodes = doc.select("div#episodes a").map {
            val name = it.text()
            val href = it.attr("href")
            Episode(href, name)
        }

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = poster
            addEpisodes(DubStatus.Subbed, episodes)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document
        val iframe = doc.select("iframe").attr("src")

        // Pasa el iframe al extractor de CloudStream
        loadExtractor(iframe, data, subtitleCallback, callback)
        return true
    }
}
