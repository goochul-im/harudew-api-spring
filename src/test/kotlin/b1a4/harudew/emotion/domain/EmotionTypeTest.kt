package b1a4.harudew.emotion.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class EmotionTypeTest {

    @ParameterizedTest
    @CsvSource(
        "행복, 활력", "기쁨, 활력", "즐거움, 활력", "설렘, 활력", "활력, 활력", "자신감, 활력", "자긍심, 활력", "뿌듯함, 활력", "성취감, 활력", "만족감, 활력", "기대, 활력", "흥분, 활력",
        "평온, 안정", "편안, 안정", "안정, 안정", "차분, 안정", "무난, 안정",
        "유대, 유대", "신뢰, 유대", "존경, 유대", "친밀, 유대", "애정, 유대", "사랑, 유대", "공감, 유대", "감사, 유대",
        "부담, 스트레스", "피로, 스트레스", "지침, 스트레스", "놀람, 스트레스", "굴욕, 스트레스", "분노, 스트레스", "거부감, 스트레스", "불쾌, 스트레스", "짜증, 스트레스", "경멸, 스트레스",
        "불안, 불안", "초조, 불안", "질투, 불안", "시기, 불안", "실망, 불안", "배신감, 불안", "부끄러움, 불안", "수치, 불안", "창피, 불안", "긴장, 불안",
        "우울, 우울", "공허, 우울", "외로움, 우울", "무기력, 우울", "지루, 우울", "속상, 우울", "상처, 우울", "억울, 우울", "후회, 우울", "뉘우침, 우울", "죄책감, 우울", "슬픔, 우울"
    )
    fun `감정 타입을 감정 그룹으로 변환한다`(emotionType: EmotionType, expectedGroup: EmotionGroup) {
        // given

        // when
        val result = emotionType.toEmotionGroup()

        // then
        assertThat(result).isEqualTo(expectedGroup)
    }

    @Test
    fun `모든 감정 타입이 감정 그룹으로 매핑 되는지 확인한다`() {
        // given
        val allTypes = EmotionType.entries

        // when & then
        allTypes.forEach { emotionType ->
            assertThat(emotionType.toEmotionGroup()).isNotNull
        }
    }

    @ParameterizedTest
    @EnumSource(value = EmotionType::class, names = ["감사", "질투", "분노", "사랑"])
    fun `감정 타입에서 RELATION 기반을 가져온다`(type: EmotionType) {
        // given

        // when
        val base = type.getEmotionBase()

        // then
        assertThat(base).isEqualTo(EmotionBase.RELATION)
    }

    @ParameterizedTest
    @EnumSource(value = EmotionType::class, names = ["자긍심", "부끄러움", "후회"])
    fun `감정 타입에서 SELF 기반을 가져온다`(type: EmotionType) {
        // given

        // when
        val base = type.getEmotionBase()

        // then
        assertThat(base).isEqualTo(EmotionBase.SELF)
    }

    @ParameterizedTest
    @EnumSource(value = EmotionType::class, names = ["행복", "평온", "긴장", "우울"])
    fun `감정 타입에서 STATE 기반을 가져온다`(type: EmotionType) {
        // given

        // when
        val base = type.getEmotionBase()

        // then
        assertThat(base).isEqualTo(EmotionBase.STATE)
    }

    @Test
    fun `RELATION 기반 감정에서 관계 레이블을 가져온다`() {
        // given
        val connectionType = EmotionType.감사
        val distanceType = EmotionType.질투

        // when
        val connectionLabel = connectionType.getRelationLabel()
        val distanceLabel = distanceType.getRelationLabel()

        // then
        assertThat(connectionLabel).isEqualTo(RelationLabel.연결)
        assertThat(distanceLabel).isEqualTo(RelationLabel.거리)
    }

    @Test
    fun `RELATION 기반이 아닌 감정에서 관계 레이블을 가져오려 하면 예외가 발생한다`() {
        // given
        val nonRelationType = EmotionType.행복 // STATE

        // when & then
        assertThatThrownBy { nonRelationType.getRelationLabel() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Relation 기반이 아닙니다")
    }

    @Test
    fun `SELF 기반 감정에서 자아 레이블을 가져온다`() {
        // given
        val positiveType = EmotionType.자긍심
        val negativeType = EmotionType.부끄러움

        // when
        val positiveLabel = positiveType.getSelfLabel()
        val negativeLabel = negativeType.getSelfLabel()

        // then
        assertThat(positiveLabel).isEqualTo(SelfLabel.긍정)
        assertThat(negativeLabel).isEqualTo(SelfLabel.부정)
    }

    @Test
    fun `SELF 기반이 아닌 감정에서 자아 레이블을 가져오려 하면 예외가 발생한다`() {
        // given
        val nonSelfType = EmotionType.감사 // RELATION

        // when & then
        assertThatThrownBy { nonSelfType.getSelfLabel() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Self 기반이 아닙니다")
    }

    @Test
    fun `STATE 기반 감정에서 상태 레이블을 가져온다`() {
        // given
        val highType = EmotionType.행복
        val calmType = EmotionType.평온
        val tensionType = EmotionType.긴장
        val lethargyType = EmotionType.우울

        // when
        val highLabel = highType.getStateLabel()
        val calmLabel = calmType.getStateLabel()
        val tensionLabel = tensionType.getStateLabel()
        val lethargyLabel = lethargyType.getStateLabel()

        // then
        assertThat(highLabel).isEqualTo(StateLabel.고양)
        assertThat(calmLabel).isEqualTo(StateLabel.평온)
        assertThat(tensionLabel).isEqualTo(StateLabel.긴장)
        assertThat(lethargyLabel).isEqualTo(StateLabel.무기력)
    }

    @Test
    fun `STATE 기반이 아닌 감정에서 상태 레이블을 가져오려 하면 예외가 발생한다`() {
        // given
        val nonStateType = EmotionType.자긍심 // SELF

        // when & then
        assertThatThrownBy { nonStateType.getStateLabel() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("State 기반이 아닙니다")
    }

    @ParameterizedTest
    @ValueSource(strings = ["행복", "슬픔", "감사", "자긍심"])
    fun `문자열이 유효한 감정 타입인지 확인한다 참`(value: String) {
        // given

        // when
        val result = isEmotionType(value)

        // then
        assertThat(result).isTrue()
    }

    @ParameterizedTest
    @ValueSource(strings = ["없는감정", "Happy", "", " "])
    fun `문자열이 유효한 감정 타입인지 확인한다 거짓`(value: String) {
        // given

        // when
        val result = isEmotionType(value)

        // then
        assertThat(result).isFalse()
    }
}
