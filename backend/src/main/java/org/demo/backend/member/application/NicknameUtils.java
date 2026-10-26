package org.demo.backend.member.application;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NicknameUtils {

    private static final List<String> adjectives = Arrays.asList(
            "춤추는", "잠자는", "밥먹는", "섹시한", "노래하는", "멋진", "세련된", "귀여운", "예쁜"
    );
    private static final List<String> nouns = Arrays.asList(
            "고래", "상어", "강아지", "고양이", "햄스터", "병아리", "원숭이", "송아지", "물개", "곰탱이"
    );

    public static String generate() {
        int adjIndex = ThreadLocalRandom.current().nextInt(adjectives.size());
        int nounIndex = ThreadLocalRandom.current().nextInt(nouns.size());
        return adjectives.get(adjIndex) + " " + nouns.get(nounIndex);
    }
}
