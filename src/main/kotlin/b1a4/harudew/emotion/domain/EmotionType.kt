package b1a4.harudew.emotion.domain


sealed interface EmotionLabel {
    val base: EmotionBase
}

data class RelationLabels(override val base: EmotionBase = EmotionBase.RELATION, val relationLabel: RelationLabel) : EmotionLabel
data class SelfLabels(override val base: EmotionBase = EmotionBase.SELF, val selfLabel: SelfLabel) : EmotionLabel
data class StateLabels(override val base: EmotionBase = EmotionBase.STATE, val stateLabel: StateLabel) : EmotionLabel

/**
 * 상세 감정 타입입니다. 각 인스턴스는 자신의 기반(Base) 정보를 가집니다.
 */
enum class EmotionType(val labels: EmotionLabel) {
    // [RELATION] 기반 감정
    감사(RelationLabels(relationLabel = RelationLabel.연결)), 존경(RelationLabels(relationLabel = RelationLabel.연결)),
    신뢰(RelationLabels(relationLabel = RelationLabel.연결)), 애정(RelationLabels(relationLabel = RelationLabel.연결)),
    친밀(RelationLabels(relationLabel = RelationLabel.연결)), 유대(RelationLabels(relationLabel = RelationLabel.연결)),
    사랑(RelationLabels(relationLabel = RelationLabel.연결)), 공감(RelationLabels(relationLabel = RelationLabel.연결)),

    질투(RelationLabels(relationLabel = RelationLabel.거리)), 시기(RelationLabels(relationLabel = RelationLabel.거리)),
    분노(RelationLabels(relationLabel = RelationLabel.거리)), 짜증(RelationLabels(relationLabel = RelationLabel.거리)),
    실망(RelationLabels(relationLabel = RelationLabel.거리)), 억울(RelationLabels(relationLabel = RelationLabel.거리)),
    속상(RelationLabels(relationLabel = RelationLabel.거리)), 상처(RelationLabels(relationLabel = RelationLabel.거리)),
    배신감(RelationLabels(relationLabel = RelationLabel.거리)), 경멸(RelationLabels(relationLabel = RelationLabel.거리)),
    거부감(RelationLabels(relationLabel = RelationLabel.거리)), 불쾌(RelationLabels(relationLabel = RelationLabel.거리)),

    // [SELF] 기반 감정
    자긍심(SelfLabels(selfLabel = SelfLabel.긍정)), 자신감(SelfLabels(selfLabel = SelfLabel.긍정)),
    뿌듯함(SelfLabels(selfLabel = SelfLabel.긍정)), 성취감(SelfLabels(selfLabel = SelfLabel.긍정)),
    만족감(SelfLabels(selfLabel = SelfLabel.긍정)),

    부끄러움(SelfLabels(selfLabel = SelfLabel.부정)), 수치(SelfLabels(selfLabel = SelfLabel.부정)),
    죄책감(SelfLabels(selfLabel = SelfLabel.부정)), 후회(SelfLabels(selfLabel = SelfLabel.부정)),
    뉘우침(SelfLabels(selfLabel = SelfLabel.부정)), 창피(SelfLabels(selfLabel = SelfLabel.부정)),
    굴욕(SelfLabels(selfLabel = SelfLabel.부정)),

    // [STATE] 기반 감정
    행복(StateLabels(stateLabel = StateLabel.고양)), 기쁨(StateLabels(stateLabel = StateLabel.고양)),
    즐거움(StateLabels(stateLabel = StateLabel.고양)), 설렘(StateLabels(stateLabel = StateLabel.고양)),
    기대(StateLabels(stateLabel = StateLabel.고양)), 흥분(StateLabels(stateLabel = StateLabel.고양)),
    활력(StateLabels(stateLabel = StateLabel.고양)),

    평온(StateLabels(stateLabel = StateLabel.평온)), 편안(StateLabels(stateLabel = StateLabel.평온)),
    안정(StateLabels(stateLabel = StateLabel.평온)), 차분(StateLabels(stateLabel = StateLabel.평온)),
    무난(StateLabels(stateLabel = StateLabel.평온)),

    긴장(StateLabels(stateLabel = StateLabel.긴장)), 불안(StateLabels(stateLabel = StateLabel.긴장)),
    초조(StateLabels(stateLabel = StateLabel.긴장)), 부담(StateLabels(stateLabel = StateLabel.긴장)),
    놀람(StateLabels(stateLabel = StateLabel.긴장)),

    피로(StateLabels(stateLabel = StateLabel.무기력)), 지침(StateLabels(stateLabel = StateLabel.무기력)),
    무기력(StateLabels(stateLabel = StateLabel.무기력)), 지루(StateLabels(stateLabel = StateLabel.무기력)),
    공허(StateLabels(stateLabel = StateLabel.무기력)), 외로움(StateLabels(stateLabel = StateLabel.무기력)),
    우울(StateLabels(stateLabel = StateLabel.무기력)), 슬픔(StateLabels(stateLabel = StateLabel.무기력))
}

/**
 * EmotionType을 EmotionGroup으로 매핑합니다.
 * * **사용 예시:**
 * ```kotlin
 * val 활력 : EmotionGroup = 행복.toEmotionGroup()
 * val 안정 : EmotionGroup = 평온.toEmotionGroup()
 * ```
 */
fun EmotionType.toEmotionGroup(): EmotionGroup {
    return when (this) {
        // 활력
        EmotionType.행복, EmotionType.기쁨, EmotionType.즐거움, EmotionType.설렘,
        EmotionType.활력, EmotionType.자신감, EmotionType.자긍심, EmotionType.뿌듯함,
        EmotionType.성취감, EmotionType.만족감, EmotionType.기대, EmotionType.흥분 -> EmotionGroup.활력

        // 안정
        EmotionType.평온, EmotionType.편안, EmotionType.안정,
        EmotionType.차분, EmotionType.무난 -> EmotionGroup.안정

        // 유대
        EmotionType.유대, EmotionType.신뢰, EmotionType.존경, EmotionType.친밀,
        EmotionType.애정, EmotionType.사랑, EmotionType.공감, EmotionType.감사 -> EmotionGroup.유대

        // 스트레스
        EmotionType.부담, EmotionType.피로, EmotionType.지침, EmotionType.놀람,
        EmotionType.굴욕, EmotionType.분노, EmotionType.거부감, EmotionType.불쾌,
        EmotionType.짜증, EmotionType.경멸 -> EmotionGroup.스트레스

        // 불안
        EmotionType.불안, EmotionType.초조, EmotionType.질투, EmotionType.시기,
        EmotionType.실망, EmotionType.배신감, EmotionType.부끄러움, EmotionType.수치,
        EmotionType.창피, EmotionType.긴장 -> EmotionGroup.불안

        // 우울
        EmotionType.우울, EmotionType.공허, EmotionType.외로움, EmotionType.무기력,
        EmotionType.지루, EmotionType.속상, EmotionType.상처, EmotionType.억울,
        EmotionType.후회, EmotionType.뉘우침, EmotionType.죄책감, EmotionType.슬픔 -> EmotionGroup.우울
    }
}

/**
 * EmotionType을 EmotionBase로 변환합니다.
 */
fun EmotionType.getEmotionBase(): EmotionBase {
    return this.labels.base
}

/**
 * relation 감정을 '연결 | 거리' 레이블로 변환합니다
 */
fun EmotionType.getRelationLabel(): RelationLabel {
    require(this.labels is RelationLabels) { "이 감정(${this.name})은 Relation 기반이 아닙니다." }
    return this.labels.relationLabel
}

/**
 * self 감정을 '긍정' | '부정' 레이블로 변환합니다
 */
fun EmotionType.getSelfLabel(): SelfLabel {
    require(this.labels is SelfLabels) { "이 감정(${this.name})은 Self 기반이 아닙니다." }
    return this.labels.selfLabel
}

/**
 * state 감정을 '고양' | '긴장' | '평온' | '무기력' 레이블로 변환합니다
 */
fun EmotionType.getStateLabel(): StateLabel {
    require(this.labels is StateLabels) { "이 감정(${this.name})은 State 기반이 아닙니다." }
    return this.labels.stateLabel
}

/**
 * 해당 문자열이 EmotionType과 일치하는지 확인합니다
 */
fun isEmotionType(value: String): Boolean {
    // 모든 enum 상수를 순회하여 문자열과 일치하는지 확인합니다.
    return EmotionType.entries.any { it.name == value }
}
