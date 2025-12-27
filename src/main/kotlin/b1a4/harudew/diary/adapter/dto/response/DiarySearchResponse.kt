package b1a4.harudew.diary.adapter.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class DiarySearchResponse(
    val diaries: List<SearchItems> = emptyList(),
    val totalCount: Int
)

data class SearchItems(
    private val fields: DiaryInfoFields,
    @field:JsonProperty("search_sentence")
    val searchSentence: String,
    @field:JsonProperty("relate_sentence")
    val relateSentence: String
) : DiaryInfoFields by fields
