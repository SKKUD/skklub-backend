package com.skklub.admin.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClub is a Querydsl query type for Club
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClub extends EntityPathBase<Club> {

    private static final long serialVersionUID = -1495842426L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QClub club = new QClub("club");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath activityDescription = createString("activityDescription");

    public final ListPath<ActivityImage, QActivityImage> activityImages = this.<ActivityImage, QActivityImage>createList("activityImages", ActivityImage.class, QActivityImage.class, PathInits.DIRECT2);

    public final StringPath belongs = createString("belongs");

    public final StringPath briefActivityDescription = createString("briefActivityDescription");

    public final EnumPath<com.skklub.admin.domain.enums.Campus> campus = createEnum("campus", com.skklub.admin.domain.enums.Campus.class);

    public final StringPath clubDescription = createString("clubDescription");

    public final EnumPath<com.skklub.admin.domain.enums.ClubType> clubType = createEnum("clubType", com.skklub.admin.domain.enums.ClubType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath createdBy = _super.createdBy;

    public final NumberPath<Integer> establishAt = createNumber("establishAt", Integer.class);

    public final StringPath headLine = createString("headLine");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedAt = _super.lastModifiedAt;

    //inherited
    public final StringPath lastModifiedBy = _super.lastModifiedBy;

    public final QLogo logo;

    public final StringPath mandatoryActivatePeriod = createString("mandatoryActivatePeriod");

    public final NumberPath<Integer> memberAmount = createNumber("memberAmount", Integer.class);

    public final StringPath name = createString("name");

    public final QUser president;

    public final QRecruit recruit;

    public final StringPath regularMeetingTime = createString("regularMeetingTime");

    public final StringPath roomLocation = createString("roomLocation");

    public final StringPath webLink1 = createString("webLink1");

    public final StringPath webLink2 = createString("webLink2");

    public QClub(String variable) {
        this(Club.class, forVariable(variable), INITS);
    }

    public QClub(Path<? extends Club> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QClub(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QClub(PathMetadata metadata, PathInits inits) {
        this(Club.class, metadata, inits);
    }

    public QClub(Class<? extends Club> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.logo = inits.isInitialized("logo") ? new QLogo(forProperty("logo")) : null;
        this.president = inits.isInitialized("president") ? new QUser(forProperty("president")) : null;
        this.recruit = inits.isInitialized("recruit") ? new QRecruit(forProperty("recruit")) : null;
    }

}

