package b1a4.harudew.activity.adapter.dto.response

import b1a4.harudew.person.dto.response.PeopleAnalysisResponse
import com.fasterxml.jackson.annotation.JsonProperty

data class ActivityAnalysisResponse(
    @field:JsonProperty("activity")
    val activityName: String,
    val peoples: List<PeopleAnalysisResponse>
) {

}
