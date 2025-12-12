package b1a4.harudew.activity.port.out

import b1a4.harudew.person.port.out.PeopleAnalysisResponse
import com.fasterxml.jackson.annotation.JsonProperty

data class ActivityAnalysisResponse(
    @field:JsonProperty("activity")
    val activityName: String,
    val peoples: List<PeopleAnalysisResponse>
) {

}
