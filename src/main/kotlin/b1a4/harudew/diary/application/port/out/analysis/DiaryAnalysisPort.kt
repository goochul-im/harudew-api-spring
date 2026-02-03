package b1a4.harudew.diary.application.port.out.analysis

interface DiaryAnalysisPort {

    fun getAnalysis(content: String): DiaryAnalysisResponse

}
