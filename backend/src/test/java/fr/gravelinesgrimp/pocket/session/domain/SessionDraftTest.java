package fr.gravelinesgrimp.pocket.session.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

class SessionDraftTest {

    private static final UUID ROUTE = UUID.randomUUID();

    @Test
    void aCleanAscentIsToppedWithoutAFall() {
        assertThatCode(() -> ascent(true, true, 0).check()).doesNotThrowAnyException();
        assertThatCode(() -> ascent(true, false, 3).check()).doesNotThrowAnyException();
        assertThatCode(() -> ascent(false, false, 2).check()).doesNotThrowAnyException();
        assertCode(() -> ascent(false, true, 0).check(), ErrorCode.INVALID_ASCENT);
        assertCode(() -> ascent(true, true, 1).check(), ErrorCode.INVALID_ASCENT);
    }

    @Test
    void fallsAndFeelingStayInRange() {
        assertCode(() -> ascent(false, false, -1).check(), ErrorCode.INVALID_ASCENT);
        assertCode(() -> ascent(false, false, 100).check(), ErrorCode.INVALID_ASCENT);
        assertThatCode(() -> ascent(false, false, 99).check()).doesNotThrowAnyException();
        assertCode(
                () -> new AscentDraft(ROUTE, ClimbMode.LEAD, true, false, 0, false, 0).check(),
                ErrorCode.INVALID_ASCENT);
        assertCode(
                () -> new AscentDraft(ROUTE, ClimbMode.LEAD, true, false, 0, false, 11).check(),
                ErrorCode.INVALID_ASCENT);
        assertThatCode(
                        () ->
                                new AscentDraft(ROUTE, ClimbMode.LEAD, true, false, 0, false, 10)
                                        .check())
                .doesNotThrowAnyException();
    }

    @Test
    void aSessionHasOneToFiftyAscents() {
        assertCode(() -> session(List.of(), null).check(), ErrorCode.VALIDATION_FAILED);
        assertThatCode(() -> session(Collections.nCopies(50, ascent(true, true, 0)), null).check())
                .doesNotThrowAnyException();
        assertCode(
                () -> session(Collections.nCopies(51, ascent(true, true, 0)), null).check(),
                ErrorCode.VALIDATION_FAILED);
    }

    @Test
    void everyAscentIsChecked() {
        assertCode(
                () -> session(List.of(ascent(true, true, 0), ascent(false, true, 0)), null).check(),
                ErrorCode.INVALID_ASCENT);
    }

    @Test
    void theCommentIsTrimmedAndAnEmptyOneIsNone() {
        assertThat(session(List.of(ascent(true, true, 0)), "  Grosse séance  ").comment())
                .isEqualTo("Grosse séance");
        assertThat(session(List.of(ascent(true, true, 0)), "   ").comment()).isNull();
    }

    @Test
    void friendsSeeItUnlessToldOtherwise() {
        var draft =
                new SessionDraft(
                        LocalDate.parse("2026-10-23"),
                        null,
                        null,
                        null,
                        List.of(ascent(true, true, 0)));
        assertThat(draft.visibility()).isEqualTo(SessionVisibility.FRIENDS);
    }

    @Test
    void theSessionFeelingIsOutOfTen() {
        var ascents = List.of(ascent(true, true, 0));
        assertCode(
                () ->
                        new SessionDraft(
                                        LocalDate.parse("2026-10-23"),
                                        0,
                                        null,
                                        SessionVisibility.PRIVATE,
                                        ascents)
                                .check(),
                ErrorCode.VALIDATION_FAILED);
    }

    static AscentDraft ascent(boolean topped, boolean clean, int falls) {
        return new AscentDraft(ROUTE, ClimbMode.TOP_ROPE, topped, clean, falls, false, null);
    }

    private static SessionDraft session(List<AscentDraft> ascents, String comment) {
        return new SessionDraft(
                LocalDate.parse("2026-10-23"), 7, comment, SessionVisibility.FRIENDS, ascents);
    }

    private static void assertCode(Runnable action, ErrorCode code) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(
                        BusinessException.class, e -> assertThat(e.code()).isEqualTo(code));
    }
}
