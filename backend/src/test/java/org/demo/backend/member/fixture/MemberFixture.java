package org.demo.backend.member.fixture;

import lombok.Getter;

import java.util.UUID;

@Getter
public class MemberFixture {

    private final String email;
    private final String password;

    private MemberFixture(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static MemberFixture create() {
        return new MemberFixture(generateRandomEmail(), "1234");
    }

    public static MemberFixture withEmail(String email) {
        return new MemberFixture(email, "1234");
    }

    public static MemberFixture withPassword(String password) {
        return new MemberFixture(generateRandomEmail(), password);
    }

    public static MemberFixture of(String email, String password) {
        return new MemberFixture(email, password);
    }

    private static String generateRandomEmail() {
        return "test-" + UUID.randomUUID() + "@test.com";
    }
}
