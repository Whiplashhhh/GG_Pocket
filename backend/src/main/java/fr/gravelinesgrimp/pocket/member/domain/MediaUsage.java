package fr.gravelinesgrimp.pocket.member.domain;

/** What an uploaded file is for; drives who may see it. */
public enum MediaUsage {
    /** Profile photo: visible to every signed-in member. */
    AVATAR,
    /** Photo of a route in the topo: visible to every signed-in member. */
    ROUTE_PHOTO;

    public boolean visibleToEveryMember() {
        return this == AVATAR || this == ROUTE_PHOTO;
    }
}
