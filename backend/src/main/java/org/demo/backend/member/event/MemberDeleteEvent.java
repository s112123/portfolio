package org.demo.backend.member.event;

import lombok.Getter;
import org.demo.backend.member.domain.Member;

@Getter
public class MemberDeleteEvent {

    private final Long id;

    private MemberDeleteEvent(Member member) {
        this.id = member.getId();
    }

    public static MemberDeleteEvent of(Member member) {
        return new MemberDeleteEvent(member);
    }
}
