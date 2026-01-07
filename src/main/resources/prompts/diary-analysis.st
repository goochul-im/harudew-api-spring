\{
  "activity_analysis": [
    \{
      "activity": "",
      "peoples": [\{
        "name": "",
        "interactions": [\{
          "emotion": "",
          "emotion_intensity": ""
        \}],
        "name_intimacy": ""
      \}],
      "self_emotions": [\{
        "emotion": "",
        "emotion_intensity": ""
      \}],
      "state_emotions": [\{
        "emotion": "",
        "emotion_intensity": ""
      \}],
      "problem": [\{
        "situation": "",
        "approach": "",
        "outcome": "",
        "conflict_response_code":""
      \}],
      "strength": ""
    \}
  ],
  "reflection": \{
    "achievements": [],
    "shortcomings": [],
    "todo": []
  \}
\}

You are a diary-analysis expert.
RETURN **ONLY valid JSON** that fits the schema above.

==============  GLOBAL RULES  ==============
• Analyse ONLY from writer's view, no speculation ➜ if unclear → "None".
• All *_text fields* = "Korean noun phrases of 14 characters or less including spaces". (예: "기술 미흡", "추가 논의", "운동 완료")
• Self-check before output: enum match, array length sync.

==============  1. ACTIVITY  ==============
Definition = Actions performed by the author (including intentions and plans). + 작성자가 겪은 사건
Extract ALL regardless of importance.
예: 일하다·회의하다·수영·요리·대화 등.
Without specific actions, activity_analysis = []

==============  2. PROBLEM  ==============
Problem must occur DURING the activity.
Fields
  • situation  = 부정 맥락 핵심어(어려움, 갈등, 실패, …)
  • approach   = 해결 행동
  • outcome    = 현재 상태/결과
  • decision_code    = CHOOSE ONE FROM enum [합리적, 직관적, 의존적, 회피적, 충동적]
  • conflict_response_code    =   CHOOSE ONE FROM enum [회피형, 경쟁형, 타협형, 수용형, 협력형형]
↳ situation="None" ⇒ 나머지 4필드도 "None".
↳ approach="None"  ⇒ outcome="None".

==============  3. EMOTIONS  ==============
Relation(22) ↔ 특정 인물, Self(10) ↔ 자기평가, State(28) ↔ 대상 없음.
NEVER use words outside each list.
If emotions that are not in the category are inferred, write them as emotions that are most similar to the category. example, "그리움"->"애정"

[ Relation ]
감사, 존경, 신뢰, 애정, 친밀, 유대, 사랑, 공감, 질투, 시기, 분노, 짜증, 실망, 억울, 속상, 상처, 배신감, 경멸, 거부감, 불쾌

[ Self ]
자긍심, 자신감, 뿌듯함, 성취감, 만족감, 부끄러움, 수치, 죄책감, 후회, 뉘우침, 창피, 굴욕

[ State ]
행복, 기쁨, 즐거움, 설렘, 평온, 편안, 안정, 차분, 기대, 긴장, 불안, 초조, 부담, 피로, 지침, 무기력, 지루, 공허, 외로움, 우울, 슬픔, 놀람, 흥분, 활력

Intensity = base 4 → +2 (“너무 / 정말 / 매우”)
               - 2 (“조금 / 약간 / 살짝”)
               +2 신체반응(“심장이 뛰었다” 등)
               +1 지속표현(“계속”, “오랫동안”)
CAP 1 - 9.
IF no modifier FOUND → intensity 4.
MUST cite the exact modifier word in an internal note, then erase the note before final JSON.
example:
“조금 서운했다”     →  emotion_intensity 2
“너무 너무 화가 났다” →  8
“긴장했다” (수정어 없음) → 4

== MIN-EMOTION RULE ==
• 각 activity는 반드시
  - peoples.interactions.relation_emotion OR
  - state_emotions.state_emotion
  둘 중 하나 이상에 최소 1개 감정을 기록해야 한다.
• relation_emotion이 비어 있으면 해당 person 객체 삭제.

==============  4. STRENGTH  ==============
Choose ONE per activity from 24 enum, else "None".
창의성 호기심 판단력 학습애 통찰력 용감함 끈기 정직함 활력 사랑 친절함 사회적지능 팀워크 공정함 리더십 용서 겸손 신중함 자기조절 미적감상 감사 희망 유머 None

==============  5. PEOPLE  ==============
  • Include only directly mentioned persons.
  • unclear 묘사(“모자 쓴 분”) 삭제
  • remove 호칭·애칭(“민수형”→“민수”, “도영이”→“도영”)
  • Remove person p if
        p.name matches /(친구|팀원|동료|코치)$/ AND p.interactions.relation_emotion == [].
  • 정확한 호칭이 나타나지 않는 대상에 대해서는 "익명의 누군가"를 반환하라

  • For every person p:
      - Keep only RELATION_EMOTION in p.interactions.relation_emotion
      - Move SELF_EMOTION → activity.self_emotions
      - Move STATE_EMOTION → activity.state_emotions

  • For activity.self_emotions:
      - Keep only SELF_EMOTION enum, else map or drop.

  • For activity.state_emotions:
      - Keep only STATE_EMOTION enum, else map or drop.

  • After moves, if peoples == [] AND state_emotions.state_emotion == []:
      ⇒ state_emotions.state_emotion = ["무난"]; s_emotion_intensity = [4]

  • name_intimacy: 애칭1.0/친근0.9/이름0.5/성+직함0.4/거리0.2.

============== CASE : NO ACTIVITY  ==============
**If there is no specific action at all:**:
- activity_analysis = []
- All emotions and thoughts move to reflection
- Record internal conflicts/concerns in shortcomings
- Record action plans in todo

============== 일기 내용 ===============
분석할 일기 내용 :
{content}
